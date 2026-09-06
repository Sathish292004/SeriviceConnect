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
                ),
                @Index(
                        name = "idx_service_requests_provider_requested_start",
                        columnList = "provider_id,requested_start_at"
                )
        }
)
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "catalog_item_id", nullable = false)
    private Long catalogItemId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "service_type", nullable = false, length = 255)
    private String serviceType;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "service_address", nullable = false, length = 500)
    private String serviceAddress;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "requested_start_at")
    private OffsetDateTime requestedStartAt;

    @Column(name = "requested_end_at")
    private OffsetDateTime requestedEndAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;


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


    public Long getId() {
        return id;
    }


    public Long getCustomerId() {
        return customerId;
    }


    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public Long getCatalogItemId() {
        return catalogItemId;
    }


    public void setCatalogItemId(Long catalogItemId) {
        this.catalogItemId = catalogItemId;
    }


    public Long getProviderId() {
        return providerId;
    }


    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }


    public String getServiceType() {
        return serviceType;
    }


    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getServiceAddress() {
        return serviceAddress;
    }


    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }


    public Double getLatitude() {
        return latitude;
    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public OffsetDateTime getRequestedStartAt() {
        return requestedStartAt;
    }


    public void setRequestedStartAt(
            OffsetDateTime requestedStartAt) {

        this.requestedStartAt =
                requestedStartAt;
    }


    public OffsetDateTime getRequestedEndAt() {
        return requestedEndAt;
    }


    public void setRequestedEndAt(
            OffsetDateTime requestedEndAt) {

        this.requestedEndAt =
                requestedEndAt;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(
            OffsetDateTime createdAt) {

        this.createdAt = createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(
            OffsetDateTime updatedAt) {

        this.updatedAt = updatedAt;
    }
}