package com.capg.pharma.notificationservice.dto;

/**
 * DTO representing a notification event received from RabbitMQ or via HTTP.
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

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
