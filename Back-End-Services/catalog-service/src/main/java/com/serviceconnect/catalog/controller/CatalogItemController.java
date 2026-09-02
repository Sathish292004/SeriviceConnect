package com.serviceconnect.catalog.controller;

import com.serviceconnect.catalog.client.ProviderServiceClient;
import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.service.CatalogItemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogItemController {

    private final CatalogItemService catalogItemService;

    private final ProviderServiceClient providerServiceClient;


    // ============================================================
    // CREATE CATALOG ITEM
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CatalogItemResponse> create(

            Authentication authentication,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @Valid
            @RequestBody
            CatalogItemRequest request) {

        // --------------------------------------------------------
        // 1. Get authenticated user's ID from JWT
        // --------------------------------------------------------

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );


        // --------------------------------------------------------
        // 2. Get Provider using user ID
        // --------------------------------------------------------

        ProviderServiceClient.ProviderResponse provider =
                providerServiceClient.getProviderByUserId(
                        userId,
                        authorizationHeader
                );


        // --------------------------------------------------------
        // 3. Get actual Provider entity ID
        // --------------------------------------------------------

        Long providerId =
                provider.id();


        // --------------------------------------------------------
        // 4. Create catalog item using Provider ID
        // --------------------------------------------------------

        CatalogItemResponse response =
                catalogItemService.create(
                        providerId,
                        request
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ============================================================
    // GET CATALOG ITEM BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemResponse> getById(

            @PathVariable
            Long id) {

        return ResponseEntity.ok(
                catalogItemService.getById(id)
        );
    }


    // ============================================================
    // GET ALL ACTIVE CATALOG ITEMS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<CatalogItemResponse>> getAll() {

        return ResponseEntity.ok(
                catalogItemService.getAllActive()
        );
    }


    // ============================================================
    // GET PROVIDER CATALOG ITEMS
    // ============================================================

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<CatalogItemResponse>> getByProvider(

            @PathVariable
            Long providerId) {

        return ResponseEntity.ok(
                catalogItemService
                        .getByProvider(providerId)
        );
    }


    // ============================================================
    // GET CATALOG ITEMS BY CATEGORY
    // ============================================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CatalogItemResponse>> getByCategory(

            @PathVariable
            String category) {

        return ResponseEntity.ok(
                catalogItemService
                        .getByCategory(category)
        );
    }


    // ============================================================
    // UPDATE CATALOG ITEM
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CatalogItemResponse> update(

            Authentication authentication,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            CatalogItemRequest request) {

        Long providerId =
                Long.valueOf(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                catalogItemService.update(
                        providerId,
                        id,
                        request
                )
        );
    }


    // ============================================================
    // DEACTIVATE CATALOG ITEM
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deactivate(

            Authentication authentication,

            @PathVariable
            Long id) {

        Long providerId =
                Long.valueOf(
                        authentication.getName()
                );

        catalogItemService.deactivate(
                providerId,
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}