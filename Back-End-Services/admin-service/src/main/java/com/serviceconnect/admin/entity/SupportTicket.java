package com.serviceconnect.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "support_tickets",
        indexes = {
                @Index(name = "idx_support_ticket_customer", columnList = "customer_id"),
                @Index(name = "idx_support_ticket_agent", columnList = "assigned_agent_id"),
                @Index(name = "idx_support_ticket_status", columnList = "status"),
                @Index(name = "idx_support_ticket_created_at", columnList = "created_at"),
                @Index(name = "idx_support_ticket_priority", columnList = "priority")
        }
)
@Getter
@Setter
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "ticket_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String ticketNumber;

    @Column(
            name = "customer_id",
            nullable = false
    )
    private Long customerId;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(
            nullable = false,
            length = 200
    )
    private String subject;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TicketPriority priority;

    @Column(
            nullable = false,
            length = 50
    )
    private String category;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Version
    @Column(
            nullable = false
    )
    private Long version;
}