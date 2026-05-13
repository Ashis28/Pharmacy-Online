package com.capg.pharma.authservice.controller;

import com.capg.pharma.authservice.dto.LoginRequest;
import com.capg.pharma.authservice.dto.LoginResponse;
import com.capg.pharma.authservice.dto.SignupRequest;
import com.capg.pharma.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication endpoints.
 *
 * <p>Exposes public endpoints for user registration and login.
 * No JWT token is required to call these endpoints.</p>
 *
 * <p>Base path: {@code /api/auth}</p>
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs the AuthController with the authentication service.
     *
     * @param authService the service handling signup and login logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     *
     * <p>Validates the request body, checks for duplicate emails, and persists
     * the user with a BCrypt-hashed password. Defaults to CUSTOMER role if none specified.</p>
     *
     * @param req the signup request with name, email, password, and optional roles
     * @return {@code 201 Created} with a success message
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account. Defaults to CUSTOMER role.")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(req));
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * <p>Verifies the email and password, then issues a signed JWT valid for 24 hours.
     * Include the token in subsequent requests as {@code Authorization: Bearer <token>}.</p>
     *
     * @param req the login request with email and password
     * @return {@code 200 OK} with a {@link LoginResponse} containing the JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token", description = "Authenticates credentials and returns a signed JWT.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
