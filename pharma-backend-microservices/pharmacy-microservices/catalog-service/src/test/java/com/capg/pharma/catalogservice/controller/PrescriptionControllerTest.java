package com.capg.pharma.catalogservice.controller;

import com.capg.pharma.catalogservice.config.TestSecurityConfig;
import com.capg.pharma.catalogservice.dto.PrescriptionResponse;
import com.capg.pharma.catalogservice.exception.PrescriptionNotFoundException;
import com.capg.pharma.catalogservice.service.PrescriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PrescriptionController.class)
@Import(TestSecurityConfig.class)
class PrescriptionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PrescriptionService prescriptionService;

    private PrescriptionResponse response;

    @BeforeEach
    void setUp() {
        response = new PrescriptionResponse();
        response.setId(1L);
        response.setCustomerId(10L);
        response.setCustomerEmail("customer@example.com");
        response.setImageUrl("http://example.com/rx.jpg");
        response.setStatus("PENDING");
        response.setUploadedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void upload_returnsOk() throws Exception {
        when(prescriptionService.upload(anyLong(), anyString(), anyString())).thenReturn(response);

        Map<String, String> body = Map.of("customerId", "10", "imageUrl", "http://example.com/rx.jpg");

        mockMvc.perform(post("/api/catalog/prescriptions/upload")
                        .with(csrf())
                        .header("X-Auth-User", "customer@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void myPrescriptions_returnsOk() throws Exception {
        when(prescriptionService.getByCustomer(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/prescriptions/my").param("customerId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerEmail").value("customer@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPending_returnsOk() throws Exception {
        when(prescriptionService.getPending()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/prescriptions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_returnsOk() throws Exception {
        response.setStatus("APPROVED");
        when(prescriptionService.approve(1L)).thenReturn(response);

        mockMvc.perform(put("/api/catalog/prescriptions/1/approve").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_returns404WhenNotFound() throws Exception {
        when(prescriptionService.approve(99L))
                .thenThrow(new PrescriptionNotFoundException("Prescription not found with id: 99"));

        mockMvc.perform(put("/api/catalog/prescriptions/99/approve").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_returnsOk() throws Exception {
        response.setStatus("REJECTED");
        response.setRejectionReason("Illegible");
        when(prescriptionService.reject(eq(1L), anyString())).thenReturn(response);

        Map<String, String> body = Map.of("reason", "Illegible");

        mockMvc.perform(put("/api/catalog/prescriptions/1/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Illegible"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_returns404WhenNotFound() throws Exception {
        when(prescriptionService.reject(eq(99L), anyString()))
                .thenThrow(new PrescriptionNotFoundException("Prescription not found with id: 99"));

        Map<String, String> body = Map.of("reason", "Illegible");

        mockMvc.perform(put("/api/catalog/prescriptions/99/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }
}
