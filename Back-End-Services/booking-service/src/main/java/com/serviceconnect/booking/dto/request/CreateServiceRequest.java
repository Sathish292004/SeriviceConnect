package com.serviceconnect.booking.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateServiceRequest(

        @NotNull
        Long providerId,

        @NotBlank
        @Size(max = 100)
        String serviceType,

        @Size(max = 2000)
        String description,

        @NotBlank
        @Size(max = 500)
        String serviceAddress,

        @DecimalMin(
                value = "-90.0",
                message = "Latitude must be between -90 and 90"
        )
        @DecimalMax(
                value = "90.0",
                message = "Latitude must be between -90 and 90"
        )
        Double latitude,

        @DecimalMin(
                value = "-180.0",
                message = "Longitude must be between -180 and 180"
        )
        @DecimalMax(
                value = "180.0",
                message = "Longitude must be between -180 and 180"
        )
        Double longitude

) {

    // ============================================================
    // COORDINATE VALIDATION
    // ============================================================

    public void validateCoordinates() {

        // Both coordinates omitted -> allowed
        if (latitude == null && longitude == null) {
            return;
        }

        // Only one coordinate provided -> not allowed
        if (latitude == null || longitude == null) {

            throw new IllegalArgumentException(
                    "Latitude and longitude must be provided together"
            );
        }
    }
}