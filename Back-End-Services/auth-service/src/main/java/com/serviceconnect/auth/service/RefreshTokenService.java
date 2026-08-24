package com.serviceconnect.auth.service;

import com.serviceconnect.auth.entity.RefreshToken;
import com.serviceconnect.auth.entity.User;
import com.serviceconnect.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final long REFRESH_TOKEN_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String createRefreshToken(User user) {

        byte[] randomBytes =
                new byte[REFRESH_TOKEN_BYTES];

        secureRandom.nextBytes(randomBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        OffsetDateTime now =
                OffsetDateTime.now();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .id(UUID.randomUUID())
                        .user(user)
                        .tokenHash(hash(rawToken))
                        .createdAt(now)
                        .expiresAt(
                                now.plusDays(
                                        REFRESH_TOKEN_DAYS
                                )
                        )
                        .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public User validateAndRevoke(String rawToken) {

        String tokenHash = hash(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.isExpired()) {
            throw new IllegalArgumentException(
                    "Refresh token has expired"
            );
        }

        refreshToken.setRevokedAt(
                OffsetDateTime.now()
        );

        return refreshToken.getUser();
    }

    private String hash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    @Transactional
    public void revoke(String rawToken) {

        String tokenHash = hash(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );

        if (refreshToken.isRevoked()) {
            return;
        }

        refreshToken.setRevokedAt(
                OffsetDateTime.now()
        );
    }
}