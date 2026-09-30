package com.serviceconnect.booking.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record QuoteResponse(
        Long id,
        Long conversationId,
        Long customerId,
        Long providerId,
        Long catalogItemId,
        String serviceName,
        String description,
        String note,
        BigDecimal amount,
        String currency,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime expiresAt
) {
}
