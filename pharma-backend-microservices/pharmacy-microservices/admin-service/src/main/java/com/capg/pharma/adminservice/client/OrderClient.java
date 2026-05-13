package com.capg.pharma.adminservice.client;

import com.capg.pharma.adminservice.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign client for the Order Service.
 *
 * <p>Used by the Admin Service to retrieve orders, update statuses,
 * and fetch revenue/count statistics for the dashboard.</p>
 */
@FeignClient(name = "order-service")
public interface OrderClient {

    /**
     * Retrieves all orders in the system.
     *
     * @return list of all orders
     */
    @GetMapping("/api/orders")
    List<OrderResponse> getAllOrders();

    /**
     * Updates the status of an order.
     *
     * @param id   the order's primary key
     * @param body map containing {@code status}
     * @return the updated order
     */
    @PutMapping("/api/orders/{id}/status")
    OrderResponse updateOrderStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> body);

    /**
     * Returns the total count of all orders.
     *
     * @return total order count
     */
    @GetMapping("/api/orders/count")
    long getTotalOrderCount();

    /**
     * Returns revenue from DELIVERED orders within a date range.
     *
     * @param from start date (ISO format)
     * @param to   end date (ISO format)
     * @return map containing {@code revenue} key with the total amount
     */
    @GetMapping("/api/orders/revenue")
    Map<String, Object> getRevenue(@RequestParam("from") String from, @RequestParam("to") String to);
}
