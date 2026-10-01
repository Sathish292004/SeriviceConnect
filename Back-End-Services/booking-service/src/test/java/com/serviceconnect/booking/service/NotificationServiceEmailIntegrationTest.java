package com.serviceconnect.booking.service;

import com.serviceconnect.booking.dto.request.CreateNotificationRequest;
import com.serviceconnect.booking.dto.response.NotificationResponse;
import com.serviceconnect.booking.entity.Notification;
import com.serviceconnect.booking.entity.NotificationType;
import com.serviceconnect.booking.repository.NotificationPreferenceRepository;
import com.serviceconnect.booking.repository.NotificationRepository;
import com.serviceconnect.booking.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceEmailIntegrationTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private ChatSafetyService chatSafetyService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Creates ONE authoritative notification and dispatches to both push and email delivery channels")
    void testCreateAuthoritativeNotificationWithEmailDelivery() {
        CreateNotificationRequest request = new CreateNotificationRequest(
                7L,
                NotificationType.BOOKING_CREATED,
                "New Booking Confirmed",
                "Customer Sathish booked Deep Home Cleaning.",
                "BOOKING",
                501L,
                "/provider/bookings/501",
                "BOOKING_501_CREATED"
        );

        when(notificationRepository.findByIdempotencyKey("BOOKING_501_CREATED")).thenReturn(Optional.empty());
        when(chatSafetyService.sanitize("New Booking Confirmed")).thenReturn("New Booking Confirmed");
        when(chatSafetyService.sanitize("Customer Sathish booked Deep Home Cleaning.")).thenReturn("Customer Sathish booked Deep Home Cleaning.");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(99L);
            return n;
        });

        when(pushSubscriptionRepository.findByUserId(7L)).thenReturn(Collections.emptyList());
        when(emailNotificationService.dispatchEmailNotification(99L)).thenReturn(true);

        NotificationResponse response = notificationService.createNotification(request);

        assertNotNull(response);
        assertEquals(99L, response.id());
        assertEquals(7L, response.recipientUserId());
        assertEquals("BOOKING_CREATED", response.type());
        assertEquals("New Booking Confirmed", response.title());
        assertEquals("Customer Sathish booked Deep Home Cleaning.", response.message());

        // Verify ONE save call to PostgreSQL (Single Authoritative Record)
        verify(notificationRepository, times(1)).save(any(Notification.class));

        // Verify push channel dispatch
        verify(pushSubscriptionRepository, times(1)).findByUserId(7L);

        // Verify email channel dispatch
        verify(emailNotificationService, times(1)).dispatchEmailNotification(99L);
    }
}
