package com.capg.pharma.adminservice.service;

import com.capg.pharma.adminservice.client.CatalogClient;
import com.capg.pharma.adminservice.client.OrderClient;
import com.capg.pharma.adminservice.dto.*;
import com.capg.pharma.adminservice.entity.AuditLog;
import com.capg.pharma.adminservice.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock CatalogClient catalogClient;
    @Mock OrderClient orderClient;
    @Mock AuditLogRepository auditLogRepo;

    @InjectMocks AdminService adminService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", null, List.of())
        );
    }

    @Test
    void getDashboard_returnsAggregatedData() {
        when(orderClient.getTotalOrderCount()).thenReturn(50L);
        when(catalogClient.getPendingPrescriptions()).thenReturn(List.of(new PrescriptionResponse()));
        when(catalogClient.getLowStockCount()).thenReturn(3L);
        when(orderClient.getRevenue(anyString(), anyString()))
                .thenReturn(Map.of("revenue", new BigDecimal("5000.00")));

        DashboardResponse result = adminService.getDashboard();
        assertThat(result.getTotalOrders()).isEqualTo(50L);
        assertThat(result.getPendingPrescriptions()).isEqualTo(1L);
        assertThat(result.getLowStockCount()).isEqualTo(3L);
        assertThat(result.getMonthlyRevenue()).isEqualByComparingTo("5000.00");
    }

    @Test
    void addMedicine_savesAndAudits() {
        MedicineRequest req = new MedicineRequest();
        req.setName("Ibuprofen");

        MedicineResponse resp = new MedicineResponse();
        resp.setId(1L);
        resp.setName("Ibuprofen");

        when(catalogClient.addMedicine(req)).thenReturn(resp);
        when(auditLogRepo.save(any(AuditLog.class))).thenReturn(new AuditLog());

        MedicineResponse result = adminService.addMedicine(req);
        assertThat(result.getName()).isEqualTo("Ibuprofen");
        verify(auditLogRepo).save(any(AuditLog.class));
    }

    @Test
    void deleteMedicine_callsClientAndAudits() {
        doNothing().when(catalogClient).deleteMedicine(1L);
        when(auditLogRepo.save(any(AuditLog.class))).thenReturn(new AuditLog());

        adminService.deleteMedicine(1L);
        verify(catalogClient).deleteMedicine(1L);
        verify(auditLogRepo).save(any(AuditLog.class));
    }

    @Test
    void getAllOrders_returnsOrders() {
        OrderResponse order = new OrderResponse();
        order.setId(1L);
        when(orderClient.getAllOrders()).thenReturn(List.of(order));

        List<OrderResponse> result = adminService.getAllOrders();
        assertThat(result).hasSize(1);
    }

    @Test
    void updateOrderStatus_updatesAndAudits() {
        OrderResponse order = new OrderResponse();
        order.setId(1L);
        order.setStatus("SHIPPED");

        when(orderClient.updateOrderStatus(eq(1L), anyMap())).thenReturn(order);
        when(auditLogRepo.save(any(AuditLog.class))).thenReturn(new AuditLog());

        OrderResponse result = adminService.updateOrderStatus(1L, "SHIPPED");
        assertThat(result.getStatus()).isEqualTo("SHIPPED");
        verify(auditLogRepo).save(any(AuditLog.class));
    }

    @Test
    void approvePrescription_approvesAndAudits() {
        PrescriptionResponse resp = new PrescriptionResponse();
        resp.setId(1L);
        resp.setStatus("APPROVED");

        when(catalogClient.approvePrescription(1L)).thenReturn(resp);
        when(auditLogRepo.save(any(AuditLog.class))).thenReturn(new AuditLog());

        PrescriptionResponse result = adminService.approvePrescription(1L);
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void rejectPrescription_rejectsAndAudits() {
        PrescriptionResponse resp = new PrescriptionResponse();
        resp.setId(1L);
        resp.setStatus("REJECTED");

        when(catalogClient.rejectPrescription(eq(1L), anyMap())).thenReturn(resp);
        when(auditLogRepo.save(any(AuditLog.class))).thenReturn(new AuditLog());

        PrescriptionResponse result = adminService.rejectPrescription(1L, "Illegible");
        assertThat(result.getStatus()).isEqualTo("REJECTED");
    }
}
