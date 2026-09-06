package com.serviceconnect.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "catalog_items",
        indexes = {
                @Index(
                        name = "idx_catalog_provider_active",
                        columnList = "provider_id, active"
                ),
                @Index(
                        name = "idx_catalog_active_category",
                        columnList = "active, category"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class CatalogItem {

    // ============================================================
    // ID
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // PROVIDER
    // ============================================================

    @Column(
            name = "provider_id",
            nullable = false
    )
    private Long providerId;


    // ============================================================
    // SERVICE DETAILS
    // ============================================================

    @Column(
            nullable = false,
            length = 150
    )
    private String name;


    @Column(
            length = 1000
    )
    private String description;


    @Column(
            nullable = false,
            length = 100
    )
    private String category;


    // ============================================================
    // PRICING
    // ============================================================

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;


    @Column(
            name = "duration_minutes"
    )
    private Integer durationMinutes;


    // ============================================================
    // STATUS
    // ============================================================

    @Column(
            nullable = false
    )
    private Boolean active = true;


    // ============================================================
    // AUDIT
    // ============================================================

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    // ============================================================
    // LIFECYCLE
    // ============================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}