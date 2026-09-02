package com.serviceconnect.auth.service;

import com.serviceconnect.auth.entity.User;
import com.serviceconnect.auth.entity.VerificationChallenge;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.entity.VerificationPurpose;
import com.serviceconnect.auth.repository.UserRepository;
import com.serviceconnect.auth.repository.VerificationChallengeRepository;
import com.serviceconnect.auth.verification.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private static final String GENERIC_MESSAGE =
            "If an account exists for this email, a password reset code has been sent.";

    private final UserRepository userRepository;
    private final VerificationChallengeRepository verificationChallengeRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Requests a password reset code.
     *
     * The response is intentionally generic so that the API
     * does not reveal whether an email address is registered.
     */
    @Transactional
    public String requestPasswordReset(String email) {

        String normalizedEmail =
                email.trim().toLowerCase();

        User user =
                userRepository.findByEmail(normalizedEmail)
                        .orElse(null);

        /*
         * Security:
         * Never reveal whether the email exists.
         */
        if (user == null) {
            return GENERIC_MESSAGE;
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        var existingChallenge =
                verificationChallengeRepository
                        .findTopByUserIdAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                                user.getId(),
                                VerificationChannel.EMAIL,
                                VerificationPurpose.PASSWORD_RESET
                        );

        if (existingChallenge.isPresent()) {

            VerificationChallenge challenge =
                    existingChallenge.get();

            if (challenge.getLastSentAt()
                    .plusSeconds(RESEND_COOLDOWN_SECONDS)
                    .isAfter(now)) {

                /*
                 * Do not reveal throttling information through
                 * a different response to the caller.
                 */
                return GENERIC_MESSAGE;
            }

            /*
             * Invalidate the previous reset code.
             */
            challenge.setConsumedAt(now);

            verificationChallengeRepository.save(
                    challenge
            );
        }

        String code =
                generateCode();

        VerificationChallenge challenge =
                VerificationChallenge.builder()
                        .user(user)
                        .channel(VerificationChannel.EMAIL)
                        .purpose(VerificationPurpose.PASSWORD_RESET)
                        .codeHash(
                                passwordEncoder.encode(code)
                        )
                        .expiresAt(
                                now.plusMinutes(
                                        OTP_EXPIRY_MINUTES
                                )
                        )
                        .attempts(0)
                        .createdAt(now)
                        .lastSentAt(now)
                        .build();

        verificationChallengeRepository.save(
                challenge
        );

        /*
         * Never return or log the actual password reset code.
         */
        emailSender.sendVerificationCode(
                user.getEmail(),
                code
        );

        return GENERIC_MESSAGE;
    }

    /**
     * Resets the user's password after validating the
     * password reset code.
     */
    @Transactional
    public void resetPassword(
            String email,
            String code,
            String newPassword
    ) {

        String normalizedEmail =
                email.trim().toLowerCase();

        User user =
                userRepository.findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid password reset request"
                                )
                        );

        VerificationChallenge challenge =
                verificationChallengeRepository
                        .findTopByUserIdAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                                user.getId(),
                                VerificationChannel.EMAIL,
                                VerificationPurpose.PASSWORD_RESET
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired password reset code"
                                )
                        );

        OffsetDateTime now =
                OffsetDateTime.now();

        /*
         * Check expiration.
         */
        if (challenge.getExpiresAt().isBefore(now)) {

            challenge.setConsumedAt(now);

            verificationChallengeRepository.save(
                    challenge
            );

            throw new IllegalArgumentException(
                    "Password reset code has expired"
            );
        }

        /*
         * Check maximum verification attempts.
         */
        if (challenge.getAttempts() >= MAX_ATTEMPTS) {

            challenge.setConsumedAt(now);

            verificationChallengeRepository.save(
                    challenge
            );

            throw new IllegalArgumentException(
                    "Maximum password reset attempts exceeded"
            );
        }

        /*
         * Verify the submitted code against the BCrypt hash.
         */
        if (!passwordEncoder.matches(
                code,
                challenge.getCodeHash()
        )) {

            challenge.setAttempts(
                    challenge.getAttempts() + 1
            );

            verificationChallengeRepository.save(
                    challenge
            );

            throw new IllegalArgumentException(
                    "Invalid password reset code"
            );
        }

        /*
         * Password is valid and code is correct.
         *
         * Encode the new password before storing it.
         */
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        user.setUpdatedAt(now);

        userRepository.save(user);

        /*
         * Password reset codes are single-use.
         */
        challenge.setConsumedAt(now);

        verificationChallengeRepository.save(
                challenge
        );
    }

    private String generateCode() {

        int minimum = 100000;
        int maximum = 1000000;

        return String.valueOf(
                secureRandom.nextInt(
                        maximum - minimum
                ) + minimum
        );
    }
}