package com.capg.pharma.catalogservice.controller;

import com.capg.pharma.catalogservice.dto.PrescriptionResponse;
import com.capg.pharma.catalogservice.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for prescription management endpoints.
 *
 * <p>Customers can upload prescriptions and view their own.
 * Admins can list pending prescriptions and approve/reject them.</p>
 *
 * <p>Base path: {@code /api/catalog/prescriptions}</p>
 */
@RestController
@RequestMapping("/api/catalog/prescriptions")
@Tag(name = "Prescriptions", description = "Prescription upload and review")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    /**
     * Constructs the controller with the prescription service.
     *
     * @param prescriptionService the service handling prescription business logic
     */
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /**
     * Uploads a new prescription for review.
     *
     * <p>The customer's email is extracted from the {@code X-Auth-User} header
     * injected by the gateway after JWT validation.</p>
     *
     * @param email the authenticated customer's email (from gateway header)
     * @param body  request body containing {@code customerId} and {@code imageUrl}
     * @return {@code 200 OK} with the created prescription record
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload a prescription image")
    public ResponseEntity<PrescriptionResponse> upload(
            @RequestHeader("X-Auth-User") String email,
            @RequestBody Map<String, String> body) {
        Long customerId = Long.parseLong(body.get("customerId"));
        String imageUrl = body.get("imageUrl");
        return ResponseEntity.ok(prescriptionService.upload(customerId, email, imageUrl));
    }

    /**
     * Returns all prescriptions uploaded by the requesting customer.
     *
     * @param customerId the customer's ID
     * @return {@code 200 OK} with the customer's prescription history
     */
    @GetMapping("/my")
    @Operation(summary = "Get my prescriptions")
    public ResponseEntity<List<PrescriptionResponse>> myPrescriptions(@RequestParam Long customerId) {
        return ResponseEntity.ok(prescriptionService.getByCustomer(customerId));
    }

    /**
     * Returns all prescriptions awaiting admin review. Admin only.
     *
     * @return {@code 200 OK} with list of PENDING prescriptions
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending prescriptions (Admin)")
    public ResponseEntity<List<PrescriptionResponse>> getPending() {
        return ResponseEntity.ok(prescriptionService.getPending());
    }

    /**
     * Approves a prescription. Admin only.
     *
     * @param id the prescription's primary key
     * @return {@code 200 OK} with the updated prescription (status: APPROVED)
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a prescription (Admin)")
    public ResponseEntity<PrescriptionResponse> approve(@PathVariable("id") Long id) {
        return ResponseEntity.ok(prescriptionService.approve(id));
    }

    /**
     * Rejects a prescription with a reason. Admin only.
     *
     * @param id   the prescription's primary key
     * @param body request body containing {@code reason}
     * @return {@code 200 OK} with the updated prescription (status: REJECTED)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a prescription (Admin)")
    public ResponseEntity<PrescriptionResponse> reject(@PathVariable("id") Long id,
                                                        @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(prescriptionService.reject(id, body.get("reason")));
    }
}
