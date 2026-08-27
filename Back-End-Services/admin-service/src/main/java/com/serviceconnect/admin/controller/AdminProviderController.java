package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.client.ProviderServiceClient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/providers")
@RequiredArgsConstructor
public class AdminProviderController {

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // ADMIN - GET ALL / FILTER PROVIDERS
    // ============================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProviderServiceClient.ProviderResponse>>
    getAllProviders(

            @RequestParam(required = false)
            String status,

            @RequestHeader("Authorization")
            String authorizationHeader) {

        if (status == null || status.isBlank()) {

            return ResponseEntity.ok(
                    providerServiceClient.getAllProviders(
                            authorizationHeader
                    )
            );
        }

        return ResponseEntity.ok(
                providerServiceClient.getProvidersByStatus(
                        status,
                        authorizationHeader
                )
        );
    }


    // ============================================================
    // ADMIN - UPDATE PROVIDER STATUS
    // ============================================================

    @PatchMapping("/{providerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderServiceClient.ProviderResponse>
    updateProviderStatus(

            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            ProviderServiceClient.UpdateProviderStatusRequest request,

            @RequestHeader("Authorization")
            String authorizationHeader) {

        return ResponseEntity.ok(
                providerServiceClient.updateProviderStatus(
                        providerId,
                        request.status(),
                        authorizationHeader
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
            Long providerId,

            @RequestHeader("Authorization")
            String authorizationHeader) {

        providerServiceClient.deleteProvider(
                providerId,
                authorizationHeader
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}