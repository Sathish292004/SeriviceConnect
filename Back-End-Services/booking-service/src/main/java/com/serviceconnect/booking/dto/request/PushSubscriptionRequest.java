package com.serviceconnect.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PushSubscriptionRequest(
        @NotBlank(message = "Endpoint is required")
        String endpoint,

        @NotNull(message = "Keys are required")
        Keys keys,

        String userAgent
) {
    public record Keys(
            @NotBlank(message = "p256dh key is required")
            String p256dh,

            @NotBlank(message = "auth key is required")
            String auth
    ) {}
}
