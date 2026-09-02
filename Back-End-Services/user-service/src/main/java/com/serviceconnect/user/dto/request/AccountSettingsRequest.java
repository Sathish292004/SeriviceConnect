package com.serviceconnect.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "AccountSettingsRequest",
        description = "Request used to update the authenticated user's account profile settings"
)
public record AccountSettingsRequest(

        @Schema(
                description = "User's first name",
                example = "Sathish",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "First name must not be blank")
        @Size(
                max = 100,
                message = "First name must not exceed 100 characters"
        )
        String firstName,

        @Schema(
                description = "User's last name",
                example = "Kumar",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Last name must not be blank")
        @Size(
                max = 100,
                message = "Last name must not exceed 100 characters"
        )
        String lastName,

        @Schema(
                description = "User's phone number",
                example = "9876543210",
                maxLength = 20
        )
        @Size(
                max = 20,
                message = "Phone number must not exceed 20 characters"
        )
        String phone
) {
}