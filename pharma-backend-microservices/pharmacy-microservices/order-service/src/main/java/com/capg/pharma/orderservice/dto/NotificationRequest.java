package com.capg.pharma.orderservice.dto;

/**
 * DTO for notification events published to RabbitMQ.
 * Consumed by the notification-service to send emails or push notifications.
 */
public class NotificationRequest {

    /** Recipient's email address. */
    private String recipientEmail;
    /** Email subject line. */
    private String subject;
    /** Notification body message. */
    private String message;
    /** Event type (e.g. "ORDER_PLACED", "ORDER_SHIPPED"). */
    private String type;

    /**
     * Constructs a NotificationRequest with all fields.
     *
     * @param recipientEmail the recipient's email
     * @param subject        the email subject
     * @param message        the notification body
     * @param type           the event type
     */
    public NotificationRequest(String recipientEmail, String subject, String message, String type) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.type = type;
    }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
