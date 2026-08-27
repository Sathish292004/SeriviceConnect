package com.serviceconnect.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(

        @NotNull(message = "Booking ID is required")
        Long bookingId,

        @NotNull(message = "Provider ID is required")
        Long providerId,

        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @Size(
                max = 1000,
                message = "Comment must not exceed 1000 characters"
        )
        String comment
) {
}