package com.serviceconnect.provider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "provider_photos",
        indexes = {
                @Index(name = "idx_provider_photos_provider_id",
                        columnList = "provider_id"),
                @Index(name = "idx_provider_photos_display_order",
                        columnList = "provider_id, display_order")
        }
)
@Getter
@Setter
public class ProviderPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}