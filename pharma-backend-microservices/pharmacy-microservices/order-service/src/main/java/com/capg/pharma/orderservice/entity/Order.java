package com.capg.pharma.orderservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a customer order.
 *
 * <p>An order contains one or more {@link OrderItem}s. The total amount is
 * calculated at order placement time from medicine prices (snapshot). Status
 * transitions are managed by admins.</p>
 */
@Entity
@Table(name = "orders")
public class Order {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID of the customer who placed the order (logical FK to auth_db). */
    @Column(nullable = false)
    private Long customerId;

    /** Email of the customer, used for notifications. */
    private String customerEmail;

    /** Current order status. Defaults to PENDING on creation. */
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    /** Total order amount calculated at placement time. */
    private BigDecimal totalAmount;

    /** Delivery address provided by the customer. */
    private String deliveryAddress;

    /** Timestamp when the order was placed. */
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Line items in this order. Cascaded — items are saved/deleted with the order. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Possible lifecycle states for an order.
     */
    public enum OrderStatus {
        /** Order placed, awaiting payment confirmation. */
        PENDING,
        /** Payment received. */
        PAID,
        /** Order packed and ready for dispatch. */
        PACKED,
        /** Order dispatched for delivery. */
        SHIPPED,
        /** Order delivered to customer. */
        DELIVERED,
        /** Order cancelled. */
        CANCELLED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
