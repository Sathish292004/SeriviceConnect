package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.dto.request.CreateHelpArticleRequest;
import com.serviceconnect.admin.dto.request.UpdateHelpArticleRequest;
import com.serviceconnect.admin.dto.response.HelpArticleResponse;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.service.HelpCenterService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/help-center/articles")
@RequiredArgsConstructor
@Validated
public class AdminHelpCenterController {

    private final HelpCenterService helpCenterService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpArticleResponse>
    createArticle(
            @Valid
            @RequestBody
            CreateHelpArticleRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        helpCenterService.createArticle(
                                request
                        )
                );
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<
            PageResponse<HelpArticleResponse>>
    getArticles(

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.ASC,
                                "displayOrder"
                        ).and(
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "title"
                                )
                        )
                );

        return ResponseEntity.ok(
                helpCenterService
                        .getAllArticles(
                                pageable
                        )
        );
    }


    @GetMapping("/{articleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpArticleResponse>
    getArticle(
            @PathVariable
            @Positive
            Long articleId) {

        return ResponseEntity.ok(
                helpCenterService
                        .getArticle(
                                articleId
                        )
        );
    }


    @PutMapping("/{articleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HelpArticleResponse>
    updateArticle(

            @PathVariable
            @Positive
            Long articleId,

            @Valid
            @RequestBody
            UpdateHelpArticleRequest request) {

        return ResponseEntity.ok(
                helpCenterService.updateArticle(
                        articleId,
                        request
                )
        );
    }


    @DeleteMapping("/{articleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void>
    deleteArticle(
            @PathVariable
            @Positive
            Long articleId) {

        helpCenterService.deleteArticle(
                articleId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}