package com.serviceconnect.booking.repository;

import com.serviceconnect.booking.entity.ServiceRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {


    // ============================================================
    // CUSTOMER REQUESTS
    // ============================================================

    List<ServiceRequest>
    findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );


    // ============================================================
    // PROVIDER REQUESTS
    // ============================================================

    List<ServiceRequest>
    findByProviderIdOrderByCreatedAtDesc(
            Long providerId
    );


    List<ServiceRequest>
    findByProviderIdAndStatusOrderByCreatedAtDesc(
            Long providerId,
            String status
    );


    // ============================================================
    // IDEMPOTENCY
    // ============================================================

    Optional<ServiceRequest>
    findByCustomerIdAndIdempotencyKey(
            Long customerId,
            String idempotencyKey
    );


    // ============================================================
    // BOOKING OVERLAP
    // ============================================================

    @Query("""
            SELECT CASE
                WHEN COUNT(sr) > 0 THEN true
                ELSE false
            END
            FROM ServiceRequest sr
            WHERE sr.providerId = :providerId
              AND sr.status IN ('PENDING', 'ACCEPTED')
              AND sr.requestedStartAt < :requestedEndAt
              AND sr.requestedEndAt > :requestedStartAt
            """)
    boolean existsOverlappingActiveBooking(
            @Param("providerId")
            Long providerId,

            @Param("requestedStartAt")
            OffsetDateTime requestedStartAt,

            @Param("requestedEndAt")
            OffsetDateTime requestedEndAt
    );
}