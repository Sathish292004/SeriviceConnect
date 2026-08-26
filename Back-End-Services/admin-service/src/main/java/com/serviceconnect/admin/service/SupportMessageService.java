package com.serviceconnect.admin.service;

import com.serviceconnect.admin.dto.request.CreateSupportMessageRequest;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.dto.response.SupportMessageResponse;
import com.serviceconnect.admin.entity.SenderType;
import com.serviceconnect.admin.entity.SupportMessage;
import com.serviceconnect.admin.entity.SupportTicket;
import com.serviceconnect.admin.entity.TicketStatus;
import com.serviceconnect.admin.mapper.AdminMapper;
import com.serviceconnect.admin.repository.SupportMessageRepository;
import com.serviceconnect.admin.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportMessageService {

    private final SupportMessageRepository messageRepository;
    private final SupportTicketRepository ticketRepository;
    private final AdminMapper adminMapper;

    /**
     * CUSTOMER sends a message to their own ticket.
     */
    public SupportMessageResponse sendCustomerMessage(
            Long customerId,
            Long ticketId,
            CreateSupportMessageRequest request) {

        SupportTicket ticket =
                ticketRepository.findByIdAndCustomerId(
                        ticketId,
                        customerId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );

        validateTicketCanReceiveMessage(ticket);

        SupportMessage message =
                createMessage(
                        ticket,
                        customerId,
                        SenderType.CUSTOMER,
                        request
                );

        return adminMapper.toSupportMessageResponse(
                messageRepository.save(message)
        );
    }

    /**
     * SUPPORT_AGENT sends a message only to an assigned ticket.
     */
    public SupportMessageResponse sendAgentMessage(
            Long agentId,
            Long ticketId,
            CreateSupportMessageRequest request) {

        SupportTicket ticket =
                ticketRepository.findByIdAndAssignedAgentId(
                        ticketId,
                        agentId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );

        validateTicketCanReceiveMessage(ticket);

        SupportMessage message =
                createMessage(
                        ticket,
                        agentId,
                        SenderType.SUPPORT_AGENT,
                        request
                );

        return adminMapper.toSupportMessageResponse(
                messageRepository.save(message)
        );
    }

    /**
     * ADMIN can send a message to any ticket.
     */
    public SupportMessageResponse sendAdminMessage(
            Long adminId,
            Long ticketId,
            CreateSupportMessageRequest request) {

        SupportTicket ticket =
                ticketRepository.findById(ticketId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Ticket not found"
                                )
                        );

        validateTicketCanReceiveMessage(ticket);

        SupportMessage message =
                createMessage(
                        ticket,
                        adminId,
                        SenderType.ADMIN,
                        request
                );

        return adminMapper.toSupportMessageResponse(
                messageRepository.save(message)
        );
    }

    /**
     * CUSTOMER can read only their own ticket messages.
     */
    @Transactional(readOnly = true)
    public PageResponse<SupportMessageResponse> getCustomerMessages(
            Long customerId,
            Long ticketId,
            Pageable pageable) {

        verifyCustomerTicket(
                customerId,
                ticketId
        );

        Page<SupportMessage> page =
                messageRepository.findByTicketIdOrderByCreatedAtAsc(
                        ticketId,
                        pageable
                );

        return toPageResponse(page);
    }

    /**
     * SUPPORT_AGENT can read messages only from assigned tickets.
     */
    @Transactional(readOnly = true)
    public PageResponse<SupportMessageResponse> getAgentMessages(
            Long agentId,
            Long ticketId,
            Pageable pageable) {

        verifyAgentTicket(
                agentId,
                ticketId
        );

        Page<SupportMessage> page =
                messageRepository.findByTicketIdOrderByCreatedAtAsc(
                        ticketId,
                        pageable
                );

        return toPageResponse(page);
    }

    /**
     * ADMIN can read any ticket conversation.
     */
    @Transactional(readOnly = true)
    public PageResponse<SupportMessageResponse> getAdminMessages(
            Long ticketId,
            Pageable pageable) {

        verifyTicket(ticketId);

        Page<SupportMessage> page =
                messageRepository.findByTicketIdOrderByCreatedAtAsc(
                        ticketId,
                        pageable
                );

        return toPageResponse(page);
    }

    private SupportMessage createMessage(
            SupportTicket ticket,
            Long senderId,
            SenderType senderType,
            CreateSupportMessageRequest request) {

        SupportMessage message =
                adminMapper.toSupportMessageEntity(request);

        message.setTicket(ticket);
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setCreatedAt(OffsetDateTime.now());

        return message;
    }

    private void validateTicketCanReceiveMessage(
            SupportTicket ticket) {

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalStateException(
                    "Closed tickets cannot receive new messages"
            );
        }
    }

    private void verifyCustomerTicket(
            Long customerId,
            Long ticketId) {

        ticketRepository.findByIdAndCustomerId(
                ticketId,
                customerId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Ticket not found"
                )
        );
    }

    private void verifyAgentTicket(
            Long agentId,
            Long ticketId) {

        ticketRepository.findByIdAndAssignedAgentId(
                ticketId,
                agentId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Ticket not found"
                )
        );
    }

    private void verifyTicket(Long ticketId) {

        ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );
    }

    private PageResponse<SupportMessageResponse> toPageResponse(
            Page<SupportMessage> page) {

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(adminMapper::toSupportMessageResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}