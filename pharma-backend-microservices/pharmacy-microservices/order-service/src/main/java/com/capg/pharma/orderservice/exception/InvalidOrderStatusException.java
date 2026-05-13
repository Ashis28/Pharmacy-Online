package com.capg.pharma.orderservice.exception;

/** Thrown when an invalid order status string is provided. Results in 400. */
public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) { super(message); }
}
