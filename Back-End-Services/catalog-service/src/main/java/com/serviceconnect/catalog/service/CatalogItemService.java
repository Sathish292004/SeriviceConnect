package com.serviceconnect.catalog.service;

import com.serviceconnect.catalog.client.ProviderServiceClient;
import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.entity.CatalogItem;
import com.serviceconnect.catalog.repository.CatalogItemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // CREATE CATALOG ITEM
    // ============================================================

    public CatalogItemResponse create(
            Long providerId,
            CatalogItemRequest request) {

        CatalogItem item =
                new CatalogItem();

        item.setProviderId(providerId);

        item.setName(
                request.name()
        );

        item.setDescription(
                request.description()
        );

        item.setCategory(
                request.category()
        );

        item.setPrice(
                request.price()
        );

        item.setDurationMinutes(
                request.durationMinutes()
        );

        item.setActive(true);

        CatalogItem saved =
                catalogItemRepository.save(item);

        return toResponse(saved);
    }


    // ============================================================
    // GET CATALOG ITEM
    //
    // CUSTOMER CATALOG RULE:
    //
    // Item must be:
    // 1. Active
    // 2. Belong to an APPROVED provider
    // ============================================================

    public CatalogItemResponse getById(
            Long id,
            String authorizationHeader) {

        CatalogItem item =
                catalogItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );

        if (!Boolean.TRUE.equals(
                item.getActive()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Catalog item not available"
            );
        }

        Set<Long> approvedProviderIds =
                getApprovedProviderIds(
                        authorizationHeader
                );

        if (!approvedProviderIds.contains(
                item.getProviderId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Catalog item not available"
            );
        }

        return toResponse(item);
    }


    // ============================================================
    // GET APPROVED + ACTIVE CATALOG
    // ============================================================

    public List<CatalogItemResponse> getAllActive(
            String authorizationHeader) {

        Set<Long> approvedProviderIds =
                getApprovedProviderIds(
                        authorizationHeader
                );

        return catalogItemRepository
                .findByActiveTrue()
                .stream()
                .filter(item ->
                        approvedProviderIds.contains(
                                item.getProviderId()
                        )
                )
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET APPROVED + ACTIVE PROVIDER CATALOG
    // ============================================================

    public List<CatalogItemResponse> getByProvider(
            Long providerId,
            String authorizationHeader) {

        Set<Long> approvedProviderIds =
                getApprovedProviderIds(
                        authorizationHeader
                );

        if (!approvedProviderIds.contains(
                providerId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider not available"
            );
        }

        return catalogItemRepository
                .findByProviderIdAndActiveTrue(
                        providerId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET APPROVED + ACTIVE CATEGORY CATALOG
    // ============================================================

    public List<CatalogItemResponse> getByCategory(
            String category,
            String authorizationHeader) {

        Set<Long> approvedProviderIds =
                getApprovedProviderIds(
                        authorizationHeader
                );

        return catalogItemRepository
                .findByCategory(category)
                .stream()
                .filter(CatalogItem::getActive)
                .filter(item ->
                        approvedProviderIds.contains(
                                item.getProviderId()
                        )
                )
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // UPDATE CATALOG ITEM
    // ============================================================

    public CatalogItemResponse update(
            Long providerId,
            Long id,
            CatalogItemRequest request) {

        CatalogItem item =
                catalogItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );

        if (!item.getProviderId()
                .equals(providerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update another provider's catalog item"
            );
        }

        item.setName(
                request.name()
        );

        item.setDescription(
                request.description()
        );

        item.setCategory(
                request.category()
        );

        item.setPrice(
                request.price()
        );

        item.setDurationMinutes(
                request.durationMinutes()
        );

        CatalogItem updated =
                catalogItemRepository.save(item);

        return toResponse(updated);
    }


    // ============================================================
    // DEACTIVATE CATALOG ITEM
    // ============================================================

    public void deactivate(
            Long providerId,
            Long id) {

        CatalogItem item =
                catalogItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );

        if (!item.getProviderId()
                .equals(providerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot deactivate another provider's catalog item"
            );
        }

        item.setActive(false);

        catalogItemRepository.save(item);
    }


    // ============================================================
    // GET APPROVED PROVIDER IDS
    // ============================================================

    private Set<Long> getApprovedProviderIds(
            String authorizationHeader) {

        if (authorizationHeader == null
                || authorizationHeader.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authorization is required"
            );
        }

        List<ProviderServiceClient.ProviderResponse>
                providers =
                providerServiceClient
                        .getApprovedProviders(
                                authorizationHeader
                        );

        Set<Long> approvedProviderIds =
                new HashSet<>();

        for (
                ProviderServiceClient.ProviderResponse provider
                : providers
        ) {

            if (provider.id() != null) {

                approvedProviderIds.add(
                        provider.id()
                );
            }
        }

        return approvedProviderIds;
    }


    // ============================================================
    // ENTITY → RESPONSE
    // ============================================================

    private CatalogItemResponse toResponse(
            CatalogItem item) {

        return new CatalogItemResponse(
                item.getId(),
                item.getProviderId(),
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice(),
                item.getDurationMinutes(),
                item.getActive(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}