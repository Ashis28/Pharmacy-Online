package com.capg.pharma.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Request DTO for user registration.
 *
 * <p>If {@code roles} is null or empty, the service defaults to {@code CUSTOMER}.
 * Valid role values: {@code CUSTOMER}, {@code ADMIN}.</p>
 *
 * <p>When registering as ADMIN, {@code adminToken} must match the configured
 * admin registration secret (pattern: {@code PHARMA-ADMIN-\d{4}}).</p>
 */
public class SignupRequest {

    /** User's full display name. Must not be blank. */
    @NotBlank
    private String name;

    /** User's email address. Must be a valid email format and not blank. */
    @Email
    @NotBlank
    private String email;

    /** Plain-text password. Will be BCrypt-hashed before storage. Must not be blank. */
    @NotBlank
    private String password;

    /**
     * Optional set of role names to assign. Defaults to {@code ["CUSTOMER"]} if omitted.
     * Valid values: {@code "CUSTOMER"}, {@code "ADMIN"}.
     */
    private Set<String> roles;

    /**
     * Admin registration token. Required when roles contains "ADMIN".
     * Must match pattern: PHARMA-ADMIN-{4-digit-year} e.g. PHARMA-ADMIN-2026
     */
    private String adminToken;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public String getAdminToken() { return adminToken; }
    public void setAdminToken(String adminToken) { this.adminToken = adminToken; }
}
