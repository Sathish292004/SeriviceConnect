package com.serviceconnect.booking.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateConversationRequest(
        @NotNull(message = "Provider ID is required")
        Long providerId,

        Long catalogItemId
) {
}
