package com.serviceconnect.review.controller;

import com.serviceconnect.review.dto.request.ReviewRequest;
import com.serviceconnect.review.dto.response.PageResponse;
import com.serviceconnect.review.dto.response.ReviewResponse;
import com.serviceconnect.review.service.ReviewService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;


    // ============================================================
    // CREATE REVIEW
    // CUSTOMER ONLY
    // ============================================================

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ReviewResponse> create(

            @AuthenticationPrincipal
            Jwt jwt,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @Valid
            @RequestBody
            ReviewRequest request) {

        Long customerId =
                Long.valueOf(
                        jwt.getSubject()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        reviewService.create(
                                customerId,
                                authorizationHeader,
                                request
                        )
                );
    }


    // ============================================================
    // GET REVIEW BY ID
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getById(

            @PathVariable
            Long id) {

        return ResponseEntity.ok(
                reviewService.getById(id)
        );
    }


    // ============================================================
    // GET REVIEW BY BOOKING
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ReviewResponse> getByBooking(

            @PathVariable
            Long bookingId) {

        return ResponseEntity.ok(
                reviewService.getByBooking(bookingId)
        );
    }


    // ============================================================
    // GET PROVIDER REVIEWS
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getByProvider(

            @PathVariable
            Long providerId,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int size) {

        Pageable pageable =
                buildPageable(
                        page,
                        size
                );

        return ResponseEntity.ok(
                reviewService.getByProvider(
                        providerId,
                        pageable
                )
        );
    }


    // ============================================================
    // GET CUSTOMER REVIEWS
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<PageResponse<ReviewResponse>> getByCustomer(

            @PathVariable
            Long customerId,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int size) {

        Pageable pageable =
                buildPageable(
                        page,
                        size
                );

        return ResponseEntity.ok(
                reviewService.getByCustomer(
                        customerId,
                        pageable
                )
        );
    }


    // ============================================================
    // GET ALL ACTIVE REVIEWS
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping
    public ResponseEntity<PageResponse<ReviewResponse>> getAll(

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int size) {

        Pageable pageable =
                buildPageable(
                        page,
                        size
                );

        return ResponseEntity.ok(
                reviewService.getAllActive(
                        pageable
                )
        );
    }


    // ============================================================
    // UPDATE REVIEW
    // CUSTOMER ONLY
    // ============================================================

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            ReviewRequest request) {

        Long customerId =
                Long.valueOf(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                reviewService.update(
                        customerId,
                        id,
                        request
                )
        );
    }


    // ============================================================
    // DEACTIVATE REVIEW
    // CUSTOMER ONLY
    // ============================================================

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(

            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            Long id) {

        Long customerId =
                Long.valueOf(
                        jwt.getSubject()
                );

        reviewService.deactivate(
                customerId,
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // BUILD PAGINATION
    // ============================================================

    private Pageable buildPageable(
            int page,
            int size
    ) {

        return PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );
    }
}