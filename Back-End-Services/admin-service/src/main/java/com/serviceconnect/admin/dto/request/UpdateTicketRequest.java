package com.serviceconnect.admin.dto.request;

import com.serviceconnect.admin.entity.TicketPriority;
import com.serviceconnect.admin.entity.TicketStatus;
import jakarta.validation.constraints.Size;

public record UpdateTicketRequest(

        TicketStatus status,

        TicketPriority priority,

        @Size(
                min = 5,
                max = 200,
                message = "Subject must be between 5 and 200 characters"
        )
        String subject,

        @Size(
                max = 50,
                message = "Category must not exceed 50 characters"
        )
        String category
) {
}