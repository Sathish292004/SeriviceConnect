package com.serviceconnect.auth.service;

import com.serviceconnect.auth.entity.User;
import com.serviceconnect.auth.entity.VerificationToken;
import com.serviceconnect.auth.repository.UserRepository;
import com.serviceconnect.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String createToken(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        OffsetDateTime now = OffsetDateTime.now();

        verificationTokenRepository
                .findTopByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(
                        userId
                )
                .ifPresent(existingToken -> {

                    existingToken.setConsumedAt(now);

                    verificationTokenRepository.save(
                            existingToken
                    );
                });

        String rawToken = generateToken();

        VerificationToken token =
                VerificationToken.builder()
                        .user(user)
                        .tokenHash(hashToken(rawToken))
                        .expiresAt(
                                now.plusMinutes(
                                        TOKEN_EXPIRY_MINUTES
                                )
                        )
                        .createdAt(now)
                        .build();

        verificationTokenRepository.save(token);

        return rawToken;
    }

    @Transactional(readOnly = true)
    public User validate(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {

            throw new IllegalArgumentException(
                    "Verification token is required"
            );
        }

        String tokenHash = hashToken(rawToken);

        VerificationToken token =
                verificationTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid verification token"
                                )
                        );

        OffsetDateTime now = OffsetDateTime.now();

        if (token.getConsumedAt() != null) {

            throw new IllegalStateException(
                    "Verification token has already been used"
            );
        }

        if (token.getExpiresAt().isBefore(now)) {

            throw new IllegalStateException(
                    "Verification token has expired"
            );
        }

        return token.getUser();
    }

    @Transactional
    public void consume(Long userId) {

        VerificationToken token =
                verificationTokenRepository
                        .findTopByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Verification token not found"
                                )
                        );

        token.setConsumedAt(
                OffsetDateTime.now()
        );

        verificationTokenRepository.save(token);
    }

    private String generateToken() {

        byte[] bytes = new byte[TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {

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
}