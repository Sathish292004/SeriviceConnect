package com.serviceconnect.provider.service;

import com.serviceconnect.provider.dto.request.AddProviderPhotoRequest;
import com.serviceconnect.provider.dto.response.ProviderPhotoResponse;
import com.serviceconnect.provider.entity.Provider;
import com.serviceconnect.provider.entity.ProviderPhoto;
import com.serviceconnect.provider.repository.ProviderPhotoRepository;
import com.serviceconnect.provider.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProviderPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final ProviderPhotoRepository providerPhotoRepository;
    private final ProviderRepository providerRepository;

    // ============================================================
    // MULTIPART DIRECT UPLOAD
    // ============================================================

    public ProviderPhotoResponse uploadPhoto(
            Long providerId,
            Long authenticatedUserId,
            String role,
            MultipartFile file,
            Integer displayOrder) {

        // 1. Verify provider exists
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Provider not found"
                ));

        // 2. Enforce provider ownership
        validateOwnership(provider, authenticatedUserId, role);

        // 3. Validate file presence
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File is required and must not be empty"
            );
        }

        // 4. Validate file size (<= 5MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size exceeds maximum limit of 5MB"
            );
        }

        // 5. Validate MIME content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported image format. Only JPG, PNG, and WEBP are accepted"
            );
        }

        // 6. Validate filename and extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "photo.jpg";
        }
        String cleanFilename = StringUtils.cleanPath(originalFilename);
        if (cleanFilename.contains("..") || cleanFilename.contains("/") || cleanFilename.contains("\\")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file name"
            );
        }

        int dotIndex = cleanFilename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File must have a valid image extension (.jpg, .jpeg, .png, .webp)"
            );
        }

        String extension = cleanFilename.substring(dotIndex).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file extension. Only .jpg, .jpeg, .png, and .webp are allowed"
            );
        }

        String baseName = cleanFilename.substring(0, dotIndex).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (baseName.length() > 50) {
            baseName = baseName.substring(0, 50);
        }
        String storedFilename = UUID.randomUUID() + "_" + baseName + extension;

        // 7. Store on disk under uploads/providers/{providerId}/
        Path providerUploadDir = Paths.get("uploads", "providers", String.valueOf(providerId)).toAbsolutePath().normalize();
        try {
            Files.createDirectories(providerUploadDir);
            Path targetPath = providerUploadDir.resolve(storedFilename).normalize();
            if (!targetPath.startsWith(providerUploadDir)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid path traversal attempt"
                );
            }
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Portfolio photo stored successfully: providerId={}, file={}", providerId, targetPath);
        } catch (IOException e) {
            log.error("Failed to store portfolio photo on disk", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save uploaded photo"
            );
        }

        // 8. Save entity
        ProviderPhoto photo = new ProviderPhoto();
        photo.setProviderId(providerId);
        photo.setImageUrl("/api/v1/providers/" + providerId + "/photos/files/" + storedFilename);
        photo.setDisplayOrder(displayOrder == null ? 0 : displayOrder);
        photo.setCreatedAt(OffsetDateTime.now());

        ProviderPhoto savedPhoto = providerPhotoRepository.save(photo);
        return toResponse(savedPhoto);
    }

    // ============================================================
    // LOAD PHOTO RESOURCE (IMAGE SERVING)
    // ============================================================

    @Transactional(readOnly = true)
    public Resource loadPhotoResource(Long providerId, String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        Path providerUploadDir = Paths.get("uploads", "providers", String.valueOf(providerId)).toAbsolutePath().normalize();
        Path filePath = providerUploadDir.resolve(filename).normalize();

        if (!filePath.startsWith(providerUploadDir) || !Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.warn("Photo file not found: providerId={}, filename={}", providerId, filename);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }

        try {
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            log.error("Failed to load photo resource URL", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }
    }

    @Transactional(readOnly = true)
    public Resource loadPhotoResourceByFilename(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        Path baseDir = Paths.get("uploads", "providers").toAbsolutePath().normalize();
        if (!Files.exists(baseDir)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }

        try (var stream = Files.walk(baseDir, 2)) {
            var foundPath = stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().equals(filename))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
            return new UrlResource(foundPath.toUri());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }
    }

    // ============================================================
    // ADD PHOTO (LEGACY JSON ENDPOINT)
    // ============================================================

    public ProviderPhotoResponse addPhoto(
            Long providerId,
            AddProviderPhotoRequest request) {

        providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Provider not found"
                        )
                );

        ProviderPhoto photo = new ProviderPhoto();
        photo.setProviderId(providerId);
        photo.setImageUrl(request.imageUrl().trim());
        photo.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
        photo.setCreatedAt(OffsetDateTime.now());

        ProviderPhoto savedPhoto = providerPhotoRepository.save(photo);
        return toResponse(savedPhoto);
    }

    // ============================================================
    // GET PROVIDER PHOTOS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ProviderPhotoResponse> getProviderPhotos(Long providerId) {
        providerRepository.findById(providerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Provider not found"
                        )
                );

        return providerPhotoRepository
                .findByProviderIdOrderByDisplayOrderAsc(providerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // DELETE PHOTO WITH FILE CLEANUP
    // ============================================================

    public void deletePhoto(
            Long providerId,
            Long photoId,
            Long authenticatedUserId,
            String role) {

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Provider not found"
                ));

        validateOwnership(provider, authenticatedUserId, role);

        ProviderPhoto photo = providerPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Photo not found"
                ));

        if (!photo.getProviderId().equals(providerId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Photo does not belong to this provider"
            );
        }

        // Clean up file from disk if stored locally
        String imageUrl = photo.getImageUrl();
        if (imageUrl != null && imageUrl.contains("/files/")) {
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            if (!filename.isBlank() && !filename.contains("..")) {
                Path uploadDir = Paths.get("uploads", "providers", String.valueOf(providerId)).toAbsolutePath().normalize();
                Path filePath = uploadDir.resolve(filename).normalize();
                if (filePath.startsWith(uploadDir)) {
                    try {
                        Files.deleteIfExists(filePath);
                        log.info("Deleted photo file on disk: {}", filePath);
                    } catch (IOException e) {
                        log.warn("Failed to delete photo file on disk: {}", filePath, e);
                    }
                }
            }
        }

        providerPhotoRepository.delete(photo);
    }

    // Backward compatibility overload
    public void deletePhoto(Long providerId, Long photoId) {
        deletePhoto(providerId, photoId, null, "ADMIN");
    }

    // ============================================================
    // OWNERSHIP VALIDATION HELPER
    // ============================================================

    private void validateOwnership(Provider provider, Long authenticatedUserId, String role) {
        if ("ADMIN".equals(role)) {
            return;
        }

        if (!"PROVIDER".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only providers can manage portfolio photos"
            );
        }

        if (authenticatedUserId == null || !provider.getUserId().equals(authenticatedUserId)) {
            log.warn("Cross-provider photo modification attempt: ownerUserId={}, authenticatedUserId={}",
                    provider.getUserId(), authenticatedUserId);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only manage photos for your own provider profile"
            );
        }
    }

    private ProviderPhotoResponse toResponse(ProviderPhoto photo) {
        return new ProviderPhotoResponse(
                photo.getId(),
                photo.getProviderId(),
                photo.getImageUrl(),
                photo.getDisplayOrder(),
                photo.getCreatedAt()
        );
    }
}