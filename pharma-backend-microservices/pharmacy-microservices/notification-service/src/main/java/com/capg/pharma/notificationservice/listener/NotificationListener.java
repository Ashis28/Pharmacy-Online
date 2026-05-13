package com.capg.pharma.notificationservice.listener;

import com.capg.pharma.notificationservice.config.RabbitMQConfig;
import com.capg.pharma.notificationservice.dto.NotificationRequest;
import com.capg.pharma.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message listener for notification events.
 *
 * <p>Consumes messages from the {@code notification.queue} and sends
 * transactional emails via {@link EmailService}. When email is disabled
 * (default for local dev), the email content is logged instead.</p>
 */
@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final EmailService emailService;

    /**
     * Constructs the listener with the email service.
     *
     * @param emailService service responsible for SMTP delivery
     */
    public NotificationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Handles incoming notification events from RabbitMQ.
     *
     * <p>Triggered automatically when a message arrives on {@code notification.queue}.
     * The message is deserialized from JSON to {@link NotificationRequest} by
     * the configured {@link com.capg.pharma.notificationservice.config.RabbitMQConfig#messageConverter()}.</p>
     *
     * @param request the deserialized notification event
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationRequest request) {
        log.info("[NOTIFICATION] type={} to={} subject={}",
                request.getType(), request.getRecipientEmail(), request.getSubject());

        emailService.sendEmail(
                request.getRecipientEmail(),
                request.getSubject(),
                request.getMessage()
        );
    }
}
