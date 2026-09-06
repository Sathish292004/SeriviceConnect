package com.serviceconnect.catalog.service;

import com.serviceconnect.catalog.client.ProviderServiceClient;
import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.dto.response.PageResponse;
import com.serviceconnect.catalog.entity.CatalogItem;
import com.serviceconnect.catalog.repository.CatalogItemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // CREATE CATALOG ITEM
    // ============================================================
    //
    // IMPORTANT:
    // caller supplies USER ID
    // service resolves USER ID -> PROVIDER ID
    //
    // Provider ID is never trusted from the client.
    // ============================================================

    public CatalogItemResponse create(
            Long authenticatedUserId,
            String authorizationHeader,
            CatalogItemRequest request) {

        Long providerId =
                resolveProviderId(
                        authenticatedUserId,
                        authorizationHeader
                );


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
    // CUSTOMER - GET ITEM BY ID
    // ============================================================

    public CatalogItemResponse getById(
            Long id,
            String authorizationHeader) {

        CatalogItem item =
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );


        if (!Boolean.TRUE.equals(
                item.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Catalog item not found"
            );
        }


        providerServiceClient
                .validateApprovedProvider(
                        item.getProviderId()
                );


        return toResponse(item);
    }


    // ============================================================
    // CUSTOMER - SEARCH + PAGINATION
    // ============================================================

    public PageResponse<CatalogItemResponse> search(

            String search,

            String category,

            String authorizationHeader,

            Pageable pageable) {


        List<Long> approvedProviderIds =
                providerServiceClient
                        .getApprovedProviders(
                                authorizationHeader
                        )
                        .stream()
                        .map(
                                ProviderServiceClient
                                        .ProviderResponse::id
                        )
                        .toList();


        if (approvedProviderIds.isEmpty()) {

            return new PageResponse<>(
                    List.of(),
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    0,
                    0,
                    true,
                    true
            );
        }


        boolean hasSearch =
                search != null
                        && !search.isBlank();


        boolean hasCategory =
                category != null
                        && !category.isBlank();


        Page<CatalogItem> page;


        // ========================================================
        // SEARCH + CATEGORY
        // ========================================================

        if (hasSearch && hasCategory) {

            page =
                    catalogItemRepository
                            .searchActiveApprovedProvidersByCategory(
                                    search.trim(),
                                    category.trim(),
                                    approvedProviderIds,
                                    pageable
                            );
        }


        // ========================================================
        // SEARCH ONLY
        // ========================================================

        else if (hasSearch) {

            page =
                    catalogItemRepository
                            .searchActiveApprovedProviders(
                                    search.trim(),
                                    approvedProviderIds,
                                    pageable
                            );
        }


        // ========================================================
        // CATEGORY ONLY
        // ========================================================

        else if (hasCategory) {

            page =
                    catalogItemRepository
                            .findActiveByCategoryForApprovedProviders(
                                    category.trim(),
                                    approvedProviderIds,
                                    pageable
                            );
        }


        // ========================================================
        // ALL ACTIVE APPROVED PROVIDER ITEMS
        // ========================================================

        else {

            page =
                    catalogItemRepository
                            .findActiveForApprovedProviders(
                                    approvedProviderIds,
                                    pageable
                            );
        }


        return toPageResponse(page);
    }


    // ============================================================
    // CUSTOMER - PROVIDER ITEMS
    // ============================================================

    public PageResponse<CatalogItemResponse> getByProvider(
            Long providerId,
            String authorizationHeader,
            Pageable pageable) {

        providerServiceClient
                .validateApprovedProvider(
                        providerId
                );


        Page<CatalogItem> page =
                catalogItemRepository
                        .findByProviderIdAndActiveTrue(
                                providerId,
                                pageable
                        );


        return toPageResponse(page);
    }


    // ============================================================
    // UPDATE CATALOG ITEM
    // ============================================================
    //
    // authenticatedUserId is used to resolve the caller's
    // provider ID before the ownership check.
    // ============================================================

    public CatalogItemResponse update(
            Long authenticatedUserId,
            String authorizationHeader,
            Long id,
            CatalogItemRequest request) {

        Long providerId =
                resolveProviderId(
                        authenticatedUserId,
                        authorizationHeader
                );


        CatalogItem item =
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );


        validateOwnership(
                item,
                providerId
        );


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
            Long authenticatedUserId,
            String authorizationHeader,
            Long id) {

        Long providerId =
                resolveProviderId(
                        authenticatedUserId,
                        authorizationHeader
                );


        CatalogItem item =
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );


        validateOwnership(
                item,
                providerId
        );


        if (!Boolean.TRUE.equals(
                item.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item is already inactive"
            );
        }


        item.setActive(false);

        catalogItemRepository.save(item);
    }


    // ============================================================
    // ACTIVATE CATALOG ITEM
    // ============================================================

    public CatalogItemResponse activate(
            Long authenticatedUserId,
            String authorizationHeader,
            Long id) {

        Long providerId =
                resolveProviderId(
                        authenticatedUserId,
                        authorizationHeader
                );


        CatalogItem item =
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );


        validateOwnership(
                item,
                providerId
        );


        if (Boolean.TRUE.equals(
                item.getActive())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Catalog item is already active"
            );
        }


        item.setActive(true);


        CatalogItem updated =
                catalogItemRepository.save(item);


        return toResponse(updated);
    }


    // ============================================================
    // RESOLVE PROVIDER FROM AUTHENTICATED USER
    // ============================================================

    private Long resolveProviderId(
            Long authenticatedUserId,
            String authorizationHeader) {

        if (authenticatedUserId == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user is required"
            );
        }


        ProviderServiceClient.ProviderResponse provider =
                providerServiceClient.getProviderByUserId(
                        authenticatedUserId,
                        authorizationHeader
                );


        if (provider == null
                || provider.id() == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Provider profile not found"
            );
        }


        if (provider.userId() == null
                || !provider.userId()
                .equals(authenticatedUserId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Provider ownership could not be verified"
            );
        }


        return provider.id();
    }


    // ============================================================
    // OWNERSHIP VALIDATION
    // ============================================================

    private void validateOwnership(
            CatalogItem item,
            Long providerId) {

        if (!item.getProviderId()
                .equals(providerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another provider's catalog item"
            );
        }
    }


    // ============================================================
    // ENTITY -> RESPONSE
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


    // ============================================================
    // PAGE -> RESPONSE
    // ============================================================

    private PageResponse<CatalogItemResponse> toPageResponse(
            Page<CatalogItem> page) {

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
}