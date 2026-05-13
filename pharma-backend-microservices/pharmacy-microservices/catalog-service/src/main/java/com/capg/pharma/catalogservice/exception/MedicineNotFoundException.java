package com.capg.pharma.catalogservice.exception;

/** Thrown when a medicine ID is not found in the catalog. Results in 404. */
public class MedicineNotFoundException extends RuntimeException {
    public MedicineNotFoundException(String message) { super(message); }
}
