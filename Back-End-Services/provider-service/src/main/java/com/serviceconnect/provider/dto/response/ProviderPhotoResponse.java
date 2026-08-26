package com.serviceconnect.provider.dto.response;

import java.time.OffsetDateTime;

public record ProviderPhotoResponse(

        Long id,

        Long providerId,

        String imageUrl,

        Integer displayOrder,

        OffsetDateTime createdAt

) {
}