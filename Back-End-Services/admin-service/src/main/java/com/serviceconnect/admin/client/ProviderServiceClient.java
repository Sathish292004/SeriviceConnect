package com.serviceconnect.admin.client;

import com.serviceconnect.admin.dto.response.PageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProviderServiceClient {

    @Value("${provider-service.url}")
    private String providerServiceUrl;


    // ============================================================
    // REST CLIENT
    // ============================================================

    private RestClient restClient() {

        return RestClient
                .builder()
                .baseUrl(providerServiceUrl)
                .build();
    }


    // ============================================================
    // UPDATE PROVIDER STATUS
    // ADMIN
    // ============================================================

    public ProviderResponse updateProviderStatus(
            Long providerId,
            String status,
            String authorizationHeader) {

        return restClient()
                .patch()
                .uri(
                        "/api/v1/providers/{providerId}/status",
                        providerId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(
                        new UpdateProviderStatusRequest(
                                status
                        )
                )
                .retrieve()
                .body(
                        ProviderResponse.class
                );
    }


    // ============================================================
    // DELETE PROVIDER
    // ADMIN
    // ============================================================

    public void deleteProvider(
            Long providerId,
            String authorizationHeader) {

        restClient()
                .delete()
                .uri(
                        "/api/v1/providers/{providerId}",
                        providerId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .toBodilessEntity();
    }


    // ============================================================
    // GET ALL PROVIDERS
    // ADMIN
    // ============================================================

    public PageResponse<ProviderResponse> getAllProviders(
            int page,
            int size,
            String authorizationHeader) {

        return restClient()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/v1/providers/admin/all")
                                .queryParam(
                                        "page",
                                        page
                                )
                                .queryParam(
                                        "size",
                                        size
                                )
                                .build()
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                PageResponse<ProviderResponse>>() {}
                );
    }


    // ============================================================
    // GET PROVIDERS BY STATUS
    // ADMIN
    // ============================================================

    public PageResponse<ProviderResponse> getProvidersByStatus(
            String status,
            int page,
            int size,
            String authorizationHeader) {

        return restClient()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/api/v1/providers/admin/all")
                                .queryParam(
                                        "status",
                                        status
                                )
                                .queryParam(
                                        "page",
                                        page
                                )
                                .queryParam(
                                        "size",
                                        size
                                )
                                .build()
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                PageResponse<ProviderResponse>>() {}
                );
    }


    // ============================================================
    // DTO - UPDATE STATUS REQUEST
    // ============================================================

    public record UpdateProviderStatusRequest(
            String status
    ) {
    }


    // ============================================================
    // DTO - PROVIDER RESPONSE
    // ============================================================

    public record ProviderResponse(

            Long id,

            Long userId,

            String businessName,

            String description,

            String phone,

            String email,

            String address,

            String city,

            String state,

            String postalCode,

            Double latitude,

            Double longitude,

            String status,

            OffsetDateTime createdAt,

            OffsetDateTime updatedAt,

            List<ProviderPhotoResponse> photos

    ) {
    }


    // ============================================================
    // DTO - PROVIDER PHOTO RESPONSE
    // ============================================================

    public record ProviderPhotoResponse(

            Long id,

            Long providerId,

            String imageUrl,

            Integer displayOrder,

            OffsetDateTime createdAt

    ) {
    }
}