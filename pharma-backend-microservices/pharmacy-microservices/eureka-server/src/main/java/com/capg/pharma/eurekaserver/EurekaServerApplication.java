package com.capg.pharma.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Entry point for the Eureka Service Registry.
 *
 * <p>All microservices register themselves here on startup. The gateway
 * uses Eureka to resolve service names to actual host:port addresses,
 * enabling client-side load balancing via {@code lb://service-name} URIs.</p>
 *
 * <p>Runs on port 8761 by default. Access the Eureka dashboard at
 * {@code http://localhost:8761}.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * Bootstraps the Eureka server.
     *
     * @param args command-line arguments passed to the Spring application context
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
