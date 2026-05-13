package com.capg.pharma.orderservice.exception;

/** Thrown when an order ID is not found. Results in 404. */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) { super(message); }
}
