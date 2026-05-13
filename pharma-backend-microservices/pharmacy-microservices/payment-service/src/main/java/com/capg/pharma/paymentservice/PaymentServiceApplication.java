package com.capg.pharma.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.ZipkinAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Payment Service.
 */
@SpringBootApplication(exclude = { ZipkinAutoConfiguration.class })
@EnableDiscoveryClient
@EnableFeignClients
public class PaymentServiceApplication {

    /**
     * Bootstraps the Payment Service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
