package com.capg.pharma.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for placing a new order.
 */
public class OrderRequest {

    /** ID of the customer placing the order. Must not be null. */
    @NotNull
    private Long customerId;

    /** List of items to order. Must contain at least one item. */
    @NotEmpty
    private List<OrderItemRequest> items;

    /** Delivery address for the order. */
    private String deliveryAddress;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    /**
     * Represents a single item in the order request.
     */
    public static class OrderItemRequest {

        /** ID of the medicine to order. Must not be null. */
        @NotNull
        private Long medicineId;

        /** Quantity to order. Must not be null. */
        @NotNull
        private Integer quantity;

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
