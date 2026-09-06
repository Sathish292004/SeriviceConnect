package com.serviceconnect.review.service;

import com.serviceconnect.review.client.BookingServiceClient;
import com.serviceconnect.review.client.ProviderServiceClient;
import com.serviceconnect.review.dto.request.ReviewRequest;
import com.serviceconnect.review.dto.response.ReviewResponse;
import com.serviceconnect.review.entity.Review;
import com.serviceconnect.review.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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

            log.warn(
                    "Review creation rejected: customer authentication missing"
            );

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

            log.warn(
                    "Review creation rejected: review already exists, " +
                            "bookingId={}, customerId={}",
                    request.bookingId(),
                    customerId
            );

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

            log.error(
                    "Review creation failed: unable to verify booking, " +
                            "bookingId={}, customerId={}",
                    request.bookingId(),
                    customerId,
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify booking with Booking Service"
            );
        }


        // --------------------------------------------------------
        // 4. VERIFY BOOKING EXISTS
        // --------------------------------------------------------

        if (booking == null) {

            log.warn(
                    "Review creation rejected: booking not found, " +
                            "bookingId={}, customerId={}",
                    request.bookingId(),
                    customerId
            );

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

            log.warn(
                    "Review creation denied: booking ownership failed, " +
                            "bookingId={}, customerId={}",
                    request.bookingId(),
                    customerId
            );

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

            log.warn(
                    "Review creation rejected: provider does not match booking, " +
                            "bookingId={}, providerId={}",
                    request.bookingId(),
                    request.providerId()
            );

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

            log.warn(
                    "Review creation rejected: booking is not completed, " +
                            "bookingId={}, status={}",
                    request.bookingId(),
                    booking.status()
            );

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

            log.error(
                    "Review creation failed: unable to verify provider, " +
                            "providerId={}, bookingId={}",
                    request.providerId(),
                    request.bookingId(),
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify provider with Provider Service"
            );
        }


        // --------------------------------------------------------
        // 9. VERIFY PROVIDER EXISTS
        // --------------------------------------------------------

        if (provider == null) {

            log.warn(
                    "Review creation rejected: provider not found, " +
                            "providerId={}, bookingId={}",
                    request.providerId(),
                    request.bookingId()
            );

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

            log.error(
                    "Review provider verification failed: invalid provider identity, " +
                            "providerId={}, bookingId={}",
                    request.providerId(),
                    request.bookingId()
            );

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

            log.warn(
                    "Review creation rejected: provider not approved, " +
                            "providerId={}, status={}",
                    request.providerId(),
                    provider.status()
            );

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

            log.info(
                    "Review created: reviewId={}, bookingId={}, " +
                            "customerId={}, providerId={}",
                    saved.getId(),
                    saved.getBookingId(),
                    saved.getCustomerId(),
                    saved.getProviderId()
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

                log.warn(
                        "Review creation rejected by database unique constraint: " +
                                "bookingId={}, customerId={}, providerId={}",
                        request.bookingId(),
                        customerId,
                        request.providerId()
                );

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Review already exists for this booking"
                );
            }

            log.error(
                    "Review creation failed due to database integrity violation: " +
                            "bookingId={}, customerId={}, providerId={}",
                    request.bookingId(),
                    customerId,
                    request.providerId(),
                    exception
            );

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
                        .orElseThrow(() -> {

                            log.warn(
                                    "Review not found: reviewId={}",
                                    id
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Review not found"
                            );
                        });


        if (!Boolean.TRUE.equals(
                review.getActive()
        )) {

            log.warn(
                    "Review unavailable: reviewId={}, reason=inactive",
                    id
            );

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

        List<ReviewResponse> reviews =
                reviewRepository
                        .findByActiveTrue()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        log.debug(
                "Active reviews retrieved: count={}",
                reviews.size()
        );

        return reviews;
    }


    // ============================================================
    // GET PROVIDER REVIEWS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByProvider(
            Long providerId
    ) {

        List<ReviewResponse> reviews =
                reviewRepository
                        .findByProviderIdAndActiveTrue(
                                providerId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        log.debug(
                "Provider reviews retrieved: providerId={}, count={}",
                providerId,
                reviews.size()
        );

        return reviews;
    }


    // ============================================================
    // GET CUSTOMER REVIEWS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByCustomer(
            Long customerId
    ) {

        List<ReviewResponse> reviews =
                reviewRepository
                        .findByCustomerIdAndActiveTrue(
                                customerId
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();

        log.debug(
                "Customer reviews retrieved: customerId={}, count={}",
                customerId,
                reviews.size()
        );

        return reviews;
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
                        .orElseThrow(() -> {

                            log.warn(
                                    "Review not found for booking: bookingId={}",
                                    bookingId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Review not found for this booking"
                            );
                        });


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

            log.warn(
                    "Review update rejected: customer authentication missing, " +
                            "reviewId={}",
                    id
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer authentication is required"
            );
        }


        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Review update failed: review not found, " +
                                            "reviewId={}, customerId={}",
                                    id,
                                    customerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Review not found"
                            );
                        });


        // --------------------------------------------------------
        // 1. ONLY OWNER CAN UPDATE
        // --------------------------------------------------------

        if (review.getCustomerId() == null
                || !review.getCustomerId().equals(
                customerId
        )) {

            log.warn(
                    "Review update denied: ownership validation failed, " +
                            "reviewId={}, customerId={}",
                    id,
                    customerId
            );

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

            log.warn(
                    "Review update rejected: review inactive, " +
                            "reviewId={}, customerId={}",
                    id,
                    customerId
            );

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

            log.warn(
                    "Review update rejected: provider change attempted, " +
                            "reviewId={}, customerId={}",
                    id,
                    customerId
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider cannot be changed"
            );
        }


        if (!review.getBookingId().equals(
                request.bookingId()
        )) {

            log.warn(
                    "Review update rejected: booking change attempted, " +
                            "reviewId={}, customerId={}",
                    id,
                    customerId
            );

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


        log.info(
                "Review updated: reviewId={}, bookingId={}, " +
                        "customerId={}, providerId={}",
                updated.getId(),
                updated.getBookingId(),
                updated.getCustomerId(),
                updated.getProviderId()
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

            log.warn(
                    "Review deactivation rejected: customer authentication missing, " +
                            "reviewId={}",
                    id
            );

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Customer authentication is required"
            );
        }


        Review review =
                reviewRepository.findById(id)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Review deactivation failed: review not found, " +
                                            "reviewId={}, customerId={}",
                                    id,
                                    customerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Review not found"
                            );
                        });


        // --------------------------------------------------------
        // ONLY OWNER CAN DEACTIVATE
        // --------------------------------------------------------

        if (review.getCustomerId() == null
                || !review.getCustomerId().equals(
                customerId
        )) {

            log.warn(
                    "Review deactivation denied: ownership validation failed, " +
                            "reviewId={}, customerId={}",
                    id,
                    customerId
            );

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

            log.warn(
                    "Review deactivation rejected: review already inactive, " +
                            "reviewId={}, customerId={}",
                    id,
                    customerId
            );

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Review not found"
            );
        }


        review.setActive(false);

        reviewRepository.save(
                review
        );


        log.info(
                "Review deactivated: reviewId={}, bookingId={}, " +
                        "customerId={}, providerId={}",
                review.getId(),
                review.getBookingId(),
                review.getCustomerId(),
                review.getProviderId()
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