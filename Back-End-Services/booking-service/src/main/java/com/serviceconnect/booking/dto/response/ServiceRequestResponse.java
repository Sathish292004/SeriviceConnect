package com.serviceconnect.booking.dto.response;

import java.math.BigDecimal;
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

        BigDecimal priceSnapshot,

        String status,

        String customerPhone,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt

) {
}