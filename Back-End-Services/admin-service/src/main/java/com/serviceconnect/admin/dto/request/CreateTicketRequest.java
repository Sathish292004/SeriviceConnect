package com.serviceconnect.admin.dto.request;

import com.serviceconnect.admin.entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotBlank(message = "Subject is required")
        @Size(
                min = 5,
                max = 200,
                message = "Subject must be between 5 and 200 characters"
        )
        String subject,

        @NotBlank(message = "Description is required")
        @Size(
                min = 10,
                max = 5000,
                message = "Description must be between 10 and 5000 characters"
        )
        String description,

        @NotBlank(message = "Category is required")
        @Size(
                max = 50,
                message = "Category must not exceed 50 characters"
        )
        String category,

        @NotNull(message = "Priority is required")
        TicketPriority priority
) {
}