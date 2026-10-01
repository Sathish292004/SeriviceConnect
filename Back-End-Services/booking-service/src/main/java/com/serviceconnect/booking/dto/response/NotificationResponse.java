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
        OffsetDateTime createdAt,
        boolean emailSent,
        OffsetDateTime emailSentAt,
        String emailRecipient
) {
    public NotificationResponse(
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
        this(id, recipientUserId, type, title, message, relatedEntityType, relatedEntityId, deepLink, read, createdAt, false, null, null);
    }
}
