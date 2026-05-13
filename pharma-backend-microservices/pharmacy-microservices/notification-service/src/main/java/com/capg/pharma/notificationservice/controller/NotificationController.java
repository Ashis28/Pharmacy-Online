package com.capg.pharma.notificationservice.controller;

import com.capg.pharma.notificationservice.dto.NotificationRequest;
import com.capg.pharma.notificationservice.listener.NotificationListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for direct notification delivery (HTTP fallback).
 *
 * <p>Provides an HTTP endpoint for sending notifications directly,
 * bypassing RabbitMQ. Useful for testing and direct invocation scenarios.</p>
 *
 * <p>Base path: {@code /api/notifications}</p>
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Direct notification delivery endpoint")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationListener notificationListener;

    /**
     * Constructs the controller with the notification listener.
     *
     * @param notificationListener the listener that processes notification events
     */
    public NotificationController(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    /**
     * Sends a notification directly via HTTP (bypasses RabbitMQ).
     *
     * @param req the notification request with recipient, subject, message, and type
     * @return {@code 200 OK} with a confirmation message
     */
    @PostMapping("/send")
    @Operation(summary = "Send a notification directly (HTTP)")
    public ResponseEntity<String> send(@RequestBody NotificationRequest req) {
        notificationListener.handleNotification(req);
        return ResponseEntity.ok("Notification processed for: " + req.getRecipientEmail());
    }
}
