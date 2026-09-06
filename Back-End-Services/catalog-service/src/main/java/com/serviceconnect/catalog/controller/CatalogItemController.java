package com.serviceconnect.catalog.controller;


import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.service.CatalogItemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

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


    // ============================================================
    // CREATE CATALOG ITEM
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CatalogItemResponse> create(

            Authentication authentication,

            @Valid
            @RequestBody
            CatalogItemRequest request) {

        Long providerId =
                Long.valueOf(
                        authentication.getName()
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
    // GET CATALOG ITEM BY ID
    //
    // ONLY ACTIVE + APPROVED PROVIDER
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<CatalogItemResponse> getById(

            @PathVariable
            Long id,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader) {

        return ResponseEntity.ok(
                catalogItemService.getById(
                        id,
                        authorizationHeader
                )
        );
    }


    // ============================================================
    // GET APPROVED + ACTIVE CATALOG
    // ============================================================

    @GetMapping
    public ResponseEntity<List<CatalogItemResponse>> getAll(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader) {

        return ResponseEntity.ok(
                catalogItemService.getAllActive(
                        authorizationHeader
                )
        );
    }


    // ============================================================
    // GET PROVIDER CATALOG
    //
    // ONLY WHEN PROVIDER IS APPROVED
    // ============================================================

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<CatalogItemResponse>>
    getByProvider(

            @PathVariable
            Long providerId,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader) {

        return ResponseEntity.ok(
                catalogItemService.getByProvider(
                        providerId,
                        authorizationHeader
                )
        );
    }


    // ============================================================
    // GET CATEGORY CATALOG
    //
    // ONLY ACTIVE ITEMS FROM APPROVED PROVIDERS
    // ============================================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<CatalogItemResponse>>
    getByCategory(

            @PathVariable
            String category,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader) {

        return ResponseEntity.ok(
                catalogItemService.getByCategory(
                        category,
                        authorizationHeader
                )
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