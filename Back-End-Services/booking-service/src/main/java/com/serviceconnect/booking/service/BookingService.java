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
            CreateServiceRequest request) {

        // ========================================================
        // VALIDATE PROVIDER
        // ========================================================

        providerServiceClient.validateApprovedProvider(
                request.providerId()
        );


        // ========================================================
        // VALIDATE CATALOG ITEM
        // ========================================================

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


        // ========================================================
        // CATALOG ITEM MUST BE ACTIVE
        // ========================================================

        if (!Boolean.TRUE.equals(catalogItem.active())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item is not active"
            );
        }


        // ========================================================
        // CATALOG ITEM MUST BELONG TO PROVIDER
        // ========================================================

        if (!request.providerId()
                .equals(catalogItem.providerId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item does not belong to selected provider"
            );
        }


        // ========================================================
        // CREATE SERVICE REQUEST
        // ========================================================

        OffsetDateTime now =
                OffsetDateTime.now();

        ServiceRequest serviceRequest =
                new ServiceRequest();


        // Customer ID comes from JWT.

        serviceRequest.setCustomerId(
                customerId
        );


        serviceRequest.setProviderId(
                request.providerId()
        );


        // Store Catalog reference.

        serviceRequest.setCatalogItemId(
                catalogItem.id()
        );


        // Store catalog name as historical snapshot.

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


        // Every new request starts as PENDING.

        serviceRequest.setStatus(
                "PENDING"
        );


        serviceRequest.setCreatedAt(
                now
        );


        serviceRequest.setUpdatedAt(
                now
        );


        // ========================================================
        // SAVE
        // ========================================================

        ServiceRequest savedRequest =
                serviceRequestRepository.save(
                        serviceRequest
                );


        // Customer must NOT receive phone number.

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
            String role) {

        ServiceRequest request =
                findRequest(requestId);


        // ========================================================
        // ADMIN
        // ========================================================

        if ("ROLE_ADMIN".equals(role)) {

            // Admin can view any request,
            // but must NOT receive customer phone.

            return toResponse(
                    request,
                    false
            );
        }


        // ========================================================
        // CUSTOMER
        // ========================================================

        if ("ROLE_CUSTOMER".equals(role)) {

            /*
             * Customer can only view their own request.
             */

            if (!request.getCustomerId().equals(userId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view your own requests"
                );
            }


            // Customer must never receive phone.

            return toResponse(
                    request,
                    false
            );
        }


        // ========================================================
        // PROVIDER
        // ========================================================

        if ("ROLE_PROVIDER".equals(role)) {

            /*
             * Provider can only view requests assigned
             * to that provider.
             */

            if (!request.getProviderId().equals(userId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only view requests assigned to you"
                );
            }


            /*
             * Provider can see phone only after the request
             * has been accepted or completed.
             */

            boolean canSeePhone =
                    "ACCEPTED".equals(request.getStatus())
                            || "COMPLETED".equals(request.getStatus());


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
            Long customerId) {

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
            Long providerId) {

        return serviceRequestRepository
                .findByProviderIdOrderByCreatedAtDesc(
                        providerId
                )
                .stream()
                .map(request -> {

                    boolean canSeePhone =
                            canProviderSeePhone(request);

                    return toResponse(
                            request,
                            canSeePhone
                    );
                })
                .toList();
    }


    // ============================================================
    // PROVIDER - GET REQUESTS BY STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getProviderRequestsByStatus(
            Long providerId,
            String status) {

        String normalizedStatus =
                normalizeStatus(status);


        return serviceRequestRepository
                .findByProviderIdAndStatusOrderByCreatedAtDesc(
                        providerId,
                        normalizedStatus
                )
                .stream()
                .map(request -> {

                    boolean canSeePhone =
                            canProviderSeePhone(request);

                    return toResponse(
                            request,
                            canSeePhone
                    );
                })
                .toList();
    }


    // ============================================================
    // CUSTOMER - CANCEL REQUEST
    // ============================================================

    public ServiceRequestResponse cancelRequest(
            Long requestId,
            Long customerId) {

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
            Long providerId) {

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


        // Provider can now see customer phone.

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
            Long providerId) {

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


        // Rejected request -> no phone.

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
            Long providerId) {

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


        // Provider can still see customer phone.

        return toResponse(
                updatedRequest,
                true
        );
    }


    // ============================================================
    // FIND REQUEST
    // ============================================================

    private ServiceRequest findRequest(
            Long requestId) {

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
            Long customerId) {

        if (!request.getCustomerId().equals(customerId)) {

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
            Long providerId) {

        if (!request.getProviderId().equals(providerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This request does not belong to this provider"
            );
        }
    }


    // ============================================================
    // VALIDATE STATUS TRANSITION
    // ============================================================

    private void validateTransition(
            ServiceRequest request,
            String targetStatus) {

        String currentStatus =
                normalizeStatus(
                        request.getStatus()
                );


        switch (targetStatus) {

            case "ACCEPTED" -> {

                if (!"PENDING".equals(currentStatus)) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only pending requests can be accepted"
                    );
                }
            }


            case "REJECTED" -> {

                if (!"PENDING".equals(currentStatus)) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only pending requests can be rejected"
                    );
                }
            }


            case "CANCELLED" -> {

                if (!"PENDING".equals(currentStatus)) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Only pending requests can be cancelled"
                    );
                }
            }


            case "COMPLETED" -> {

                if (!"ACCEPTED".equals(currentStatus)) {

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
            String status) {

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
            String status) {

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
            ServiceRequest request) {

        return "ACCEPTED".equals(request.getStatus())
                || "COMPLETED".equals(request.getStatus());
    }


    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private ServiceRequestResponse toResponse(
            ServiceRequest request,
            boolean includePhone) {

        String customerPhone = null;


        /*
         * Phone is fetched from user-service ONLY
         * when the caller has already been authorized
         * to see it.
         */

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

                request.getStatus(),

                customerPhone,

                request.getCreatedAt(),

                request.getUpdatedAt()
        );
    }
}