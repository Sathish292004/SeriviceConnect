package com.serviceconnect.review.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(

        Long id,

        Long bookingId,

        Long customerId,

        Long providerId,

        Integer rating,

        String comment,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}