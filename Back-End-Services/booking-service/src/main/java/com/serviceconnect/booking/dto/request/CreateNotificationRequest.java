package com.serviceconnect.booking.dto.request;

import com.serviceconnect.booking.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateNotificationRequest(
        @NotNull(message = "Recipient user ID is required")
        @Positive(message = "Recipient user ID must be positive")
        Long recipientUserId,

        @NotNull(message = "Notification type is required")
        NotificationType type,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Message is required")
        String message,

        String relatedEntityType,
        Long relatedEntityId,
        String deepLink,
        String idempotencyKey
) {
}
