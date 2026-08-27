package com.serviceconnect.booking.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class CatalogServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${services.catalog.url}")
    private String catalogServiceUrl;

    public CatalogItemResponse getCatalogItem(
            Long catalogItemId,
            String authorizationHeader) {

        return restClientBuilder
                .baseUrl(catalogServiceUrl)
                .build()
                .get()
                .uri("/api/catalog/{id}", catalogItemId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(CatalogItemResponse.class);
    }

    public record CatalogItemResponse(
            Long id,
            Long providerId,
            String name,
            String description,
            String category,
            java.math.BigDecimal price,
            Integer durationMinutes,
            Boolean active,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
}