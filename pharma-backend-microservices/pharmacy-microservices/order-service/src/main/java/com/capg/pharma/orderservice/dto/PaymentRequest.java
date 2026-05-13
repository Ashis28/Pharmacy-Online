package com.capg.pharma.orderservice.dto;

import java.math.BigDecimal;

/** DTO for initiating payment processing via the Payment Service Feign client. */
public class PaymentRequest {
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String paymentMethod;

    public PaymentRequest(Long orderId, Long customerId, BigDecimal amount, String paymentMethod) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
