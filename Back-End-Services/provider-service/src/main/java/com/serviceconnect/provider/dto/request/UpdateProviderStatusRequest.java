package com.serviceconnect.provider.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProviderStatusRequest(

        @NotBlank
        String status
) {
}