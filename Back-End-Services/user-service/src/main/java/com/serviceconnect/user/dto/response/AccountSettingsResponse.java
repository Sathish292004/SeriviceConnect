package com.serviceconnect.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AccountSettingsResponse",
        description = "User account profile settings"
)
public record AccountSettingsResponse(

        @Schema(
                description = "User ID from Auth Service",
                example = "16",
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
        String phone,

        @Schema(
                description = "Whether the user's onboarding is complete",
                example = "true"
        )
        boolean onboardingCompleted
) {
}