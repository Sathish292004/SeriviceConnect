package com.serviceconnect.provider.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ProviderResponse(

        Long id,

        Long userId,

        String businessName,

        String description,

        String phone,

        String email,

        String address,

        String city,

        String state,

        String postalCode,

        Double latitude,

        Double longitude,

        String status,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt,

        List<ProviderPhotoResponse> photos

) {
}