package com.capg.pharma.paymentservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a payment transaction.
 *
 * <p>Each payment is linked to an order ID (logical FK to order_db).
 * Transaction IDs are generated internally using UUID.</p>
 */
@Entity
@Table(name = "payments")
public class Payment {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID of the order this payment is for (logical FK to order_db). */
    @Column(nullable = false)
    private Long orderId;

    /** ID of the customer who made the payment. */
    @Column(nullable = false)
    private Long customerId;

    /** Payment amount. */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Payment method (e.g. "CARD", "UPI", "CASH"). */
    private String paymentMethod;

    /** Current payment status. Defaults to PENDING. */
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Unique transaction identifier generated on successful payment. */
    private String transactionId;

    /** Timestamp when the payment was processed. */
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Possible payment states.
     */
    public enum PaymentStatus {
        /** Payment initiated but not yet confirmed. */
        PENDING,
        /** Payment successfully processed. */
        SUCCESS,
        /** Payment failed. */
        FAILED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
