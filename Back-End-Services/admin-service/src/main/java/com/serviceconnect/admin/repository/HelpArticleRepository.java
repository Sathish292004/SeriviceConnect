package com.serviceconnect.admin.repository;

import com.serviceconnect.admin.entity.HelpArticle;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HelpArticleRepository
        extends JpaRepository<HelpArticle, Long> {

    Optional<HelpArticle> findBySlugAndPublishedTrue(
            String slug
    );

    boolean existsBySlug(
            String slug
    );

    boolean existsBySlugAndIdNot(
            String slug,
            Long id
    );

    @Query("""
            SELECT article
            FROM HelpArticle article
            WHERE article.published = true
              AND (:category IS NULL OR article.category = :category)
              AND (
                    :search IS NULL
                    OR LOWER(article.title)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(article.content)
                        LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY article.displayOrder ASC,
                     article.title ASC
            """)
    Page<HelpArticle> searchPublished(
            @Param("category")
            String category,

            @Param("search")
            String search,

            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT article.category
            FROM HelpArticle article
            WHERE article.published = true
            ORDER BY article.category ASC
            """)
    List<String> findPublishedCategories();
}