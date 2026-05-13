package com.capg.pharma.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 configuration for the Notification Service.
 * Swagger UI: {@code http://localhost:9096/swagger-ui.html}
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the OpenAPI metadata for the Notification Service.
     *
     * @return the configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("Async notification delivery via RabbitMQ and direct HTTP")
                        .version("1.0.0"));
    }
}
