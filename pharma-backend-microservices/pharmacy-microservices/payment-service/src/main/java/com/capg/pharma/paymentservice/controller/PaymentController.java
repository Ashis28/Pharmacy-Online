package com.capg.pharma.paymentservice.controller;

import com.capg.pharma.paymentservice.dto.PaymentRequest;
import com.capg.pharma.paymentservice.dto.PaymentResponse;
import com.capg.pharma.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment processing endpoints.
 *
 * <p>Base path: {@code /api/payments}</p>
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing and transaction records")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Constructs the controller with the payment service.
     *
     * @param paymentService the service handling payment business logic
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Processes a payment for an order.
     *
     * @param req the payment request with order ID, amount, and method
     * @return {@code 200 OK} with the payment transaction record
     */
    @PostMapping("/process")
    @Operation(summary = "Process a payment for an order")
    public ResponseEntity<PaymentResponse> process(@RequestBody PaymentRequest req) {
        return ResponseEntity.ok(paymentService.processPayment(req));
    }

    /**
     * Retrieves the payment record for a given order.
     *
     * @param orderId the order's ID
     * @return {@code 200 OK} with the payment record, or {@code 404} if not found
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order ID")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }
}
