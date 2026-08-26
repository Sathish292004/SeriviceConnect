package com.serviceconnect.provider.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "providers",
        indexes = {
                @Index(
                        name = "idx_provider_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_provider_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_provider_city",
                        columnList = "city"
                )
        }
)
@Getter
@Setter
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private Long userId;

    @Column(
            name = "business_name",
            nullable = false,
            length = 150
    )
    private String businessName;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            nullable = false,
            length = 20
    )
    private String phone;

    @Column(
            nullable = false,
            length = 255
    )
    private String email;

    @Column(
            columnDefinition = "TEXT"
    )
    private String address;

    @Column(
            length = 100
    )
    private String city;

    @Column(
            length = 100
    )
    private String state;

    @Column(
            name = "postal_code",
            length = 20
    )
    private String postalCode;

    // Provider location
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(
            nullable = false,
            length = 30
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