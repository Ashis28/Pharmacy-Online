package com.capg.pharma.adminservice.client;

import com.capg.pharma.adminservice.dto.MedicineRequest;
import com.capg.pharma.adminservice.dto.MedicineResponse;
import com.capg.pharma.adminservice.dto.PrescriptionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign client for the Catalog Service.
 *
 * <p>Used by the Admin Service to perform medicine CRUD and prescription
 * review operations by delegating to the catalog-service REST API.</p>
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    /**
     * Creates a new medicine in the catalog.
     *
     * @param request the medicine creation request
     * @return the created medicine
     */
    @PostMapping("/api/catalog/medicines")
    MedicineResponse addMedicine(@RequestBody MedicineRequest request);

    /**
     * Updates an existing medicine.
     *
     * @param id      the medicine's primary key
     * @param request the update request
     * @return the updated medicine
     */
    @PutMapping("/api/catalog/medicines/{id}")
    MedicineResponse updateMedicine(@PathVariable("id") Long id, @RequestBody MedicineRequest request);

    /**
     * Deletes a medicine from the catalog.
     *
     * @param id the medicine's primary key
     */
    @DeleteMapping("/api/catalog/medicines/{id}")
    void deleteMedicine(@PathVariable("id") Long id);

    /**
     * Retrieves all prescriptions awaiting admin review.
     *
     * @return list of PENDING prescriptions
     */
    @GetMapping("/api/catalog/prescriptions/pending")
    List<PrescriptionResponse> getPendingPrescriptions();

    /**
     * Approves a prescription.
     *
     * @param id the prescription's primary key
     * @return the updated prescription
     */
    @PutMapping("/api/catalog/prescriptions/{id}/approve")
    PrescriptionResponse approvePrescription(@PathVariable("id") Long id);

    /**
     * Rejects a prescription with a reason.
     *
     * @param id   the prescription's primary key
     * @param body map containing {@code reason}
     * @return the updated prescription
     */
    @PutMapping("/api/catalog/prescriptions/{id}/reject")
    PrescriptionResponse rejectPrescription(@PathVariable("id") Long id, @RequestBody Map<String, String> body);

    /**
     * Returns the count of low-stock medicines.
     *
     * @return count of medicines with stock ≤ 10
     */
    @GetMapping("/api/catalog/medicines/low-stock-count")
    long getLowStockCount();
}
