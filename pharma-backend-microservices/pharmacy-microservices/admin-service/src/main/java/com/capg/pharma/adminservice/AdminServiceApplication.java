package com.capg.pharma.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Admin Service.
 *
 * <p>Acts as an orchestrator for admin operations. Delegates data operations
 * to catalog-service and order-service via Feign clients, and maintains
 * an audit log of all admin actions in its own database.</p>
 *
 * <p>All endpoints require ADMIN role. Runs on port 9094. Database: {@code admin_db}.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AdminServiceApplication {

    /**
     * Bootstraps the Admin Service.
     *hello
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
