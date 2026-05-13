package com.capg.pharma.catalogservice.entity;

import jakarta.persistence.*;

/**
 * JPA entity representing a medicine category (e.g. Antibiotics, Vitamins).
 *
 * <p>Categories are used to group medicines for browsing and filtering.
 * Each medicine belongs to at most one category.</p>
 */
@Entity
@Table(name = "categories")
public class Category {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique category name (e.g. "Antibiotics"). */
    @Column(nullable = false, unique = true)
    private String name;

    /** Optional description of the category. */
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
