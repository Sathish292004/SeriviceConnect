package com.serviceconnect.auth.dto.response;

import com.serviceconnect.auth.enums.Role;
import lombok.Builder;

@Builder
public record UserRoleResponse(
        Long id,
        Role role
) {
}