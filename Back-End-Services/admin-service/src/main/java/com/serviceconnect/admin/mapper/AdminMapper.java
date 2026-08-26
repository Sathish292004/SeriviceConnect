package com.serviceconnect.admin.mapper;

import com.serviceconnect.admin.dto.request.CreateSupportMessageRequest;
import com.serviceconnect.admin.dto.request.CreateTicketRequest;
import com.serviceconnect.admin.dto.response.SupportMessageResponse;
import com.serviceconnect.admin.dto.response.TicketResponse;
import com.serviceconnect.admin.entity.SupportMessage;
import com.serviceconnect.admin.entity.SupportTicket;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    // ============================================================
    // SUPPORT TICKET
    // ============================================================

    public SupportTicket toTicketEntity(
            CreateTicketRequest request) {

        SupportTicket ticket = new SupportTicket();

        ticket.setSubject(request.subject());
        ticket.setDescription(request.description());
        ticket.setCategory(request.category());
        ticket.setPriority(request.priority());

        return ticket;
    }

    public TicketResponse toTicketResponse(
            SupportTicket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getCustomerId(),
                ticket.getAssignedAgentId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getVersion()
        );
    }


    // ============================================================
    // SUPPORT MESSAGE
    // ============================================================

    public SupportMessage toSupportMessageEntity(
            CreateSupportMessageRequest request) {

        SupportMessage message = new SupportMessage();

        message.setMessage(request.message());

        return message;
    }

    public SupportMessageResponse toSupportMessageResponse(
            SupportMessage message) {

        return new SupportMessageResponse(
                message.getId(),
                message.getTicket().getId(),
                message.getSenderId(),
                message.getSenderType(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}