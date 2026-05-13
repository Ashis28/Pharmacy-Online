package com.capg.pharma.orderservice.client;

import com.capg.pharma.orderservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the Notification Service (HTTP fallback).
 *
 * <p>This client is a fallback for direct HTTP notification delivery.
 * The primary notification path is via RabbitMQ ({@link com.capg.pharma.orderservice.messaging.NotificationPublisher}).
 * This client is kept for potential direct invocation scenarios.</p>
 */
@FeignClient(name = "notification-service")
public interface NotificationClient {

    /**
     * Sends a notification directly to the notification service via HTTP.
     *
     * @param request the notification payload
     * @return a confirmation message
     */
    @PostMapping("/api/notifications/send")
    String sendNotification(@RequestBody NotificationRequest request);
}
