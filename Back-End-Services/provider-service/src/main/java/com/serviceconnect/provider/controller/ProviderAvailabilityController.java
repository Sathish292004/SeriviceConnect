package com.serviceconnect.provider.controller;

import com.serviceconnect.provider.dto.request.ProviderAvailabilityActiveRequest;
import com.serviceconnect.provider.dto.request.ProviderAvailabilityRequest;
import com.serviceconnect.provider.dto.response.ProviderAvailabilityResponse;
import com.serviceconnect.provider.service.ProviderAvailabilityService;
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
public class ProviderAvailabilityController {

    private final ProviderAvailabilityService availabilityService;

    // ============================================================
    // PROVIDER - CREATE
    // ============================================================

    @PostMapping("/{providerId}/availability")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderAvailabilityResponse> create(
            @PathVariable @Positive Long providerId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ProviderAvailabilityRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        availabilityService.create(
                                providerId,
                                getAuthenticatedUserId(jwt),
                                request
                        )
                );
    }

    // ============================================================
    // PROVIDER - GET OWN AVAILABILITY
    // Includes inactive
    // ============================================================

    @GetMapping("/{providerId}/availability/manage")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ProviderAvailabilityResponse>>
    getOwnAvailability(
            @PathVariable @Positive Long providerId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        return ResponseEntity.ok(
                availabilityService.getProviderAvailability(
                        providerId,
                        getAuthenticatedUserId(jwt)
                )
        );
    }

    // ============================================================
    // CUSTOMER - GET ACTIVE AVAILABILITY
    // ============================================================

    @GetMapping("/{providerId}/availability")
    public ResponseEntity<List<ProviderAvailabilityResponse>>
    getActiveAvailability(
            @PathVariable @Positive Long providerId
    ) {

        return ResponseEntity.ok(
                availabilityService.getActiveAvailability(
                        providerId
                )
        );
    }

    // ============================================================
    // PROVIDER - UPDATE
    // ============================================================

    @PutMapping("/availability/{availabilityId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderAvailabilityResponse> update(
            @PathVariable @Positive Long availabilityId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ProviderAvailabilityRequest request
    ) {

        return ResponseEntity.ok(
                availabilityService.update(
                        availabilityId,
                        getAuthenticatedUserId(jwt),
                        request
                )
        );
    }

    // ============================================================
    // PROVIDER - ENABLE / DISABLE
    // ============================================================

    @PatchMapping("/availability/{availabilityId}/active")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderAvailabilityResponse>
    setActive(
            @PathVariable @Positive Long availabilityId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            ProviderAvailabilityActiveRequest request
    ) {

        return ResponseEntity.ok(
                availabilityService.setActive(
                        availabilityId,
                        getAuthenticatedUserId(jwt),
                        request.active()
                )
        );
    }

    // ============================================================
    // PROVIDER - DELETE
    // ============================================================

    @DeleteMapping("/availability/{availabilityId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive Long availabilityId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        availabilityService.delete(
                availabilityId,
                getAuthenticatedUserId(jwt)
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ============================================================
    // JWT USER ID
    // ============================================================

    private Long getAuthenticatedUserId(
            Jwt jwt
    ) {

        if (jwt == null ||
                jwt.getSubject() == null ||
                jwt.getSubject().isBlank()) {

            throw new org.springframework.web.server
                    .ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user is required"
            );
        }

        try {

            return Long.valueOf(
                    jwt.getSubject()
            );

        } catch (NumberFormatException ex) {

            throw new org.springframework.web.server
                    .ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authenticated user"
            );
        }
    }
}