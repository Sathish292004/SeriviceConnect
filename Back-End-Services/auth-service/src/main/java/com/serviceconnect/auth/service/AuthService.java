package com.serviceconnect.auth.service;

import com.serviceconnect.auth.client.UserServiceClient;
import com.serviceconnect.auth.dto.request.LoginRequest;
import com.serviceconnect.auth.dto.request.RefreshTokenRequest;
import com.serviceconnect.auth.dto.request.RegisterRequest;
import com.serviceconnect.auth.dto.response.LoginResponse;
import com.serviceconnect.auth.dto.response.RefreshTokenResponse;
import com.serviceconnect.auth.dto.response.RegisterResponse;
import com.serviceconnect.auth.dto.response.SecuritySettingsResponse;
import com.serviceconnect.auth.dto.response.UserRoleResponse;
import com.serviceconnect.auth.entity.User;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.enums.Role;
import com.serviceconnect.auth.exception.EmailAlreadyExistsException;
import com.serviceconnect.auth.exception.PhoneAlreadyExistsException;
import com.serviceconnect.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserServiceClient userServiceClient;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    private final RefreshTokenService refreshTokenService;

    private final VerificationTokenService verificationTokenService;

    // Email and phone verification
    private final VerificationService verificationService;


    // ============================================================
    // CUSTOMER REGISTRATION
    // ============================================================

    @Transactional
    public RegisterResponse register(
            RegisterRequest request) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        String phone =
                request.phone()
                        .trim();


        if (userRepository.existsByEmail(email)) {

            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }


        if (userRepository.existsByPhone(phone)) {

            throw new PhoneAlreadyExistsException(
                    "Phone number is already registered"
            );
        }


        OffsetDateTime now =
                OffsetDateTime.now();


        User user =
                User.builder()
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .phone(phone)
                        .role(Role.CUSTOMER)
                        .enabled(false)
                        .emailVerified(false)
                        .phoneVerified(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();


        User savedUser =
                userRepository.save(user);


        log.info(
                "Customer registration completed: userId={}, email={}",
                savedUser.getId(),
                savedUser.getEmail()
        );


        // ========================================================
        // CREATE VERIFICATION TOKEN
        // ========================================================

        String verificationToken =
                verificationTokenService.createToken(
                        savedUser.getId()
                );


        // ========================================================
        // GENERATE OTP AND SEND THROUGH BREVO
        // ========================================================

        verificationService.generateVerificationCode(
                verificationToken,
                VerificationChannel.EMAIL
        );


        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .enabled(savedUser.isEnabled())
                .verificationToken(verificationToken)
                .build();
    }


    // ============================================================
    // LOGIN
    // ============================================================

    @Transactional
    public LoginResponse login(
            LoginRequest request) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );


        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Authenticated user not found"
                                )
                        );


        String accessToken =
                jwtTokenService.generateAccessToken(
                        user
                );


        String refreshToken =
                refreshTokenService.createRefreshToken(
                        user
                );


        log.info(
                "User login successful: userId={}, role={}",
                user.getId(),
                user.getRole()
        );


        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .build();
    }


    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    @Transactional
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request) {

        User user =
                refreshTokenService.validateAndRevoke(
                        request.refreshToken()
                );


        String newAccessToken =
                jwtTokenService.generateAccessToken(
                        user
                );


        String newRefreshToken =
                refreshTokenService.createRefreshToken(
                        user
                );


        log.info(
                "Session refreshed successfully: userId={}",
                user.getId()
        );


        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .build();
    }


    // ============================================================
    // LOGOUT
    // ============================================================

    @Transactional
    public void logout(
            String refreshToken) {

        refreshTokenService.revoke(
                refreshToken
        );


        log.info(
                "User logout completed"
        );
    }


    // ============================================================
    // LOGOUT ALL SESSIONS
    // ============================================================

    @Transactional
    public void logoutAllSessions(
            Long userId) {

        refreshTokenService.revokeAllForUser(
                userId
        );


        log.info(
                "All sessions revoked: userId={}",
                userId
        );
    }


    // ============================================================
    // PROVIDER REGISTRATION
    // ============================================================

    @Transactional
    public RegisterResponse registerProvider(
            RegisterRequest request) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        String phone =
                request.phone()
                        .trim();


        if (userRepository.existsByEmail(email)) {

            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }


        if (userRepository.existsByPhone(phone)) {

            throw new PhoneAlreadyExistsException(
                    "Phone number is already registered"
            );
        }


        OffsetDateTime now =
                OffsetDateTime.now();


        User user =
                User.builder()
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        request.password()
                                )
                        )
                        .phone(phone)
                        .role(Role.PROVIDER)
                        .enabled(false)
                        .emailVerified(false)
                        .phoneVerified(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();


        User savedUser =
                userRepository.save(user);


        log.info(
                "Provider registration completed: userId={}, email={}",
                savedUser.getId(),
                savedUser.getEmail()
        );


        // ========================================================
        // CREATE VERIFICATION TOKEN
        // ========================================================

        String verificationToken =
                verificationTokenService.createToken(
                        savedUser.getId()
                );


        // ========================================================
        // GENERATE OTP AND SEND THROUGH BREVO
        // ========================================================

        verificationService.generateVerificationCode(
                verificationToken,
                VerificationChannel.EMAIL
        );


        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .enabled(savedUser.isEnabled())
                .verificationToken(verificationToken)
                .build();
    }


    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @Transactional
    public void changePassword(
            Long userId,
            String currentPassword,
            String newPassword) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );


        // ========================================================
        // VERIFY CURRENT PASSWORD
        // ========================================================

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect"
            );
        }


        // ========================================================
        // PREVENT REUSING CURRENT PASSWORD
        // ========================================================

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be different from current password"
            );
        }


        // ========================================================
        // HASH NEW PASSWORD
        // ========================================================

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        user.setUpdatedAt(
                OffsetDateTime.now()
        );


        userRepository.save(user);


        // ========================================================
        // INVALIDATE ALL REFRESH TOKENS
        // ========================================================

        refreshTokenService.revokeAllForUser(
                userId
        );


        log.info(
                "Password changed and all sessions revoked: userId={}",
                userId
        );
    }


    // ============================================================
    // GET USER ROLE
    // ============================================================

    @Transactional(readOnly = true)
    public UserRoleResponse getUserRole(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );


        return UserRoleResponse.builder()
                .id(user.getId())
                .role(user.getRole())
                .build();
    }


    // ============================================================
    // GET SECURITY SETTINGS
    // ============================================================

    @Transactional(readOnly = true)
    public SecuritySettingsResponse getSecuritySettings(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );


        return new SecuritySettingsResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isEnabled(),
                user.isEmailVerified(),
                user.isPhoneVerified()
        );
    }


    // ============================================================
    // DELETE ACCOUNT
    // ============================================================

    @Transactional
    public void deleteAccount(
            Long userId,
            String password,
            String authorizationHeader) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );


        // ========================================================
        // VERIFY CURRENT PASSWORD
        // ========================================================

        if (!passwordEncoder.matches(
                password,
                user.getPassword()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is incorrect"
            );
        }


        // ========================================================
        // DELETE USER PROFILE
        // ========================================================

        userServiceClient.deleteUserProfile(
                userId,
                authorizationHeader
        );


        // ========================================================
        // REVOKE ALL REFRESH TOKENS
        // ========================================================

        refreshTokenService.revokeAllForUser(
                userId
        );


        // ========================================================
        // DELETE AUTH ACCOUNT
        // ========================================================

        userRepository.delete(user);


        log.info(
                "User account deleted: userId={}",
                userId
        );
    }
}