package com.serviceconnect.admin.service;

import com.serviceconnect.admin.dto.response.AuditLogResponse;
import com.serviceconnect.admin.dto.response.PageResponse;
import com.serviceconnect.admin.entity.AuditLog;
import com.serviceconnect.admin.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;


    // ============================================================
    // CREATE AUDIT LOG
    // ============================================================

    /**
     * Creates an audit record inside the current transaction.
     * The audit record is committed together with the
     * business operation that generated it.
     */
    public void log(
            Long actorId,
            String actorRole,
            String action,
            String resourceType,
            String resourceId,
            String description,
            HttpServletRequest request) {

        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actorId);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setDescription(description);

        if (request != null) {

            auditLog.setIpAddress(
                    parseClientIp(request)
            );

            auditLog.setUserAgent(
                    request.getHeader("User-Agent")
            );
        }

        auditLog.setCreatedAt(
                OffsetDateTime.now()
        );

        auditLogRepository.save(auditLog);
    }


    // ============================================================
    // ADMIN READ OPERATIONS
    // ============================================================

    /**
     * ADMIN can retrieve all audit logs.
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAllAuditLogs(
            Pageable pageable) {

        Page<AuditLog> page =
                auditLogRepository.findAll(pageable);

        return toPageResponse(page);
    }


    /**
     * ADMIN can filter audit logs by actor ID.
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogsByActor(
            Long actorId,
            Pageable pageable) {

        Page<AuditLog> page =
                auditLogRepository.findByActorId(
                        actorId,
                        pageable
                );

        return toPageResponse(page);
    }


    /**
     * ADMIN can filter audit logs by actor role.
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogsByActorRole(
            String actorRole,
            Pageable pageable) {

        Page<AuditLog> page =
                auditLogRepository.findByActorRole(
                        actorRole,
                        pageable
                );

        return toPageResponse(page);
    }


    /**
     * ADMIN can filter audit logs by action.
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogsByAction(
            String action,
            Pageable pageable) {

        Page<AuditLog> page =
                auditLogRepository.findByAction(
                        action,
                        pageable
                );

        return toPageResponse(page);
    }


    /**
     * ADMIN can retrieve logs for a resource type.
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getLogsByResourceType(
            String resourceType,
            Pageable pageable) {

        Page<AuditLog> page =
                auditLogRepository.findByResourceType(
                        resourceType,
                        pageable
                );

        return toPageResponse(page);
    }


    /**
     * ADMIN can retrieve the complete audit history
     * of a specific resource.
     */
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getResourceHistory(
            String resourceType,
            String resourceId,
            Pageable pageable) {

        Page<AuditLog> page =
                auditLogRepository
                        .findByResourceTypeAndResourceId(
                                resourceType,
                                resourceId,
                                pageable
                        );

        return toPageResponse(page);
    }


    // ============================================================
    // MAPPING
    // ============================================================

    private PageResponse<AuditLogResponse> toPageResponse(
            Page<AuditLog> page) {

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),

                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }


    private AuditLogResponse toResponse(
            AuditLog auditLog) {

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorId(),
                auditLog.getActorRole(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getDescription(),

                // Convert InetAddress back to String for API response
                auditLog.getIpAddress() != null
                        ? auditLog.getIpAddress().getHostAddress()
                        : null,

                auditLog.getUserAgent(),
                auditLog.getCreatedAt()
        );
    }


    // ============================================================
    // CLIENT IP
    // ============================================================

    private InetAddress parseClientIp(
            HttpServletRequest request) {

        String ip = extractClientIp(request);

        try {

            return InetAddress.getByName(ip);

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid client IP address: " + ip,
                    e
            );
        }
    }


    private String extractClientIp(
            HttpServletRequest request) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        String realIp =
                request.getHeader("X-Real-IP");

        if (realIp != null
                && !realIp.isBlank()) {

            return realIp;
        }

        return request.getRemoteAddr();
    }
}