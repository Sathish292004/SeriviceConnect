package com.serviceconnect.catalog.controller;

import com.serviceconnect.catalog.dto.request.CatalogItemRequest;
import com.serviceconnect.catalog.dto.response.CatalogItemResponse;
import com.serviceconnect.catalog.dto.response.PageResponse;
import com.serviceconnect.catalog.service.CatalogItemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

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

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
            )
            String authorizationHeader,

            @Valid
            @RequestBody
            CatalogItemRequest request) {

        Long authenticatedUserId =
                Long.valueOf(
                        authentication.getName()
                );


        CatalogItemResponse response =
                catalogItemService.create(
                        authenticatedUserId,
                        authorizationHeader,
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
            Long id,

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
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
    // SEARCH + PAGINATION
    // ============================================================

    @GetMapping
    public ResponseEntity<PageResponse<CatalogItemResponse>> search(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String category,

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
            )
            String authorizationHeader,

            @PageableDefault(
                    page = 0,
                    size = 20
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                catalogItemService.search(
                        search,
                        category,
                        authorizationHeader,
                        pageable
                )
        );
    }


    // ============================================================
    // CUSTOMER - PROVIDER ITEMS
    // ============================================================

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<PageResponse<CatalogItemResponse>>
    getByProvider(

            @PathVariable
            Long providerId,

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
            )
            String authorizationHeader,

            @PageableDefault(
                    page = 0,
                    size = 20
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                catalogItemService.getByProvider(
                        providerId,
                        authorizationHeader,
                        pageable
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

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
            )
            String authorizationHeader,

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            CatalogItemRequest request) {

        Long authenticatedUserId =
                Long.valueOf(
                        authentication.getName()
                );


        return ResponseEntity.ok(
                catalogItemService.update(
                        authenticatedUserId,
                        authorizationHeader,
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

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
            )
            String authorizationHeader,

            @PathVariable
            Long id) {

        Long authenticatedUserId =
                Long.valueOf(
                        authentication.getName()
                );


        catalogItemService.deactivate(
                authenticatedUserId,
                authorizationHeader,
                id
        );


        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // ACTIVATE CATALOG ITEM
    // ============================================================

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<CatalogItemResponse> activate(

            Authentication authentication,

            @RequestHeader(
                    HttpHeaders.AUTHORIZATION
            )
            String authorizationHeader,

            @PathVariable
            Long id) {

        Long authenticatedUserId =
                Long.valueOf(
                        authentication.getName()
                );


        return ResponseEntity.ok(
                catalogItemService.activate(
                        authenticatedUserId,
                        authorizationHeader,
                        id
                )
        );
    }
}