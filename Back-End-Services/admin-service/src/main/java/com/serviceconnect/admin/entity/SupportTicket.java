package com.serviceconnect.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "support_tickets",
        indexes = {
                @Index(
                        name = "idx_support_ticket_customer_created",
                        columnList = "customer_id, created_at"
                ),
                @Index(
                        name = "idx_support_ticket_agent_created",
                        columnList = "assigned_agent_id, created_at"
                ),
                @Index(
                        name = "idx_support_ticket_status_created",
                        columnList = "status, created_at"
                )
        }
)
@Getter
@Setter
public class SupportTicket {

    // ============================================================
    // ID
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // TICKET NUMBER
    // ============================================================

    @Column(
            name = "ticket_number",
            nullable = false,
            unique = true,
            length = 30
    )
    private String ticketNumber;


    // ============================================================
    // CUSTOMER
    // ============================================================

    @Column(
            name = "customer_id",
            nullable = false
    )
    private Long customerId;


    // ============================================================
    // ASSIGNED AGENT
    // ============================================================

    @Column(
            name = "assigned_agent_id"
    )
    private Long assignedAgentId;


    // ============================================================
    // CONTENT
    // ============================================================

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


    // ============================================================
    // STATUS
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TicketStatus status;


    // ============================================================
    // PRIORITY
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TicketPriority priority;


    // ============================================================
    // CATEGORY
    // ============================================================

    @Column(
            nullable = false,
            length = 50
    )
    private String category;


    // ============================================================
    // CREATED AT
    // ============================================================

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;


    // ============================================================
    // UPDATED AT
    // ============================================================

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;


    // ============================================================
    // RESOLVED AT
    // ============================================================

    @Column(
            name = "resolved_at"
    )
    private OffsetDateTime resolvedAt;


    // ============================================================
    // OPTIMISTIC LOCK
    // ============================================================

    @Version
    @Column(
            nullable = false
    )
    private Long version;
}