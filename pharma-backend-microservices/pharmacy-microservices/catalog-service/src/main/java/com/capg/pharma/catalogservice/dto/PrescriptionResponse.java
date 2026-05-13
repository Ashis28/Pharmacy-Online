package com.capg.pharma.catalogservice.dto;

import java.time.LocalDateTime;

/**
 * Response DTO representing a prescription record.
 * Returned by prescription upload, listing, and review endpoints.
 */
public class PrescriptionResponse {

    /** Prescription primary key. */
    private Long id;
    /** ID of the customer who uploaded this prescription. */
    private Long customerId;
    /** Email of the customer. */
    private String customerEmail;
    /** URL of the uploaded prescription image. */
    private String imageUrl;
    /** Current status: PENDING, APPROVED, or REJECTED. */
    private String status;
    /** Reason for rejection (only set when status is REJECTED). */
    private String rejectionReason;
    /** Timestamp when the prescription was uploaded. */
    private LocalDateTime uploadedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
