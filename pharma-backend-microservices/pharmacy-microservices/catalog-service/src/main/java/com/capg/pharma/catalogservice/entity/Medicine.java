package com.capg.pharma.catalogservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing a medicine in the pharmacy catalog.
 *
 * <p>Medicines belong to a {@link Category} and may require a prescription.
 * Stock quantity is tracked here; low-stock alerts are triggered when
 * {@code stockQuantity <= 10}.</p>
 */
@Entity
@Table(name = "medicines")
public class Medicine {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Medicine name (e.g. "Amoxicillin 500mg"). Must not be null. */
    @Column(nullable = false)
    private String name;

    /** Optional detailed description of the medicine. */
    private String description;

    /** Retail price per unit. Must be non-negative. */
    @Column(nullable = false)
    private BigDecimal price;

    /** Current stock quantity. Must be non-negative. */
    @Column(nullable = false)
    private Integer stockQuantity;

    /** Whether this medicine requires a valid prescription to purchase. */
    private boolean requiresPrescription;

    /** Expiry date of the current stock batch. */
    private LocalDate expiryDate;

    /** The category this medicine belongs to. Loaded lazily. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

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
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
