package com.capg.pharma.paymentservice.repository;

import com.capg.pharma.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Payment} entities.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds all payments for a given order.
     *
     * @param orderId the order's ID
     * @return list of payments for that order
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Finds all payments made by a specific customer.
     *
     * @param customerId the customer's ID
     * @return list of payments by that customer
     */
    List<Payment> findByCustomerId(Long customerId);
}
