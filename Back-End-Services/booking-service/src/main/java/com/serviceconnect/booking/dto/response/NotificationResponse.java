package com.serviceconnect.booking.dto.response;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        Long recipientUserId,
        String type,
        String title,
        String message,
        String relatedEntityType,
        Long relatedEntityId,
        String deepLink,
        boolean read,
        OffsetDateTime createdAt
) {
}
