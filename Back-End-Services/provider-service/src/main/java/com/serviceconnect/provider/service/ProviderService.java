package com.serviceconnect.provider.service;

import com.serviceconnect.provider.dto.request.CreateProviderRequest;
import com.serviceconnect.provider.dto.response.ProviderPhotoResponse;
import com.serviceconnect.provider.dto.response.ProviderResponse;
import com.serviceconnect.provider.entity.Provider;
import com.serviceconnect.provider.entity.ProviderPhoto;
import com.serviceconnect.provider.repository.ProviderPhotoRepository;
import com.serviceconnect.provider.repository.ProviderRepository;
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
            throw new IllegalStateException(
                    "Provider profile already exists"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Provider provider = new Provider();

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

        // New providers always start as PENDING
        provider.setStatus("PENDING");

        provider.setCreatedAt(now);
        provider.setUpdatedAt(now);

        Provider savedProvider =
                providerRepository.save(provider);

        return toResponse(savedProvider);
    }


    // ============================================================
    // GET PROVIDER BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(
            Long providerId) {

        Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider not found"
                                )
                        );

        return toResponse(provider);
    }


    // ============================================================
    // GET PROVIDER BY USER ID
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderResponse getProviderByUserId(
            Long userId) {

        Provider provider =
                providerRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider profile not found"
                                )
                        );

        return toResponse(provider);
    }


    // ============================================================
    // UPDATE PROVIDER
    // ============================================================

    public ProviderResponse updateProvider(
            Long providerId,
            CreateProviderRequest request) {

        Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider not found"
                                )
                        );

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

        provider.setUpdatedAt(
                OffsetDateTime.now()
        );

        Provider updatedProvider =
                providerRepository.save(provider);

        return toResponse(updatedProvider);
    }


    // ============================================================
    // DELETE PROVIDER
    // ADMIN ONLY
    // ============================================================

    public void deleteProvider(
            Long providerId) {

        Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider not found"
                                )
                        );

        providerRepository.delete(provider);
    }


    // ============================================================
    // UPDATE PROVIDER STATUS
    // ADMIN ONLY
    //
    // APPROVED  -> Customer can see
    // REJECTED  -> Customer cannot see
    // SUSPENDED -> Customer cannot see
    // ============================================================

    public ProviderResponse updateProviderStatus(
            Long providerId,
            String status) {

        Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider not found"
                                )
                        );

        String newStatus =
                status.trim().toUpperCase();

        if (!newStatus.equals("APPROVED")
                && !newStatus.equals("REJECTED")
                && !newStatus.equals("SUSPENDED")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be APPROVED, REJECTED or SUSPENDED"
            );
        }

        provider.setStatus(newStatus);

        provider.setUpdatedAt(
                OffsetDateTime.now()
        );

        Provider updatedProvider =
                providerRepository.save(provider);

        return toResponse(updatedProvider);
    }


    // ============================================================
    // CUSTOMER - GET APPROVED PROVIDERS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProviderResponse> getApprovedProviders() {

        return providerRepository
                .findByStatus("APPROVED")
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // CUSTOMER - GET ONE APPROVED PROVIDER
    // ============================================================

    @Transactional(readOnly = true)
    public ProviderResponse getApprovedProvider(
            Long providerId) {

        Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider not found"
                                )
                        );

        if (!"APPROVED".equalsIgnoreCase(
                provider.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not available"
            );
        }

        return toResponse(provider);
    }


    // ============================================================
    // UPDATE PROVIDER LIVE LOCATION
    // ============================================================

    public ProviderResponse updateProviderLocation(
            Long providerId,
            Double latitude,
            Double longitude) {

        Provider provider =
                providerRepository.findById(providerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Provider not found"
                                )
                        );

        provider.setLatitude(latitude);
        provider.setLongitude(longitude);

        provider.setUpdatedAt(
                OffsetDateTime.now()
        );

        Provider updatedProvider =
                providerRepository.save(provider);

        return toResponse(updatedProvider);
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
}