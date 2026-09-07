package com.serviceconnect.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateHelpArticleRequest(

        @NotBlank(message = "Slug is required")
        @Size(
                max = 150,
                message = "Slug must not exceed 150 characters"
        )
        @Pattern(
                regexp = "[a-z0-9]+(?:-[a-z0-9]+)*",
                message = "Slug must contain only lowercase letters, numbers, and hyphens"
        )
        String slug,

        @NotBlank(message = "Title is required")
        @Size(
                min = 3,
                max = 200,
                message = "Title must be between 3 and 200 characters"
        )
        String title,

        @NotBlank(message = "Category is required")
        @Size(
                min = 2,
                max = 80,
                message = "Category must be between 2 and 80 characters"
        )
        String category,

        @NotBlank(message = "Content is required")
        @Size(
                min = 10,
                max = 20000,
                message = "Content must be between 10 and 20000 characters"
        )
        String content,

        @NotNull(message = "Published flag is required")
        Boolean published,

        @NotNull(message = "Display order is required")
        @PositiveOrZero(
                message = "Display order cannot be negative"
        )
        Integer displayOrder
) {
}