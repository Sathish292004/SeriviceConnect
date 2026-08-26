package com.serviceconnect.provider.controller;

import com.serviceconnect.provider.dto.request.AddProviderPhotoRequest;
import com.serviceconnect.provider.dto.response.ProviderPhotoResponse;
import com.serviceconnect.provider.service.ProviderPhotoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers/{providerId}/photos")
@RequiredArgsConstructor
public class ProviderPhotoController {

    private final ProviderPhotoService providerPhotoService;


    // ============================================================
    // ADD PROVIDER PHOTO
    // ============================================================

    @PostMapping
    public ResponseEntity<ProviderPhotoResponse> addPhoto(
            @PathVariable
            @Positive
            Long providerId,

            @Valid
            @RequestBody
            AddProviderPhotoRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        providerPhotoService.addPhoto(
                                providerId,
                                request
                        )
                );
    }


    // ============================================================
    // GET PROVIDER PHOTOS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<ProviderPhotoResponse>> getProviderPhotos(
            @PathVariable
            @Positive
            Long providerId) {

        return ResponseEntity.ok(
                providerPhotoService.getProviderPhotos(
                        providerId
                )
        );
    }


    // ============================================================
    // DELETE PROVIDER PHOTO
    // ============================================================

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable
            @Positive
            Long providerId,

            @PathVariable
            @Positive
            Long photoId) {

        providerPhotoService.deletePhoto(
                providerId,
                photoId
        );

        return ResponseEntity.noContent().build();
    }
}