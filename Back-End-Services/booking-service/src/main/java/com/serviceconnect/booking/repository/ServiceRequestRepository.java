package com.serviceconnect.booking.repository;

import com.serviceconnect.booking.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository
        extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    List<ServiceRequest> findByProviderIdOrderByCreatedAtDesc(
            Long providerId
    );

    List<ServiceRequest> findByProviderIdAndStatusOrderByCreatedAtDesc(
            Long providerId,
            String status
    );

    List<ServiceRequest> findByCustomerIdAndStatusOrderByCreatedAtDesc(
            Long customerId,
            String status
    );
}