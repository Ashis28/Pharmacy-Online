package com.capg.pharma.catalogservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO representing a medicine in the catalog.
 * Returned by all medicine read and write endpoints.
 */
public class MedicineResponse {

    /** Medicine primary key. */
    private Long id;
    /** Medicine name. */
    private String name;
    /** Optional description. */
    private String description;
    /** Retail price per unit. */
    private BigDecimal price;
    /** Current stock quantity. */
    private Integer stockQuantity;
    /** Whether a prescription is required. */
    private boolean requiresPrescription;
    /** Expiry date of current stock. */
    private LocalDate expiryDate;
    /** Name of the category this medicine belongs to (may be null). */
    private String categoryName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
