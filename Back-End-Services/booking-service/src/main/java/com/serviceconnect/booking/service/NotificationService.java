package com.serviceconnect.booking.service;

import com.serviceconnect.booking.dto.request.CreateNotificationRequest;
import com.serviceconnect.booking.dto.request.PushSubscriptionRequest;
import com.serviceconnect.booking.dto.response.NotificationResponse;
import com.serviceconnect.booking.dto.response.UnreadCountResponse;
import com.serviceconnect.booking.entity.Notification;
import com.serviceconnect.booking.entity.NotificationPreference;
import com.serviceconnect.booking.entity.NotificationType;
import com.serviceconnect.booking.entity.PushSubscription;
import com.serviceconnect.booking.repository.NotificationPreferenceRepository;
import com.serviceconnect.booking.repository.NotificationRepository;
import com.serviceconnect.booking.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final ChatSafetyService chatSafetyService;
    private final EmailNotificationService emailNotificationService;

    // ============================================================
    // QUERY NOTIFICATIONS
    // ============================================================

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long authenticatedUserId, int page, int size) {
        if (authenticatedUserId == null || authenticatedUserId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        int pageSize = (size <= 0 || size > 100) ? 20 : size;
        int pageIndex = Math.max(0, page);

        Page<Notification> paged = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(
                authenticatedUserId,
                PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return paged.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        long count = notificationRepository.countByRecipientUserIdAndReadFalse(authenticatedUserId);
        return new UnreadCountResponse(count);
    }

    // ============================================================
    // READ / UNREAD STATE
    // ============================================================

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        // Security check: isolate per user
        if (!notification.getRecipientUserId().equals(authenticatedUserId)) {
            log.warn("Unauthorized attempt to read notification {} by user {}", notificationId, authenticatedUserId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to notification");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Transactional
    public int markAllAsRead(Long authenticatedUserId) {
        if (authenticatedUserId == null || authenticatedUserId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }

        return notificationRepository.markAllAsRead(authenticatedUserId);
    }

    // ============================================================
    // CREATE NOTIFICATION (AUTHORITATIVE)
    // ============================================================

    @Transactional
    public NotificationResponse createNotification(
            Long recipientUserId,
            NotificationType type,
            String title,
            String message,
            String relatedEntityType,
            Long relatedEntityId,
            String deepLink,
            String idempotencyKey
    ) {
        return createNotification(new CreateNotificationRequest(
                recipientUserId,
                type,
                title,
                message,
                relatedEntityType,
                relatedEntityId,
                deepLink,
                idempotencyKey
        ));
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        if (request.recipientUserId() == null || request.recipientUserId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid recipient user ID is required");
        }
        if (request.type() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification type is required");
        }

        // Deduplication via idempotency key
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            Optional<Notification> existing = notificationRepository.findByIdempotencyKey(request.idempotencyKey().trim());
            if (existing.isPresent()) {
                log.info("Duplicate notification prevented by idempotency key: {}", request.idempotencyKey());
                return toResponse(existing.get());
            }
        }

        // Chat / text safety sanitization before saving
        String safeTitle = chatSafetyService.sanitize(request.title() != null ? request.title().trim() : "");
        String safeMessage = chatSafetyService.sanitize(request.message() != null ? request.message().trim() : "");

        Notification notification = new Notification();
        notification.setRecipientUserId(request.recipientUserId());
        notification.setType(request.type());
        notification.setTitle(safeTitle);
        notification.setMessage(safeMessage);
        notification.setRelatedEntityType(request.relatedEntityType());
        notification.setRelatedEntityId(request.relatedEntityId());
        notification.setDeepLink(request.deepLink());
        notification.setRead(false);
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            notification.setIdempotencyKey(request.idempotencyKey().trim());
        }
        notification.setCreatedAt(OffsetDateTime.now());

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: id={}, recipient={}, type={}, idempotencyKey={}",
                saved.getId(), saved.getRecipientUserId(), saved.getType(), saved.getIdempotencyKey());

        // Dispatch browser push outside transaction boundary or gracefully
        dispatchPushNotificationSilently(saved);

        // Dispatch email notification delivery channel
        dispatchEmailNotificationSilently(saved);

        return toResponse(saved);
    }

    // ============================================================
    // PUSH SUBSCRIPTION MANAGEMENT
    // ============================================================

    @Transactional
    public void registerPushSubscription(Long userId, PushSubscriptionRequest request) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }
        if (request.endpoint() == null || request.endpoint().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Push endpoint is required");
        }
        if (request.keys() == null || request.keys().p256dh() == null || request.keys().auth() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Push subscription keys are required");
        }

        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(request.endpoint().trim());
        if (existing.isPresent()) {
            PushSubscription sub = existing.get();
            sub.setUserId(userId);
            sub.setP256dh(request.keys().p256dh().trim());
            sub.setAuth(request.keys().auth().trim());
            sub.setUserAgent(request.userAgent());
            sub.setUpdatedAt(OffsetDateTime.now());
            pushSubscriptionRepository.save(sub);
            log.info("Updated existing push subscription for user: {}", userId);
        } else {
            PushSubscription sub = new PushSubscription();
            sub.setUserId(userId);
            sub.setEndpoint(request.endpoint().trim());
            sub.setP256dh(request.keys().p256dh().trim());
            sub.setAuth(request.keys().auth().trim());
            sub.setUserAgent(request.userAgent());
            pushSubscriptionRepository.save(sub);
            log.info("Registered new push subscription for user: {}", userId);
        }
    }

    @Transactional
    public void removePushSubscription(Long userId, String endpoint) {
        if (userId == null || userId <= 0 || endpoint == null || endpoint.isBlank()) {
            return;
        }
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint.trim());
        log.info("Removed push subscription for user: {}", userId);
    }

    // ============================================================
    // PUSH DISPATCH (FAILURE DOES NOT ROLL BACK POSTGRESQL STATE)
    // ============================================================

    private void dispatchPushNotificationSilently(Notification notification) {
        try {
            List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(notification.getRecipientUserId());
            if (subs.isEmpty()) {
                return;
            }
            log.info("User {} has {} registered push subscription(s) for notification {}",
                    notification.getRecipientUserId(), subs.size(), notification.getId());
            // Delivery happens via Web Push / Service Worker
        } catch (Exception ex) {
            // Push delivery failure must never delete or roll back the PostgreSQL notification
            log.warn("Silent push dispatch notice: {}", ex.getMessage());
        }
    }

    // ============================================================
    // EMAIL DISPATCH (FAILURE DOES NOT ROLL BACK POSTGRESQL STATE)
    // ============================================================

    private void dispatchEmailNotificationSilently(Notification notification) {
        try {
            emailNotificationService.dispatchEmailNotification(notification.getId());
        } catch (Exception ex) {
            // Email delivery failure must never delete or roll back the PostgreSQL notification
            log.warn("Silent email dispatch notice: {}", ex.getMessage());
        }
    }

    // ============================================================
    // NOTIFICATION PREFERENCES
    // ============================================================

    @Transactional(readOnly = true)
    public NotificationPreference getPreferences(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }
        return notificationPreferenceRepository.findById(userId).orElseGet(() -> {
            NotificationPreference pref = new NotificationPreference();
            pref.setUserId(userId);
            return pref;
        });
    }

    @Transactional
    public NotificationPreference updatePreferences(Long userId, NotificationPreference updated) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User authentication required");
        }
        NotificationPreference pref = notificationPreferenceRepository.findById(userId).orElseGet(() -> {
            NotificationPreference p = new NotificationPreference();
            p.setUserId(userId);
            return p;
        });

        pref.setEmailEnabled(updated.isEmailEnabled());
        pref.setChatMessages(updated.isChatMessages());
        pref.setQuotes(updated.isQuotes());
        pref.setBookings(updated.isBookings());
        pref.setSupport(updated.isSupport());
        pref.setReviews(updated.isReviews());
        pref.setReminders(updated.isReminders());

        return notificationPreferenceRepository.save(pref);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipientUserId(),
                n.getType().name(),
                n.getTitle(),
                n.getMessage(),
                n.getRelatedEntityType(),
                n.getRelatedEntityId(),
                n.getDeepLink(),
                n.isRead(),
                n.getCreatedAt(),
                n.isEmailSent(),
                n.getEmailSentAt(),
                n.getEmailRecipient()
        );
    }
}
