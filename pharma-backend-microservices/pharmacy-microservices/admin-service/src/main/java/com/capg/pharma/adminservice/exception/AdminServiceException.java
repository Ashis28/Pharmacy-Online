package com.capg.pharma.adminservice.exception;

/** Thrown for general admin business rule violations. Results in 400. */
public class AdminServiceException extends RuntimeException {
    public AdminServiceException(String message) { super(message); }
}
