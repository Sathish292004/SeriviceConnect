package com.serviceconnect.auth.dto.response;

import com.serviceconnect.auth.enums.Role;
import lombok.Builder;

@Builder
public record RegisterResponse(

        Long id,
        String email,
        String phone,
        Role role,
        boolean enabled

) {
}