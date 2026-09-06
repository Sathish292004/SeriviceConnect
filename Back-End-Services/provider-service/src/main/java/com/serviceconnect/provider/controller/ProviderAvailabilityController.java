package com.serviceconnect.provider.controller;

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
    // PROVIDER - CREATE AVAILABILITY
    // ============================================================

    @PostMapping("/{providerId}/availability")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderAvailabilityResponse> create(
            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            ProviderAvailabilityRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Long userId =
                Long.parseLong(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        availabilityService.create(
                                providerId,
                                userId,
                                request
                        )
                );
    }


    // ============================================================
    // PROVIDER - GET OWN AVAILABILITY
    // ============================================================

    @GetMapping("/{providerId}/availability")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<List<ProviderAvailabilityResponse>> getOwn(
            @PathVariable
            @Positive
            Long providerId,

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Long userId =
                Long.parseLong(jwt.getSubject());

        return ResponseEntity.ok(
                availabilityService.getProviderAvailability(
                        providerId,
                        userId
                )
        );
    }


    // ============================================================
    // CUSTOMER / ADMIN - GET ACTIVE AVAILABILITY
    // ============================================================

    @GetMapping("/{providerId}/availability/active")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<ProviderAvailabilityResponse>> getActive(
            @PathVariable
            @Positive
            Long providerId
    ) {

        return ResponseEntity.ok(
                availabilityService.getActiveAvailability(
                        providerId
                )
        );
    }


    // ============================================================
    // PROVIDER - UPDATE AVAILABILITY
    // ============================================================

    @PutMapping("/{providerId}/availability/{availabilityId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderAvailabilityResponse> update(
            @PathVariable
            @Positive
            Long providerId,

            @PathVariable
            @Positive
            Long availabilityId,

            @Valid
            @RequestBody
            ProviderAvailabilityRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Long userId =
                Long.parseLong(jwt.getSubject());

        return ResponseEntity.ok(
                availabilityService.update(
                        availabilityId,
                        userId,
                        request
                )
        );
    }


    // ============================================================
    // PROVIDER - DELETE AVAILABILITY
    // ============================================================

    @DeleteMapping("/{providerId}/availability/{availabilityId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Positive
            Long providerId,

            @PathVariable
            @Positive
            Long availabilityId,

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Long userId =
                Long.parseLong(jwt.getSubject());

        availabilityService.delete(
                availabilityId,
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // PROVIDER - ENABLE / DISABLE AVAILABILITY
    // ============================================================

    @PatchMapping("/{providerId}/availability/{availabilityId}/active")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ProviderAvailabilityResponse> setActive(
            @PathVariable
            @Positive
            Long providerId,

            @PathVariable
            @Positive
            Long availabilityId,

            @RequestParam
            boolean active,

            @AuthenticationPrincipal
            Jwt jwt
    ) {

        Long userId =
                Long.parseLong(jwt.getSubject());

        return ResponseEntity.ok(
                availabilityService.setActive(
                        availabilityId,
                        userId,
                        active
                )
        );
    }
}