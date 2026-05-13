package com.capg.pharma.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API Gateway service.
 *
 * <p>The gateway is the single entry point for all client requests. It:
 * <ul>
 *   <li>Validates JWT tokens on protected routes via {@code JwtAuthFilter}</li>
 *   <li>Routes requests to downstream microservices using Eureka service discovery</li>
 *   <li>Injects {@code X-Auth-User} and {@code X-Auth-Roles} headers for downstream services</li>
 *   <li>Propagates distributed trace headers for Zipkin</li>
 * </ul>
 * Runs on port 8888.</p>
 */
@SpringBootApplication
public class GatewayApplication {

    /**
     * Bootstraps the Spring Cloud Gateway application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
