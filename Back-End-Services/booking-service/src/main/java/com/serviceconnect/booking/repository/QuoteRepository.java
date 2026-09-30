package com.serviceconnect.booking.repository;

import com.serviceconnect.booking.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByConversationIdOrderByCreatedAtDesc(Long conversationId);

    List<Quote> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Quote> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    Optional<Quote> findByIdAndCustomerId(Long id, Long customerId);

    Optional<Quote> findByIdAndProviderId(Long id, Long providerId);
}
