package com.capg.pharma.catalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Catalog Service.
 *
 * <p>Manages the medicine catalog including categories, medicines, and prescription uploads.
 * Exposes REST APIs consumed by customers (browse/search), admins (CRUD), and
 * the order-service (price lookup via Feign).</p>
 *
 * <p>Runs on port 9092. Database: {@code catalog_db}.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CatalogServiceApplication {

    /**
     * Bootstraps the Catalog Service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
