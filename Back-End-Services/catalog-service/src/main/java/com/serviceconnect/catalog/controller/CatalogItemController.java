package com.serviceconnect.catalog.controller;

import com.serviceconnect.catalog.client.ProviderServiceClient;
import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.service.CatalogItemService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

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
    // PROVIDER - CREATE CATALOG ITEM
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

        Long providerId =
                resolveProviderId(
                        authentication,
                        authorizationHeader
                );

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
    // GET ACTIVE CATALOG ITEM BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemResponse> getById(

            @PathVariable
            @Positive
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
    // GET ACTIVE CATALOG ITEMS BY PROVIDER
    // ============================================================

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<CatalogItemResponse>> getByProvider(

            @PathVariable
            @Positive
            Long providerId) {

        return ResponseEntity.ok(
                catalogItemService.getByProvider(
                        providerId
                )
        );
    }


    // ============================================================
    // GET ACTIVE CATALOG ITEMS BY CATEGORY
    // ============================================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CatalogItemResponse>> getByCategory(

            @PathVariable
            String category) {

        return ResponseEntity.ok(
                catalogItemService.getByCategory(
                        category
                )
        );
    }


    // ============================================================
    // PROVIDER - UPDATE OWN CATALOG ITEM
    // ============================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CatalogItemResponse> update(

            Authentication authentication,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @PathVariable
            @Positive
            Long id,

            @Valid
            @RequestBody
            CatalogItemRequest request) {

        Long providerId =
                resolveProviderId(
                        authentication,
                        authorizationHeader
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
    // PROVIDER - DEACTIVATE OWN CATALOG ITEM
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<Void> deactivate(

            Authentication authentication,

            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader,

            @PathVariable
            @Positive
            Long id) {

        Long providerId =
                resolveProviderId(
                        authentication,
                        authorizationHeader
                );

        catalogItemService.deactivate(
                providerId,
                id
        );

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // RESOLVE AUTHENTICATED USER → PROVIDER ID
    // ============================================================

    private Long resolveProviderId(

            Authentication authentication,
            String authorizationHeader) {

        Long userId =
                Long.valueOf(
                        authentication.getName()
                );

        ProviderServiceClient.ProviderResponse provider =
                providerServiceClient.getProviderByUserId(
                        userId,
                        authorizationHeader
                );

        return provider.id();
    }
}