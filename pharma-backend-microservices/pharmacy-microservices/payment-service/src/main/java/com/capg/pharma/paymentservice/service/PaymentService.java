package com.capg.pharma.paymentservice.service;

import com.capg.pharma.paymentservice.dto.PaymentRequest;
import com.capg.pharma.paymentservice.dto.PaymentResponse;
import com.capg.pharma.paymentservice.entity.Payment;
import com.capg.pharma.paymentservice.exception.PaymentNotFoundException;
import com.capg.pharma.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Business logic service for payment processing.
 *
 * <p>Simulates payment processing by generating a transaction ID and
 * recording the payment as SUCCESS. In production, this would integrate
 * with a real payment gateway (Razorpay, Stripe, etc.).</p>
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;

    /**
     * Constructs PaymentService with the payment repository.
     *
     * @param paymentRepo repository for payment persistence
     */
    public PaymentService(PaymentRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    /**
     * Processes a payment for an order.
     *
     * <p>Creates a payment record with a generated transaction ID and
     * SUCCESS status. In production, this would call an external payment gateway.</p>
     *
     * @param req the payment request with order ID, amount, and method
     * @return the created payment record as a response DTO
     */
    public PaymentResponse processPayment(PaymentRequest req) {
        Payment payment = new Payment();
        payment.setOrderId(req.getOrderId());
        payment.setCustomerId(req.getCustomerId());
        payment.setAmount(req.getAmount());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Payment saved = paymentRepo.save(payment);
        return toResponse(saved);
    }

    /**
     * Retrieves the payment record for a given order.
     *
     * @param orderId the order's ID
     * @return the payment record as a response DTO
     * @throws PaymentNotFoundException if no payment exists for the given order ID
     */
    public PaymentResponse getByOrderId(Long orderId) {
        return paymentRepo.findByOrderId(orderId).stream()
                .findFirst().map(this::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order: " + orderId));
    }

    /**
     * Converts a Payment entity to a PaymentResponse DTO.
     *
     * @param p the payment entity
     * @return the response DTO
     */
    private PaymentResponse toResponse(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(p.getId());
        r.setOrderId(p.getOrderId());
        r.setAmount(p.getAmount());
        r.setStatus(p.getStatus().name());
        r.setTransactionId(p.getTransactionId());
        r.setCreatedAt(p.getCreatedAt());
        return r;
    }
}
