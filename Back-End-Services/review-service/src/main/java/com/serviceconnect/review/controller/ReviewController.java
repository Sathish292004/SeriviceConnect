package com.serviceconnect.review.controller;

import com.serviceconnect.review.dto.request.ReviewRequest;
import com.serviceconnect.review.dto.response.ReviewResponse;
import com.serviceconnect.review.service.ReviewService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
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
    public ResponseEntity<List<ReviewResponse>> getByProvider(

            @PathVariable
            Long providerId) {

        return ResponseEntity.ok(
                reviewService.getByProvider(
                        providerId
                )
        );
    }


    // ============================================================
    // GET CUSTOMER REVIEWS
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReviewResponse>> getByCustomer(

            @PathVariable
            Long customerId) {

        return ResponseEntity.ok(
                reviewService.getByCustomer(
                        customerId
                )
        );
    }


    // ============================================================
    // GET ALL ACTIVE REVIEWS
    // AUTHENTICATED USERS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAll() {

        return ResponseEntity.ok(
                reviewService.getAllActive()
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

}