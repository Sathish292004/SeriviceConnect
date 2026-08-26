package com.serviceconnect.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(

        @NotNull(message = "Agent ID is required")
        Long agentId
) {
}