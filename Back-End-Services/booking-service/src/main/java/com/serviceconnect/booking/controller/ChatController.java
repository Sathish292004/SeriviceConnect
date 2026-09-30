package com.serviceconnect.booking.controller;

import com.serviceconnect.booking.dto.request.CreateConversationRequest;
import com.serviceconnect.booking.dto.request.CreateQuoteRequest;
import com.serviceconnect.booking.dto.request.SendMessageRequest;
import com.serviceconnect.booking.dto.response.ConversationResponse;
import com.serviceconnect.booking.dto.response.MessageResponse;
import com.serviceconnect.booking.dto.response.QuoteResponse;
import com.serviceconnect.booking.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Validated
public class ChatController {

    private final ChatService chatService;

    // ============================================================
    // CONVERSATIONS
    // ============================================================

    @PostMapping("/conversations")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ConversationResponse> createOrGetConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        Long customerId = getUserId(jwt);
        ConversationResponse response = chatService.createOrGetConversation(
                customerId,
                request.providerId(),
                request.catalogItemId(),
                authHeader
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        Long userId = getUserId(jwt);
        String role = getRole(jwt);

        List<ConversationResponse> list;
        if ("PROVIDER".equalsIgnoreCase(role)) {
            list = chatService.getProviderConversations(userId, authHeader);
        } else {
            list = chatService.getCustomerConversations(userId, authHeader);
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        Long userId = getUserId(jwt);
        String role = getRole(jwt);
        return ResponseEntity.ok(chatService.getConversationDetails(id, userId, role, authHeader));
    }

    // ============================================================
    // MESSAGES
    // ============================================================

    @PostMapping("/conversations/{id}/messages")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable @Positive Long id,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = getUserId(jwt);
        String role = getRole(jwt);
        MessageResponse response = chatService.sendMessage(id, userId, role, request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // QUOTES
    // ============================================================

    @PostMapping("/conversations/{id}/quotes")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<QuoteResponse> createQuote(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateQuoteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long providerUserId = getUserId(jwt);
        QuoteResponse response = chatService.createQuote(id, providerUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/conversations/{id}/quotes")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'PROVIDER')")
    public ResponseEntity<List<QuoteResponse>> getQuotes(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = getUserId(jwt);
        String role = getRole(jwt);
        return ResponseEntity.ok(chatService.getConversationQuotes(id, userId, role));
    }

    @PatchMapping("/quotes/{quoteId}/accept")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<QuoteResponse> acceptQuote(
            @PathVariable @Positive Long quoteId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = getUserId(jwt);
        return ResponseEntity.ok(chatService.acceptQuote(quoteId, customerId));
    }

    @PatchMapping("/quotes/{quoteId}/decline")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<QuoteResponse> declineQuote(
            @PathVariable @Positive Long quoteId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = getUserId(jwt);
        return ResponseEntity.ok(chatService.declineQuote(quoteId, customerId));
    }

    // ============================================================
    // JWT HELPERS
    // ============================================================

    private Long getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("User ID not found in JWT");
        }
        return Long.parseLong(subject);
    }

    private String getRole(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if (role == null || role.isBlank()) {
            throw new IllegalStateException("Role not found in JWT");
        }
        return role.trim().toUpperCase();
    }
}
