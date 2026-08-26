package com.serviceconnect.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "support_messages",
        indexes = {
                @Index(
                        name = "idx_support_message_ticket",
                        columnList = "ticket_id"
                ),
                @Index(
                        name = "idx_support_message_sender",
                        columnList = "sender_id"
                ),
                @Index(
                        name = "idx_support_message_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ticket_id",
            nullable = false
    )
    private SupportTicket ticket;

    @Column(
            name = "sender_id",
            nullable = false
    )
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "sender_type",
            nullable = false,
            length = 30
    )
    private SenderType senderType;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;
}