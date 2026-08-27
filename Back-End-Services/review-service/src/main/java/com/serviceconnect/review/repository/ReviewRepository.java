package com.serviceconnect.review.repository;

import com.serviceconnect.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    Optional<Review> findByBookingIdAndActiveTrue(Long bookingId);

    List<Review> findByProviderIdAndActiveTrue(Long providerId);

    List<Review> findByCustomerIdAndActiveTrue(Long customerId);

    List<Review> findByActiveTrue();
}