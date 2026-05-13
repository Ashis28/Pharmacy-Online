package com.capg.pharma.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Entry point for the Authentication Service.
 *
 * <p>Handles user registration (signup), login, and JWT token issuance.
 * This is the only service that issues tokens; all other services only validate them.</p>
 *
 * <p>Security: JWT-only, CSRF disabled (stateless REST API).
 * Runs on port 9091.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    /**
     * Bootstraps the Auth Service Spring application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
