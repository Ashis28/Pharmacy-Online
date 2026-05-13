package com.capg.pharma.orderservice.dto;

import java.math.BigDecimal;

/**
 * DTO representing medicine data fetched from the Catalog Service via Feign.
 * Used to snapshot medicine name and price into order items at placement time.
 */
public class MedicineDto {

    /** Medicine primary key. */
    private Long id;
    /** Medicine name (snapshotted into order items). */
    private String name;
    /** Current retail price (snapshotted into order items). */
    private BigDecimal price;
    /** Current stock quantity. */
    private Integer stockQuantity;
    /** Whether a prescription is required. */
    private boolean requiresPrescription;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
}
