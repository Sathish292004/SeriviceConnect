package com.serviceconnect.review.service;

import com.serviceconnect.review.client.BookingServiceClient;
import com.serviceconnect.review.client.ProviderServiceClient;
import com.serviceconnect.review.dto.request.ReviewRequest;
import com.serviceconnect.review.dto.response.ReviewResponse;
import com.serviceconnect.review.entity.Review;
import com.serviceconnect.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private static final String REVIEW_BOOKING_CONSTRAINT =
            "uk_review_booking";

    private final ReviewRepository reviewRepository;

    private final BookingServiceClient bookingServiceClient;

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // CREATE REVIEW
    // ============================================================

    public ReviewResponse create(
            Long customerId,
            String authorizationHeader,
            ReviewRequest request
    ) {

        // --------------------------------------------------------
        // 1. VALIDATE CUSTOMER
        // --------------------------------------------------------

        if (customerId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer authentication is required"
            );
        }


        // --------------------------------------------------------
        // 2. PREVENT DUPLICATE REVIEW
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
        // 3. GET BOOKING FROM BOOKING SERVICE
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
        // 4. VERIFY BOOKING EXISTS
        // --------------------------------------------------------

        if (booking == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }


        // --------------------------------------------------------
        // 5. CUSTOMER MUST OWN BOOKING
        // --------------------------------------------------------

        if (booking.customerId() == null
                || !booking.customerId().equals(
                customerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot review another customer's booking"
            );
        }


        // --------------------------------------------------------
        // 6. PROVIDER MUST MATCH BOOKING
        // --------------------------------------------------------

        if (booking.providerId() == null
                || !booking.providerId().equals(
                request.providerId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider does not match the booking"
            );
        }


        // --------------------------------------------------------
        // 7. ONLY COMPLETED BOOKINGS CAN BE REVIEWED
        // --------------------------------------------------------

        if (!"COMPLETED".equalsIgnoreCase(
                booking.status()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review can only be created for completed bookings"
            );
        }


        // --------------------------------------------------------
        // 8. VERIFY PROVIDER WITH PROVIDER SERVICE
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
        // 9. VERIFY PROVIDER EXISTS
        // --------------------------------------------------------

        if (provider == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not found"
            );
        }


        // --------------------------------------------------------
        // 10. VERIFY PROVIDER ID
        // --------------------------------------------------------

        if (provider.id() == null
                || !provider.id().equals(
                request.providerId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid provider"
            );
        }


        // --------------------------------------------------------
        // 11. PROVIDER MUST BE APPROVED
        // --------------------------------------------------------

        if (!"APPROVED".equalsIgnoreCase(
                provider.status()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reviews can only be created for approved providers"
            );
        }


        // --------------------------------------------------------
        // 12. CREATE REVIEW
        // --------------------------------------------------------

        Review review =
                new Review();

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
        // 13. SAVE REVIEW
        //
        // Database unique constraint protects against
        // concurrent duplicate-review creation.
        // --------------------------------------------------------

        try {

            Review saved =
                    reviewRepository.saveAndFlush(
                            review
                    );

            return toResponse(
                    saved
            );

        } catch (
                DataIntegrityViolationException exception
        ) {

            if (isBookingConstraintViolation(
                    exception
            )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Review already exists for this booking"
                );
            }

            throw exception;
        }
    }


    // ============================================================
    // GET REVIEW BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public ReviewResponse getById(
            Long id
    ) {

        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );


        if (!Boolean.TRUE.equals(
                review.getActive()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }


        return toResponse(
                review
        );
    }


    // ============================================================
    // GET ALL ACTIVE REVIEWS
    // ============================================================

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByProvider(
            Long providerId
    ) {

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

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByCustomer(
            Long customerId
    ) {

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

    @Transactional(readOnly = true)
    public ReviewResponse getByBooking(
            Long bookingId
    ) {

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


        return toResponse(
                review
        );
    }


    // ============================================================
    // UPDATE REVIEW
    // ============================================================

    public ReviewResponse update(
            Long customerId,
            Long id,
            ReviewRequest request
    ) {

        if (customerId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer authentication is required"
            );
        }


        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );


        // --------------------------------------------------------
        // 1. ONLY OWNER CAN UPDATE
        // --------------------------------------------------------

        if (review.getCustomerId() == null
                || !review.getCustomerId().equals(
                customerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update another customer's review"
            );
        }


        // --------------------------------------------------------
        // 2. REVIEW MUST BE ACTIVE
        // --------------------------------------------------------

        if (!Boolean.TRUE.equals(
                review.getActive()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }


        // --------------------------------------------------------
        // 3. BOOKING AND PROVIDER CANNOT CHANGE
        // --------------------------------------------------------

        if (!review.getProviderId().equals(
                request.providerId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider cannot be changed"
            );
        }


        if (!review.getBookingId().equals(
                request.bookingId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking cannot be changed"
            );
        }


        review.setRating(
                request.rating()
        );

        review.setComment(
                request.comment()
        );


        Review updated =
                reviewRepository.save(
                        review
                );


        return toResponse(
                updated
        );
    }


    // ============================================================
    // DELETE / DEACTIVATE REVIEW
    // ============================================================

    public void deactivate(
            Long customerId,
            Long id
    ) {

        if (customerId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer authentication is required"
            );
        }


        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Review not found"
                                )
                        );


        // --------------------------------------------------------
        // ONLY OWNER CAN DEACTIVATE
        // --------------------------------------------------------

        if (review.getCustomerId() == null
                || !review.getCustomerId().equals(
                customerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete another customer's review"
            );
        }


        // --------------------------------------------------------
        // REVIEW MUST BE ACTIVE
        // --------------------------------------------------------

        if (!Boolean.TRUE.equals(
                review.getActive()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }


        review.setActive(false);

        reviewRepository.save(
                review
        );
    }


    // ============================================================
    // CHECK DATABASE UNIQUE CONSTRAINT
    // ============================================================

    private boolean isBookingConstraintViolation(
            DataIntegrityViolationException exception
    ) {

        Throwable cause =
                exception;

        while (cause != null) {

            String message =
                    cause.getMessage();

            if (message != null
                    && message.contains(
                    REVIEW_BOOKING_CONSTRAINT
            )) {

                return true;
            }

            cause =
                    cause.getCause();
        }

        return false;
    }


    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private ReviewResponse toResponse(
            Review review
    ) {

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