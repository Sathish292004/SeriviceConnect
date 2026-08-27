package com.serviceconnect.booking.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ReviewServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${services.review.url}")
    private String reviewServiceUrl;

    public ReviewResponse getReviewByBookingId(
            Long bookingId,
            String authorizationHeader) {

        return restClientBuilder
                .baseUrl(reviewServiceUrl)
                .build()
                .get()
                .uri("/api/reviews/booking/{bookingId}", bookingId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        authorizationHeader
                )
                .retrieve()
                .body(ReviewResponse.class);
    }

    public record ReviewResponse(
            Long id,
            Long bookingId,
            Long customerId,
            Long providerId,
            Integer rating,
            String comment,
            Boolean active,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
    }
}