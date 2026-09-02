package com.serviceconnect.auth.repository;

import com.serviceconnect.auth.entity.VerificationChallenge;
import com.serviceconnect.auth.entity.VerificationChannel;
import com.serviceconnect.auth.entity.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationChallengeRepository
        extends JpaRepository<VerificationChallenge, UUID> {

    Optional<VerificationChallenge> findTopByUserIdAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            Long userId,
            VerificationChannel channel,
            VerificationPurpose purpose
    );
}