package com.serviceconnect.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "OnboardingStatusResponse",
        description = "Current onboarding completion status"
)
public record OnboardingStatusResponse(

        @Schema(
                description = "Whether the user's onboarding is complete",
                example = "false"
        )
        boolean completed,

        @Schema(
                description = "Profile fields that are still required",
                example = "[\"firstName\", \"lastName\"]"
        )
        List<String> missingFields
) {
}