package com.capg.pharma.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 configuration for the Auth Service.
 *
 * <p>Exposes the API documentation at:
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:9091/swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code http://localhost:9091/v3/api-docs}</li>
 * </ul>
 * </p>
 *
 * <p>Configures Bearer token authentication so the Swagger UI can send
 * JWT tokens when testing protected endpoints.</p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the OpenAPI metadata and JWT Bearer security scheme.
     *
     * @return the configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .description("Handles user registration, login, and JWT token issuance")
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
