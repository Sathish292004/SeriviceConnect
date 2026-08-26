package com.serviceconnect.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.net.InetAddress;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_actor",
                        columnList = "actor_id"
                ),
                @Index(
                        name = "idx_audit_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_audit_resource",
                        columnList = "resource_type, resource_id"
                ),
                @Index(
                        name = "idx_audit_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "actor_id",
            nullable = false
    )
    private Long actorId;

    @Column(
            name = "actor_role",
            nullable = false,
            length = 50
    )
    private String actorRole;

    @Column(
            nullable = false,
            length = 100
    )
    private String action;

    @Column(
            name = "resource_type",
            nullable = false,
            length = 100
    )
    private String resourceType;

    @Column(
            name = "resource_id",
            length = 100
    )
    private String resourceId;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "ip_address",
            columnDefinition = "INET"
    )
    private InetAddress ipAddress;

    @Column(
            name = "user_agent",
            columnDefinition = "TEXT"
    )
    private String userAgent;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;
}