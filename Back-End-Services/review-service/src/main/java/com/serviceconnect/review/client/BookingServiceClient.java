package com.serviceconnect.review.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BookingServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${services.booking.url}")
    private String bookingServiceUrl;

    public BookingResponse getBookingById(
            Long bookingId,
            String authorizationHeader) {

        return restClientBuilder
                .baseUrl(bookingServiceUrl)
                .build()
                .get()
                .uri("/api/bookings/{id}", bookingId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(BookingResponse.class);
    }

    public record BookingResponse(
            Long id,
            Long customerId,
            Long providerId,
            Long catalogItemId,
            String serviceType,
            String description,
            String serviceAddress,
            Double latitude,
            Double longitude,
            String status,
            String customerPhone,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime updatedAt
    ) {
    }
}