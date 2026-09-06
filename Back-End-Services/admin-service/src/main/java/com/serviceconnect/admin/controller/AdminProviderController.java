package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.client.ProviderServiceClient;
import com.serviceconnect.admin.dto.response.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/providers")
@RequiredArgsConstructor
@Validated
public class AdminProviderController {

    private final ProviderServiceClient providerServiceClient;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ProviderServiceClient.ProviderResponse>> getAllProviders(
            @RequestParam(required = false) String status,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int size,

            @RequestHeader("Authorization") String authorizationHeader) {

        if (status == null || status.isBlank()) {
            return ResponseEntity.ok(
                    providerServiceClient.getAllProviders(
                            page,
                            size,
                            authorizationHeader
                    )
            );
        }

        return ResponseEntity.ok(
                providerServiceClient.getProvidersByStatus(
                        status,
                        page,
                        size,
                        authorizationHeader
                )
        );
    }

    @PatchMapping("/{providerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderServiceClient.ProviderResponse> updateProviderStatus(
            @PathVariable Long providerId,
            @RequestBody ProviderServiceClient.UpdateProviderStatusRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        return ResponseEntity.ok(
                providerServiceClient.updateProviderStatus(
                        providerId,
                        request.status(),
                        authorizationHeader
                )
        );
    }

    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable Long providerId,
            @RequestHeader("Authorization") String authorizationHeader) {

        providerServiceClient.deleteProvider(
                providerId,
                authorizationHeader
        );

        return ResponseEntity.noContent().build();
    }
}