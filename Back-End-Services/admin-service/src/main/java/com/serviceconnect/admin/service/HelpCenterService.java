package com.serviceconnect.admin.service;

import com.serviceconnect.admin.dto.request.CreateHelpArticleRequest;
import com.serviceconnect.admin.dto.request.UpdateHelpArticleRequest;
import com.serviceconnect.admin.dto.response.HelpArticleResponse;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.entity.HelpArticle;
import com.serviceconnect.admin.repository.HelpArticleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HelpCenterService {

    private final HelpArticleRepository helpArticleRepository;


    // ============================================================
    // PUBLIC
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<HelpArticleResponse> searchPublishedArticles(
            String category,
            String search,
            Pageable pageable) {

        String normalizedCategory =
                normalize(category);

        String normalizedSearch =
                normalize(search);

        Page<HelpArticle> page =
                helpArticleRepository.searchPublished(
                        normalizedCategory,
                        normalizedSearch,
                        pageable
                );

        return toPageResponse(page);
    }


    @Transactional(readOnly = true)
    public HelpArticleResponse getPublishedArticle(
            String slug) {

        HelpArticle article =
                helpArticleRepository
                        .findBySlugAndPublishedTrue(slug)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Help article not found"
                                )
                        );

        return toResponse(article);
    }


    @Transactional(readOnly = true)
    public List<String> getPublishedCategories() {

        return helpArticleRepository
                .findPublishedCategories();
    }


    // ============================================================
    // ADMIN
    // ============================================================

    public HelpArticleResponse createArticle(
            CreateHelpArticleRequest request) {

        String slug =
                normalizeRequired(
                        request.slug()
                );

        if (helpArticleRepository.existsBySlug(slug)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A help article with this slug already exists"
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now();

        HelpArticle article =
                new HelpArticle();

        article.setSlug(slug);

        article.setTitle(
                normalizeRequired(
                        request.title()
                )
        );

        article.setCategory(
                normalizeRequired(
                        request.category()
                )
        );

        article.setContent(
                request.content().trim()
        );

        article.setPublished(
                request.published()
        );

        article.setDisplayOrder(
                request.displayOrder() == null
                        ? 0
                        : request.displayOrder()
        );

        article.setCreatedAt(now);

        article.setUpdatedAt(now);

        try {

            HelpArticle saved =
                    helpArticleRepository.save(
                            article
                    );

            log.info(
                    "Help article created: articleId={}, slug={}, published={}",
                    saved.getId(),
                    saved.getSlug(),
                    saved.isPublished()
            );

            return toResponse(saved);

        } catch (DataIntegrityViolationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A help article with this slug already exists",
                    exception
            );
        }
    }


    @Transactional(readOnly = true)
    public PageResponse<HelpArticleResponse> getAllArticles(
            Pageable pageable) {

        return toPageResponse(
                helpArticleRepository.findAll(
                        pageable
                )
        );
    }


    @Transactional(readOnly = true)
    public HelpArticleResponse getArticle(
            Long articleId) {

        return toResponse(
                findArticle(articleId)
        );
    }


    public HelpArticleResponse updateArticle(
            Long articleId,
            UpdateHelpArticleRequest request) {

        HelpArticle article =
                findArticle(articleId);

        String slug =
                normalizeRequired(
                        request.slug()
                );

        if (helpArticleRepository.existsBySlugAndIdNot(
                slug,
                articleId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A help article with this slug already exists"
            );
        }

        article.setSlug(slug);

        article.setTitle(
                normalizeRequired(
                        request.title()
                )
        );

        article.setCategory(
                normalizeRequired(
                        request.category()
                )
        );

        article.setContent(
                request.content().trim()
        );

        article.setPublished(
                request.published()
        );

        article.setDisplayOrder(
                request.displayOrder()
        );

        article.setUpdatedAt(
                OffsetDateTime.now()
        );

        try {

            HelpArticle saved =
                    helpArticleRepository.save(
                            article
                    );

            log.info(
                    "Help article updated: articleId={}, slug={}, published={}",
                    saved.getId(),
                    saved.getSlug(),
                    saved.isPublished()
            );

            return toResponse(saved);

        } catch (DataIntegrityViolationException exception) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A help article with this slug already exists",
                    exception
            );
        }
    }


    public void deleteArticle(
            Long articleId) {

        HelpArticle article =
                findArticle(articleId);

        helpArticleRepository.delete(article);

        log.info(
                "Help article deleted: articleId={}, slug={}",
                articleId,
                article.getSlug()
        );
    }


    // ============================================================
    // HELPERS
    // ============================================================

    private HelpArticle findArticle(
            Long articleId) {

        return helpArticleRepository
                .findById(articleId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Help article not found"
                        )
                );
    }


    private PageResponse<HelpArticleResponse>
    toPageResponse(
            Page<HelpArticle> page) {

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),

                page.getNumber(),

                page.getSize(),

                page.getTotalElements(),

                page.getTotalPages(),

                page.isFirst(),

                page.isLast()
        );
    }


    private HelpArticleResponse toResponse(
            HelpArticle article) {

        return new HelpArticleResponse(
                article.getId(),
                article.getSlug(),
                article.getTitle(),
                article.getCategory(),
                article.getContent(),
                article.isPublished(),
                article.getDisplayOrder(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getVersion()
        );
    }


    private String normalize(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }


    private String normalizeRequired(
            String value) {

        String normalized =
                normalize(value);

        if (normalized == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required help article field is missing"
            );
        }

        return normalized;
    }
}