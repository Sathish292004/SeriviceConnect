package com.serviceconnect.admin.dto.response;

public record ErrorResponse(
        int status,
        String message
) {
}