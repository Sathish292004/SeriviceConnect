package com.serviceconnect.auth.controller;

import com.serviceconnect.auth.dto.request.LoginRequest;
import com.serviceconnect.auth.dto.request.LogoutRequest;
import com.serviceconnect.auth.dto.request.RefreshTokenRequest;
import com.serviceconnect.auth.dto.request.RegisterRequest;
import com.serviceconnect.auth.dto.request.VerificationRequest;
import com.serviceconnect.auth.dto.response.*;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.service.AuthService;
import com.serviceconnect.auth.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerificationService verificationService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(
            @AuthenticationPrincipal Jwt jwt
    ) {

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException(
                    "JWT subject is missing"
            );
        }

        Long id = Long.valueOf(subject);

        String email = jwt.getClaimAsString("email");

        String role = jwt.getClaimAsString("role");

        MeResponse response = MeResponse.builder()
                .id(id)
                .email(email)
                .role(
                        com.serviceconnect.auth.enums.Role
                                .valueOf(role)
                )
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        RefreshTokenResponse response =
                authService.refresh(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {

        authService.logout(
                request.refreshToken()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register/provider")
    public ResponseEntity<RegisterResponse> registerProvider(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerProvider(request));
    }

    @PostMapping("/verification/email/request")
    public ResponseEntity<Void> requestEmailVerification(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.generateVerificationCode(
                request.verificationToken(),
                VerificationChannel.EMAIL
        );

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verification/email/verify")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.verifyCode(
                request.verificationToken(),
                VerificationChannel.EMAIL,
                request.code()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verification/phone/request")
    public ResponseEntity<Void> requestPhoneVerification(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.generateVerificationCode(
                request.verificationToken(),
                VerificationChannel.PHONE
        );

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verification/phone/verify")
    public ResponseEntity<Void> verifyPhone(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.verifyCode(
                request.verificationToken(),
                VerificationChannel.PHONE,
                request.code()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/internal/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRoleResponse> getUserRole(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                authService.getUserRole(userId)
        );
    }
}