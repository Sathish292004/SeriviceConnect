package com.serviceconnect.booking.controller;

import com.serviceconnect.booking.client.ProviderServiceClient;
import com.serviceconnect.booking.dto.request.CreateServiceRequest;
import com.serviceconnect.booking.dto.request.UpdateServiceRequestStatus;
import com.serviceconnect.booking.dto.response.ServiceRequestResponse;
import com.serviceconnect.booking.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // CUSTOMER - CREATE SERVICE REQUEST
    // ============================================================

    @PostMapping("/requests")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ServiceRequestResponse> createRequest(

            @Valid
            @RequestBody
            CreateServiceRequest request,

            @AuthenticationPrincipal
            Jwt jwt) {

        /*
         * Customer ID comes from JWT.
         * customerId from request body is ignored.
         */

        Long customerId =
                getUserId(jwt);

        /*
         * Forward the customer's JWT to downstream services.
         *
         * Catalog Service may require authentication.
         */
        String authorizationHeader =
                "Bearer " + jwt.getTokenValue();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bookingService.createServiceRequest(
                                customerId,
                                authorizationHeader,
                                request
                        )
                );
    }


    // ============================================================
    // GET REQUEST BY ID
    // ============================================================

    @GetMapping("/requests/{requestId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER', 'ADMIN')")
    public ResponseEntity<ServiceRequestResponse> getRequest(

            @PathVariable
            @Positive
            Long requestId,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long userId =
                getUserId(jwt);

        String authority =
                getAuthority(jwt);

        /*
         * Provider JWT contains USER ID.
         *
         * Booking table stores PROVIDER ID.
         *
         * Therefore, convert:
         *
         * userId -> providerId
         */

        if ("ROLE_PROVIDER".equals(authority)) {

            Long providerId =
                    providerServiceClient.getProviderIdByUserId(
                            userId
                    );

            userId = providerId;
        }

        return ResponseEntity.ok(
                bookingService.getRequestById(
                        requestId,
                        userId,
                        authority
                )
        );
    }


    // ============================================================
    // CUSTOMER - GET MY REQUESTS
    // ============================================================

    @GetMapping("/customers/requests")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ServiceRequestResponse>>
    getCustomerRequests(

            @AuthenticationPrincipal
            Jwt jwt) {

        Long customerId =
                getUserId(jwt);

        return ResponseEntity.ok(
                bookingService.getCustomerRequests(
                        customerId
                )
        );
    }


    // ============================================================
    // PROVIDER - GET ALL INCOMING REQUESTS
    // ============================================================

    @GetMapping("/providers/requests")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ServiceRequestResponse>>
    getProviderRequests(

            @AuthenticationPrincipal
            Jwt jwt) {

        Long userId =
                getUserId(jwt);

        Long providerId =
                providerServiceClient.getProviderIdByUserId(
                        userId
                );

        return ResponseEntity.ok(
                bookingService.getProviderRequests(
                        providerId
                )
        );
    }


    // ============================================================
    // PROVIDER - GET REQUESTS BY STATUS
    // ============================================================

    @GetMapping("/providers/requests/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ServiceRequestResponse>>
    getProviderRequestsByStatus(

            @RequestParam
            String status,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long userId =
                getUserId(jwt);

        Long providerId =
                providerServiceClient.getProviderIdByUserId(
                        userId
                );

        return ResponseEntity.ok(
                bookingService.getProviderRequestsByStatus(
                        providerId,
                        status
                )
        );
    }


    // ============================================================
    // CUSTOMER - CANCEL REQUEST
    // ============================================================

    @PatchMapping("/requests/{requestId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ServiceRequestResponse> cancelRequest(

            @PathVariable
            @Positive
            Long requestId,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long customerId =
                getUserId(jwt);

        return ResponseEntity.ok(
                bookingService.cancelRequest(
                        requestId,
                        customerId
                )
        );
    }


    // ============================================================
    // PROVIDER - UPDATE REQUEST STATUS
    // ACCEPT / REJECT / COMPLETE
    // ============================================================

    @PatchMapping("/requests/{requestId}/status")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ServiceRequestResponse>
    updateRequestStatus(

            @PathVariable
            @Positive
            Long requestId,

            @Valid
            @RequestBody
            UpdateServiceRequestStatus request,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long userId =
                getUserId(jwt);

        /*
         * JWT gives USER ID.
         *
         * Booking uses PROVIDER ID.
         *
         * Example:
         *
         * JWT sub = 7
         * Provider ID = 3
         *
         * Therefore resolve provider ID first.
         */

        Long providerId =
                providerServiceClient.getProviderIdByUserId(
                        userId
                );

        String status =
                request.status()
                        .trim()
                        .toUpperCase();

        ServiceRequestResponse response;

        if ("ACCEPTED".equals(status)) {

            response =
                    bookingService.acceptRequest(
                            requestId,
                            providerId
                    );

        } else if ("REJECTED".equals(status)) {

            response =
                    bookingService.rejectRequest(
                            requestId,
                            providerId
                    );

        } else if ("COMPLETED".equals(status)) {

            response =
                    bookingService.completeRequest(
                            requestId,
                            providerId
                    );

        } else {

            throw new IllegalArgumentException(
                    "Provider can only set ACCEPTED, REJECTED or COMPLETED"
            );
        }

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // JWT USER ID
    // ============================================================

    private Long getUserId(Jwt jwt) {

        if (jwt == null) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication required"
            );
        }

        String subject =
                jwt.getSubject();

        if (subject == null
                || subject.isBlank()) {

            throw new IllegalStateException(
                    "User ID not found in JWT"
            );
        }

        try {

            return Long.parseLong(subject);

        } catch (NumberFormatException exception) {

            throw new IllegalStateException(
                    "Invalid user ID in JWT subject"
            );
        }
    }


    // ============================================================
    // JWT ROLE
    // ============================================================

    private String getAuthority(Jwt jwt) {

        String role =
                jwt.getClaimAsString("role");

        if (role == null
                || role.isBlank()) {

            throw new IllegalStateException(
                    "Role not found in JWT"
            );
        }

        return "ROLE_" +
                role.trim().toUpperCase();
    }
}