package com.capg.pharma.catalogservice.controller;

import com.capg.pharma.catalogservice.config.TestSecurityConfig;
import com.capg.pharma.catalogservice.dto.MedicineRequest;
import com.capg.pharma.catalogservice.dto.MedicineResponse;
import com.capg.pharma.catalogservice.exception.MedicineNotFoundException;
import com.capg.pharma.catalogservice.service.MedicineService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MedicineController.class)
@Import(TestSecurityConfig.class)
class MedicineControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean MedicineService medicineService;

    private MedicineResponse response;
    private MedicineRequest request;

    @BeforeEach
    void setUp() {
        response = new MedicineResponse();
        response.setId(1L);
        response.setName("Amoxicillin");
        response.setPrice(new BigDecimal("50.00"));
        response.setStockQuantity(100);
        response.setCategoryName("Antibiotics");

        request = new MedicineRequest();
        request.setName("Amoxicillin");
        request.setPrice(new BigDecimal("50.00"));
        request.setStockQuantity(100);
        request.setCategoryId(1L);
    }

    @Test
    void getAll_returnsOk() throws Exception {
        when(medicineService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Amoxicillin"));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(medicineService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/catalog/medicines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(medicineService.getById(99L))
                .thenThrow(new MedicineNotFoundException("Medicine not found with id: 99"));

        mockMvc.perform(get("/api/catalog/medicines/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Medicine not found with id: 99"));
    }

    @Test
    void search_returnsOk() throws Exception {
        when(medicineService.search("amox")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalog/medicines/search").param("name", "amox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Amoxicillin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLowStockCount_returnsOk() throws Exception {
        when(medicineService.getLowStockCount()).thenReturn(3L);

        mockMvc.perform(get("/api/catalog/medicines/low-stock-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_returns201() throws Exception {
        when(medicineService.create(any(MedicineRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/catalog/medicines")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Amoxicillin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_returnsOk() throws Exception {
        when(medicineService.update(eq(1L), any(MedicineRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/catalog/medicines/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amoxicillin"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns204() throws Exception {
        doNothing().when(medicineService).delete(1L);

        mockMvc.perform(delete("/api/catalog/medicines/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_returns404WhenNotFound() throws Exception {
        doThrow(new MedicineNotFoundException("Medicine not found with id: 99"))
                .when(medicineService).delete(99L);

        mockMvc.perform(delete("/api/catalog/medicines/99").with(csrf()))
                .andExpect(status().isNotFound());
    }
}
