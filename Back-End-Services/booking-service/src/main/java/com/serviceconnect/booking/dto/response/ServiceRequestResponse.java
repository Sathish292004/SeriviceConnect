package com.serviceconnect.booking.dto.response;

import java.time.OffsetDateTime;

public record ServiceRequestResponse(
        Long id,
        Long customerId,
        Long providerId,
        String serviceType,
        String description,
        String serviceAddress,
        Double latitude,
        Double longitude,
        String status,
        String customerPhone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}