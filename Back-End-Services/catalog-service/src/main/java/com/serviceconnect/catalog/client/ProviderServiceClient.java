package com.serviceconnect.catalog.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProviderServiceClient {

    @Value("${services.provider.url}")
    private String providerServiceUrl;

    // ============================================================
    // GET APPROVED PROVIDERS
    // ============================================================

    public List<ProviderResponse> getApprovedProviders(
            String authorizationHeader
    ) {

        RestClient client =
                RestClient.builder()
                        .baseUrl(providerServiceUrl)
                        .build();

        ProviderResponse[] response =
                client.get()
                        .uri("/api/v1/providers")
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                authorizationHeader
                        )
                        .retrieve()
                        .body(ProviderResponse[].class);

        if (response == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(response);
    }

    // ============================================================
    // PROVIDER RESPONSE
    // Only fields required by catalog are represented.
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
            Object createdAt,
            Object updatedAt,
            Object photos
    ) {
    }
}