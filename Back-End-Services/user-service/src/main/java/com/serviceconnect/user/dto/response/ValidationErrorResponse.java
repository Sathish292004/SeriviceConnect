package com.serviceconnect.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(
        name = "ValidationErrorResponse",
        description = "Response returned when request validation fails"
)
public record ValidationErrorResponse(

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,

        @Schema(
                description = "Validation error message",
                example = "Validation failed"
        )
        String message,

        @Schema(
                description = "Validation errors grouped by field",
                example = "{\"email\":\"must be a well-formed email address\",\"firstName\":\"must not be blank\"}"
        )
        Map<String, String> errors
) {
}