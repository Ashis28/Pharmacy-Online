package com.capg.pharma.catalogservice.repository;

import com.capg.pharma.catalogservice.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Prescription} entities.
 * Provides finders used by admin review and customer history views.
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * Finds all prescriptions with a given status.
     *
     * @param status the status to filter by (e.g. PENDING)
     * @return list of matching prescriptions
     */
    List<Prescription> findByStatus(Prescription.PrescriptionStatus status);

    /**
     * Finds all prescriptions uploaded by a specific customer.
     *
     * @param customerId the customer's ID
     * @return list of prescriptions for that customer
     */
    List<Prescription> findByCustomerId(Long customerId);
}
