package com.capg.pharma.orderservice.client;

import com.capg.pharma.orderservice.dto.PaymentRequest;
import com.capg.pharma.orderservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the Payment Service.
 *
 * <p>Used by the Order Service to trigger payment processing
 * after an order is placed (optional integration).</p>
 */
@FeignClient(name = "payment-service")
public interface PaymentClient {

    /**
     * Initiates payment processing for an order.
     *
     * @param request the payment details including order ID, amount, and method
     * @return the payment result with transaction ID and status
     */
    @PostMapping("/api/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
}
