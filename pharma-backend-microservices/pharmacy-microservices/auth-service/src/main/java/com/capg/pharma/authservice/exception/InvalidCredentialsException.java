package com.capg.pharma.authservice.exception;

/**
 * Thrown when a login attempt provides a correct email but an incorrect password.
 * Results in a {@code 401 Unauthorized} HTTP response.
 */
public class InvalidCredentialsException extends RuntimeException {
    /**
     * @param message descriptive error message
     */
    public InvalidCredentialsException(String message) { super(message); }
}
