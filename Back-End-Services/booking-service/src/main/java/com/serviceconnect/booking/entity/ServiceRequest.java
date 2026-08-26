package com.serviceconnect.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "service_requests",
        indexes = {
                @Index(
                        name = "idx_service_requests_customer",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_service_requests_provider",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_service_requests_provider_status",
                        columnList = "provider_id,status"
                ),
                @Index(
                        name = "idx_service_requests_customer_status",
                        columnList = "customer_id,status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "customer_id",
            nullable = false
    )
    private Long customerId;


    @Column(
            name = "provider_id",
            nullable = false
    )
    private Long providerId;


    @Column(
            name = "service_type",
            nullable = false,
            length = 100
    )
    private String serviceType;


    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;


    @Column(
            name = "service_address",
            nullable = false,
            length = 500
    )
    private String serviceAddress;


    @Column(name = "latitude")
    private Double latitude;


    @Column(name = "longitude")
    private Double longitude;


    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status;


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
}