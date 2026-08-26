package com.serviceconnect.provider.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProviderPhotoRequest(

        @NotBlank(message = "Image URL is required")
        @Size(max = 2048, message = "Image URL is too long")
        String imageUrl,

        Integer displayOrder
) {
}