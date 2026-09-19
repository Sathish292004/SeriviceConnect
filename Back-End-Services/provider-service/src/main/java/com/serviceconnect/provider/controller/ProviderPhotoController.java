package com.serviceconnect.provider.controller;

import com.serviceconnect.provider.dto.request.AddProviderPhotoRequest;
import com.serviceconnect.provider.dto.response.ProviderPhotoResponse;
import com.serviceconnect.provider.service.ProviderPhotoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers/{providerId}/photos")
@RequiredArgsConstructor
public class ProviderPhotoController {

    private final ProviderPhotoService providerPhotoService;

    // ============================================================
    // MULTIPART DIRECT UPLOAD
    // ============================================================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ProviderPhotoResponse> uploadPhoto(
            @PathVariable
            @Positive
            Long providerId,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(value = "displayOrder", required = false, defaultValue = "0")
            Integer displayOrder,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long authenticatedUserId = getUserId(jwt);
        String role = getRole(jwt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        providerPhotoService.uploadPhoto(
                                providerId,
                                authenticatedUserId,
                                role,
                                file,
                                displayOrder
                        )
                );
    }

    // ============================================================
    // SERVE UPLOADED PHOTO FILE (PUBLIC)
    // ============================================================

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> servePhoto(
            @PathVariable
            @Positive
            Long providerId,

            @PathVariable
            String filename) {

        Resource resource = providerPhotoService.loadPhotoResource(providerId, filename);
        MediaType mediaType = determineMediaType(filename);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                .body(resource);
    }

    // ============================================================
    // ADD PROVIDER PHOTO (LEGACY JSON ENDPOINT)
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
    // GET PROVIDER PHOTOS (PUBLIC)
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
    // DELETE PROVIDER PHOTO (AUTHENTICATED PROVIDER / ADMIN)
    // ============================================================

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable
            @Positive
            Long providerId,

            @PathVariable
            @Positive
            Long photoId,

            @AuthenticationPrincipal
            Jwt jwt) {

        Long authenticatedUserId = getUserId(jwt);
        String role = getRole(jwt);

        providerPhotoService.deletePhoto(
                providerId,
                photoId,
                authenticatedUserId,
                role
        );

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private Long getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User ID not found in JWT");
        }
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID in JWT");
        }
    }

    private String getRole(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            return "";
        }
        return role.trim().toUpperCase();
    }

    private MediaType determineMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".webp")) {
            return MediaType.valueOf("image/webp");
        } else {
            return MediaType.IMAGE_JPEG;
        }
    }
}