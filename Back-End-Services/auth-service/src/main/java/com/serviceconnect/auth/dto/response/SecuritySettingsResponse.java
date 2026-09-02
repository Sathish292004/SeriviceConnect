package com.serviceconnect.auth.dto.response;

import com.serviceconnect.auth.enums.Role;

public record SecuritySettingsResponse(

        Long id,

        String email,

        String phone,

        Role role,

        boolean enabled,

        boolean emailVerified,

        boolean phoneVerified
) {
}