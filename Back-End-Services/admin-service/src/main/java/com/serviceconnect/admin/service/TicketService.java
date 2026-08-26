package com.serviceconnect.admin.service;

import com.serviceconnect.admin.dto.request.AssignTicketRequest;
import com.serviceconnect.admin.dto.request.CreateTicketRequest;
import com.serviceconnect.admin.dto.request.UpdateTicketRequest;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.dto.response.TicketResponse;
import com.serviceconnect.admin.entity.SupportTicket;
import com.serviceconnect.admin.entity.TicketStatus;
import com.serviceconnect.admin.mapper.AdminMapper;
import com.serviceconnect.admin.repository.SupportTicketRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketService {

    private final AuditLogService auditLogService;
    private final SupportTicketRepository ticketRepository;
    private final AdminMapper adminMapper;


    // ============================================================
    // CUSTOMER
    // ============================================================

    /**
     * CUSTOMER creates a new support ticket.
     */
    public TicketResponse createTicket(
            Long customerId,
            CreateTicketRequest request,
            HttpServletRequest httpRequest) {

        SupportTicket ticket =
                adminMapper.toTicketEntity(request);

        ticket.setCustomerId(customerId);
        ticket.setTicketNumber(generateTicketNumber());
        ticket.setStatus(TicketStatus.OPEN);

        OffsetDateTime now = OffsetDateTime.now();

        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        SupportTicket savedTicket =
                ticketRepository.save(ticket);

        auditLogService.log(
                customerId,
                "CUSTOMER",
                "TICKET_CREATED",
                "TICKET",
                savedTicket.getId().toString(),
                "Customer created support ticket "
                        + savedTicket.getTicketNumber(),
                httpRequest
        );

        return adminMapper.toTicketResponse(savedTicket);
    }


    /**
     * CUSTOMER can retrieve only their own ticket.
     */
    @Transactional(readOnly = true)
    public TicketResponse getCustomerTicket(
            Long customerId,
            Long ticketId) {

        SupportTicket ticket =
                ticketRepository.findByIdAndCustomerId(
                        ticketId,
                        customerId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );

        return adminMapper.toTicketResponse(ticket);
    }


    /**
     * CUSTOMER ticket listing.
     */
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getCustomerTickets(
            Long customerId,
            Pageable pageable) {

        Page<SupportTicket> page =
                ticketRepository.findByCustomerId(
                        customerId,
                        pageable
                );

        return toPageResponse(page);
    }


    // ============================================================
    // SUPPORT AGENT
    // ============================================================

    /**
     * SUPPORT_AGENT can retrieve only tickets assigned to them.
     */
    @Transactional(readOnly = true)
    public TicketResponse getAgentTicket(
            Long agentId,
            Long ticketId) {

        SupportTicket ticket =
                ticketRepository.findByIdAndAssignedAgentId(
                        ticketId,
                        agentId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );

        return adminMapper.toTicketResponse(ticket);
    }


    /**
     * SUPPORT_AGENT updates only an assigned ticket.
     */
    public TicketResponse updateAgentTicket(
            Long agentId,
            Long ticketId,
            UpdateTicketRequest request,
            HttpServletRequest httpRequest) {

        SupportTicket ticket =
                ticketRepository.findByIdAndAssignedAgentId(
                        ticketId,
                        agentId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );

        TicketStatus previousStatus =
                ticket.getStatus();

        applyAgentUpdates(
                ticket,
                request
        );

        ticket.setUpdatedAt(
                OffsetDateTime.now()
        );

        SupportTicket updated =
                saveWithOptimisticLock(ticket);

        auditLogService.log(
                agentId,
                "SUPPORT_AGENT",
                "TICKET_UPDATED",
                "TICKET",
                updated.getId().toString(),
                "Support agent updated ticket "
                        + updated.getTicketNumber(),
                httpRequest
        );

        auditStatusChange(
                agentId,
                "SUPPORT_AGENT",
                updated,
                previousStatus,
                httpRequest
        );

        return adminMapper.toTicketResponse(updated);
    }


    /**
     * SUPPORT_AGENT ticket listing.
     */
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getAgentTickets(
            Long agentId,
            Pageable pageable) {

        Page<SupportTicket> page =
                ticketRepository.findByAssignedAgentId(
                        agentId,
                        pageable
                );

        return toPageResponse(page);
    }


    // ============================================================
    // ADMIN
    // ============================================================

    /**
     * ADMIN can retrieve any ticket.
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketForAdmin(
            Long ticketId) {

        SupportTicket ticket =
                findTicket(ticketId);

        return adminMapper.toTicketResponse(ticket);
    }


    /**
     * ADMIN updates any ticket.
     */
    public TicketResponse updateTicket(
            Long adminId,
            Long ticketId,
            UpdateTicketRequest request,
            HttpServletRequest httpRequest) {

        SupportTicket ticket =
                findTicket(ticketId);

        TicketStatus previousStatus =
                ticket.getStatus();

        applyAdminUpdates(
                ticket,
                request
        );

        ticket.setUpdatedAt(
                OffsetDateTime.now()
        );

        SupportTicket updated =
                saveWithOptimisticLock(ticket);

        auditLogService.log(
                adminId,
                "ADMIN",
                "TICKET_UPDATED",
                "TICKET",
                updated.getId().toString(),
                "Admin updated ticket "
                        + updated.getTicketNumber(),
                httpRequest
        );

        auditStatusChange(
                adminId,
                "ADMIN",
                updated,
                previousStatus,
                httpRequest
        );

        return adminMapper.toTicketResponse(updated);
    }


    /**
     * ADMIN assigns or reassigns a ticket.
     */
    public TicketResponse assignTicket(
            Long adminId,
            Long ticketId,
            AssignTicketRequest request,
            HttpServletRequest httpRequest) {

        SupportTicket ticket =
                findTicket(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new IllegalStateException(
                    "Closed tickets cannot be assigned"
            );
        }

        if (request.agentId() == null) {
            throw new IllegalArgumentException(
                    "Agent ID is required"
            );
        }

        Long previousAgentId =
                ticket.getAssignedAgentId();

        ticket.setAssignedAgentId(
                request.agentId()
        );

        ticket.setUpdatedAt(
                OffsetDateTime.now()
        );

        SupportTicket updated =
                saveWithOptimisticLock(ticket);

        String description;

        if (previousAgentId == null) {

            description =
                    "Admin assigned ticket "
                            + updated.getTicketNumber()
                            + " to support agent "
                            + request.agentId();

        } else if (!previousAgentId.equals(request.agentId())) {

            description =
                    "Admin reassigned ticket "
                            + updated.getTicketNumber()
                            + " from support agent "
                            + previousAgentId
                            + " to support agent "
                            + request.agentId();

        } else {

            description =
                    "Admin reassigned ticket "
                            + updated.getTicketNumber()
                            + " to the same support agent "
                            + request.agentId();
        }

        auditLogService.log(
                adminId,
                "ADMIN",
                "TICKET_ASSIGNED",
                "TICKET",
                updated.getId().toString(),
                description,
                httpRequest
        );

        return adminMapper.toTicketResponse(updated);
    }


    /**
     * ADMIN ticket listing.
     */
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getAllTickets(
            Pageable pageable) {

        Page<SupportTicket> page =
                ticketRepository.findAll(pageable);

        return toPageResponse(page);
    }


    /**
     * ADMIN ticket filtering by status.
     */
    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> getTicketsByStatus(
            TicketStatus status,
            Pageable pageable) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Ticket status is required"
            );
        }

        Page<SupportTicket> page =
                ticketRepository.findByStatus(
                        status,
                        pageable
                );

        return toPageResponse(page);
    }


    // ============================================================
    // UPDATE LOGIC
    // ============================================================

    /**
     * ADMIN has full ticket update permissions.
     */
    private void applyAdminUpdates(
            SupportTicket ticket,
            UpdateTicketRequest request) {

        if (request.subject() != null) {
            ticket.setSubject(
                    request.subject()
            );
        }

        if (request.category() != null) {
            ticket.setCategory(
                    request.category()
            );
        }

        if (request.priority() != null) {
            ticket.setPriority(
                    request.priority()
            );
        }

        if (request.status() != null) {
            updateStatus(
                    ticket,
                    request.status()
            );
        }
    }


    /**
     * SUPPORT_AGENT has restricted update permissions.
     */
    private void applyAgentUpdates(
            SupportTicket ticket,
            UpdateTicketRequest request) {

        if (request.subject() != null) {
            ticket.setSubject(
                    request.subject()
            );
        }

        if (request.category() != null) {
            ticket.setCategory(
                    request.category()
            );
        }

        if (request.priority() != null) {
            ticket.setPriority(
                    request.priority()
            );
        }

        if (request.status() != null) {
            updateStatus(
                    ticket,
                    request.status()
            );
        }
    }


    /**
     * Centralized ticket status update logic.
     */
    private void updateStatus(
            SupportTicket ticket,
            TicketStatus nextStatus) {

        TicketStatus currentStatus =
                ticket.getStatus();

        validateStatusTransition(
                currentStatus,
                nextStatus
        );

        ticket.setStatus(nextStatus);

        if (nextStatus == TicketStatus.RESOLVED
                || nextStatus == TicketStatus.CLOSED) {

            if (ticket.getResolvedAt() == null) {

                ticket.setResolvedAt(
                        OffsetDateTime.now()
                );
            }
        }
    }


    // ============================================================
    // STATUS AUDITING
    // ============================================================

    private void auditStatusChange(
            Long actorId,
            String actorRole,
            SupportTicket ticket,
            TicketStatus previousStatus,
            HttpServletRequest httpRequest) {

        TicketStatus currentStatus =
                ticket.getStatus();

        if (previousStatus == null
                || currentStatus == null
                || previousStatus == currentStatus) {

            return;
        }

        auditLogService.log(
                actorId,
                actorRole,
                "TICKET_STATUS_CHANGED",
                "TICKET",
                ticket.getId().toString(),
                "Ticket "
                        + ticket.getTicketNumber()
                        + " status changed from "
                        + previousStatus
                        + " to "
                        + currentStatus,
                httpRequest
        );
    }


    // ============================================================
    // STATUS LIFECYCLE
    // ============================================================

    /**
     * Valid ticket lifecycle:
     *
     * OPEN
     *   ↓
     * IN_PROGRESS
     *   ↓
     * RESOLVED
     *   ↓
     * CLOSED
     *
     * A CLOSED ticket can never be reopened.
     */
    private void validateStatusTransition(
            TicketStatus current,
            TicketStatus next) {

        if (current == null) {
            throw new IllegalStateException(
                    "Ticket has no current status"
            );
        }

        if (next == null) {
            throw new IllegalArgumentException(
                    "Ticket status is required"
            );
        }

        if (current == next) {
            return;
        }

        if (current == TicketStatus.CLOSED) {
            throw new IllegalStateException(
                    "Closed tickets cannot be modified"
            );
        }

        if (current == TicketStatus.RESOLVED
                && next != TicketStatus.CLOSED) {

            throw new IllegalStateException(
                    "Resolved ticket can only be closed"
            );
        }

        if (current == TicketStatus.OPEN
                && next == TicketStatus.CLOSED) {

            throw new IllegalStateException(
                    "Open ticket must be resolved before closing"
            );
        }
    }


    // ============================================================
    // DATABASE
    // ============================================================

    private SupportTicket findTicket(
            Long ticketId) {

        if (ticketId == null) {
            throw new IllegalArgumentException(
                    "Ticket ID is required"
            );
        }

        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Ticket not found"
                        )
                );
    }


    /**
     * Handles concurrent ticket modifications.
     *
     * @Version on SupportTicket protects against lost updates.
     */
    private SupportTicket saveWithOptimisticLock(
            SupportTicket ticket) {

        try {

            return ticketRepository.save(ticket);

        } catch (OptimisticLockingFailureException exception) {

            throw new IllegalStateException(
                    "Ticket was modified by another request. "
                            + "Please refresh and try again."
            );
        }
    }


    // ============================================================
    // PAGINATION
    // ============================================================

    private PageResponse<TicketResponse> toPageResponse(
            Page<SupportTicket> page) {

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(adminMapper::toTicketResponse)
                        .toList(),

                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }


    // ============================================================
    // TICKET NUMBER
    // ============================================================

    private String generateTicketNumber() {

        return "SC-"
                + OffsetDateTime.now()
                .toLocalDate()
                .toString()
                .replace("-", "")
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}