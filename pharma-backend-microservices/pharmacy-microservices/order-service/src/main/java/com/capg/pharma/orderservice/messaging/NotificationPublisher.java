package com.capg.pharma.orderservice.messaging;

import com.capg.pharma.orderservice.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ publisher for order-related notification events.
 *
 * <p>Publishes {@link NotificationRequest} messages to the pharmacy exchange.
 * Failures are caught and logged as warnings — notification delivery is
 * non-critical and must not block order placement.</p>
 */
@Component
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * Constructs the publisher with the configured RabbitTemplate.
     *
     * @param rabbitTemplate the template for sending messages to RabbitMQ
     */
    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a notification event to the pharmacy exchange.
     *
     * <p>Sends the message to {@code pharmacy.exchange} with routing key
     * {@code notification.send}. If RabbitMQ is unavailable, the exception
     * is caught and logged — the order is not affected.</p>
     *
     * @param request the notification payload to publish
     */
    public void publish(NotificationRequest request) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, request);
            log.info("[MQ] Notification published: type={} to={}", request.getType(), request.getRecipientEmail());
        } catch (Exception e) {
            log.warn("[MQ] Failed to publish notification: {}", e.getMessage());
        }
    }
}
