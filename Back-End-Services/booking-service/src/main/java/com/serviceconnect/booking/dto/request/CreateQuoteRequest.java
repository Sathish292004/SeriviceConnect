package com.serviceconnect.booking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateQuoteRequest(
        Long catalogItemId,

        String serviceName,

        @NotBlank(message = "Description is required")
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
        BigDecimal amount,

        @Size(max = 1000, message = "Note cannot exceed 1000 characters")
        String note,

        java.time.OffsetDateTime expiresAt
) {
}
