package com.serviceconnect.booking.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ConversationResponse(
        Long id,
        Long customerId,
        Long providerId,
        Long catalogItemId,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String lastMessage,
        OffsetDateTime lastMessageAt,
        String otherPartyName,
        String serviceName,
        List<MessageResponse> messages,
        List<QuoteResponse> quotes,
        String customerName,
        String providerName
) {
}
