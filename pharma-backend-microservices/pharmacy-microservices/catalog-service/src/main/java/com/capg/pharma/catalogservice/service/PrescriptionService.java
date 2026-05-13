package com.capg.pharma.catalogservice.service;

import com.capg.pharma.catalogservice.dto.PrescriptionResponse;
import com.capg.pharma.catalogservice.entity.Prescription;
import com.capg.pharma.catalogservice.exception.PrescriptionNotFoundException;
import com.capg.pharma.catalogservice.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic service for prescription management.
 *
 * <p>Handles prescription uploads by customers and review (approve/reject) by admins.
 * Prescription status transitions: PENDING → APPROVED or PENDING → REJECTED.</p>
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;

    /**
     * Constructs PrescriptionService with the prescription repository.
     *
     * @param prescriptionRepo repository for prescription persistence
     */
    public PrescriptionService(PrescriptionRepository prescriptionRepo) {
        this.prescriptionRepo = prescriptionRepo;
    }

    /**
     * Records a new prescription upload from a customer.
     *
     * @param customerId the ID of the uploading customer
     * @param email      the customer's email address
     * @param imageUrl   URL of the uploaded prescription image
     * @return the created prescription as a response DTO
     */
    public PrescriptionResponse upload(Long customerId, String email, String imageUrl) {
        Prescription p = new Prescription();
        p.setCustomerId(customerId);
        p.setCustomerEmail(email);
        p.setImageUrl(imageUrl);
        return toResponse(prescriptionRepo.save(p));
    }

    /**
     * Retrieves all prescriptions currently awaiting admin review.
     *
     * @return list of PENDING prescriptions
     */
    public List<PrescriptionResponse> getPending() {
        return prescriptionRepo.findByStatus(Prescription.PrescriptionStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Retrieves all prescriptions uploaded by a specific customer.
     *
     * @param customerId the customer's ID
     * @return list of prescriptions for that customer
     */
    public List<PrescriptionResponse> getByCustomer(Long customerId) {
        return prescriptionRepo.findByCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Approves a pending prescription.
     *
     * @param id the prescription's primary key
     * @return the updated prescription with APPROVED status
     * @throws PrescriptionNotFoundException if no prescription exists with the given ID
     */
    public PrescriptionResponse approve(Long id) {
        Prescription p = findOrThrow(id);
        p.setStatus(Prescription.PrescriptionStatus.APPROVED);
        return toResponse(prescriptionRepo.save(p));
    }

    /**
     * Rejects a pending prescription with a reason.
     *
     * @param id     the prescription's primary key
     * @param reason the reason for rejection
     * @return the updated prescription with REJECTED status and rejection reason
     * @throws PrescriptionNotFoundException if no prescription exists with the given ID
     */
    public PrescriptionResponse reject(Long id, String reason) {
        Prescription p = findOrThrow(id);
        p.setStatus(Prescription.PrescriptionStatus.REJECTED);
        p.setRejectionReason(reason);
        return toResponse(prescriptionRepo.save(p));
    }

    /**
     * Finds a prescription by ID or throws if not found.
     *
     * @param id the prescription's primary key
     * @return the prescription entity
     * @throws PrescriptionNotFoundException if not found
     */
    private Prescription findOrThrow(Long id) {
        return prescriptionRepo.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription not found with id: " + id));
    }

    /**
     * Converts a Prescription entity to a PrescriptionResponse DTO.
     *
     * @param p the prescription entity
     * @return the response DTO
     */
    private PrescriptionResponse toResponse(Prescription p) {
        PrescriptionResponse r = new PrescriptionResponse();
        r.setId(p.getId());
        r.setCustomerId(p.getCustomerId());
        r.setCustomerEmail(p.getCustomerEmail());
        r.setImageUrl(p.getImageUrl());
        r.setStatus(p.getStatus().name());
        r.setRejectionReason(p.getRejectionReason());
        r.setUploadedAt(p.getUploadedAt());
        return r;
    }
}
