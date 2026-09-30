package com.serviceconnect.booking.controller;

import com.serviceconnect.booking.dto.request.CreateNotificationRequest;
import com.serviceconnect.booking.dto.request.PushSubscriptionRequest;
import com.serviceconnect.booking.dto.response.NotificationResponse;
import com.serviceconnect.booking.dto.response.UnreadCountResponse;
import com.serviceconnect.booking.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/notifications", "/api/v1/bookings/notifications"})
@RequiredArgsConstructor
@Validated
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // ============================================================
    // LIST NOTIFICATIONS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long authenticatedUserId = getUserId(jwt);
        return ResponseEntity.ok(notificationService.getNotifications(authenticatedUserId, page, size));
    }

    // ============================================================
    // UNREAD COUNT
    // ============================================================

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long authenticatedUserId = getUserId(jwt);
        return ResponseEntity.ok(notificationService.getUnreadCount(authenticatedUserId));
    }

    // ============================================================
    // MARK AS READ
    // ============================================================

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long authenticatedUserId = getUserId(jwt);
        return ResponseEntity.ok(notificationService.markAsRead(id, authenticatedUserId));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long authenticatedUserId = getUserId(jwt);
        int updated = notificationService.markAllAsRead(authenticatedUserId);
        return ResponseEntity.ok(Map.of("success", true, "updatedCount", updated));
    }

    // ============================================================
    // PUSH SUBSCRIPTIONS
    // ============================================================

    @PostMapping("/push-subscriptions")
    public ResponseEntity<Map<String, Object>> registerPushSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PushSubscriptionRequest request
    ) {
        Long authenticatedUserId = getUserId(jwt);
        notificationService.registerPushSubscription(authenticatedUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true));
    }

    @DeleteMapping("/push-subscriptions")
    public ResponseEntity<Map<String, Object>> removePushSubscription(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String endpoint
    ) {
        Long authenticatedUserId = getUserId(jwt);
        notificationService.removePushSubscription(authenticatedUserId, endpoint);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ============================================================
    // EMIT NOTIFICATION EVENT (AUTHORITATIVE)
    // ============================================================

    @PostMapping("/events")
    public ResponseEntity<NotificationResponse> emitNotificationEvent(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        // Enforce RBAC / authentication
        Long callerId = getUserId(jwt);
        log.info("Notification event requested by callerId={}: type={}, recipient={}",
                callerId, request.type(), request.recipientUserId());
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // JWT HELPER
    // ============================================================

    private Long getUserId(Jwt jwt) {
        if (jwt == null) {
            throw new AccessDeniedException("Authentication required");
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("User ID not found in JWT");
        }
        return Long.parseLong(subject);
    }
}
