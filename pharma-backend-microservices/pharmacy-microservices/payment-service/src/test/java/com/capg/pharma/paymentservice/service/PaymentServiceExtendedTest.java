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

/**
 * Extended tests for PaymentService covering all payment methods and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceExtendedTest {

    @Mock PaymentRepository paymentRepo;

    @InjectMocks PaymentService paymentService;

    private Payment successPayment;

    @BeforeEach
    void setUp() {
        successPayment = new Payment();
        successPayment.setId(1L);
        successPayment.setOrderId(100L);
        successPayment.setCustomerId(10L);
        successPayment.setAmount(new BigDecimal("250.00"));
        successPayment.setStatus(Payment.PaymentStatus.SUCCESS);
        successPayment.setTransactionId("TXN-TEST001");
    }

    @Test
    void processPayment_upi_returnsSuccess() {
        successPayment.setPaymentMethod("UPI");
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        PaymentRequest req = buildRequest("UPI", 100L, new BigDecimal("250.00"));
        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getTransactionId()).isNotBlank();
    }

    @Test
    void processPayment_card_returnsSuccess() {
        successPayment.setPaymentMethod("CARD");
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        PaymentRequest req = buildRequest("CARD", 101L, new BigDecimal("500.00"));
        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void processPayment_netBanking_returnsSuccess() {
        successPayment.setPaymentMethod("NET_BANKING");
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        PaymentRequest req = buildRequest("NET_BANKING", 102L, new BigDecimal("750.00"));
        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void processPayment_cash_returnsSuccess() {
        successPayment.setPaymentMethod("CASH");
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        PaymentRequest req = buildRequest("CASH", 103L, new BigDecimal("95.00"));
        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void processPayment_transactionIdStartsWithTxnPrefix() {
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        PaymentRequest req = buildRequest("UPI", 104L, new BigDecimal("100.00"));
        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getTransactionId()).startsWith("TXN-");
    }

    @Test
    void processPayment_savesPaymentToRepository() {
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        paymentService.processPayment(buildRequest("CARD", 105L, new BigDecimal("200.00")));

        verify(paymentRepo, times(1)).save(any(Payment.class));
    }

    @Test
    void getByOrderId_returnsLatestPayment() {
        Payment p1 = new Payment();
        p1.setId(1L);
        p1.setOrderId(200L);
        p1.setStatus(Payment.PaymentStatus.SUCCESS);
        p1.setTransactionId("TXN-FIRST");

        when(paymentRepo.findByOrderId(200L)).thenReturn(List.of(p1));

        PaymentResponse result = paymentService.getByOrderId(200L);
        assertThat(result.getTransactionId()).isEqualTo("TXN-FIRST");
    }

    @Test
    void getByOrderId_throwsPaymentNotFoundException() {
        when(paymentRepo.findByOrderId(999L)).thenReturn(List.of());

        assertThatThrownBy(() -> paymentService.getByOrderId(999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void processPayment_amountIsPreservedInResponse() {
        successPayment.setAmount(new BigDecimal("333.50"));
        when(paymentRepo.save(any(Payment.class))).thenReturn(successPayment);

        PaymentRequest req = buildRequest("UPI", 106L, new BigDecimal("333.50"));
        PaymentResponse result = paymentService.processPayment(req);

        assertThat(result.getAmount()).isEqualByComparingTo("333.50");
    }

    private PaymentRequest buildRequest(String method, Long orderId, BigDecimal amount) {
        PaymentRequest req = new PaymentRequest();
        req.setOrderId(orderId);
        req.setCustomerId(10L);
        req.setAmount(amount);
        req.setPaymentMethod(method);
        return req;
    }
}
