package com.capg.pharma.gatewayservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the aggregated Swagger UI served by the gateway.
 *
 * <p>Access the unified API docs at:
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8888/swagger-ui.html}</li>
 * </ul>
 * All service specs are fetched through the gateway via {@code /docs/{service}/v3/api-docs}.
 * Use the dropdown in the top-right of the Swagger UI to switch between services.
 * </p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the aggregated OpenAPI metadata and JWT Bearer security scheme
     * for the gateway Swagger UI.
     *
     * @return the configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pharma Microservices API")
                        .description(
                                "Aggregated API documentation for the Pharmacy Platform. " +
                                "All requests are routed through the gateway on port 8888. " +
                                "Use the dropdown (top-right) to switch between services. " +
                                "Authenticate once using the Authorize button — the JWT will " +
                                "be sent with every request.")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
