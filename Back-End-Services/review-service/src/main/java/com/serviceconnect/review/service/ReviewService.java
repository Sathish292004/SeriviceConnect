package com.serviceconnect.review.service;

import com.serviceconnect.review.dto.request.ReviewRequest;
import com.serviceconnect.review.dto.response.ReviewResponse;
import com.serviceconnect.review.entity.Review;
import com.serviceconnect.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;


    // ============================================================
    // CREATE REVIEW
    // ============================================================

    public ReviewResponse create(
            Long customerId,
            ReviewRequest request) {

        // One booking can have only one review
        if (reviewRepository.findByBookingId(request.bookingId()).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Review already exists for this booking"
            );
        }

        Review review = new Review();

        review.setBookingId(request.bookingId());
        review.setCustomerId(customerId);
        review.setProviderId(request.providerId());
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setActive(true);

        Review saved = reviewRepository.save(review);

        return toResponse(saved);
    }


    // ============================================================
    // GET REVIEW BY ID
    // ============================================================

    public ReviewResponse getById(Long id) {

        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );

        if (!Boolean.TRUE.equals(review.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }

        return toResponse(review);
    }


    // ============================================================
    // GET ALL ACTIVE REVIEWS
    // ============================================================

    public List<ReviewResponse> getAllActive() {

        return reviewRepository
                .findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET PROVIDER REVIEWS
    // ============================================================

    public List<ReviewResponse> getByProvider(
            Long providerId) {

        return reviewRepository
                .findByProviderIdAndActiveTrue(providerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET CUSTOMER REVIEWS
    // ============================================================

    public List<ReviewResponse> getByCustomer(
            Long customerId) {

        return reviewRepository
                .findByCustomerIdAndActiveTrue(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET REVIEW BY BOOKING
    // ============================================================

    public ReviewResponse getByBooking(
            Long bookingId) {

        Review review =
                reviewRepository
                        .findByBookingIdAndActiveTrue(bookingId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found for this booking"
                                )
                        );

        return toResponse(review);
    }


    // ============================================================
    // UPDATE REVIEW
    // ============================================================

    public ReviewResponse update(
            Long customerId,
            Long id,
            ReviewRequest request) {

        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );

        // Only the review owner can update it
        if (!review.getCustomerId().equals(customerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update another customer's review"
            );
        }

        if (!Boolean.TRUE.equals(review.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }

        // Provider and booking ownership should not change
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review updated =
                reviewRepository.save(review);

        return toResponse(updated);
    }


    // ============================================================
    // DELETE / DEACTIVATE REVIEW
    // ============================================================

    public void deactivate(
            Long customerId,
            Long id) {

        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );

        // Only the review owner can deactivate it
        if (!review.getCustomerId().equals(customerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete another customer's review"
            );
        }

        if (!Boolean.TRUE.equals(review.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }

        review.setActive(false);

        reviewRepository.save(review);
    }


    // ============================================================
    // ENTITY → RESPONSE
    // ============================================================

    private ReviewResponse toResponse(
            Review review) {

        return new ReviewResponse(
                review.getId(),
                review.getBookingId(),
                review.getCustomerId(),
                review.getProviderId(),
                review.getRating(),
                review.getComment(),
                review.getActive(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}