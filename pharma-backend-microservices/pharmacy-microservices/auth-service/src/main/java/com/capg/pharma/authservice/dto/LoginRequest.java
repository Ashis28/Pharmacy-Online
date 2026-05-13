package com.capg.pharma.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 *
 * <p>On successful login, the service returns a {@link LoginResponse} containing
 * a signed JWT token valid for 24 hours.</p>
 */
public class LoginRequest {

    /** User's registered email address. */
    @Email
    @NotBlank
    private String email;

    /** User's plain-text password for verification against the stored BCrypt hash. */
    @NotBlank
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
