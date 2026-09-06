package com.serviceconnect.booking.service;

import com.serviceconnect.booking.client.CatalogServiceClient;
import com.serviceconnect.booking.client.ProviderServiceClient;
import com.serviceconnect.booking.client.UserServiceClient;
import com.serviceconnect.booking.dto.request.CreateServiceRequest;
import com.serviceconnect.booking.dto.response.ServiceRequestResponse;
import com.serviceconnect.booking.entity.ServiceRequest;
import com.serviceconnect.booking.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    // ============================================================
    // BOOKING STATUS CONSTANTS
    // ============================================================

    private static final String STATUS_PENDING =
            "PENDING";

    private static final String STATUS_ACCEPTED =
            "ACCEPTED";

    private static final String STATUS_REJECTED =
            "REJECTED";

    private static final String STATUS_CANCELLED =
            "CANCELLED";

    private static final String STATUS_COMPLETED =
            "COMPLETED";


    // ============================================================
    // DATABASE CONSTRAINT NAME
    // ============================================================

    private static final String BOOKING_OVERLAP_CONSTRAINT =
            "ex_service_requests_no_provider_overlap";


    // ============================================================
    // DEPENDENCIES
    // ============================================================

    private final ServiceRequestRepository serviceRequestRepository;

    private final UserServiceClient userServiceClient;

    private final ProviderServiceClient providerServiceClient;

    private final CatalogServiceClient catalogServiceClient;


    // ============================================================
    // CUSTOMER - CREATE SERVICE REQUEST
    // ============================================================

    public ServiceRequestResponse createServiceRequest(
            Long customerId,
            String authorizationHeader,
            String idempotencyKey,
            CreateServiceRequest request
    ) {

        // --------------------------------------------------------
        // VALIDATE PROVIDER
        // --------------------------------------------------------

        boolean approvedProvider =
                providerServiceClient.validateApprovedProvider(
                        request.providerId(),
                        authorizationHeader
                );

        if (!approvedProvider) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider is not approved"
            );
        }


        // --------------------------------------------------------
        // GET CATALOG ITEM
        // --------------------------------------------------------

        CatalogServiceClient.CatalogItemResponse catalogItem =
                catalogServiceClient.getCatalogItem(
                        request.catalogItemId(),
                        authorizationHeader
                );

        if (catalogItem == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Catalog item not found"
            );
        }


        // --------------------------------------------------------
        // CATALOG ITEM MUST BE ACTIVE
        // --------------------------------------------------------

        if (!Boolean.TRUE.equals(
                catalogItem.active()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item is not active"
            );
        }


        // --------------------------------------------------------
        // CATALOG ITEM MUST BELONG TO PROVIDER
        // --------------------------------------------------------

        if (!request.providerId().equals(
                catalogItem.providerId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item does not belong to selected provider"
            );
        }


        // --------------------------------------------------------
        // VALIDATE SERVICE DURATION
        // --------------------------------------------------------

        if (catalogItem.durationMinutes() == null
                || catalogItem.durationMinutes() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item has an invalid service duration"
            );
        }


        // --------------------------------------------------------
        // VALIDATE REQUESTED START
        // --------------------------------------------------------

        if (request.requestedStartAt() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested start time is required"
            );
        }


        if (request.requestedStartAt().isBefore(
                OffsetDateTime.now()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested start time must be in the future"
            );
        }


        // --------------------------------------------------------
        // CALCULATE REQUESTED END
        // --------------------------------------------------------

        OffsetDateTime requestedEndAt =
                request.requestedStartAt()
                        .plusMinutes(
                                catalogItem.durationMinutes()
                        );


        // --------------------------------------------------------
        // VALIDATE PROVIDER AVAILABILITY
        // --------------------------------------------------------

        boolean providerAvailable =
                providerServiceClient.checkAvailability(
                        request.providerId(),
                        request.requestedStartAt(),
                        requestedEndAt,
                        authorizationHeader
                );

        if (!providerAvailable) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Provider is not available for the requested time"
            );
        }


        // --------------------------------------------------------
        // CREATE SERVICE REQUEST
        // --------------------------------------------------------

        OffsetDateTime now =
                OffsetDateTime.now();

        ServiceRequest serviceRequest =
                new ServiceRequest();


        serviceRequest.setCustomerId(
                customerId
        );


        serviceRequest.setProviderId(
                request.providerId()
        );


        serviceRequest.setCatalogItemId(
                catalogItem.id()
        );


        // --------------------------------------------------------
        // SERVICE TYPE SNAPSHOT
        // --------------------------------------------------------

        if (catalogItem.name() == null
                || catalogItem.name().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item name is invalid"
            );
        }


        serviceRequest.setServiceType(
                catalogItem.name().trim()
        );


        // --------------------------------------------------------
        // PRICE SNAPSHOT
        //
        // Capture the catalog price at booking creation time.
        // Future catalog price changes must not affect this booking.
        // --------------------------------------------------------

        if (catalogItem.price() == null
                || catalogItem.price().signum() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item has an invalid price"
            );
        }


        serviceRequest.setPriceSnapshot(
                catalogItem.price()
        );


        // --------------------------------------------------------
        // REQUEST DETAILS
        // --------------------------------------------------------

        serviceRequest.setDescription(
                request.description()
        );


        serviceRequest.setServiceAddress(
                request.serviceAddress().trim()
        );


        serviceRequest.setLatitude(
                request.latitude()
        );


        serviceRequest.setLongitude(
                request.longitude()
        );


        // --------------------------------------------------------
        // BOOKING INTERVAL
        // --------------------------------------------------------

        serviceRequest.setRequestedStartAt(
                request.requestedStartAt()
        );


        serviceRequest.setRequestedEndAt(
                requestedEndAt
        );


        // --------------------------------------------------------
        // INITIAL STATE
        // --------------------------------------------------------

        serviceRequest.setStatus(
                STATUS_PENDING
        );


        serviceRequest.setCreatedAt(
                now
        );

        serviceRequest.setUpdatedAt(
                now
        );


        // --------------------------------------------------------
        // APPLICATION-LEVEL OVERLAP CHECK
        //
        // This provides a fast predictable conflict response.
        // The database exclusion constraint remains the final
        // concurrency protection.
        // --------------------------------------------------------

        boolean overlappingBooking =
                serviceRequestRepository.existsOverlappingActiveBooking(
                        request.providerId(),
                        request.requestedStartAt(),
                        requestedEndAt
                );

        if (overlappingBooking) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Provider time slot is already booked"
            );
        }


        // --------------------------------------------------------
        // SAVE
        //
        // saveAndFlush is intentional so the PostgreSQL exclusion
        // constraint is evaluated immediately.
        // --------------------------------------------------------

        ServiceRequest savedRequest;

        try {

            savedRequest =
                    serviceRequestRepository.saveAndFlush(
                            serviceRequest
                    );

        } catch (DataIntegrityViolationException exception) {

            if (isBookingOverlapViolation(exception)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Provider time slot is already booked"
                );
            }

            throw exception;
        }


        // --------------------------------------------------------
        // RESPONSE
        // --------------------------------------------------------

        return toResponse(
                savedRequest,
                false
        );
    }


    // ============================================================
    // GET REQUEST BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequestById(
            Long requestId,
            Long userId,
            String role
    ) {

        ServiceRequest request =
                findRequest(requestId);


        // --------------------------------------------------------
        // ADMIN
        // --------------------------------------------------------

        if ("ROLE_ADMIN".equals(role)) {

            return toResponse(
                    request,
                    false
            );
        }


        // --------------------------------------------------------
        // CUSTOMER
        // --------------------------------------------------------

        if ("ROLE_CUSTOMER".equals(role)) {

            if (!request.getCustomerId().equals(
                    userId
            )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view your own requests"
                );
            }

            return toResponse(
                    request,
                    false
            );
        }


        // --------------------------------------------------------
        // PROVIDER
        // --------------------------------------------------------

        if ("ROLE_PROVIDER".equals(role)) {

            if (!request.getProviderId().equals(
                    userId
            )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view requests assigned to you"
                );
            }

            return toResponse(
                    request,
                    canProviderSeePhone(request)
            );
        }


        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Invalid user role"
        );
    }


    // ============================================================
    // CUSTOMER - GET MY REQUESTS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getCustomerRequests(
            Long customerId
    ) {

        return serviceRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId
                )
                .stream()
                .map(request ->
                        toResponse(
                                request,
                                false
                        )
                )
                .toList();
    }


    // ============================================================
    // PROVIDER - GET INCOMING REQUESTS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getProviderRequests(
            Long providerId
    ) {

        return serviceRequestRepository
                .findByProviderIdOrderByCreatedAtDesc(
                        providerId
                )
                .stream()
                .map(request ->
                        toResponse(
                                request,
                                canProviderSeePhone(request)
                        )
                )
                .toList();
    }


    // ============================================================
    // PROVIDER - GET REQUESTS BY STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getProviderRequestsByStatus(
            Long providerId,
            String status
    ) {

        String normalizedStatus =
                normalizeStatus(status);

        return serviceRequestRepository
                .findByProviderIdAndStatusOrderByCreatedAtDesc(
                        providerId,
                        normalizedStatus
                )
                .stream()
                .map(request ->
                        toResponse(
                                request,
                                canProviderSeePhone(request)
                        )
                )
                .toList();
    }


    // ============================================================
    // CUSTOMER - CANCEL REQUEST
    //
    // PENDING  -> CANCELLED
    // ACCEPTED -> CANCELLED only before start time
    // ============================================================

    public ServiceRequestResponse cancelRequest(
            Long requestId,
            Long customerId
    ) {

        ServiceRequest request =
                findRequest(requestId);


        // --------------------------------------------------------
        // CUSTOMER OWNERSHIP
        // --------------------------------------------------------

        validateCustomer(
                request,
                customerId
        );


        // --------------------------------------------------------
        // VALIDATE CANCELLATION RULES
        // --------------------------------------------------------

        validateCancellation(
                request
        );


        // --------------------------------------------------------
        // UPDATE STATUS
        // --------------------------------------------------------

        updateStatus(
                request,
                STATUS_CANCELLED
        );


        ServiceRequest updatedRequest =
                serviceRequestRepository.save(
                        request
                );


        return toResponse(
                updatedRequest,
                false
        );
    }


    // ============================================================
    // PROVIDER - ACCEPT REQUEST
    //
    // PENDING -> ACCEPTED
    // ============================================================

    public ServiceRequestResponse acceptRequest(
            Long requestId,
            Long providerId
    ) {

        ServiceRequest request =
                findRequest(requestId);


        validateProvider(
                request,
                providerId
        );


        transition(
                request,
                STATUS_ACCEPTED
        );


        ServiceRequest updatedRequest =
                serviceRequestRepository.save(
                        request
                );


        return toResponse(
                updatedRequest,
                true
        );
    }


    // ============================================================
    // PROVIDER - REJECT REQUEST
    //
    // PENDING -> REJECTED
    // ============================================================

    public ServiceRequestResponse rejectRequest(
            Long requestId,
            Long providerId
    ) {

        ServiceRequest request =
                findRequest(requestId);


        validateProvider(
                request,
                providerId
        );


        transition(
                request,
                STATUS_REJECTED
        );


        ServiceRequest updatedRequest =
                serviceRequestRepository.save(
                        request
                );


        return toResponse(
                updatedRequest,
                false
        );
    }


    // ============================================================
    // PROVIDER - COMPLETE REQUEST
    //
    // ACCEPTED -> COMPLETED
    // ============================================================

    public ServiceRequestResponse completeRequest(
            Long requestId,
            Long providerId
    ) {

        ServiceRequest request =
                findRequest(requestId);


        validateProvider(
                request,
                providerId
        );


        transition(
                request,
                STATUS_COMPLETED
        );


        ServiceRequest updatedRequest =
                serviceRequestRepository.save(
                        request
                );


        return toResponse(
                updatedRequest,
                true
        );
    }


    // ============================================================
    // VALIDATE CANCELLATION RULES
    // ============================================================

    private void validateCancellation(
            ServiceRequest request
    ) {

        String currentStatus =
                normalizeStatus(
                        request.getStatus()
                );


        // --------------------------------------------------------
        // PENDING
        //
        // A customer can cancel a pending request at any time.
        // --------------------------------------------------------

        if (STATUS_PENDING.equals(currentStatus)) {
            return;
        }


        // --------------------------------------------------------
        // ACCEPTED
        //
        // A customer can cancel an accepted request only before
        // its scheduled service start time.
        // --------------------------------------------------------

        if (STATUS_ACCEPTED.equals(currentStatus)) {

            if (request.getRequestedStartAt() == null) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Accepted request has no scheduled start time"
                );
            }


            if (!request.getRequestedStartAt().isAfter(
                    OffsetDateTime.now()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Accepted request cannot be cancelled after the service start time"
                );
            }

            return;
        }


        // --------------------------------------------------------
        // REJECTED
        // --------------------------------------------------------

        if (STATUS_REJECTED.equals(currentStatus)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Rejected request cannot be cancelled"
            );
        }


        // --------------------------------------------------------
        // ALREADY CANCELLED
        // --------------------------------------------------------

        if (STATUS_CANCELLED.equals(currentStatus)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Request is already cancelled"
            );
        }


        // --------------------------------------------------------
        // COMPLETED
        // --------------------------------------------------------

        if (STATUS_COMPLETED.equals(currentStatus)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completed request cannot be cancelled"
            );
        }


        // --------------------------------------------------------
        // UNKNOWN STATE
        // --------------------------------------------------------

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid service request status"
        );
    }


    // ============================================================
    // CENTRALIZED STATE TRANSITION
    // ============================================================

    private void transition(
            ServiceRequest request,
            String targetStatus
    ) {

        String currentStatus =
                normalizeStatus(
                        request.getStatus()
                );

        String normalizedTargetStatus =
                normalizeStatus(
                        targetStatus
                );


        if (currentStatus.equals(
                normalizedTargetStatus
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Booking is already in status "
                            + currentStatus
            );
        }


        boolean validTransition =
                switch (currentStatus) {

                    case STATUS_PENDING ->
                            normalizedTargetStatus.equals(
                                    STATUS_ACCEPTED
                            )
                                    || normalizedTargetStatus.equals(
                                    STATUS_REJECTED
                            );

                    case STATUS_ACCEPTED ->
                            normalizedTargetStatus.equals(
                                    STATUS_COMPLETED
                            );

                    case STATUS_REJECTED,
                         STATUS_CANCELLED,
                         STATUS_COMPLETED ->
                            false;

                    default ->
                            false;
                };


        if (!validTransition) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid booking status transition: "
                            + currentStatus
                            + " -> "
                            + normalizedTargetStatus
            );
        }


        updateStatus(
                request,
                normalizedTargetStatus
        );
    }


    // ============================================================
    // UPDATE STATUS
    // ============================================================

    private void updateStatus(
            ServiceRequest request,
            String status
    ) {

        request.setStatus(
                status
        );

        request.setUpdatedAt(
                OffsetDateTime.now()
        );
    }


    // ============================================================
    // FIND REQUEST
    // ============================================================

    private ServiceRequest findRequest(
            Long requestId
    ) {

        return serviceRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Service request not found"
                        )
                );
    }


    // ============================================================
    // VALIDATE CUSTOMER OWNERSHIP
    // ============================================================

    private void validateCustomer(
            ServiceRequest request,
            Long customerId
    ) {

        if (customerId == null
                || !request.getCustomerId().equals(
                customerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify this request"
            );
        }
    }


    // ============================================================
    // VALIDATE PROVIDER OWNERSHIP
    // ============================================================

    private void validateProvider(
            ServiceRequest request,
            Long providerId
    ) {

        if (providerId == null
                || !request.getProviderId().equals(
                providerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This request does not belong to this provider"
            );
        }
    }


    // ============================================================
    // NORMALIZE AND VALIDATE STATUS
    // ============================================================

    private String normalizeStatus(
            String status
    ) {

        if (status == null
                || status.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status is required"
            );
        }


        String normalized =
                status.trim()
                        .toUpperCase();


        if (!STATUS_PENDING.equals(normalized)
                && !STATUS_ACCEPTED.equals(normalized)
                && !STATUS_REJECTED.equals(normalized)
                && !STATUS_CANCELLED.equals(normalized)
                && !STATUS_COMPLETED.equals(normalized)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid service request status"
            );
        }


        return normalized;
    }


    // ============================================================
    // PROVIDER PHONE VISIBILITY
    // ============================================================

    private boolean canProviderSeePhone(
            ServiceRequest request
    ) {

        String status =
                request.getStatus();

        return STATUS_ACCEPTED.equals(status)
                || STATUS_COMPLETED.equals(status);
    }


    // ============================================================
    // CHECK DATABASE OVERLAP CONSTRAINT VIOLATION
    // ============================================================

    private boolean isBookingOverlapViolation(
            DataIntegrityViolationException exception
    ) {

        Throwable cause =
                exception;

        while (cause != null) {

            String message =
                    cause.getMessage();

            if (message != null
                    && message.contains(
                    BOOKING_OVERLAP_CONSTRAINT
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

    private ServiceRequestResponse toResponse(
            ServiceRequest request,
            boolean includePhone
    ) {

        String customerPhone = null;


        if (includePhone) {

            customerPhone =
                    userServiceClient.getCustomerPhone(
                            request.getCustomerId()
                    );
        }


        return new ServiceRequestResponse(
                request.getId(),
                request.getCustomerId(),
                request.getProviderId(),
                request.getCatalogItemId(),
                request.getServiceType(),
                request.getDescription(),
                request.getServiceAddress(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRequestedStartAt(),
                request.getRequestedEndAt(),
                request.getPriceSnapshot(),
                request.getStatus(),
                customerPhone,
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}