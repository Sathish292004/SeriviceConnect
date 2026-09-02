package com.serviceconnect.auth.repository;

import com.serviceconnect.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken>
    findTopByUserIdAndConsumedAtIsNullOrderByCreatedAtDesc(
            Long userId
    );

    Optional<VerificationToken>
    findByTokenHash(String tokenHash);
}