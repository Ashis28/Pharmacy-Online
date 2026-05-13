package com.capg.pharma.catalogservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a prescription uploaded by a customer.
 *
 * <p>Customers upload prescription images (via URL) which admins then
 * approve or reject. Only approved prescriptions allow purchase of
 * prescription-required medicines.</p>
 */
@Entity
@Table(name = "prescriptions")
public class Prescription {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID of the customer who uploaded this prescription (logical FK to auth_db). */
    @Column(nullable = false)
    private Long customerId;

    /** Email of the customer, used for notifications. */
    private String customerEmail;

    /** URL pointing to the uploaded prescription image. */
    @Column(nullable = false)
    private String imageUrl;

    /** Current review status. Defaults to PENDING on upload. */
    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status = PrescriptionStatus.PENDING;

    /** Reason provided by admin when rejecting a prescription. */
    private String rejectionReason;

    /** Timestamp when the prescription was uploaded. */
    private LocalDateTime uploadedAt = LocalDateTime.now();

    /**
     * Possible review states for a prescription.
     */
    public enum PrescriptionStatus {
        /** Awaiting admin review. */
        PENDING,
        /** Approved by admin — customer may purchase prescription medicines. */
        APPROVED,
        /** Rejected by admin — reason stored in {@code rejectionReason}. */
        REJECTED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
