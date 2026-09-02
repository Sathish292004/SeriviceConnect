package com.serviceconnect.auth.repository;

import com.serviceconnect.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    @Modifying
    @Query("""
            UPDATE RefreshToken r
            SET r.revokedAt = :revokedAt
            WHERE r.user.id = :userId
              AND r.revokedAt IS NULL
            """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") OffsetDateTime revokedAt
    );
}