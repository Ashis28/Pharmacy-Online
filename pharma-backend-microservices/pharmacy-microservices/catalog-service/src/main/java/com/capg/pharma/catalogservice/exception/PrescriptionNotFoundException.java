package com.capg.pharma.catalogservice.exception;

/** Thrown when a prescription ID is not found. Results in 404. */
public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(String message) { super(message); }
}
