package com.serviceconnect.provider.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProviderRequest(

        @NotBlank(message = "Business name is required")
        @Size(
                max = 150,
                message = "Business name must not exceed 150 characters"
        )
        String businessName,

        @Size(
                max = 2000,
                message = "Description must not exceed 2000 characters"
        )
        String description,

        @NotBlank(message = "Phone is required")
        @Size(
                max = 20,
                message = "Phone must not exceed 20 characters"
        )
        String phone,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(
                max = 255,
                message = "Email must not exceed 255 characters"
        )
        String email,

        @Size(
                max = 500,
                message = "Address must not exceed 500 characters"
        )
        String address,

        @Size(
                max = 100,
                message = "City must not exceed 100 characters"
        )
        String city,

        @Size(
                max = 100,
                message = "State must not exceed 100 characters"
        )
        String state,

        @Size(
                max = 20,
                message = "Postal code must not exceed 20 characters"
        )
        String postalCode,

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
}