package com.serviceconnect.catalog.service;

import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.entity.CatalogItem;
import com.serviceconnect.catalog.repository.CatalogItemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogItemService {

    private final CatalogItemRepository catalogItemRepository;


    // ============================================================
    // CREATE CATALOG ITEM
    // ============================================================

    public CatalogItemResponse create(
            Long providerId,
            CatalogItemRequest request) {

        CatalogItem item = new CatalogItem();

        item.setProviderId(providerId);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setPrice(request.price());
        item.setDurationMinutes(request.durationMinutes());
        item.setActive(true);

        CatalogItem saved =
                catalogItemRepository.save(item);

        return toResponse(saved);
    }


    // ============================================================
    // GET CATALOG ITEM
    // ============================================================

    public CatalogItemResponse getById(Long id) {

        CatalogItem item =
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );

        return toResponse(item);
    }


    // ============================================================
    // GET ALL ACTIVE ITEMS
    // ============================================================

    public List<CatalogItemResponse> getAllActive() {

        return catalogItemRepository
                .findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET PROVIDER ITEMS
    // ============================================================

    public List<CatalogItemResponse> getByProvider(
            Long providerId) {

        return catalogItemRepository
                .findByProviderIdAndActiveTrue(providerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET BY CATEGORY
    // ============================================================

    public List<CatalogItemResponse> getByCategory(
            String category) {

        return catalogItemRepository
                .findByCategory(category)
                .stream()
                .filter(CatalogItem::getActive)
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
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );

        if (!item.getProviderId().equals(providerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot update another provider's catalog item"
            );
        }

        item.setName(request.name());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setPrice(request.price());
        item.setDurationMinutes(request.durationMinutes());

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
                catalogItemRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Catalog item not found"
                                )
                        );

        if (!item.getProviderId().equals(providerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot deactivate another provider's catalog item"
            );
        }

        item.setActive(false);

        catalogItemRepository.save(item);
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