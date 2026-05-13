package com.capg.pharma.adminservice.controller;

import com.capg.pharma.adminservice.dto.*;
import com.capg.pharma.adminservice.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for admin management endpoints.
 *
 * <p>All endpoints require ADMIN role. Delegates operations to downstream
 * services via the AdminService and records all actions in the audit log.</p>
 *
 * <p>Base path: {@code /api/admin}</p>
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard, medicine management, prescription review, order management")
public class AdminController {

    private final AdminService adminService;

    /**
     * Constructs the controller with the admin service.
     *
     * @param adminService the service handling admin business logic
     */
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Returns the admin dashboard summary (orders, prescriptions, stock, revenue).
     *
     * @return {@code 200 OK} with the dashboard data
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard summary")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    /**
     * Adds a new medicine to the catalog.
     *
     * @param req the medicine creation request
     * @return {@code 201 Created} with the created medicine
     */
    @PostMapping("/medicines")
    @Operation(summary = "Add a new medicine")
    public ResponseEntity<MedicineResponse> addMedicine(@Valid @RequestBody MedicineRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addMedicine(req));
    }

    /**
     * Updates an existing medicine.
     *
     * @param id  the medicine's primary key
     * @param req the update request
     * @return {@code 200 OK} with the updated medicine
     */
    @PutMapping("/medicines/{id}")
    @Operation(summary = "Update a medicine")
    public ResponseEntity<MedicineResponse> updateMedicine(@PathVariable Long id,
                                                            @Valid @RequestBody MedicineRequest req) {
        return ResponseEntity.ok(adminService.updateMedicine(id, req));
    }

    /**
     * Deletes a medicine from the catalog.
     *
     * @param id the medicine's primary key
     * @return {@code 204 No Content}
     */
    @DeleteMapping("/medicines/{id}")
    @Operation(summary = "Delete a medicine")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        adminService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns all prescriptions awaiting review.
     *
     * @return {@code 200 OK} with list of PENDING prescriptions
     */
    @GetMapping("/prescriptions/pending")
    @Operation(summary = "Get pending prescriptions")
    public ResponseEntity<List<PrescriptionResponse>> getPendingPrescriptions() {
        return ResponseEntity.ok(adminService.getPendingPrescriptions());
    }

    /**
     * Approves a prescription.
     *
     * @param id the prescription's primary key
     * @return {@code 200 OK} with the updated prescription
     */
    @PutMapping("/prescriptions/{id}/approve")
    @Operation(summary = "Approve a prescription")
    public ResponseEntity<PrescriptionResponse> approvePrescription(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approvePrescription(id));
    }

    /**
     * Rejects a prescription with a reason.
     *
     * @param id   the prescription's primary key
     * @param body request body containing {@code reason}
     * @return {@code 200 OK} with the updated prescription
     */
    @PutMapping("/prescriptions/{id}/reject")
    @Operation(summary = "Reject a prescription")
    public ResponseEntity<PrescriptionResponse> rejectPrescription(@PathVariable Long id,
                                                                     @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.rejectPrescription(id, body.get("reason")));
    }

    /**
     * Returns all orders in the system.
     *
     * @return {@code 200 OK} with all orders
     */
    @GetMapping("/orders")
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    /**
     * Updates the status of an order.
     *
     * @param id   the order's primary key
     * @param body request body containing {@code status}
     * @return {@code 200 OK} with the updated order
     */
    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateOrderStatus(id, body.get("status")));
    }
}
