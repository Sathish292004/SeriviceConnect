package com.serviceconnect.booking.service;

import com.serviceconnect.booking.client.AuthServiceClient;
import com.serviceconnect.booking.entity.Notification;
import com.serviceconnect.booking.entity.NotificationPreference;
import com.serviceconnect.booking.entity.NotificationType;
import com.serviceconnect.booking.repository.NotificationPreferenceRepository;
import com.serviceconnect.booking.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "apiKey", "dummy-brevo-api-key");
        ReflectionTestUtils.setField(emailNotificationService, "apiUrl", "https://api.brevo.com/v3/smtp/email");
        ReflectionTestUtils.setField(emailNotificationService, "senderName", "ServiceConnect");
        ReflectionTestUtils.setField(emailNotificationService, "senderEmail", "noreply@serviceconnect.com");
        ReflectionTestUtils.setField(emailNotificationService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    @DisplayName("1. Dispatches email for eligible BOOKING_CREATED notification and saves delivery status")
    void testDispatchBookingCreatedEmail() {
        Notification notification = new Notification();
        notification.setId(100L);
        notification.setRecipientUserId(7L);
        notification.setType(NotificationType.BOOKING_CREATED);
        notification.setTitle("New Booking");
        notification.setMessage("Customer Sathish booked AC Repair.");
        notification.setDeepLink("/provider/bookings");
        notification.setCreatedAt(OffsetDateTime.now());

        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(preferenceRepository.findById(7L)).thenReturn(Optional.empty()); // Default preferences
        when(authServiceClient.getUserEmail(7L)).thenReturn("apex.electric@example.com");
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = emailNotificationService.dispatchEmailNotification(100L);

        assertTrue(result, "Email dispatch should succeed");
        assertTrue(notification.isEmailSent(), "Notification emailSent must be true");
        assertEquals("apex.electric@example.com", notification.getEmailRecipient(), "Recipient email must be recorded");
        assertNotNull(notification.getEmailSentAt(), "Delivery timestamp must be recorded");
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("2. Skips email for CHAT_MESSAGE to prevent inbox flooding")
    void testSkipChatMessageEmail() {
        Notification notification = new Notification();
        notification.setId(101L);
        notification.setRecipientUserId(7L);
        notification.setType(NotificationType.CHAT_MESSAGE);
        notification.setTitle("New message from Sathish");
        notification.setMessage("Hi, are you available tomorrow?");

        when(notificationRepository.findById(101L)).thenReturn(Optional.of(notification));

        boolean result = emailNotificationService.dispatchEmailNotification(101L);

        assertFalse(result, "CHAT_MESSAGE should not trigger email delivery");
        assertFalse(notification.isEmailSent());
        verify(authServiceClient, never()).getUserEmail(any());
        verify(notificationRepository, never()).save(notification);
    }

    @Test
    @DisplayName("3. Suppresses email when user has email_enabled=false in preferences")
    void testSuppressWhenUserPreferenceDisabled() {
        Notification notification = new Notification();
        notification.setId(102L);
        notification.setRecipientUserId(22L);
        notification.setType(NotificationType.BOOKING_ACCEPTED);
        notification.setTitle("Booking Accepted");
        notification.setMessage("Apex Electrical accepted your booking.");

        NotificationPreference pref = new NotificationPreference();
        pref.setUserId(22L);
        pref.setEmailEnabled(false); // User turned off email notifications

        when(notificationRepository.findById(102L)).thenReturn(Optional.of(notification));
        when(preferenceRepository.findById(22L)).thenReturn(Optional.of(pref));

        boolean result = emailNotificationService.dispatchEmailNotification(102L);

        assertFalse(result, "Email should be suppressed when emailEnabled is false");
        assertFalse(notification.isEmailSent());
        verify(authServiceClient, never()).getUserEmail(any());
        verify(notificationRepository, never()).save(notification);
    }

    @Test
    @DisplayName("4. Gracefully handles missing registered email without throwing exception")
    void testHandleMissingEmailGracefully() {
        Notification notification = new Notification();
        notification.setId(103L);
        notification.setRecipientUserId(999L);
        notification.setType(NotificationType.QUOTE_CREATED);
        notification.setTitle("New Quote Received");
        notification.setMessage("Apex sent a quote of ₹1,200.");

        when(notificationRepository.findById(103L)).thenReturn(Optional.of(notification));
        when(preferenceRepository.findById(999L)).thenReturn(Optional.empty());
        when(authServiceClient.getUserEmail(999L)).thenReturn(null); // No email found in auth-service

        boolean result = emailNotificationService.dispatchEmailNotification(103L);

        assertFalse(result, "Should return false cleanly when email is not found");
        assertFalse(notification.isEmailSent());
        verify(notificationRepository, never()).save(notification);
    }

    @Test
    @DisplayName("5. Prevents duplicate email sending if already marked sent")
    void testPreventDuplicateEmail() {
        Notification notification = new Notification();
        notification.setId(104L);
        notification.setRecipientUserId(7L);
        notification.setType(NotificationType.BOOKING_CREATED);
        notification.setEmailSent(true); // Already sent
        notification.setEmailRecipient("provider@example.com");

        when(notificationRepository.findById(104L)).thenReturn(Optional.of(notification));

        boolean result = emailNotificationService.dispatchEmailNotification(104L);

        assertTrue(result, "Should return true for already delivered email without re-sending");
        verify(authServiceClient, never()).getUserEmail(any());
        verify(notificationRepository, never()).save(notification);
    }
}
