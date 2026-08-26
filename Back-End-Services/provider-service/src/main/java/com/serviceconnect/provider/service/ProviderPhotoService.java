package com.serviceconnect.provider.service;

import com.serviceconnect.provider.dto.request.AddProviderPhotoRequest;
import com.serviceconnect.provider.dto.response.ProviderPhotoResponse;
import com.serviceconnect.provider.entity.Provider;
import com.serviceconnect.provider.entity.ProviderPhoto;
import com.serviceconnect.provider.repository.ProviderPhotoRepository;
import com.serviceconnect.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProviderPhotoService {

    private final ProviderPhotoRepository providerPhotoRepository;
    private final ProviderRepository providerRepository;

    public ProviderPhotoResponse addPhoto(
            Long providerId,
            AddProviderPhotoRequest request) {

        // Make sure provider exists
        providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Provider not found"
                        )
                );

        ProviderPhoto photo = new ProviderPhoto();

        photo.setProviderId(providerId);

        photo.setImageUrl(
                request.imageUrl().trim()
        );

        photo.setDisplayOrder(
                request.displayOrder() == null
                        ? 0
                        : request.displayOrder()
        );

        photo.setCreatedAt(
                OffsetDateTime.now()
        );

        ProviderPhoto savedPhoto =
                providerPhotoRepository.save(photo);

        return toResponse(savedPhoto);
    }


    @Transactional(readOnly = true)
    public List<ProviderPhotoResponse> getProviderPhotos(
            Long providerId) {

        // Make sure provider exists
        providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Provider not found"
                        )
                );

        return providerPhotoRepository
                .findByProviderIdOrderByDisplayOrderAsc(providerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public void deletePhoto(
            Long providerId,
            Long photoId) {

        ProviderPhoto photo =
                providerPhotoRepository.findById(photoId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Photo not found"
                                )
                        );

        // Prevent deleting another provider's photo
        if (!photo.getProviderId().equals(providerId)) {
            throw new IllegalArgumentException(
                    "Photo does not belong to this provider"
            );
        }

        providerPhotoRepository.delete(photo);
    }


    private ProviderPhotoResponse toResponse(
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