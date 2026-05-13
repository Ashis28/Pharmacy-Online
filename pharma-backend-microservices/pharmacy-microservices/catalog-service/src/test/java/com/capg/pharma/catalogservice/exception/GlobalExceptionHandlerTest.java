package com.capg.pharma.catalogservice.exception;

import com.capg.pharma.catalogservice.config.TestSecurityConfig;
import com.capg.pharma.catalogservice.controller.MedicineController;
import com.capg.pharma.catalogservice.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MedicineController.class)
@Import(TestSecurityConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;

    @MockBean MedicineService medicineService;

    @Test
    void handleMedicineNotFound_returns404() throws Exception {
        when(medicineService.getById(99L))
                .thenThrow(new MedicineNotFoundException("Medicine not found with id: 99"));

        mockMvc.perform(get("/api/catalog/medicines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Medicine not found with id: 99"));
    }

    @Test
    void handleCategoryNotFound_returns404() throws Exception {
        when(medicineService.getById(1L))
                .thenThrow(new CategoryNotFoundException("Category not found with id: 5"));

        mockMvc.perform(get("/api/catalog/medicines/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handlePrescriptionNotFound_returns404() throws Exception {
        when(medicineService.getById(1L))
                .thenThrow(new PrescriptionNotFoundException("Prescription not found with id: 1"));

        mockMvc.perform(get("/api/catalog/medicines/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handleIllegalArgument_returns400() throws Exception {
        when(medicineService.getById(1L))
                .thenThrow(new IllegalArgumentException("Bad argument"));

        mockMvc.perform(get("/api/catalog/medicines/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void handleGenericException_returns500() throws Exception {
        when(medicineService.getById(1L))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/catalog/medicines/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void handleValidation_returns400OnInvalidRequest() throws Exception {
        // Empty name (@NotBlank) and null price/stock (@NotNull) trigger validation
        String invalidJson = "{\"name\":\"\",\"price\":null,\"stockQuantity\":null}";

        mockMvc.perform(post("/api/catalog/medicines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
