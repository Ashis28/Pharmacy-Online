package com.capg.pharma.paymentservice.exception;

/** Thrown when no payment record exists for a given order ID. Results in 404. */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) { super(message); }
}
