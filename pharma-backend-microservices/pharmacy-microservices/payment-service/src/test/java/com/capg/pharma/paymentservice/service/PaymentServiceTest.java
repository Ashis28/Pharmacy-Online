package com.capg.pharma.paymentservice.service;

import com.capg.pharma.paymentservice.dto.PaymentRequest;
import com.capg.pharma.paymentservice.dto.PaymentResponse;
import com.capg.pharma.paymentservice.entity.Payment;
import com.capg.pharma.paymentservice.exception.PaymentNotFoundException;
import com.capg.pharma.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepo;

    @InjectMocks PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(100L);
        payment.setCustomerId(10L);
        payment.setAmount(new BigDecimal("250.00"));
        payment.setPaymentMethod("CARD");
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId("TXN-ABCD1234");
    }

    @Test
    void processPayment_savesAndReturnsPayment() {
        PaymentRequest req = new PaymentRequest();
        req.setOrderId(100L);
        req.setCustomerId(10L);
        req.setAmount(new BigDecimal("250.00"));
        req.setPaymentMethod("CARD");

        when(paymentRepo.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse result = paymentService.processPayment(req);
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTransactionId()).isNotBlank();
        verify(paymentRepo).save(any(Payment.class));
    }

    @Test
    void getByOrderId_returnsPayment() {
        when(paymentRepo.findByOrderId(100L)).thenReturn(List.of(payment));
        PaymentResponse result = paymentService.getByOrderId(100L);
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getTransactionId()).isEqualTo("TXN-ABCD1234");
    }

    @Test
    void getByOrderId_throwsWhenNotFound() {
        when(paymentRepo.findByOrderId(999L)).thenReturn(List.of());
        assertThatThrownBy(() -> paymentService.getByOrderId(999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void processPayment_generatesUniqueTransactionId() {
        PaymentRequest req = new PaymentRequest();
        req.setOrderId(101L);
        req.setCustomerId(11L);
        req.setAmount(new BigDecimal("100.00"));
        req.setPaymentMethod("UPI");

        Payment p1 = new Payment();
        p1.setId(2L);
        p1.setOrderId(101L);
        p1.setStatus(Payment.PaymentStatus.SUCCESS);
        p1.setTransactionId("TXN-UNIQUE01");

        when(paymentRepo.save(any(Payment.class))).thenReturn(p1);

        PaymentResponse result = paymentService.processPayment(req);
        assertThat(result.getTransactionId()).startsWith("TXN-");
    }
}
