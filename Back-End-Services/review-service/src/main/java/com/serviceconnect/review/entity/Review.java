package com.serviceconnect.review.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_booking",
                        columnNames = "booking_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Review {

    // ============================================================
    // ID
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // BOOKING
    // ============================================================

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;


    // ============================================================
    // CUSTOMER
    // ============================================================

    @Column(name = "customer_id", nullable = false)
    private Long customerId;


    // ============================================================
    // PROVIDER
    // ============================================================

    @Column(name = "provider_id", nullable = false)
    private Long providerId;


    // ============================================================
    // RATING
    // ============================================================

    @Column(nullable = false)
    private Integer rating;


    // ============================================================
    // COMMENT
    // ============================================================

    @Column(length = 1000)
    private String comment;


    // ============================================================
    // ACTIVE
    // ============================================================

    @Column(nullable = false)
    private Boolean active = true;


    // ============================================================
    // CREATED AT
    // ============================================================

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // ============================================================
    // UPDATED AT
    // ============================================================

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // ============================================================
    // PRE PERSIST
    // ============================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (active == null) {
            active = true;
        }
    }


    // ============================================================
    // PRE UPDATE
    // ============================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}