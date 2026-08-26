package com.serviceconnect.admin.controller;

import com.serviceconnect.admin.dto.response.AuditLogResponse;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.service.AuditLogService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;


    // ============================================================
    // ALL AUDIT LOGS
    // ============================================================

    @GetMapping
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getAllAuditLogs(
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getAllAuditLogs(
                        pageable
                )
        );
    }


    // ============================================================
    // FILTER BY ACTOR
    // ============================================================

    @GetMapping("/actor/{actorId}")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getLogsByActor(
            @PathVariable
            @Positive
            Long actorId,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getLogsByActor(
                        actorId,
                        pageable
                )
        );
    }


    // ============================================================
    // FILTER BY ACTOR ROLE
    // ============================================================

    @GetMapping("/role/{actorRole}")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getLogsByActorRole(
            @PathVariable String actorRole,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getLogsByActorRole(
                        actorRole,
                        pageable
                )
        );
    }


    // ============================================================
    // FILTER BY ACTION
    // ============================================================

    @GetMapping("/action/{action}")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getLogsByAction(
            @PathVariable String action,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getLogsByAction(
                        action,
                        pageable
                )
        );
    }


    // ============================================================
    // FILTER BY RESOURCE TYPE
    // ============================================================

    @GetMapping("/resource/{resourceType}")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getLogsByResourceType(
            @PathVariable String resourceType,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getLogsByResourceType(
                        resourceType,
                        pageable
                )
        );
    }


    // ============================================================
    // RESOURCE HISTORY
    // ============================================================

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<PageResponse<AuditLogResponse>>
    getResourceHistory(
            @PathVariable String resourceType,
            @PathVariable String resourceId,
            Pageable pageable) {

        return ResponseEntity.ok(
                auditLogService.getResourceHistory(
                        resourceType,
                        resourceId,
                        pageable
                )
        );
    }
}