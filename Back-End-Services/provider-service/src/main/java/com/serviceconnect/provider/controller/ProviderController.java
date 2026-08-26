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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;


    // ============================================================
    // CREATE PROVIDER PROFILE
    // ============================================================

    @PostMapping
    public ResponseEntity<ProviderResponse> createProvider(
            @RequestParam
            @Positive
            Long userId,

            @Valid
            @RequestBody
            CreateProviderRequest request) {

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
    // GET PROVIDER BY ID
    // ============================================================

    @GetMapping("/{providerId}")
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
    // GET PROVIDER BY USER ID
    // ============================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProviderResponse> getProviderByUserId(
            @PathVariable
            @Positive
            Long userId) {

        return ResponseEntity.ok(
                providerService.getProviderByUserId(
                        userId
                )
        );
    }


    // ============================================================
    // UPDATE PROVIDER
    // ============================================================

    @PutMapping("/{providerId}")
    public ResponseEntity<ProviderResponse> updateProvider(
            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            CreateProviderRequest request) {

        return ResponseEntity.ok(
                providerService.updateProvider(
                        providerId,
                        request
                )
        );
    }


    // ============================================================
    // DELETE PROVIDER
    // ADMIN ONLY
    // ============================================================

    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable
            @Positive
            Long providerId) {

        providerService.deleteProvider(providerId);

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
    // CUSTOMER - GET APPROVED PROVIDERS ONLY
    // ============================================================

    @GetMapping
    public ResponseEntity<List<ProviderResponse>>
    getApprovedProviders() {

        return ResponseEntity.ok(
                providerService.getApprovedProviders()
        );
    }


    // ============================================================
    // PROVIDER - UPDATE LIVE LOCATION
    // ============================================================

    @PatchMapping("/{providerId}/location")
    public ResponseEntity<ProviderResponse> updateProviderLocation(
            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            UpdateProviderLocationRequest request) {

        return ResponseEntity.ok(
                providerService.updateProviderLocation(
                        providerId,
                        request.latitude(),
                        request.longitude()
                )
        );
    }


    // ============================================================
    // CUSTOMER - GET APPROVED PROVIDER
    // ============================================================

    @GetMapping("/{providerId}/public")
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
}