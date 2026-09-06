package com.serviceconnect.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "service_requests",
        indexes = {
                @Index(
                        name = "idx_service_requests_customer_created",
                        columnList = "customer_id,created_at"
                ),
                @Index(
                        name = "idx_service_requests_provider_created",
                        columnList = "provider_id,created_at"
                ),
                @Index(
                        name = "idx_service_requests_provider_status_created",
                        columnList = "provider_id,status,created_at"
                ),
                @Index(
                        name = "idx_service_requests_provider_requested_start",
                        columnList = "provider_id,requested_start_at"
                )
        }
)
public class ServiceRequest {

    // ============================================================
    // ID
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // CUSTOMER
    // ============================================================

    @Column(
            name = "customer_id",
            nullable = false
    )
    private Long customerId;


    // ============================================================
    // CATALOG
    // ============================================================

    @Column(
            name = "catalog_item_id",
            nullable = false
    )
    private Long catalogItemId;


    // ============================================================
    // PROVIDER
    // ============================================================

    @Column(
            name = "provider_id",
            nullable = false
    )
    private Long providerId;


    // ============================================================
    // SERVICE
    // ============================================================

    @Column(
            name = "service_type",
            nullable = false,
            length = 255
    )
    private String serviceType;


    @Column(
            name = "description",
            nullable = false,
            length = 1000
    )
    private String description;


    @Column(
            name = "service_address",
            nullable = false,
            length = 500
    )
    private String serviceAddress;


    // ============================================================
    // LOCATION
    // ============================================================

    @Column(
            name = "latitude",
            nullable = false
    )
    private Double latitude;


    @Column(
            name = "longitude",
            nullable = false
    )
    private Double longitude;


    // ============================================================
    // BOOKING SCHEDULE
    // ============================================================

    @Column(
            name = "requested_start_at"
    )
    private OffsetDateTime requestedStartAt;


    @Column(
            name = "requested_end_at"
    )
    private OffsetDateTime requestedEndAt;


    // ============================================================
    // PRICE SNAPSHOT
    // ============================================================

    /**
     * Price captured from the catalog when the booking
     * is created.
     *
     * Existing bookings must keep their original price
     * even if the catalog price changes later.
     */
    @Column(
            name = "price_snapshot",
            precision = 12,
            scale = 2
    )
    private BigDecimal priceSnapshot;


    // ============================================================
    // IDEMPOTENCY
    // ============================================================

    /**
     * Client-provided idempotency key.
     *
     * The key is scoped to the customer by the database
     * unique index created in V6.
     */
    @Column(
            name = "idempotency_key",
            length = 100
    )
    private String idempotencyKey;


    /**
     * SHA-256 fingerprint of the booking request payload.
     *
     * This prevents a client from reusing the same
     * idempotency key for different booking data.
     */
    @Column(
            name = "idempotency_fingerprint",
            length = 64
    )
    private String idempotencyFingerprint;


    // ============================================================
    // STATUS
    // ============================================================

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;


    // ============================================================
    // AUDIT
    // ============================================================

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


    // ============================================================
    // JPA LIFECYCLE
    // ============================================================

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now =
                OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                OffsetDateTime.now();
    }


    // ============================================================
    // GETTERS
    // ============================================================

    public Long getId() {
        return id;
    }


    public Long getCustomerId() {
        return customerId;
    }


    public Long getCatalogItemId() {
        return catalogItemId;
    }


    public Long getProviderId() {
        return providerId;
    }


    public String getServiceType() {
        return serviceType;
    }


    public String getDescription() {
        return description;
    }


    public String getServiceAddress() {
        return serviceAddress;
    }


    public Double getLatitude() {
        return latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public OffsetDateTime getRequestedStartAt() {
        return requestedStartAt;
    }


    public OffsetDateTime getRequestedEndAt() {
        return requestedEndAt;
    }


    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }


    public String getIdempotencyKey() {
        return idempotencyKey;
    }


    public String getIdempotencyFingerprint() {
        return idempotencyFingerprint;
    }


    public String getStatus() {
        return status;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }


    // ============================================================
    // SETTERS
    // ============================================================

    public void setCustomerId(
            Long customerId
    ) {
        this.customerId = customerId;
    }


    public void setCatalogItemId(
            Long catalogItemId
    ) {
        this.catalogItemId = catalogItemId;
    }


    public void setProviderId(
            Long providerId
    ) {
        this.providerId = providerId;
    }


    public void setServiceType(
            String serviceType
    ) {
        this.serviceType = serviceType;
    }


    public void setDescription(
            String description
    ) {
        this.description = description;
    }


    public void setServiceAddress(
            String serviceAddress
    ) {
        this.serviceAddress = serviceAddress;
    }


    public void setLatitude(
            Double latitude
    ) {
        this.latitude = latitude;
    }


    public void setLongitude(
            Double longitude
    ) {
        this.longitude = longitude;
    }


    public void setRequestedStartAt(
            OffsetDateTime requestedStartAt
    ) {
        this.requestedStartAt = requestedStartAt;
    }


    public void setRequestedEndAt(
            OffsetDateTime requestedEndAt
    ) {
        this.requestedEndAt = requestedEndAt;
    }


    public void setPriceSnapshot(
            BigDecimal priceSnapshot
    ) {
        this.priceSnapshot = priceSnapshot;
    }


    public void setIdempotencyKey(
            String idempotencyKey
    ) {
        this.idempotencyKey = idempotencyKey;
    }


    public void setIdempotencyFingerprint(
            String idempotencyFingerprint
    ) {
        this.idempotencyFingerprint =
                idempotencyFingerprint;
    }


    public void setStatus(
            String status
    ) {
        this.status = status;
    }


    public void setCreatedAt(
            OffsetDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }


    public void setUpdatedAt(
            OffsetDateTime updatedAt
    ) {
        this.updatedAt = updatedAt;
    }
}