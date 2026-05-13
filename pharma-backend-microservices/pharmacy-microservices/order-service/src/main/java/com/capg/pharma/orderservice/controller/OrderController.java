package com.capg.pharma.orderservice.controller;

import com.capg.pharma.orderservice.dto.OrderRequest;
import com.capg.pharma.orderservice.dto.OrderResponse;
import com.capg.pharma.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for order management endpoints.
 *
 * <p>Customers can place orders and view their own. Admins can view all orders,
 * update statuses, and retrieve revenue/count statistics.</p>
 *
 * <p>Base path: {@code /api/orders}</p>
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order placement and lifecycle management")
public class OrderController {

    private final OrderService orderService;

    /**
     * Constructs the controller with the order service.
     *
     * @param orderService the service handling order business logic
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Places a new order for the authenticated customer.
     * Stock is decremented AFTER the order transaction commits.
     */
    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest req,
            @RequestHeader("X-Auth-User") String email) {
        OrderResponse order = orderService.placeOrder(req, email);
        // Decrement stock after the order transaction has committed
        orderService.decrementStockForOrder(req.getItems());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Retrieves a single order by its ID.
     *
     * @param id the order's primary key
     * @return {@code 200 OK} with the order, or {@code 404} if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    /**
     * Retrieves all orders for a specific customer.
     *
     * @param customerId the customer's ID
     * @return {@code 200 OK} with the customer's order history
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable("customerId") Long customerId) {
        return ResponseEntity.ok(orderService.getByCustomer(customerId));
    }

    /**
     * Retrieves all orders in the system. Admin only.
     *
     * @return {@code 200 OK} with all orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders (Admin)")
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    /**
     * Marks an order as PAID after successful payment. Any authenticated user can call this.
     *
     * @param id the order's primary key
     * @return {@code 200 OK} with the updated order
     */
    @PutMapping("/{id}/pay")
    @Operation(summary = "Mark order as PAID after payment")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.updateStatus(id, "PAID"));
    }

    /**
     * Updates the status of an order. Admin only.
     *
     * @param id   the order's primary key
     * @param body request body containing {@code status}
     * @return {@code 200 OK} with the updated order
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status (Admin)")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable("id") Long id,
                                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.updateStatus(id, body.get("status")));
    }

    /**
     * Returns the total count of all orders. Admin only.
     *
     * @return {@code 200 OK} with the order count
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total order count (Admin)")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(orderService.getCount());
    }

    /**
     * Returns total revenue from DELIVERED orders within a date range. Admin only.
     *
     * @param from start date (ISO format: yyyy-MM-dd)
     * @param to   end date (ISO format: yyyy-MM-dd)
     * @return {@code 200 OK} with revenue amount
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get revenue between dates (Admin)")
    public ResponseEntity<Map<String, Object>> getRevenue(@RequestParam String from,
                                                           @RequestParam String to) {
        return ResponseEntity.ok(Map.of("revenue", orderService.getRevenueBetween(from, to)));
    }
}
