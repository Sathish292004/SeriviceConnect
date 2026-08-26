package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.dto.request.AssignTicketRequest;
import com.serviceconnect.admin.dto.request.CreateTicketRequest;
import com.serviceconnect.admin.dto.request.UpdateTicketRequest;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.dto.response.TicketResponse;
import com.serviceconnect.admin.entity.TicketStatus;
import com.serviceconnect.admin.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
public class TicketController {

    private final TicketService ticketService;


    // ============================================================
    // CUSTOMER
    // ============================================================

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TicketResponse> createTicket(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateTicketRequest request) {

        Long customerId =
                currentUserId(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ticketService.createTicket(
                                customerId,
                                request,
                                httpRequest
                        )
                );
    }


    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PageResponse<TicketResponse>>
    getCustomerTickets(
            Authentication authentication,
            Pageable pageable) {

        Long customerId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.getCustomerTickets(
                        customerId,
                        pageable
                )
        );
    }


    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TicketResponse> getCustomerTicket(
            Authentication authentication,
            @PathVariable
            @Positive
            Long ticketId) {

        Long customerId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.getCustomerTicket(
                        customerId,
                        ticketId
                )
        );
    }


    // ============================================================
    // SUPPORT AGENT
    // ============================================================

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<PageResponse<TicketResponse>>
    getAgentTickets(
            Authentication authentication,
            Pageable pageable) {

        Long agentId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.getAgentTickets(
                        agentId,
                        pageable
                )
        );
    }


    @GetMapping("/{ticketId}/assigned")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<TicketResponse> getAgentTicket(
            Authentication authentication,
            @PathVariable
            @Positive
            Long ticketId) {

        Long agentId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.getAgentTicket(
                        agentId,
                        ticketId
                )
        );
    }


    @PatchMapping("/{ticketId}/assigned")
    @PreAuthorize("hasRole('SUPPORT_AGENT')")
    public ResponseEntity<TicketResponse> updateAgentTicket(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @PathVariable
            @Positive
            Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {

        Long agentId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.updateAgentTicket(
                        agentId,
                        ticketId,
                        request,
                        httpRequest
                )
        );
    }


    // ============================================================
    // ADMIN
    // ============================================================

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TicketResponse>>
    getAllTickets(
            Pageable pageable) {

        return ResponseEntity.ok(
                ticketService.getAllTickets(
                        pageable
                )
        );
    }


    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TicketResponse>>
    getTicketsByStatus(
            @PathVariable TicketStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(
                ticketService.getTicketsByStatus(
                        status,
                        pageable
                )
        );
    }


    @GetMapping("/admin/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> getTicketForAdmin(
            @PathVariable
            @Positive
            Long ticketId) {

        return ResponseEntity.ok(
                ticketService.getTicketForAdmin(
                        ticketId
                )
        );
    }


    @PatchMapping("/admin/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> updateTicket(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @PathVariable
            @Positive
            Long ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {

        Long adminId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.updateTicket(
                        adminId,
                        ticketId,
                        request,
                        httpRequest
                )
        );
    }


    @PatchMapping("/admin/{ticketId}/assignment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignTicket(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @PathVariable
            @Positive
            Long ticketId,
            @Valid @RequestBody AssignTicketRequest request) {

        Long adminId =
                currentUserId(authentication);

        return ResponseEntity.ok(
                ticketService.assignTicket(
                        adminId,
                        ticketId,
                        request,
                        httpRequest
                )
        );
    }


    // ============================================================
    // SECURITY CONTEXT
    // ============================================================

    private Long currentUserId(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        Object details =
                authentication.getDetails();

        if (!(details instanceof Long userId)
                || userId <= 0) {

            throw new IllegalStateException(
                    "Authenticated user ID is invalid"
            );
        }

        return userId;
    }
}