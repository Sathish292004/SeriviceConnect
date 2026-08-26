package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.dto.request.CreateSupportMessageRequest;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.dto.response.SupportMessageResponse;
import com.serviceconnect.admin.service.SupportMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class SupportMessageController {

    private final SupportMessageService supportMessageService;


    // ============================================================
    // CUSTOMER
    // ============================================================

    @PostMapping("/{ticketId}/messages")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SupportMessageResponse> sendCustomerMessage(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateSupportMessageRequest request) {

        Long customerId = currentUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        supportMessageService.sendCustomerMessage(
                                customerId,
                                ticketId,
                                request
                        )
                );
    }


    @GetMapping("/{ticketId}/messages")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PageResponse<SupportMessageResponse>>
    getCustomerMessages(
            Authentication authentication,
            @PathVariable Long ticketId,
            Pageable pageable) {

        Long customerId = currentUserId(authentication);

        return ResponseEntity.ok(
                supportMessageService.getCustomerMessages(
                        customerId,
                        ticketId,
                        pageable
                )
        );
    }


    // ============================================================
    // SUPPORT AGENT
    // ============================================================

    @PostMapping("/{ticketId}/agent/messages")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<SupportMessageResponse> sendAgentMessage(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateSupportMessageRequest request) {

        Long agentId = currentUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        supportMessageService.sendAgentMessage(
                                agentId,
                                ticketId,
                                request
                        )
                );
    }


    @GetMapping("/{ticketId}/agent/messages")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<PageResponse<SupportMessageResponse>>
    getAgentMessages(
            Authentication authentication,
            @PathVariable Long ticketId,
            Pageable pageable) {

        Long agentId = currentUserId(authentication);

        return ResponseEntity.ok(
                supportMessageService.getAgentMessages(
                        agentId,
                        ticketId,
                        pageable
                )
        );
    }


    // ============================================================
    // ADMIN
    // ============================================================

    @PostMapping("/{ticketId}/admin/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupportMessageResponse> sendAdminMessage(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateSupportMessageRequest request) {

        Long adminId = currentUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        supportMessageService.sendAdminMessage(
                                adminId,
                                ticketId,
                                request
                        )
                );
    }


    @GetMapping("/{ticketId}/admin/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<SupportMessageResponse>>
    getAdminMessages(
            @PathVariable Long ticketId,
            Pageable pageable) {

        return ResponseEntity.ok(
                supportMessageService.getAdminMessages(
                        ticketId,
                        pageable
                )
        );
    }


    // ============================================================
    // SECURITY CONTEXT
    // ============================================================

    private Long currentUserId(
            Authentication authentication) {

        Object details = authentication.getDetails();

        if (!(details instanceof Long userId)) {
            throw new IllegalStateException(
                    "Authenticated user ID is missing"
            );
        }

        return userId;
    }
}