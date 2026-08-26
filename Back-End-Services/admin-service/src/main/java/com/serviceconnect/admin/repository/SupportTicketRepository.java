package com.serviceconnect.admin.repository;

import com.serviceconnect.admin.entity.SupportTicket;
import com.serviceconnect.admin.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(
            String ticketNumber
    );

    boolean existsByTicketNumber(
            String ticketNumber
    );

    Page<SupportTicket> findByCustomerId(
            Long customerId,
            Pageable pageable
    );

    Page<SupportTicket> findByAssignedAgentId(
            Long assignedAgentId,
            Pageable pageable
    );

    Page<SupportTicket> findByStatus(
            TicketStatus status,
            Pageable pageable
    );

    Optional<SupportTicket> findByIdAndCustomerId(
            Long id,
            Long customerId
    );

    Optional<SupportTicket> findByIdAndAssignedAgentId(
            Long id,
            Long agentId
    );
}