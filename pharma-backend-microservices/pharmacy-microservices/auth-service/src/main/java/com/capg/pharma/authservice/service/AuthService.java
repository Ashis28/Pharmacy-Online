package com.capg.pharma.authservice.service;

import com.capg.pharma.authservice.dto.LoginRequest;
import com.capg.pharma.authservice.dto.LoginResponse;
import com.capg.pharma.authservice.dto.SignupRequest;
import com.capg.pharma.authservice.exception.InvalidCredentialsException;
import com.capg.pharma.authservice.exception.UserAlreadyExistsException;
import com.capg.pharma.authservice.exception.UserNotFoundException;
import com.capg.pharma.authservice.model.Role;
import com.capg.pharma.authservice.model.User;
import com.capg.pharma.authservice.repository.UserRepository;
import com.capg.pharma.authservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business logic service for user authentication and registration.
 *
 * <p>Handles:
 * <ul>
 *   <li>User signup with duplicate email detection and role validation</li>
 *   <li>User login with password verification and JWT issuance</li>
 * </ul>
 * </p>
 */
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    /**
     * Constructs the AuthService with required dependencies.
     *
     * @param userRepo   repository for user persistence
     * @param encoder    BCrypt password encoder
     * @param jwtService service for JWT generation and validation
     */
    public AuthService(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /**
     * The valid admin registration token — an opaque secret string.
     * No format is exposed to the client. Configurable via ADMIN_TOKEN env var.
     */
    @org.springframework.beans.factory.annotation.Value("${admin.registration.token:PHARMA-ADMIN-2026}")
    private String adminRegistrationToken;

    public String signup(SignupRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + req.getEmail());
        }

        Set<Role> roles;
        try {
            roles = (req.getRoles() == null || req.getRoles().isEmpty())
                    ? Set.of(Role.CUSTOMER)
                    : req.getRoles().stream().map(Role::valueOf).collect(Collectors.toSet());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified. Valid roles: CUSTOMER, ADMIN");
        }

        // Validate admin token — opaque exact-match only, no format hints
        if (roles.contains(Role.ADMIN)) {
            String token = req.getAdminToken();
            if (token == null || token.isBlank() || !adminRegistrationToken.equals(token.trim())) {
                throw new IllegalArgumentException("Invalid token.");
            }
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRoles(roles);
        userRepo.save(user);
        return "User registered successfully";
    }

    /**
     * Authenticates a user and issues a JWT token on success.
     *
     * <p>Looks up the user by email, verifies the password against the stored
     * BCrypt hash, then generates and returns a signed JWT.</p>
     *
     * @param req the login request containing email and password
     * @return a {@link LoginResponse} with the JWT token, user name, and roles
     * @throws UserNotFoundException       if no user exists with the given email
     * @throws InvalidCredentialsException if the password does not match
     */
    public LoginResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + req.getEmail()));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRoles());
        Set<String> roles = user.getRoles().stream().map(Role::name).collect(Collectors.toSet());
        return new LoginResponse(token, user.getName(), user.getId(), roles);
    }
}
