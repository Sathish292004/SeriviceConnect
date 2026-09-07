package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.dto.response.HelpArticleResponse;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.service.HelpCenterService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/help-center")
@RequiredArgsConstructor
@Validated
public class HelpCenterController {

    private final HelpCenterService helpCenterService;


    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {

        return ResponseEntity.ok(
                helpCenterService
                        .getPublishedCategories()
        );
    }


    @GetMapping("/articles")
    public ResponseEntity<
            PageResponse<HelpArticleResponse>>
    getArticles(

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int size) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size
                );

        return ResponseEntity.ok(
                helpCenterService
                        .searchPublishedArticles(
                                category,
                                search,
                                pageable
                        )
        );
    }


    @GetMapping("/articles/{slug}")
    public ResponseEntity<HelpArticleResponse>
    getArticle(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                helpCenterService
                        .getPublishedArticle(
                                slug
                        )
        );
    }
}