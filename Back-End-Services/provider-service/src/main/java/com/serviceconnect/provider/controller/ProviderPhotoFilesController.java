package com.serviceconnect.provider.controller;

import com.serviceconnect.provider.service.ProviderPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/providers/photos/files")
@RequiredArgsConstructor
public class ProviderPhotoFilesController {

    private final ProviderPhotoService providerPhotoService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> servePhotoByFilename(@PathVariable String filename) {
        Resource resource = providerPhotoService.loadPhotoResourceByFilename(filename);
        MediaType mediaType = determineMediaType(filename);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=604800")
                .body(resource);
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
