package com.capg.pharma.orderservice.client;

import com.capg.pharma.orderservice.dto.MedicineDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for the Catalog Service.
 *
 * <p>Used by the Order Service to fetch medicine details (name, price)
 * at order placement time, and to decrement stock after an order is placed.</p>
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    /**
     * Fetches a medicine by its ID from the catalog service.
     *
     * @param id the medicine's primary key
     * @return the medicine details as a DTO
     */
    @GetMapping("/api/catalog/medicines/{id}")
    MedicineDto getMedicineById(@PathVariable("id") Long id);

    /**
     * Decrements the stock of a medicine after an order is placed.
     *
     * @param id       the medicine's primary key
     * @param quantity the quantity to deduct
     */
    @PatchMapping("/api/catalog/medicines/{id}/stock")
    void decrementStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}
