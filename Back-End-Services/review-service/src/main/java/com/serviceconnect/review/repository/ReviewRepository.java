package com.serviceconnect.review.repository;

import com.serviceconnect.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    Optional<Review> findByBookingIdAndActiveTrue(Long bookingId);

    Page<Review> findByProviderIdAndActiveTrue(
            Long providerId,
            Pageable pageable
    );

    Page<Review> findByCustomerIdAndActiveTrue(
            Long customerId,
            Pageable pageable
    );

    Page<Review> findByActiveTrue(
            Pageable pageable
    );
}