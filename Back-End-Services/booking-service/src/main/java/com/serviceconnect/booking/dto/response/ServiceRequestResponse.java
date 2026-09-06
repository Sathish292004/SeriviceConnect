package com.serviceconnect.booking.dto.response;

import java.time.OffsetDateTime;

public record ServiceRequestResponse(

        Long id,

        Long customerId,

        Long providerId,

        Long catalogItemId,

        String serviceType,

        String description,

        String serviceAddress,

        Double latitude,

        Double longitude,

        OffsetDateTime requestedStartAt,

        OffsetDateTime requestedEndAt,

        String status,

        String customerPhone,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}