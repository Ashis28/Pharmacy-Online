package com.capg.pharma.adminservice.service;

import com.capg.pharma.adminservice.client.CatalogClient;
import com.capg.pharma.adminservice.client.OrderClient;
import com.capg.pharma.adminservice.dto.*;
import com.capg.pharma.adminservice.entity.AuditLog;
import com.capg.pharma.adminservice.repository.AuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business logic service for admin operations.
 *
 * <p>Orchestrates admin actions by delegating to catalog-service and order-service
 * via Feign clients. Every write operation is recorded in the audit log.</p>
 */
@Service
public class AdminService {

    private final CatalogClient catalogClient;
    private final OrderClient orderClient;
    private final AuditLogRepository auditLogRepo;

    /**
     * Constructs AdminService with required dependencies.
     *
     * @param catalogClient  Feign client for catalog operations
     * @param orderClient    Feign client for order operations
     * @param auditLogRepo   repository for audit log persistence
     */
    public AdminService(CatalogClient catalogClient, OrderClient orderClient,
                        AuditLogRepository auditLogRepo) {
        this.catalogClient = catalogClient;
        this.orderClient = orderClient;
        this.auditLogRepo = auditLogRepo;
    }

    /**
     * Builds the admin dashboard summary.
     *
     * <p>Aggregates total orders, pending prescriptions, low-stock count,
     * and monthly revenue from downstream services.</p>
     *
     * @return the dashboard summary DTO
     */
    public DashboardResponse getDashboard() {
        long totalOrders = orderClient.getTotalOrderCount();
        long pendingRx = catalogClient.getPendingPrescriptions().size();
        long lowStock = catalogClient.getLowStockCount();

        String today = LocalDate.now().toString();
        String monthStart = LocalDate.now().withDayOfMonth(1).toString();
        Map<String, Object> revenueMap = orderClient.getRevenue(monthStart, today);
        BigDecimal revenue = revenueMap.get("revenue") instanceof BigDecimal bd
                ? bd : new BigDecimal(revenueMap.get("revenue").toString());

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .pendingPrescriptions(pendingRx)
                .lowStockCount(lowStock)
                .monthlyRevenue(revenue)
                .build();
    }

    /**
     * Adds a new medicine to the catalog and records the action in the audit log.
     *
     * @param req the medicine creation request
     * @return the created medicine
     */
    public MedicineResponse addMedicine(MedicineRequest req) {
        MedicineResponse r = catalogClient.addMedicine(req);
        audit("ADD_MEDICINE", "Medicine", String.valueOf(r.getId()), "Added: " + req.getName());
        return r;
    }

    /**
     * Updates an existing medicine and records the action in the audit log.
     *
     * @param id  the medicine's primary key
     * @param req the update request
     * @return the updated medicine
     */
    public MedicineResponse updateMedicine(Long id, MedicineRequest req) {
        MedicineResponse r = catalogClient.updateMedicine(id, req);
        audit("UPDATE_MEDICINE", "Medicine", String.valueOf(id), "Updated: " + req.getName());
        return r;
    }

    /**
     * Deletes a medicine and records the action in the audit log.
     *
     * @param id the medicine's primary key
     */
    public void deleteMedicine(Long id) {
        catalogClient.deleteMedicine(id);
        audit("DELETE_MEDICINE", "Medicine", String.valueOf(id), "Deleted medicine id=" + id);
    }

    /**
     * Retrieves all prescriptions awaiting admin review.
     *
     * @return list of PENDING prescriptions
     */
    public List<PrescriptionResponse> getPendingPrescriptions() {
        return catalogClient.getPendingPrescriptions();
    }

    /**
     * Approves a prescription and records the action in the audit log.
     *
     * @param id the prescription's primary key
     * @return the updated prescription
     */
    public PrescriptionResponse approvePrescription(Long id) {
        PrescriptionResponse r = catalogClient.approvePrescription(id);
        audit("APPROVE_PRESCRIPTION", "Prescription", String.valueOf(id), "Approved");
        return r;
    }

    /**
     * Rejects a prescription with a reason and records the action in the audit log.
     *
     * @param id     the prescription's primary key
     * @param reason the rejection reason
     * @return the updated prescription
     */
    public PrescriptionResponse rejectPrescription(Long id, String reason) {
        PrescriptionResponse r = catalogClient.rejectPrescription(id, Map.of("reason", reason));
        audit("REJECT_PRESCRIPTION", "Prescription", String.valueOf(id), "Rejected: " + reason);
        return r;
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return list of all orders
     */
    public List<OrderResponse> getAllOrders() {
        return orderClient.getAllOrders();
    }

    /**
     * Updates an order's status and records the action in the audit log.
     *
     * @param id     the order's primary key
     * @param status the new status string
     * @return the updated order
     */
    public OrderResponse updateOrderStatus(Long id, String status) {
        OrderResponse r = orderClient.updateOrderStatus(id, Map.of("status", status));
        audit("UPDATE_ORDER_STATUS", "Order", String.valueOf(id), "Status -> " + status);
        return r;
    }

    /**
     * Records an admin action in the audit log.
     *
     * @param action   the action code (e.g. "ADD_MEDICINE")
     * @param entity   the entity type affected (e.g. "Medicine")
     * @param targetId the ID of the affected entity
     * @param details  human-readable description of the action
     */
    private void audit(String action, String entity, String targetId, String details) {
        String admin = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogRepo.save(AuditLog.builder()
                .adminEmail(admin).action(action)
                .targetEntity(entity).targetId(targetId).details(details)
                .build());
    }
}
