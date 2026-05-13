package com.capg.pharma.notificationservice.listener;

import com.capg.pharma.notificationservice.dto.NotificationRequest;
import com.capg.pharma.notificationservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Full tests for NotificationListener — verifies it delegates to EmailService correctly.
 */
@ExtendWith(MockitoExtension.class)
class NotificationListenerFullTest {

    @Mock EmailService emailService;

    NotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new NotificationListener(emailService);
    }

    @Test
    void handleNotification_callsEmailServiceWithCorrectArgs() {
        NotificationRequest req = new NotificationRequest();
        req.setRecipientEmail("customer@example.com");
        req.setSubject("Order Placed - #42");
        req.setMessage("Your order has been placed. Total: Rs.250.00");
        req.setType("ORDER_PLACED");

        listener.handleNotification(req);

        verify(emailService).sendEmail(
                "customer@example.com",
                "Order Placed - #42",
                "Your order has been placed. Total: Rs.250.00"
        );
    }

    @Test
    void handleNotification_withNullEmail_stillCallsEmailService() {
        NotificationRequest req = new NotificationRequest();
        req.setRecipientEmail(null);
        req.setSubject("Test");
        req.setMessage("Test message");
        req.setType("TEST");

        listener.handleNotification(req);

        verify(emailService).sendEmail(null, "Test", "Test message");
    }

    @Test
    void handleNotification_orderShipped_sendsCorrectNotification() {
        NotificationRequest req = new NotificationRequest();
        req.setRecipientEmail("user@example.com");
        req.setSubject("Order Shipped - #10");
        req.setMessage("Your order is on the way!");
        req.setType("ORDER_SHIPPED");

        listener.handleNotification(req);

        verify(emailService).sendEmail("user@example.com", "Order Shipped - #10", "Your order is on the way!");
    }
}
