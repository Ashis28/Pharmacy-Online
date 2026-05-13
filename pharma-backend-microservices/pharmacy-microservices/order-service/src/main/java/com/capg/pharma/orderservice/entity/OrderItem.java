package com.capg.pharma.orderservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * JPA entity representing a single line item within an {@link Order}.
 *
 * <p>Medicine name and unit price are stored as snapshots at order time.
 * This ensures historical orders always reflect what the customer paid,
 * even if the medicine's price or name changes later.</p>
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The parent order this item belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /** ID of the medicine (logical FK to catalog_db). */
    @Column(nullable = false)
    private Long medicineId;

    /** Snapshot of the medicine name at order time. */
    private String medicineName;

    /** Quantity ordered. */
    @Column(nullable = false)
    private Integer quantity;

    /** Snapshot of the unit price at order time. */
    @Column(nullable = false)
    private BigDecimal unitPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
