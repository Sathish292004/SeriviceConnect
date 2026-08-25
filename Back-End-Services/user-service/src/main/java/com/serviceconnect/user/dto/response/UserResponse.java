package com.serviceconnect.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        name = "UserResponse",
        description = "Response containing user profile information"
)
public record UserResponse(

        @Schema(
                description = "User ID from Auth Service",
                example = "6",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        Long id,

        @Schema(
                description = "User's first name",
                example = "Sathish"
        )
        String firstName,

        @Schema(
                description = "User's last name",
                example = "Kumar"
        )
        String lastName,

        @Schema(
                description = "User's phone number",
                example = "9876543210"
        )
        String phone
) {
}