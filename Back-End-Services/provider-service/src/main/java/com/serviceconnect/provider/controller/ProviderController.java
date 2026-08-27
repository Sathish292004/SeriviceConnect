package com.serviceconnect.provider.controller;

import com.serviceconnect.provider.dto.request.CreateProviderRequest;
import com.serviceconnect.provider.dto.request.UpdateProviderLocationRequest;
import com.serviceconnect.provider.dto.request.UpdateProviderStatusRequest;
import com.serviceconnect.provider.dto.response.ProviderResponse;
import com.serviceconnect.provider.service.ProviderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {


    private final ProviderService providerService;


    // ============================================================
    // PROVIDER - CREATE PROFILE
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> createProvider(

            @Valid
            @RequestBody
            CreateProviderRequest request,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long userId =
                getUserId(jwt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        providerService.createProvider(
                                userId,
                                request
                        )
                );
    }


    // ============================================================
    // ADMIN - GET ALL / FILTER PROVIDERS
    // ============================================================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProviderResponse>> getAllProviders(

            @RequestParam(required = false)
            String status) {

        if (status == null || status.isBlank()) {

            return ResponseEntity.ok(
                    providerService.getAllProviders()
            );
        }

        return ResponseEntity.ok(
                providerService.getProvidersByStatus(
                        status
                )
        );
    }


    // ============================================================
    // GET PROVIDER BY ID
    // ADMIN / PROVIDER ONLY
    // ============================================================

    @GetMapping("/{providerId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderResponse> getProvider(

            @PathVariable
            @Positive
            Long providerId) {

        return ResponseEntity.ok(
                providerService.getProviderById(
                        providerId
                )
        );
    }


    // ============================================================
    // PROVIDER - GET OWN PROFILE
    // ADMIN CAN GET ANY PROVIDER PROFILE
    // ============================================================

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderResponse> getProviderByUserId(

            @PathVariable
            @Positive
            Long userId,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long authenticatedUserId =
                getUserId(jwt);

        String role =
                getRole(jwt);

        if ("PROVIDER".equals(role)
                && !authenticatedUserId.equals(userId)) {

            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only view your own provider profile"
            );
        }

        return ResponseEntity.ok(
                providerService.getProviderByUserId(
                        userId
                )
        );
    }


    // ============================================================
    // PROVIDER - UPDATE OWN PROFILE
    // ============================================================

    @PutMapping("/{providerId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> updateProvider(

            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            CreateProviderRequest request,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long authenticatedUserId =
                getUserId(jwt);

        return ResponseEntity.ok(
                providerService.updateProvider(
                        providerId,
                        authenticatedUserId,
                        request
                )
        );
    }


    // ============================================================
    // ADMIN - DELETE PROVIDER
    // ============================================================

    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(

            @PathVariable
            @Positive
            Long providerId) {

        providerService.deleteProvider(
                providerId
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // ADMIN - UPDATE PROVIDER STATUS
    // ============================================================

    @PatchMapping("/{providerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponse> updateProviderStatus(

            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            UpdateProviderStatusRequest request) {

        return ResponseEntity.ok(
                providerService.updateProviderStatus(
                        providerId,
                        request.status()
                )
        );
    }


    // ============================================================
    // CUSTOMER - GET APPROVED PROVIDERS
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ProviderResponse>>
    getApprovedProviders() {

        return ResponseEntity.ok(
                providerService.getApprovedProviders()
        );
    }


    // ============================================================
    // PROVIDER - UPDATE OWN LIVE LOCATION
    // ============================================================

    @PatchMapping("/{providerId}/location")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderResponse> updateProviderLocation(

            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            UpdateProviderLocationRequest request,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long authenticatedUserId =
                getUserId(jwt);

        return ResponseEntity.ok(
                providerService.updateProviderLocation(
                        providerId,
                        authenticatedUserId,
                        request.latitude(),
                        request.longitude()
                )
        );
    }


    // ============================================================
    // CUSTOMER - GET APPROVED PROVIDER
    // ============================================================

    @GetMapping("/{providerId}/public")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ProviderResponse> getApprovedProvider(

            @PathVariable
            @Positive
            Long providerId) {

        return ResponseEntity.ok(
                providerService.getApprovedProvider(
                        providerId
                )
        );
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

    private String getRole(Jwt jwt) {

        String role =
                jwt.getClaimAsString("role");

        if (role == null
                || role.isBlank()) {

            throw new IllegalStateException(
                    "Role not found in JWT"
            );
        }

        return role
                .trim()
                .toUpperCase();
    }
}