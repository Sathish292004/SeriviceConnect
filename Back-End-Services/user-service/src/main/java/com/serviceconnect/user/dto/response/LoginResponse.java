package com.serviceconnect.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        name = "LoginResponse",
        description = "Response returned after successful authentication"
)
public record LoginResponse(

        @Schema(
                description = "JWT access token",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String accessToken,

        @Schema(
                description = "Token type",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Authenticated user's ID",
                example = "1"
        )
        Long userId,

        @Schema(
                description = "Authenticated user's email",
                example = "sathish@example.com"
        )
        String email
) {
}