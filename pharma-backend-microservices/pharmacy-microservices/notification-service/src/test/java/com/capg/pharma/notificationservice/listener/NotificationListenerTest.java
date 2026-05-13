package com.capg.pharma.notificationservice.listener;

import com.capg.pharma.notificationservice.dto.NotificationRequest;
import com.capg.pharma.notificationservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock EmailService emailService;

    NotificationListener notificationListener;

    @BeforeEach
    void setUp() {
        notificationListener = new NotificationListener(emailService);
    }

    @Test
    void handleNotification_doesNotThrow() {
        NotificationRequest req = new NotificationRequest();
        req.setRecipientEmail("user@example.com");
        req.setSubject("Order Placed");
        req.setMessage("Your order has been placed");
        req.setType("ORDER_PLACED");

        assertDoesNotThrow(() -> notificationListener.handleNotification(req));
        verify(emailService).sendEmail("user@example.com", "Order Placed", "Your order has been placed");
    }

    @Test
    void handleNotification_withNullFields_doesNotThrow() {
        NotificationRequest req = new NotificationRequest();
        assertDoesNotThrow(() -> notificationListener.handleNotification(req));
        verify(emailService).sendEmail(null, null, null);
    }
}
