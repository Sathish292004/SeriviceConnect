package com.serviceconnect.booking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateServiceRequestStatus(

        @NotBlank
        String status

) {
}