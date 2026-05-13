package com.capg.pharma.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending transactional emails via SMTP.
 *
 * <p>Email sending is guarded by the {@code notification.email.enabled} flag.
 * When disabled (default), the service logs the email content instead of
 * actually sending it — useful for local development without SMTP credentials.</p>
 *
 * <p>To enable real email delivery, set the following environment variables:</p>
 * <pre>
 *   MAIL_ENABLED=true
 *   MAIL_HOST=smtp.gmail.com
 *   MAIL_PORT=587
 *   MAIL_USERNAME=your@gmail.com
 *   MAIL_PASSWORD=your_app_password   # Gmail App Password (not account password)
 *   MAIL_FROM=your@gmail.com
 * </pre>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notification.email.from:sagarpakar777@gmail.com}")
    private String fromAddress;

    /**
     * Constructs the EmailService with the Spring {@link JavaMailSender}.
     *
     * @param mailSender the configured mail sender (auto-configured by Spring Boot)
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a plain-text email to the specified recipient.
     *
     * <p>If {@code notification.email.enabled} is {@code false}, the email is
     * only logged and not actually sent.</p>
     *
     * @param to      recipient email address
     * @param subject email subject line
     * @param body    plain-text email body
     */
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("[EMAIL-DISABLED] Would send to={} subject='{}' body='{}'", to, subject, body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[EMAIL-SENT] to={} subject='{}'", to, subject);
        } catch (MailException ex) {
            log.error("[EMAIL-FAILED] to={} subject='{}' error={}", to, subject, ex.getMessage());
            // Don't rethrow — email failure should not break the notification flow
        }
    }
}
