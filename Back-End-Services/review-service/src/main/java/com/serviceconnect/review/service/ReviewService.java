package com.serviceconnect.review.service;

import com.serviceconnect.review.client.BookingServiceClient;
import com.serviceconnect.review.client.ProviderServiceClient;
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

    private final BookingServiceClient bookingServiceClient;

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // CREATE REVIEW
    // ============================================================

    public ReviewResponse create(
            Long customerId,
            String authorizationHeader,
            ReviewRequest request) {


        // --------------------------------------------------------
        // 1. Prevent duplicate review
        // --------------------------------------------------------

        if (reviewRepository
                .findByBookingId(request.bookingId())
                .isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Review already exists for this booking"
            );
        }


        // --------------------------------------------------------
        // 2. Get booking from Booking Service
        // --------------------------------------------------------

        BookingServiceClient.BookingResponse booking;

        try {

            booking =
                    bookingServiceClient.getBookingById(
                            request.bookingId(),
                            authorizationHeader
                    );

        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify booking with Booking Service"
            );
        }


        // --------------------------------------------------------
        // 3. Verify booking exists
        // --------------------------------------------------------

        if (booking == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }


        // --------------------------------------------------------
        // 4. Verify customer owns the booking
        // --------------------------------------------------------

        if (!booking.customerId().equals(customerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot review another customer's booking"
            );
        }


        // --------------------------------------------------------
        // 5. Verify provider matches booking
        // --------------------------------------------------------

        if (!booking.providerId().equals(
                request.providerId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider does not match the booking"
            );
        }


        // --------------------------------------------------------
        // 6. Review only completed bookings
        // --------------------------------------------------------

        if (!"COMPLETED".equalsIgnoreCase(
                booking.status())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review can only be created for completed bookings"
            );
        }


        // --------------------------------------------------------
        // 7. Get provider from Provider Service
        // --------------------------------------------------------

        ProviderServiceClient.ProviderResponse provider;

        try {

            provider =
                    providerServiceClient.getProviderById(
                            request.providerId(),
                            authorizationHeader
                    );

        } catch (Exception ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify provider with Provider Service"
            );
        }


        // --------------------------------------------------------
        // 8. Verify provider exists
        // --------------------------------------------------------

        if (provider == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not found"
            );
        }


        // --------------------------------------------------------
        // 9. Verify provider is approved
        // --------------------------------------------------------

        if (!"APPROVED".equalsIgnoreCase(
                provider.status())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider is not currently approved"
            );
        }


        // --------------------------------------------------------
        // 10. Create review
        // --------------------------------------------------------

        Review review = new Review();

        review.setBookingId(
                request.bookingId()
        );

        review.setCustomerId(
                customerId
        );

        review.setProviderId(
                request.providerId()
        );

        review.setRating(
                request.rating()
        );

        review.setComment(
                request.comment()
        );

        review.setActive(true);


        // --------------------------------------------------------
        // 11. Save review
        // --------------------------------------------------------

        Review saved =
                reviewRepository.save(review);


        return toResponse(saved);
    }


    // ============================================================
    // GET REVIEW BY ID
    // ============================================================

    public ReviewResponse getById(
            Long id) {

        Review review =
                reviewRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );


        if (!Boolean.TRUE.equals(
                review.getActive())) {

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
                .findByProviderIdAndActiveTrue(
                        providerId
                )
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
                .findByCustomerIdAndActiveTrue(
                        customerId
                )
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
                        .findByBookingIdAndActiveTrue(
                                bookingId
                        )
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
                reviewRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );


        // --------------------------------------------------------
        // Only owner can update
        // --------------------------------------------------------

        if (!review.getCustomerId()
                .equals(customerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update another customer's review"
            );
        }


        // --------------------------------------------------------
        // Review must be active
        // --------------------------------------------------------

        if (!Boolean.TRUE.equals(
                review.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }


        // --------------------------------------------------------
        // Booking and provider cannot change
        // --------------------------------------------------------

        review.setRating(
                request.rating()
        );

        review.setComment(
                request.comment()
        );


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
                reviewRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );


        // --------------------------------------------------------
        // Only owner can deactivate
        // --------------------------------------------------------

        if (!review.getCustomerId()
                .equals(customerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete another customer's review"
            );
        }


        // --------------------------------------------------------
        // Review must be active
        // --------------------------------------------------------

        if (!Boolean.TRUE.equals(
                review.getActive())) {

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