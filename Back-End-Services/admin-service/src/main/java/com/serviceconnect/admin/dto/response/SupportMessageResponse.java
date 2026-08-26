package com.serviceconnect.admin.dto.response;

import com.serviceconnect.admin.entity.SenderType;

import java.time.OffsetDateTime;

public record SupportMessageResponse(

        Long id,

        Long ticketId,

        Long senderId,

        SenderType senderType,

        String message,

        OffsetDateTime createdAt
) {
}