package com.serviceconnect.auth.dto.response;

import com.serviceconnect.auth.enums.Role;
import lombok.Builder;

@Builder
public record LoginResponse(

        Long id,
        String email,
        Role role,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn

) {
}