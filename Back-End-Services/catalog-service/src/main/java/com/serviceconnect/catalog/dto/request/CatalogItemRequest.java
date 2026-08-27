package com.serviceconnect.catalog.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CatalogItemRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Price must be greater than 0")
        BigDecimal price,

        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes
) {
}