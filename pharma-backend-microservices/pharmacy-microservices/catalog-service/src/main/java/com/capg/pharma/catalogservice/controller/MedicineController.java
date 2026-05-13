package com.capg.pharma.catalogservice.controller;

import com.capg.pharma.catalogservice.dto.MedicineRequest;
import com.capg.pharma.catalogservice.dto.MedicineResponse;
import com.capg.pharma.catalogservice.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for medicine catalog endpoints.
 *
 * <p>Read operations (GET) are publicly accessible.
 * Write operations (POST, PUT, DELETE) require ADMIN role.</p>
 *
 * <p>Base path: {@code /api/catalog/medicines}</p>
 */
@RestController
@RequestMapping("/api/catalog/medicines")
@Tag(name = "Medicines", description = "Medicine catalog management")
public class MedicineController {

    private final MedicineService medicineService;

    /**
     * Constructs the controller with the medicine service.
     *
     * @param medicineService the service handling medicine business logic
     */
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    /**
     * Returns all medicines in the catalog.
     *
     * @return {@code 200 OK} with list of all medicines
     */
    @GetMapping
    @Operation(summary = "Get all medicines")
    public ResponseEntity<List<MedicineResponse>> getAll() {
        return ResponseEntity.ok(medicineService.getAll());
    }

    /**
     * Returns a single medicine by its ID.
     *
     * @param id the medicine's primary key
     * @return {@code 200 OK} with the medicine, or {@code 404} if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<MedicineResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    /**
     * Searches medicines by name (case-insensitive partial match).
     *
     * @param name the search term
     * @return {@code 200 OK} with matching medicines
     */
    @GetMapping("/search")
    @Operation(summary = "Search medicines by name")
    public ResponseEntity<List<MedicineResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(medicineService.search(name));
    }

    /**
     * Returns the count of medicines with low stock (≤10 units).
     * No auth required — count is non-sensitive and needed by the admin dashboard.
     *
     * @return {@code 200 OK} with the count
     */
    @GetMapping("/low-stock-count")
    @Operation(summary = "Get low stock medicine count")
    public ResponseEntity<Long> getLowStockCount() {
        return ResponseEntity.ok(medicineService.getLowStockCount());
    }

    /**
     * Creates a new medicine in the catalog. Admin only.
     *
     * @param req the medicine creation request
     * @return {@code 201 Created} with the created medicine
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new medicine (Admin)")
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.create(req));
    }

    /**
     * Updates an existing medicine. Admin only.
     *
     * @param id  the medicine's primary key
     * @param req the update request
     * @return {@code 200 OK} with the updated medicine
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a medicine (Admin)")
    public ResponseEntity<MedicineResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody MedicineRequest req) {
        return ResponseEntity.ok(medicineService.update(id, req));
    }

    /**
     * Decrements stock for a medicine after an order is placed. Internal use only.
     *
     * @param id       the medicine's primary key
     * @param quantity the quantity to deduct (passed as request param)
     * @return {@code 200 OK} with the updated medicine
     */
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Decrement stock after order (internal)")
    public ResponseEntity<MedicineResponse> decrementStock(
            @PathVariable("id") Long id,
            @RequestParam int quantity) {
        medicineService.decrementStock(id, quantity);
        return ResponseEntity.ok(medicineService.getById(id));
    }

    /**
     * Deletes a medicine from the catalog. Admin only.
     *
     * @param id the medicine's primary key
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a medicine (Admin)")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        medicineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
