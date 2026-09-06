package com.serviceconnect.provider.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProviderAvailabilityActiveRequest(

        @NotNull(message = "Active is required")
        Boolean active
) {
}