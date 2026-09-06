package com.serviceconnect.provider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "provider_availability",
        indexes = {
                @Index(
                        name = "idx_provider_availability_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_provider_availability_provider_day",
                        columnList = "provider_id, day_of_week"
                ),
                @Index(
                        name = "idx_provider_availability_active",
                        columnList = "provider_id, active"
                )
        }
)
@Getter
@Setter
public class ProviderAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}