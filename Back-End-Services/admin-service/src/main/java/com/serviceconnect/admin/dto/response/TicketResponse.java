package com.serviceconnect.admin.dto.response;

import com.serviceconnect.admin.entity.TicketPriority;
import com.serviceconnect.admin.entity.TicketStatus;

import java.time.OffsetDateTime;

public record TicketResponse(

        Long id,

        String ticketNumber,

        Long customerId,

        Long assignedAgentId,

        String subject,

        String description,

        TicketStatus status,

        TicketPriority priority,

        String category,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt,

        OffsetDateTime resolvedAt,

        Long version
) {
}