package com.serviceconnect.booking.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateServiceRequest(

        @NotNull(message = "Provider ID is required")
        Long providerId,

        @NotNull(message = "Catalog item ID is required")
        Long catalogItemId,

        @NotBlank(message = "Description is required")
        @Size(
                max = 1000,
                message = "Description cannot exceed 1000 characters"
        )
        String description,

        @NotBlank(message = "Service address is required")
        @Size(
                max = 500,
                message = "Service address cannot exceed 500 characters"
        )
        String serviceAddress,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0")
        @DecimalMax(value = "90.0")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0")
        @DecimalMax(value = "180.0")
        Double longitude,

        @NotNull(message = "Requested start time is required")
        OffsetDateTime requestedStartAt

) {
}