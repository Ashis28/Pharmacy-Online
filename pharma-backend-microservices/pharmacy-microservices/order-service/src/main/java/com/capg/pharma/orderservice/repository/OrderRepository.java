package com.capg.pharma.orderservice.repository;

import com.capg.pharma.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Order} entities.
 * Provides standard CRUD plus revenue and count queries for the admin dashboard.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds all orders placed by a specific customer.
     *
     * @param customerId the customer's ID
     * @return list of orders for that customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Calculates total revenue from DELIVERED orders within a date range.
     *
     * @param from start of the date range (inclusive)
     * @param to   end of the date range (inclusive)
     * @return total revenue, or 0 if no delivered orders in range
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :from AND :to AND o.status = 'DELIVERED'")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Counts orders placed within a date range.
     *
     * @param from start of the date range (inclusive)
     * @param to   end of the date range (inclusive)
     * @return count of orders in range
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    long countBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
