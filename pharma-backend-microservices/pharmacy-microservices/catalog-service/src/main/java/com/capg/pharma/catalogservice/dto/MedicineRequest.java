package com.capg.pharma.catalogservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating or updating a medicine in the catalog.
 * Used by admin endpoints and the admin-service Feign client.
 */
public class MedicineRequest {

    /** Medicine name. Must not be blank. */
    @NotBlank
    private String name;

    /** Optional description. */
    private String description;

    /** Retail price per unit. Must be non-negative. */
    @NotNull
    @Min(0)
    private BigDecimal price;

    /** Current stock quantity. Must be non-negative. */
    @NotNull
    @Min(0)
    private Integer stockQuantity;

    /** Whether a prescription is required to purchase this medicine. */
    private boolean requiresPrescription;

    /** Expiry date of the current stock batch. */
    private LocalDate expiryDate;

    /** Optional category ID. If provided, the category must exist. */
    private Long categoryId;

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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
