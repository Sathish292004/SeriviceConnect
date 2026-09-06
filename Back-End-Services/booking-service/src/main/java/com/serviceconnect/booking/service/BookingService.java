package com.serviceconnect.booking.service;

import com.serviceconnect.booking.client.CatalogServiceClient;
import com.serviceconnect.booking.client.ProviderServiceClient;
import com.serviceconnect.booking.client.UserServiceClient;
import com.serviceconnect.booking.dto.request.CreateServiceRequest;
import com.serviceconnect.booking.dto.response.ServiceRequestResponse;
import com.serviceconnect.booking.entity.ServiceRequest;
import com.serviceconnect.booking.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

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
        // CREATE REQUEST
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
        // INITIAL STATUS
        // --------------------------------------------------------

        serviceRequest.setStatus(
                "PENDING"
        );


        serviceRequest.setCreatedAt(
                now
        );


        serviceRequest.setUpdatedAt(
                now
        );


        // --------------------------------------------------------
        // SAVE
        // --------------------------------------------------------

        ServiceRequest savedRequest =
                serviceRequestRepository.save(
                        serviceRequest
                );


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


            boolean canSeePhone =
                    canProviderSeePhone(request);


            return toResponse(
                    request,
                    canSeePhone
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
    // ============================================================

    public ServiceRequestResponse cancelRequest(
            Long requestId,
            Long customerId
    ) {

        ServiceRequest request =
                findRequest(requestId);


        validateCustomer(
                request,
                customerId
        );


        validateTransition(
                request,
                "CANCELLED"
        );


        updateStatus(
                request,
                "CANCELLED"
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


        validateTransition(
                request,
                "ACCEPTED"
        );


        updateStatus(
                request,
                "ACCEPTED"
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


        validateTransition(
                request,
                "REJECTED"
        );


        updateStatus(
                request,
                "REJECTED"
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


        validateTransition(
                request,
                "COMPLETED"
        );


        updateStatus(
                request,
                "COMPLETED"
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
    // VALIDATE CUSTOMER
    // ============================================================

    private void validateCustomer(
            ServiceRequest request,
            Long customerId
    ) {

        if (!request.getCustomerId().equals(
                customerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify this request"
            );
        }
    }


    // ============================================================
    // VALIDATE PROVIDER
    // ============================================================

    private void validateProvider(
            ServiceRequest request,
            Long providerId
    ) {

        if (!request.getProviderId().equals(
                providerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This request does not belong to this provider"
            );
        }
    }


    // ============================================================
    // STATUS TRANSITION VALIDATION
    // ============================================================

    private void validateTransition(
            ServiceRequest request,
            String targetStatus
    ) {

        String currentStatus =
                normalizeStatus(
                        request.getStatus()
                );


        switch (targetStatus) {

            case "ACCEPTED" -> {

                if (!"PENDING".equals(
                        currentStatus
                )) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only pending requests can be accepted"
                    );
                }
            }


            case "REJECTED" -> {

                if (!"PENDING".equals(
                        currentStatus
                )) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only pending requests can be rejected"
                    );
                }
            }


            case "CANCELLED" -> {

                if (!"PENDING".equals(
                        currentStatus
                )) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only pending requests can be cancelled"
                    );
                }
            }


            case "COMPLETED" -> {

                if (!"ACCEPTED".equals(
                        currentStatus
                )) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only accepted requests can be completed"
                    );
                }
            }


            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid target status"
            );
        }
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
    // NORMALIZE STATUS
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
                status.trim().toUpperCase();


        if (!normalized.equals("PENDING")
                && !normalized.equals("ACCEPTED")
                && !normalized.equals("REJECTED")
                && !normalized.equals("CANCELLED")
                && !normalized.equals("COMPLETED")) {

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

        return "ACCEPTED".equals(
                request.getStatus()
        )
                || "COMPLETED".equals(
                request.getStatus()
        );
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

                request.getStatus(),

                customerPhone,

                request.getCreatedAt(),

                request.getUpdatedAt()
        );
    }
}