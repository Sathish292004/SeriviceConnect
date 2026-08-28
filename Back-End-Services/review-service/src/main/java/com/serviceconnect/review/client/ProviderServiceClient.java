package com.serviceconnect.review.client;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@RequiredArgsConstructor
public class ProviderServiceClient {

    private final RestClient.Builder restClientBuilder;


    @Value("${services.provider.url}")
    private String providerServiceUrl;


    // ============================================================
    // GET PROVIDER BY ID
    // ============================================================

    public ProviderResponse getProviderById(
            Long providerId,
            String authorizationHeader) {

        return restClientBuilder
                .baseUrl(providerServiceUrl)
                .build()
                .get()
                .uri(
                        "/api/v1/providers/{providerId}/public",
                        providerId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(ProviderResponse.class);
    }


    // ============================================================
    // PROVIDER RESPONSE
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

            java.time.OffsetDateTime createdAt,

            java.time.OffsetDateTime updatedAt,

            Object photos
    ) {
    }
}