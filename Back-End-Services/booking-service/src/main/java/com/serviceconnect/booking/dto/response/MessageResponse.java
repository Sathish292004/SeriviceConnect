package com.serviceconnect.booking.dto.response;

import java.time.OffsetDateTime;

public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderRole,
        String message,
        boolean readStatus,
        OffsetDateTime createdAt
) {
}
