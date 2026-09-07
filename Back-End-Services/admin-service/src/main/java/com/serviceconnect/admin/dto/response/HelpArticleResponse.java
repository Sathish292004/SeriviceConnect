package com.serviceconnect.admin.dto.response;

import java.time.OffsetDateTime;

public record HelpArticleResponse(

        Long id,

        String slug,

        String title,

        String category,

        String content,

        boolean published,

        int displayOrder,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt,

        Long version
) {
}