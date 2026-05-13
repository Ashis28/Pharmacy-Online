package com.capg.pharma.gatewayservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

/**
 * Spring Cloud Gateway filter that validates JWT tokens on incoming requests.
 *
 * <p>Applied to all routes except public auth endpoints. On successful validation,
 * the filter extracts the user's email (subject) and roles from the token and
 * forwards them as {@code X-Auth-User} and {@code X-Auth-Roles} headers to
 * downstream services, so they don't need to re-parse the token for identity.</p>
 *
 * <p>Returns {@code 401 Unauthorized} if the token is missing, malformed, or expired.</p>
 */
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    /** Routes that bypass JWT validation (public endpoints + Swagger/OpenAPI assets). */
    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/validate",
            "/docs/",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars/"
    );

    /**
     * Constructs the filter with its configuration class.
     */
    public JwtAuthFilter() {
        super(Config.class);
    }

    /**
     * Applies the JWT validation logic to the gateway filter chain.
     *
     * <p>Skips validation for open paths. For all other requests, extracts the
     * {@code Authorization: Bearer <token>} header, validates the JWT signature
     * and expiry, then mutates the request to add identity headers.</p>
     *
     * @param config filter configuration (currently unused, reserved for future use)
     * @return a {@link GatewayFilter} that enforces JWT authentication
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();

            // Allow public endpoints and actuator health checks through without a token
            if (OPEN_PATHS.stream().anyMatch(path::startsWith) || path.contains("/actuator")) {
                return chain.filter(exchange);
            }

            // Allow public GET requests to catalog medicines (browsing without login)
            if ("GET".equals(method) && path.startsWith("/api/catalog/medicines")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(authHeader.substring(7))
                        .getBody();

                // Propagate user identity to downstream services via custom headers
                var mutated = exchange.mutate().request(r -> r
                        .header("X-Auth-User", claims.getSubject())
                        .header("X-Auth-Roles", String.valueOf(claims.get("roles")))
                ).build();
                return chain.filter(mutated);
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    /**
     * Empty configuration class for the filter factory.
     * Reserved for future per-route configuration options.
     */
    public static class Config {}
}
