package com.capg.pharma.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing a placed order with its line items.
 */
public class OrderResponse {

    /** Order primary key. */
    private Long id;
    /** ID of the customer who placed the order. */
    private Long customerId;
    /** Email of the customer. */
    private String customerEmail;
    /** Current order status. */
    private String status;
    /** Total order amount. */
    private BigDecimal totalAmount;
    /** Delivery address. */
    private String deliveryAddress;
    /** Timestamp when the order was placed. */
    private LocalDateTime createdAt;
    /** Line items in this order. */
    private List<ItemDto> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ItemDto> getItems() { return items; }
    public void setItems(List<ItemDto> items) { this.items = items; }

    /**
     * Represents a single line item in the order response.
     */
    public static class ItemDto {
        /** Medicine ID (snapshot). */
        private Long medicineId;
        /** Medicine name (snapshot at order time). */
        private String medicineName;
        /** Quantity ordered. */
        private Integer quantity;
        /** Unit price (snapshot at order time). */
        private BigDecimal unitPrice;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
