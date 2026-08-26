package com.serviceconnect.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportMessageRequest(

        @NotBlank(message = "Message is required")
        @Size(
                min = 1,
                max = 5000,
                message = "Message must not exceed 5000 characters"
        )
        String message
) {
}