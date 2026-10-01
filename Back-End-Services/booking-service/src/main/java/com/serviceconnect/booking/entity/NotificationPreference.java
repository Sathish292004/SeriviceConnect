package com.serviceconnect.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
public class NotificationPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_messages", nullable = false)
    private boolean chatMessages = true;

    @Column(name = "quotes", nullable = false)
    private boolean quotes = true;

    @Column(name = "bookings", nullable = false)
    private boolean bookings = true;

    @Column(name = "support", nullable = false)
    private boolean support = true;

    @Column(name = "reviews", nullable = false)
    private boolean reviews = true;

    @Column(name = "reminders", nullable = false)
    private boolean reminders = true;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
