package com.capg.pharma.adminservice.repository;

import com.capg.pharma.adminservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AuditLog} entities.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds all audit log entries for a specific admin, ordered by most recent first.
     *
     * @param adminEmail the admin's email address
     * @return list of audit log entries for that admin
     */
    List<AuditLog> findByAdminEmailOrderByTimestampDesc(String adminEmail);
}
