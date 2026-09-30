package com.serviceconnect.booking.repository;

import com.serviceconnect.booking.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByCustomerIdOrderByUpdatedAtDesc(Long customerId);

    List<Conversation> findByProviderIdOrderByUpdatedAtDesc(Long providerId);

    Optional<Conversation> findByCustomerIdAndProviderIdAndCatalogItemId(
            Long customerId,
            Long providerId,
            Long catalogItemId
    );

    Optional<Conversation> findFirstByCustomerIdAndProviderIdOrderByUpdatedAtDesc(
            Long customerId,
            Long providerId
    );
}
