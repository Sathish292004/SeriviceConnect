package com.serviceconnect.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response"
)
public record ErrorResponse(

        @Schema(
                description = "HTTP status code",
                example = "404"
        )
        int status,

        @Schema(
                description = "Description of the error",
                example = "User not found"
        )
        String message
) {
}