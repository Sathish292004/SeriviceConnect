package com.serviceconnect.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "help_articles",
        indexes = {
                @Index(
                        name = "idx_help_article_published_category_order",
                        columnList = "published, category, display_order"
                ),
                @Index(
                        name = "idx_help_article_category",
                        columnList = "category"
                )
        }
)
@Getter
@Setter
public class HelpArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
    private String slug;

    @Column(
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            nullable = false,
            length = 80
    )
    private String category;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(
            nullable = false
    )
    private boolean published;

    @Column(
            name = "display_order",
            nullable = false
    )
    private int displayOrder;

    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @Version
    @Column(
            nullable = false
    )
    private Long version;
}