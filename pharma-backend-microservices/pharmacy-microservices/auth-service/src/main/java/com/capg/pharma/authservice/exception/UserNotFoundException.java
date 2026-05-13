package com.capg.pharma.authservice.exception;

/**
 * Thrown when a login attempt is made with an email that does not exist in the database.
 * Results in a {@code 404 Not Found} HTTP response.
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * @param message descriptive error message including the missing email
     */
    public UserNotFoundException(String message) { super(message); }
}
