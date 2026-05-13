package com.capg.pharma.catalogservice.repository;

import com.capg.pharma.catalogservice.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Medicine} entities.
 *
 * <p>Provides standard CRUD plus custom queries for search and low-stock reporting.</p>
 */
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /**
     * Searches medicines by name (case-insensitive partial match).
     *
     * @param name the search term
     * @return list of medicines whose names contain the search term
     */
    List<Medicine> findByNameContainingIgnoreCase(String name);

    /**
     * Finds all medicines belonging to a specific category.
     *
     * @param categoryId the category's primary key
     * @return list of medicines in that category
     */
    List<Medicine> findByCategoryId(Long categoryId);

    /**
     * Atomically decrements stock for a medicine.
     * Uses a direct UPDATE to avoid lost-update race conditions.
     * clearAutomatically=true flushes the JPA first-level cache after the update.
     *
     * @param id       the medicine's primary key
     * @param quantity the quantity to deduct
     * @return number of rows updated (1 if successful, 0 if stock would go negative)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Medicine m SET m.stockQuantity = m.stockQuantity - :quantity " +
           "WHERE m.id = :id AND m.stockQuantity >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * Counts medicines with stock quantity at or below the low-stock threshold (10 units).
     *
     * @return count of low-stock medicines
     */
    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.stockQuantity <= 10")
    long countLowStock();

    /**
     * Retrieves all medicines with stock quantity at or below the low-stock threshold.
     *
     * @return list of low-stock medicines
     */
    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= 10")
    List<Medicine> findLowStockMedicines();
}
