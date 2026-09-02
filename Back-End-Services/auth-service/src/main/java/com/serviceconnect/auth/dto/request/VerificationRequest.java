package com.serviceconnect.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificationRequest(

        @NotBlank(message = "Verification token is required")
        String verificationToken,

        @NotBlank(message = "Code is required")
        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "Code must be exactly 6 digits"
        )
        String code
) {
}