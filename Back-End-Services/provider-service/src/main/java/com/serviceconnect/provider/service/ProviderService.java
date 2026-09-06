package com.serviceconnect.provider.service;

import com.serviceconnect.provider.dto.request.CreateProviderRequest;
import com.serviceconnect.provider.dto.response.PageResponse;
import com.serviceconnect.provider.dto.response.ProviderOnboardingStatusResponse;
import com.serviceconnect.provider.dto.response.ProviderPhotoResponse;
import com.serviceconnect.provider.dto.response.ProviderResponse;
import com.serviceconnect.provider.entity.Provider;
import com.serviceconnect.provider.entity.ProviderPhoto;
import com.serviceconnect.provider.repository.ProviderPhotoRepository;
import com.serviceconnect.provider.repository.ProviderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;

    private final ProviderPhotoRepository providerPhotoRepository;


    // ============================================================
    // CREATE PROVIDER
    // ============================================================

    public ProviderResponse createProvider(
            Long userId,
            CreateProviderRequest request) {

        if (providerRepository.existsByUserId(userId)) {

            log.warn(
                    "Provider creation rejected: profile already exists, userId={}",
                    userId
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Provider profile already exists"
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        Provider provider =
                new Provider();

        provider.setUserId(userId);

        provider.setBusinessName(
                request.businessName().trim()
        );

        provider.setDescription(
                request.description()
        );

        provider.setPhone(
                request.phone().trim()
        );

        provider.setEmail(
                request.email()
                        .trim()
                        .toLowerCase()
        );

        provider.setAddress(
                request.address()
        );

        provider.setCity(
                request.city()
        );

        provider.setState(
                request.state()
        );

        provider.setPostalCode(
                request.postalCode()
        );

        // New providers always start as PENDING.
        provider.setStatus(
                "PENDING"
        );

        provider.setCreatedAt(
                now
        );

        provider.setUpdatedAt(
                now
        );

        Provider savedProvider =
                providerRepository.save(
                        provider
                );

        log.info(
                "Provider profile created: providerId={}, userId={}, status={}",
                savedProvider.getId(),
                savedProvider.getUserId(),
                savedProvider.getStatus()
        );

        return toResponse(
                savedProvider
        );
    }


    // ============================================================
    // GET PROVIDER BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(
            Long providerId) {

        Provider provider =
                providerRepository
                        .findById(providerId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Provider not found: providerId={}",
                                    providerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider not found"
                            );
                        });

        return toResponse(
                provider
        );
    }


    // ============================================================
    // GET PROVIDER BY USER ID
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderResponse getProviderByUserId(
            Long userId) {

        Provider provider =
                providerRepository
                        .findByUserId(userId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Provider profile not found: userId={}",
                                    userId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider profile not found"
                            );
                        });

        return toResponse(
                provider
        );
    }


    // ============================================================
    // ADMIN - GET ALL PROVIDERS
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<ProviderResponse> getAllProviders(
            Pageable pageable) {

        Page<Provider> page =
                providerRepository
                        .findAll(pageable);

        log.debug(
                "Providers retrieved: page={}, size={}, totalElements={}",
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return toPageResponse(
                page
        );
    }


    // ============================================================
    // ADMIN - GET PROVIDERS BY STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<ProviderResponse> getProvidersByStatus(
            String status,
            Pageable pageable) {

        if (status == null
                || status.isBlank()) {

            log.warn(
                    "Provider status query rejected: status is missing"
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider status is required"
            );
        }

        String normalizedStatus =
                status.trim().toUpperCase();

        if (!normalizedStatus.equals("PENDING")
                && !normalizedStatus.equals("APPROVED")
                && !normalizedStatus.equals("REJECTED")
                && !normalizedStatus.equals("SUSPENDED")) {

            log.warn(
                    "Provider status query rejected: invalid status={}",
                    normalizedStatus
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid provider status"
            );
        }

        Page<Provider> page =
                providerRepository
                        .findByStatus(
                                normalizedStatus,
                                pageable
                        );

        log.debug(
                "Providers retrieved by status: status={}, page={}, size={}, totalElements={}",
                normalizedStatus,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return toPageResponse(
                page
        );
    }


    // ============================================================
    // UPDATE PROVIDER PROFILE
    // PROVIDER CAN UPDATE ONLY OWN PROFILE
    //
    // PROFILE RULE:
    // If an APPROVED provider changes approval-sensitive
    // information, status becomes PENDING for admin re-review.
    // ============================================================

    public ProviderResponse updateProvider(
            Long providerId,
            Long authenticatedUserId,
            CreateProviderRequest request) {

        Provider provider =
                providerRepository
                        .findById(providerId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Provider update failed: provider not found, providerId={}",
                                    providerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider not found"
                            );
                        });

        // --------------------------------------------------------
        // OWNERSHIP CHECK
        // --------------------------------------------------------

        validateOwnership(
                provider,
                authenticatedUserId
        );

        // --------------------------------------------------------
        // NORMALIZE NEW VALUES
        // --------------------------------------------------------

        String newBusinessName =
                request.businessName().trim();

        String newPhone =
                request.phone().trim();

        String newEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        String newDescription =
                request.description();

        String newAddress =
                normalizeOptionalText(
                        request.address()
                );

        String newCity =
                normalizeOptionalText(
                        request.city()
                );

        String newState =
                normalizeOptionalText(
                        request.state()
                );

        String newPostalCode =
                normalizeOptionalText(
                        request.postalCode()
                );

        // --------------------------------------------------------
        // DETECT APPROVAL-SENSITIVE CHANGES
        //
        // Description is intentionally excluded.
        // --------------------------------------------------------

        boolean approvalSensitiveChange =
                !newBusinessName.equals(
                        provider.getBusinessName()
                )
                        || !newPhone.equals(
                        provider.getPhone()
                )
                        || !newEmail.equals(
                        provider.getEmail()
                )
                        || !equalsNullable(
                        newAddress,
                        normalizeOptionalText(
                                provider.getAddress()
                        )
                )
                        || !equalsNullable(
                        newCity,
                        normalizeOptionalText(
                                provider.getCity()
                        )
                )
                        || !equalsNullable(
                        newState,
                        normalizeOptionalText(
                                provider.getState()
                        )
                )
                        || !equalsNullable(
                        newPostalCode,
                        normalizeOptionalText(
                                provider.getPostalCode()
                        )
                );

        // --------------------------------------------------------
        // UPDATE PROFILE
        // --------------------------------------------------------

        provider.setBusinessName(
                newBusinessName
        );

        provider.setDescription(
                newDescription
        );

        provider.setPhone(
                newPhone
        );

        provider.setEmail(
                newEmail
        );

        provider.setAddress(
                newAddress
        );

        provider.setCity(
                newCity
        );

        provider.setState(
                newState
        );

        provider.setPostalCode(
                newPostalCode
        );

        // --------------------------------------------------------
        // APPROVAL RE-CHECK RULE
        //
        // APPROVED + approval-sensitive change
        //                  ↓
        //               PENDING
        // --------------------------------------------------------

        String previousStatus =
                provider.getStatus();

        boolean movedBackToPending =
                "APPROVED".equalsIgnoreCase(
                        provider.getStatus()
                )
                        && approvalSensitiveChange;

        if (movedBackToPending) {

            provider.setStatus(
                    "PENDING"
            );
        }

        // --------------------------------------------------------
        // UPDATE TIMESTAMP
        // --------------------------------------------------------

        provider.setUpdatedAt(
                OffsetDateTime.now()
        );

        Provider updatedProvider =
                providerRepository.save(
                        provider
                );

        if (movedBackToPending) {

            log.info(
                    "Provider profile updated and returned for re-review: " +
                            "providerId={}, userId={}, previousStatus={}, newStatus={}",
                    updatedProvider.getId(),
                    authenticatedUserId,
                    previousStatus,
                    updatedProvider.getStatus()
            );

        } else {

            log.info(
                    "Provider profile updated: providerId={}, userId={}, status={}",
                    updatedProvider.getId(),
                    authenticatedUserId,
                    updatedProvider.getStatus()
            );
        }

        return toResponse(
                updatedProvider
        );
    }


    // ============================================================
    // DELETE PROVIDER
    // ADMIN ONLY
    // ============================================================

    public void deleteProvider(
            Long providerId) {

        Provider provider =
                providerRepository
                        .findById(providerId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Provider deletion failed: provider not found, providerId={}",
                                    providerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider not found"
                            );
                        });

        providerRepository.delete(
                provider
        );

        log.info(
                "Provider deleted: providerId={}, userId={}",
                providerId,
                provider.getUserId()
        );
    }


    // ============================================================
    // UPDATE PROVIDER STATUS
    // ADMIN ONLY
    //
    // STATE MACHINE:
    //
    // PENDING   -> APPROVED
    // PENDING   -> REJECTED
    //
    // APPROVED  -> SUSPENDED
    //
    // SUSPENDED -> APPROVED
    // REJECTED  -> PENDING
    //
    // Invalid transitions -> 409 CONFLICT
    // ============================================================

    public ProviderResponse updateProviderStatus(
            Long providerId,
            String status) {

        // --------------------------------------------------------
        // FIND PROVIDER
        // --------------------------------------------------------

        Provider provider =
                providerRepository
                        .findById(providerId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Provider status update failed: provider not found, providerId={}",
                                    providerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider not found"
                            );
                        });

        // --------------------------------------------------------
        // VALIDATE REQUESTED STATUS
        // --------------------------------------------------------

        if (status == null
                || status.isBlank()) {

            log.warn(
                    "Provider status update rejected: status is missing, providerId={}",
                    providerId
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider status is required"
            );
        }

        // --------------------------------------------------------
        // NORMALIZE CURRENT + NEW STATUS
        // --------------------------------------------------------

        String currentStatus =
                provider.getStatus()
                        .trim()
                        .toUpperCase();

        String newStatus =
                status.trim()
                        .toUpperCase();

        // --------------------------------------------------------
        // VALIDATE STATUS VALUE
        // --------------------------------------------------------

        if (!newStatus.equals("PENDING")
                && !newStatus.equals("APPROVED")
                && !newStatus.equals("REJECTED")
                && !newStatus.equals("SUSPENDED")) {

            log.warn(
                    "Provider status update rejected: invalid target status, " +
                            "providerId={}, requestedStatus={}",
                    providerId,
                    newStatus
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be PENDING, APPROVED, REJECTED or SUSPENDED"
            );
        }

        // --------------------------------------------------------
        // SAME STATUS IS NOT A VALID TRANSITION
        // --------------------------------------------------------

        if (currentStatus.equals(newStatus)) {

            log.warn(
                    "Provider status update rejected: already in status, " +
                            "providerId={}, status={}",
                    providerId,
                    currentStatus
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provider is already in status "
                            + currentStatus
            );
        }

        // --------------------------------------------------------
        // VALIDATE STATE TRANSITION
        // --------------------------------------------------------

        boolean validTransition =
                switch (currentStatus) {

                    case "PENDING" ->
                            newStatus.equals("APPROVED")
                                    || newStatus.equals("REJECTED");

                    case "APPROVED" ->
                            newStatus.equals("SUSPENDED");

                    case "SUSPENDED" ->
                            newStatus.equals("APPROVED");

                    case "REJECTED" ->
                            newStatus.equals("PENDING");

                    default ->
                            false;
                };

        // --------------------------------------------------------
        // REJECT INVALID TRANSITION
        // --------------------------------------------------------

        if (!validTransition) {

            log.warn(
                    "Provider status transition rejected: providerId={}, " +
                            "currentStatus={}, targetStatus={}",
                    providerId,
                    currentStatus,
                    newStatus
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid provider status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }

        // --------------------------------------------------------
        // UPDATE STATUS
        // --------------------------------------------------------

        provider.setStatus(
                newStatus
        );

        provider.setUpdatedAt(
                OffsetDateTime.now()
        );

        Provider updatedProvider =
                providerRepository.save(
                        provider
                );

        log.info(
                "Provider status changed: providerId={}, previousStatus={}, newStatus={}",
                updatedProvider.getId(),
                currentStatus,
                updatedProvider.getStatus()
        );

        return toResponse(
                updatedProvider
        );
    }


    // ============================================================
    // CUSTOMER - GET APPROVED PROVIDERS
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<ProviderResponse> getApprovedProviders(
            Pageable pageable) {

        Page<Provider> page =
                providerRepository
                        .findByStatus(
                                "APPROVED",
                                pageable
                        );

        log.debug(
                "Approved providers retrieved: page={}, size={}, totalElements={}",
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );

        return toPageResponse(
                page
        );
    }


    // ============================================================
    // CUSTOMER - GET ONE APPROVED PROVIDER
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderResponse getApprovedProvider(
            Long providerId) {

        Provider provider =
                providerRepository
                        .findById(providerId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Approved provider lookup failed: provider not found, providerId={}",
                                    providerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider not found"
                            );
                        });

        if (!"APPROVED".equalsIgnoreCase(
                provider.getStatus())) {

            log.warn(
                    "Approved provider lookup rejected: provider not available, " +
                            "providerId={}, status={}",
                    providerId,
                    provider.getStatus()
            );

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not available"
            );
        }

        return toResponse(
                provider
        );
    }


    // ============================================================
    // UPDATE PROVIDER LIVE LOCATION
    // PROVIDER CAN UPDATE ONLY OWN LOCATION
    // ============================================================

    public ProviderResponse updateProviderLocation(
            Long providerId,
            Long authenticatedUserId,
            Double latitude,
            Double longitude) {

        Provider provider =
                providerRepository
                        .findById(providerId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Provider location update failed: provider not found, providerId={}",
                                    providerId
                            );

                            return new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Provider not found"
                            );
                        });

        validateOwnership(
                provider,
                authenticatedUserId
        );

        if (latitude == null
                || longitude == null) {

            log.warn(
                    "Provider location update rejected: coordinates missing, providerId={}",
                    providerId
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude and longitude are required"
            );
        }

        if (latitude < -90.0
                || latitude > 90.0) {

            log.warn(
                    "Provider location update rejected: invalid latitude, providerId={}",
                    providerId
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude must be between -90 and 90"
            );
        }

        if (longitude < -180.0
                || longitude > 180.0) {

            log.warn(
                    "Provider location update rejected: invalid longitude, providerId={}",
                    providerId
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Longitude must be between -180 and 180"
            );
        }

        provider.setLatitude(
                latitude
        );

        provider.setLongitude(
                longitude
        );

        provider.setUpdatedAt(
                OffsetDateTime.now()
        );

        Provider updatedProvider =
                providerRepository.save(
                        provider
                );

        log.info(
                "Provider live location updated: providerId={}, userId={}",
                updatedProvider.getId(),
                authenticatedUserId
        );

        return toResponse(
                updatedProvider
        );
    }


    // ============================================================
    // VALIDATE PROVIDER OWNERSHIP
    // ============================================================

    private void validateOwnership(
            Provider provider,
            Long authenticatedUserId) {

        if (authenticatedUserId == null) {

            log.warn(
                    "Provider ownership validation failed: authenticated user missing, " +
                            "providerId={}",
                    provider.getId()
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authentication required"
            );
        }

        if (!provider.getUserId()
                .equals(authenticatedUserId)) {

            log.warn(
                    "Provider ownership validation failed: providerId={}, " +
                            "authenticatedUserId={}, ownerUserId={}",
                    provider.getId(),
                    authenticatedUserId,
                    provider.getUserId()
            );

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only modify your own provider profile"
            );
        }
    }


    // ============================================================
    // NORMALIZE OPTIONAL PROFILE FIELD
    // ============================================================

    private String normalizeOptionalText(
            String value) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }


    // ============================================================
    // NULL-SAFE STRING COMPARISON
    // ============================================================

    private boolean equalsNullable(
            String first,
            String second) {

        if (first == null
                && second == null) {

            return true;
        }

        if (first == null
                || second == null) {

            return false;
        }

        return first.equals(second);
    }


    // ============================================================
    // PAGE -> RESPONSE
    // ============================================================

    private PageResponse<ProviderResponse> toPageResponse(
            Page<Provider> page) {

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),

                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }


    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private ProviderResponse toResponse(
            Provider provider) {

        List<ProviderPhotoResponse> photos =
                providerPhotoRepository
                        .findByProviderIdOrderByDisplayOrderAsc(
                                provider.getId()
                        )
                        .stream()
                        .map(this::toPhotoResponse)
                        .toList();

        return new ProviderResponse(

                provider.getId(),

                provider.getUserId(),

                provider.getBusinessName(),

                provider.getDescription(),

                provider.getPhone(),

                provider.getEmail(),

                provider.getAddress(),

                provider.getCity(),

                provider.getState(),

                provider.getPostalCode(),

                provider.getLatitude(),

                provider.getLongitude(),

                provider.getStatus(),

                provider.getCreatedAt(),

                provider.getUpdatedAt(),

                photos
        );
    }


    // ============================================================
    // PHOTO ENTITY -> RESPONSE
    // ============================================================

    private ProviderPhotoResponse toPhotoResponse(
            ProviderPhoto photo) {

        return new ProviderPhotoResponse(

                photo.getId(),

                photo.getProviderId(),

                photo.getImageUrl(),

                photo.getDisplayOrder(),

                photo.getCreatedAt()
        );
    }


    // ============================================================
    // PROVIDER - GET ONBOARDING STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderOnboardingStatusResponse getOnboardingStatus(
            Long userId) {

        return providerRepository
                .findByUserId(userId)
                .map(provider -> {

                    String status =
                            provider.getStatus()
                                    .trim()
                                    .toUpperCase();

                    boolean completed =
                            !provider.getBusinessName().isBlank()
                                    && !provider.getPhone().isBlank()
                                    && !provider.getEmail().isBlank();

                    boolean canServeCustomers =
                            "APPROVED".equals(status);

                    return new ProviderOnboardingStatusResponse(
                            true,
                            completed,
                            status,
                            canServeCustomers
                    );
                })
                .orElseGet(() ->
                        new ProviderOnboardingStatusResponse(
                                false,
                                false,
                                "NOT_STARTED",
                                false
                        )
                );
    }
}