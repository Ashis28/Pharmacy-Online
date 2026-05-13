package com.capg.pharma.paymentservice.exception;

/** Thrown when a payment gateway returns a failure response. Results in 422. */
public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) { super(message); }
}
