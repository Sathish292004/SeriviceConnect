package com.serviceconnect.provider.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProviderStatusRequest(

        @NotBlank(message = "Provider status is required")
        String status
) {
}