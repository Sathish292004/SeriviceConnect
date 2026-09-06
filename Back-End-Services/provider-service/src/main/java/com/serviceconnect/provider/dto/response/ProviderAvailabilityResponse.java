package com.serviceconnect.provider.dto.response;

import java.time.LocalTime;
import java.time.OffsetDateTime;

public record ProviderAvailabilityResponse(
        Long id,
        Long providerId,
        String dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}