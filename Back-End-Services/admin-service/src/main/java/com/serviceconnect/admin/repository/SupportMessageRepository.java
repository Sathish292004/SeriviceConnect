package com.serviceconnect.admin.repository;

import com.serviceconnect.admin.entity.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository
        extends JpaRepository<SupportMessage, Long> {

    Page<SupportMessage> findByTicketIdOrderByCreatedAtAsc(
            Long ticketId,
            Pageable pageable
    );
}