package com.capg.pharma.catalogservice.service;

import com.capg.pharma.catalogservice.dto.PrescriptionResponse;
import com.capg.pharma.catalogservice.entity.Prescription;
import com.capg.pharma.catalogservice.exception.PrescriptionNotFoundException;
import com.capg.pharma.catalogservice.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock PrescriptionRepository prescriptionRepo;

    @InjectMocks PrescriptionService prescriptionService;

    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setId(1L);
        prescription.setCustomerId(10L);
        prescription.setCustomerEmail("customer@example.com");
        prescription.setImageUrl("http://example.com/rx.jpg");
        prescription.setStatus(Prescription.PrescriptionStatus.PENDING);
    }

    @Test
    void upload_savesAndReturnsPrescription() {
        when(prescriptionRepo.save(any(Prescription.class))).thenReturn(prescription);
        PrescriptionResponse result = prescriptionService.upload(10L, "customer@example.com", "http://example.com/rx.jpg");
        assertThat(result.getCustomerId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getPending_returnsPendingPrescriptions() {
        when(prescriptionRepo.findByStatus(Prescription.PrescriptionStatus.PENDING))
                .thenReturn(List.of(prescription));
        List<PrescriptionResponse> result = prescriptionService.getPending();
        assertThat(result).hasSize(1);
    }

    @Test
    void approve_changesStatusToApproved() {
        when(prescriptionRepo.findById(1L)).thenReturn(Optional.of(prescription));
        prescription.setStatus(Prescription.PrescriptionStatus.APPROVED);
        when(prescriptionRepo.save(any())).thenReturn(prescription);

        PrescriptionResponse result = prescriptionService.approve(1L);
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void approve_throwsWhenNotFound() {
        when(prescriptionRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> prescriptionService.approve(99L))
                .isInstanceOf(PrescriptionNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void reject_changesStatusToRejected() {
        when(prescriptionRepo.findById(1L)).thenReturn(Optional.of(prescription));
        prescription.setStatus(Prescription.PrescriptionStatus.REJECTED);
        prescription.setRejectionReason("Illegible");
        when(prescriptionRepo.save(any())).thenReturn(prescription);

        PrescriptionResponse result = prescriptionService.reject(1L, "Illegible");
        assertThat(result.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void reject_throwsWhenNotFound() {
        when(prescriptionRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> prescriptionService.reject(99L, "reason"))
                .isInstanceOf(PrescriptionNotFoundException.class);
    }

    @Test
    void getByCustomer_returnsCustomerPrescriptions() {
        when(prescriptionRepo.findByCustomerId(10L)).thenReturn(List.of(prescription));
        List<PrescriptionResponse> result = prescriptionService.getByCustomer(10L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("customer@example.com");
    }
}
