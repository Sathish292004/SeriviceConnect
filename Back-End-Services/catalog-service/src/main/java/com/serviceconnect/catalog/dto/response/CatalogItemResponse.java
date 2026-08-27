package com.serviceconnect.catalog.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CatalogItemResponse(

        Long id,

        Long providerId,

        String name,

        String description,

        String category,

        BigDecimal price,

        Integer durationMinutes,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}