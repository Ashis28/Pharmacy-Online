package com.capg.pharma.authservice.exception;

/**
 * Thrown when a signup attempt is made with an email that is already registered.
 * Results in a {@code 409 Conflict} HTTP response.
 */
public class UserAlreadyExistsException extends RuntimeException {
    /**
     * @param message descriptive error message including the duplicate email
     */
    public UserAlreadyExistsException(String message) { super(message); }
}
