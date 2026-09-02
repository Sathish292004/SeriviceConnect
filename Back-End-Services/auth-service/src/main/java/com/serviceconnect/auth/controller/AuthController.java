package com.serviceconnect.auth.controller;

import com.serviceconnect.auth.dto.request.*;
import com.serviceconnect.auth.dto.response.*;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.service.AuthService;
import com.serviceconnect.auth.service.PasswordResetService;
import com.serviceconnect.auth.service.VerificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final VerificationService verificationService;


    // ============================================================
    // REGISTER
    // ============================================================

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


    // ============================================================
    // LOGIN
    // ============================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // CURRENT AUTHENTICATED USER
    // ============================================================

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

        String email =
                jwt.getClaimAsString("email");

        String role =
                jwt.getClaimAsString("role");

        MeResponse response =
                MeResponse.builder()
                        .id(id)
                        .email(email)
                        .role(
                                com.serviceconnect.auth.enums.Role
                                        .valueOf(role)
                        )
                        .build();

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        RefreshTokenResponse response =
                authService.refresh(request);

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // LOGOUT
    // ============================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {

        authService.logout(
                request.refreshToken()
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // PROVIDER REGISTRATION
    // ============================================================

    @PostMapping("/register/provider")
    public ResponseEntity<RegisterResponse> registerProvider(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        authService.registerProvider(request)
                );
    }


    // ============================================================
    // EMAIL VERIFICATION - REQUEST
    // ============================================================

    @PostMapping("/verification/email/request")
    public ResponseEntity<Void> requestEmailVerification(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.generateVerificationCode(
                request.verificationToken(),
                VerificationChannel.EMAIL
        );

        return ResponseEntity
                .accepted()
                .build();
    }


    // ============================================================
    // EMAIL VERIFICATION - VERIFY
    // ============================================================

    @PostMapping("/verification/email/verify")
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.verifyCode(
                request.verificationToken(),
                VerificationChannel.EMAIL,
                request.code()
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // PHONE VERIFICATION - REQUEST
    // ============================================================

    @PostMapping("/verification/phone/request")
    public ResponseEntity<Void> requestPhoneVerification(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.generateVerificationCode(
                request.verificationToken(),
                VerificationChannel.PHONE
        );

        return ResponseEntity
                .accepted()
                .build();
    }


    // ============================================================
    // PHONE VERIFICATION - VERIFY
    // ============================================================

    @PostMapping("/verification/phone/verify")
    public ResponseEntity<Void> verifyPhone(
            @Valid @RequestBody VerificationRequest request
    ) {

        verificationService.verifyCode(
                request.verificationToken(),
                VerificationChannel.PHONE,
                request.code()
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // INTERNAL - GET USER ROLE
    // ============================================================

    @GetMapping("/internal/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserRoleResponse> getUserRole(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                authService.getUserRole(userId)
        );
    }


    // ============================================================
    // FORGOT PASSWORD
    // ============================================================

    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        String message =
                passwordResetService.requestPasswordReset(
                        request.email()
                );

        return ResponseEntity
                .accepted()
                .body(
                        Map.of(
                                "message",
                                message
                        )
                );
    }


    // ============================================================
    // RESET PASSWORD
    // ============================================================

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        passwordResetService.resetPassword(
                request.email(),
                request.code(),
                request.newPassword()
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException(
                    "JWT subject is missing"
            );
        }

        Long userId = Long.valueOf(subject);

        authService.changePassword(
                userId,
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ============================================================
    // GET MY SECURITY SETTINGS
    // ============================================================

    @GetMapping("/me/security")
    public ResponseEntity<SecuritySettingsResponse> getMySecuritySettings(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                Long.valueOf(
                        jwt.getSubject()
                );

        SecuritySettingsResponse response =
                authService.getSecuritySettings(
                        userId
                );

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // LOGOUT ALL SESSIONS
    // ============================================================

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAllSessions(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId =
                Long.valueOf(
                        jwt.getSubject()
                );

        authService.logoutAllSessions(
                userId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ============================================================
    // DELETE MY ACCOUNT
    // ============================================================

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DeleteAccountRequest request,
            HttpServletRequest httpServletRequest
    ) {

        Long userId =
                Long.valueOf(
                        jwt.getSubject()
                );

        String authorizationHeader =
                httpServletRequest.getHeader(
                        "Authorization"
                );

        if (authorizationHeader == null
                || authorizationHeader.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization header is required"
            );
        }

        authService.deleteAccount(
                userId,
                request.password(),
                authorizationHeader
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}