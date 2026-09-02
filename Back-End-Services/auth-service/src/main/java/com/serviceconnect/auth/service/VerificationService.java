package com.serviceconnect.auth.service;

import com.serviceconnect.auth.entity.User;
import com.serviceconnect.auth.entity.VerificationChallenge;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.entity.VerificationPurpose;
import com.serviceconnect.auth.repository.VerificationChallengeRepository;
import com.serviceconnect.auth.verification.EmailSender;
import com.serviceconnect.auth.verification.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long OTP_EXPIRY_MINUTES = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final VerificationChallengeRepository verificationChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;

    private final EmailSender emailSender;
    private final SmsSender smsSender;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generateVerificationCode(
            String verificationToken,
            VerificationChannel channel
    ) {

        User user = verificationTokenService.validate(
                verificationToken
        );

        if (channel == VerificationChannel.EMAIL
                && user.isEmailVerified()) {

            throw new IllegalStateException(
                    "Email is already verified"
            );
        }

        if (channel == VerificationChannel.PHONE
                && user.isPhoneVerified()) {

            throw new IllegalStateException(
                    "Phone number is already verified"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        var existingChallenge =
                verificationChallengeRepository
                        .findTopByUserIdAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                                user.getId(),
                                channel,
                                VerificationPurpose.ACCOUNT_VERIFICATION
                        );

        if (existingChallenge.isPresent()) {

            VerificationChallenge challenge =
                    existingChallenge.get();

            Duration elapsed =
                    Duration.between(
                            challenge.getLastSentAt(),
                            now
                    );

            if (elapsed.getSeconds()
                    < RESEND_COOLDOWN_SECONDS) {

                throw new IllegalStateException(
                        "Please wait before requesting another code"
                );
            }
        }

        String code = generateOtp();

        VerificationChallenge challenge =
                VerificationChallenge.builder()
                        .user(user)
                        .channel(channel)
                        .purpose(
                                VerificationPurpose.ACCOUNT_VERIFICATION
                        )
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

        verificationChallengeRepository.save(challenge);

        /*
         * Send the OTP through the appropriate delivery channel.
         */
        if (channel == VerificationChannel.EMAIL) {

            emailSender.sendVerificationCode(
                    user.getEmail(),
                    code
            );

        } else {

            smsSender.sendVerificationCode(
                    user.getPhone(),
                    code
            );
        }

        return code;
    }

    @Transactional
    public void verifyCode(
            String verificationToken,
            VerificationChannel channel,
            String code
    ) {

        User user = verificationTokenService.validate(
                verificationToken
        );

        VerificationChallenge challenge =
                verificationChallengeRepository
                        .findTopByUserIdAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
                                user.getId(),
                                channel,
                                VerificationPurpose.ACCOUNT_VERIFICATION
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Verification code not found"
                                )
                        );

        OffsetDateTime now = OffsetDateTime.now();

        if (challenge.getExpiresAt().isBefore(now)) {

            throw new IllegalStateException(
                    "Verification code has expired"
            );
        }

        if (challenge.getAttempts() >= MAX_ATTEMPTS) {

            throw new IllegalStateException(
                    "Maximum verification attempts exceeded"
            );
        }

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
                    "Invalid verification code"
            );
        }

        challenge.setConsumedAt(now);

        verificationChallengeRepository.save(
                challenge
        );

        if (channel == VerificationChannel.EMAIL) {

            user.setEmailVerified(true);

        } else {

            user.setPhoneVerified(true);
        }

        /*
         * The account becomes enabled only after
         * BOTH email and phone have been verified.
         */
        if (user.isEmailVerified()
                && user.isPhoneVerified()) {

            user.setEnabled(true);

            verificationTokenService.consume(
                    user.getId()
            );
        }

        user.setUpdatedAt(now);

        // Persist the verification state change.
        // The transaction will also track this entity,
        // but explicitly saving keeps the intent clear.
    }

    private String generateOtp() {

        int minimum = 100_000;
        int maximum = 1_000_000;

        return String.valueOf(
                secureRandom.nextInt(
                        minimum,
                        maximum
                )
        );
    }
}