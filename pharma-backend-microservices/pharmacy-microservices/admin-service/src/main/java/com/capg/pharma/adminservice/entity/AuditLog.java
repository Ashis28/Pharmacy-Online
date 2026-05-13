package com.capg.pharma.adminservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing an admin audit log entry.
 *
 * <p>Every admin action (add/update/delete medicine, approve/reject prescription,
 * update order status) is recorded here for accountability and traceability.</p>
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email of the admin who performed the action. */
    private String adminEmail;

    /** Action performed (e.g. "ADD_MEDICINE", "APPROVE_PRESCRIPTION"). */
    private String action;

    /** Type of entity affected (e.g. "Medicine", "Order"). */
    private String targetEntity;

    /** ID of the affected entity. */
    private String targetId;

    /** Human-readable description of the action. */
    private String details;

    /** Timestamp when the action was performed. */
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Default constructor required by JPA. */
    public AuditLog() {}

    /**
     * All-args constructor for builder pattern.
     */
    public AuditLog(Long id, String adminEmail, String action, String targetEntity,
                    String targetId, String details, LocalDateTime timestamp) {
        this.id = id;
        this.adminEmail = adminEmail;
        this.action = action;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.details = details;
        this.timestamp = timestamp;
    }

    /**
     * Creates a new builder for AuditLog.
     *
     * @return a new {@link Builder} instance
     */
    public static Builder builder() { return new Builder(); }

    /**
     * Builder for constructing AuditLog instances fluently.
     */
    public static class Builder {
        private Long id;
        private String adminEmail;
        private String action;
        private String targetEntity;
        private String targetId;
        private String details;
        private LocalDateTime timestamp = LocalDateTime.now();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder adminEmail(String adminEmail) { this.adminEmail = adminEmail; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder targetEntity(String targetEntity) { this.targetEntity = targetEntity; return this; }
        public Builder targetId(String targetId) { this.targetId = targetId; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        /** Builds the AuditLog instance. */
        public AuditLog build() {
            return new AuditLog(id, adminEmail, action, targetEntity, targetId, details, timestamp);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
