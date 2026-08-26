package com.serviceconnect.admin.dto.response;

import java.time.OffsetDateTime;

public record AuditLogResponse(

        Long id,

        Long actorId,

        String actorRole,

        String action,

        String resourceType,

        String resourceId,

        String description,

        String ipAddress,

        String userAgent,

        OffsetDateTime createdAt

) {
}