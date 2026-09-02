package com.serviceconnect.auth.dto.response;

import com.serviceconnect.auth.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long id;

    private String email;

    private String phone;

    private Role role;

    private boolean enabled;

    private String verificationToken;
}