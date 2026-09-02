package com.serviceconnect.auth.service;

import com.serviceconnect.auth.dto.request.LoginRequest;
import com.serviceconnect.auth.dto.request.RefreshTokenRequest;
import com.serviceconnect.auth.dto.request.RegisterRequest;
import com.serviceconnect.auth.dto.response.LoginResponse;
import com.serviceconnect.auth.dto.response.RefreshTokenResponse;
import com.serviceconnect.auth.dto.response.RegisterResponse;
import com.serviceconnect.auth.dto.response.UserRoleResponse;
import com.serviceconnect.auth.entity.User;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.enums.Role;
import com.serviceconnect.auth.exception.EmailAlreadyExistsException;
import com.serviceconnect.auth.exception.PhoneAlreadyExistsException;
import com.serviceconnect.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;

    // Email and phone verification
    private final VerificationService verificationService;


    // =========================
    // CUSTOMER REGISTRATION
    // =========================

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        String phone = request.phone()
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

        OffsetDateTime now = OffsetDateTime.now();

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .phone(phone)
                .role(Role.CUSTOMER)
                .enabled(false)
                .emailVerified(false)
                .phoneVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        // Create verification token
        String verificationToken =
                verificationTokenService.createToken(
                        savedUser.getId()
                );

        // Generate OTP and send it through Brevo
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


    // =========================
    // LOGIN
    // =========================

    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(
                        userDetails.getUsername()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user not found"
                        )
                );

        String accessToken =
                jwtTokenService.generateAccessToken(user);

        String refreshToken =
                refreshTokenService.createRefreshToken(user);

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


    // =========================
    // REFRESH TOKEN
    // =========================

    @Transactional
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {

        User user =
                refreshTokenService.validateAndRevoke(
                        request.refreshToken()
                );

        String newAccessToken =
                jwtTokenService.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .build();
    }


    // =========================
    // LOGOUT
    // =========================

    @Transactional
    public void logout(String refreshToken) {

        refreshTokenService.revoke(refreshToken);
    }


    // =========================
    // PROVIDER REGISTRATION
    // =========================

    @Transactional
    public RegisterResponse registerProvider(
            RegisterRequest request
    ) {

        String email = request.email()
                .trim()
                .toLowerCase();

        String phone = request.phone()
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

        OffsetDateTime now = OffsetDateTime.now();

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .phone(phone)
                .role(Role.PROVIDER)
                .enabled(false)
                .emailVerified(false)
                .phoneVerified(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        // Create verification token
        String verificationToken =
                verificationTokenService.createToken(
                        savedUser.getId()
                );

        // Generate OTP and send it through Brevo
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

    // =========================
// CHANGE PASSWORD
// =========================

    @Transactional
    public void changePassword(
            Long userId,
            String currentPassword,
            String newPassword
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );

        // Verify current password
        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect"
            );
        }

        // Prevent using the same password again
        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password must be different from current password"
            );
        }

        // Hash the new password
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        user.setUpdatedAt(
                OffsetDateTime.now()
        );

        userRepository.save(user);

        // Security: invalidate all refresh tokens
        // so the user must authenticate again.
        refreshTokenService.revokeAllForUser(userId);
    }

    // =========================
    // GET USER ROLE
    // =========================

    @Transactional(readOnly = true)
    public UserRoleResponse getUserRole(Long userId) {

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
}