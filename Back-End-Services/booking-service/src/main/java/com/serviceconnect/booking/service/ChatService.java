package com.serviceconnect.booking.service;

import com.serviceconnect.booking.client.CatalogServiceClient;
import com.serviceconnect.booking.client.ProviderServiceClient;
import com.serviceconnect.booking.client.UserServiceClient;
import com.serviceconnect.booking.dto.request.CreateQuoteRequest;
import com.serviceconnect.booking.dto.response.ConversationResponse;
import com.serviceconnect.booking.dto.response.MessageResponse;
import com.serviceconnect.booking.dto.response.QuoteResponse;
import com.serviceconnect.booking.entity.Conversation;
import com.serviceconnect.booking.entity.Message;
import com.serviceconnect.booking.entity.Quote;
import com.serviceconnect.booking.repository.ConversationRepository;
import com.serviceconnect.booking.repository.MessageRepository;
import com.serviceconnect.booking.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final QuoteRepository quoteRepository;
    private final ChatSafetyService chatSafetyService;
    private final ProviderServiceClient providerServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final UserServiceClient userServiceClient;

    // ============================================================
    // CONVERSATIONS
    // ============================================================

    @Transactional
    public ConversationResponse createOrGetConversation(
            Long customerId,
            Long providerId,
            Long catalogItemId,
            String authorizationHeader
    ) {
        // Validate provider
        boolean validProvider = providerServiceClient.validateApprovedProvider(providerId, authorizationHeader);
        if (!validProvider) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provider is not approved");
        }

        Conversation conversation;
        if (catalogItemId != null) {
            conversation = conversationRepository
                    .findByCustomerIdAndProviderIdAndCatalogItemId(customerId, providerId, catalogItemId)
                    .orElse(null);
        } else {
            conversation = conversationRepository
                    .findFirstByCustomerIdAndProviderIdOrderByUpdatedAtDesc(customerId, providerId)
                    .orElse(null);
        }

        if (conversation == null) {
            conversation = new Conversation();
            conversation.setCustomerId(customerId);
            conversation.setProviderId(providerId);
            conversation.setCatalogItemId(catalogItemId);
            conversation.setStatus("ACTIVE");
            conversation.setCreatedAt(OffsetDateTime.now());
            conversation.setUpdatedAt(OffsetDateTime.now());
            conversation = conversationRepository.save(conversation);
        }

        return toConversationResponse(conversation, customerId, "CUSTOMER", authorizationHeader);
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getCustomerConversations(Long customerId, String authorizationHeader) {
        List<Conversation> list = conversationRepository.findByCustomerIdOrderByUpdatedAtDesc(customerId);
        return list.stream()
                .map(c -> toConversationResponse(c, customerId, "CUSTOMER", authorizationHeader))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getProviderConversations(Long providerUserId, String authorizationHeader) {
        Long providerId = providerServiceClient.getProviderIdByUserId(providerUserId);
        List<Conversation> list = conversationRepository.findByProviderIdOrderByUpdatedAtDesc(providerId);
        return list.stream()
                .map(c -> toConversationResponse(c, providerId, "PROVIDER", authorizationHeader))
                .collect(Collectors.toList());
    }

    @Transactional
    public ConversationResponse getConversationDetails(
            Long conversationId,
            Long authenticatedUserId,
            String role,
            String authorizationHeader
    ) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        validateAccess(conversation, authenticatedUserId, role);

        // Mark messages as read
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        for (Message msg : messages) {
            if (!msg.isReadStatus() && !msg.getSenderId().equals(authenticatedUserId)) {
                msg.setReadStatus(true);
                messageRepository.save(msg);
            }
        }

        return toConversationResponse(conversation, authenticatedUserId, role, authorizationHeader);
    }

    // ============================================================
    // MESSAGES
    // ============================================================

    @Transactional
    public MessageResponse sendMessage(
            Long conversationId,
            Long authenticatedUserId,
            String role,
            String rawMessage
    ) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be empty");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        validateAccess(conversation, authenticatedUserId, role);

        // Authoritative server-side sanitization before database persistence
        String sanitizedMessage = chatSafetyService.sanitize(rawMessage.trim());

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(authenticatedUserId);
        message.setSenderRole(role);
        message.setMessage(sanitizedMessage);
        message.setReadStatus(false);
        message.setCreatedAt(OffsetDateTime.now());

        message = messageRepository.save(message);

        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        return toMessageResponse(message);
    }

    // ============================================================
    // QUOTES
    // ============================================================

    @Transactional
    public QuoteResponse createQuote(
            Long conversationId,
            Long providerUserId,
            CreateQuoteRequest request
    ) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        Long providerId = providerServiceClient.getProviderIdByUserId(providerUserId);
        if (!conversation.getProviderId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the designated provider can create quotes for this conversation");
        }

        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote amount must be greater than zero");
        }

        // Authoritative server-side sanitization of quote text fields
        String sanitizedDescription = chatSafetyService.sanitize(request.description().trim());
        String sanitizedNote = request.note() != null ? chatSafetyService.sanitize(request.note().trim()) : null;

        Quote quote = new Quote();
        quote.setConversationId(conversationId);
        quote.setCustomerId(conversation.getCustomerId());
        quote.setProviderId(providerId);
        quote.setCatalogItemId(request.catalogItemId() != null ? request.catalogItemId() : conversation.getCatalogItemId());
        quote.setServiceName(request.serviceName());
        quote.setDescription(sanitizedDescription);
        quote.setNote(sanitizedNote);
        quote.setAmount(request.amount());
        quote.setCurrency("INR");
        quote.setStatus("PENDING");
        quote.setCreatedAt(OffsetDateTime.now());
        quote.setUpdatedAt(OffsetDateTime.now());
        if (request.expiresAt() != null) {
            quote.setExpiresAt(request.expiresAt());
        }

        quote = quoteRepository.save(quote);

        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        return toQuoteResponse(quote);
    }

    @Transactional
    public QuoteResponse acceptQuote(Long quoteId, Long customerId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found"));

        if (!quote.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only accept quotes addressed to you");
        }

        if (quote.getExpiresAt() != null && quote.getExpiresAt().isBefore(OffsetDateTime.now())) {
            quote.setStatus("EXPIRED");
            quote.setUpdatedAt(OffsetDateTime.now());
            quoteRepository.save(quote);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote has expired");
        }

        if (!"PENDING".equalsIgnoreCase(quote.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote is not pending (current status: " + quote.getStatus() + ")");
        }

        quote.setStatus("ACCEPTED");
        quote.setUpdatedAt(OffsetDateTime.now());
        quote = quoteRepository.save(quote);

        return toQuoteResponse(quote);
    }

    @Transactional
    public QuoteResponse declineQuote(Long quoteId, Long customerId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found"));

        if (!quote.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only decline quotes addressed to you");
        }

        if (quote.getExpiresAt() != null && quote.getExpiresAt().isBefore(OffsetDateTime.now())) {
            quote.setStatus("EXPIRED");
            quote.setUpdatedAt(OffsetDateTime.now());
            quoteRepository.save(quote);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote has expired");
        }

        if (!"PENDING".equalsIgnoreCase(quote.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote is not pending (current status: " + quote.getStatus() + ")");
        }

        quote.setStatus("DECLINED");
        quote.setUpdatedAt(OffsetDateTime.now());
        quote = quoteRepository.save(quote);

        return toQuoteResponse(quote);
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> getConversationQuotes(Long conversationId, Long authenticatedUserId, String role) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        validateAccess(conversation, authenticatedUserId, role);

        return quoteRepository.findByConversationIdOrderByCreatedAtDesc(conversationId)
                .stream().map(this::toQuoteResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Quote getQuoteForBooking(Long quoteId, Long customerId, Long providerId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found"));

        if (!quote.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote does not belong to the current customer");
        }

        if (!quote.getProviderId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote does not belong to the selected provider");
        }

        if (!"ACCEPTED".equalsIgnoreCase(quote.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote must be ACCEPTED before booking can be confirmed");
        }

        if (quote.getExpiresAt() != null && quote.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quote has expired");
        }

        return quote;
    }

    // ============================================================
    // HELPERS & RBAC
    // ============================================================

    private void validateAccess(Conversation conversation, Long authenticatedUserId, String role) {
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            if (!conversation.getCustomerId().equals(authenticatedUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this conversation");
            }
        } else if ("PROVIDER".equalsIgnoreCase(role)) {
            Long providerId = providerServiceClient.getProviderIdByUserId(authenticatedUserId);
            if (!conversation.getProviderId().equals(providerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this conversation");
            }
        } else if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private ConversationResponse toConversationResponse(
            Conversation c,
            Long viewerId,
            String viewerRole,
            String authHeader
    ) {
        List<MessageResponse> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(c.getId())
                .stream().map(this::toMessageResponse).collect(Collectors.toList());

        List<QuoteResponse> quotes = quoteRepository.findByConversationIdOrderByCreatedAtDesc(c.getId())
                .stream().map(this::toQuoteResponse).collect(Collectors.toList());

        Message lastMsg = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(c.getId()).orElse(null);

        String providerName = providerServiceClient.getProviderBusinessName(c.getProviderId(), authHeader);
        String customerName = userServiceClient.getUserFullName(c.getCustomerId(), authHeader);

        String otherPartyName;
        if ("PROVIDER".equalsIgnoreCase(viewerRole)) {
            otherPartyName = (customerName != null && !customerName.isBlank()) ? customerName : "Customer";
        } else {
            otherPartyName = (providerName != null && !providerName.isBlank()) ? providerName : "Provider";
        }

        String serviceName = null;
        if (c.getCatalogItemId() != null && authHeader != null) {
            try {
                var item = catalogServiceClient.getCatalogItem(c.getCatalogItemId(), authHeader);
                if (item != null) serviceName = item.name();
            } catch (Exception ignored) {}
        }

        return new ConversationResponse(
                c.getId(),
                c.getCustomerId(),
                c.getProviderId(),
                c.getCatalogItemId(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                lastMsg != null ? lastMsg.getMessage() : null,
                lastMsg != null ? lastMsg.getCreatedAt() : c.getUpdatedAt(),
                otherPartyName,
                serviceName,
                messages,
                quotes,
                customerName,
                providerName
        );
    }

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversationId(),
                m.getSenderId(),
                m.getSenderRole(),
                m.getMessage(),
                m.isReadStatus(),
                m.getCreatedAt()
        );
    }

    private QuoteResponse toQuoteResponse(Quote q) {
        String status = q.getStatus();
        if ("PENDING".equalsIgnoreCase(status) && q.getExpiresAt() != null && q.getExpiresAt().isBefore(OffsetDateTime.now())) {
            status = "EXPIRED";
        }
        return new QuoteResponse(
                q.getId(),
                q.getConversationId(),
                q.getCustomerId(),
                q.getProviderId(),
                q.getCatalogItemId(),
                q.getServiceName(),
                q.getDescription(),
                q.getNote(),
                q.getAmount(),
                q.getCurrency(),
                status,
                q.getCreatedAt(),
                q.getUpdatedAt(),
                q.getExpiresAt()
        );
    }
}
