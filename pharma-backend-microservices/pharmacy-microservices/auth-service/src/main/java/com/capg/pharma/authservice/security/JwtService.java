package com.capg.pharma.authservice.security;

import com.capg.pharma.authservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for JWT token generation and validation.
 *
 * <p>Uses HMAC-SHA256 (HS256) signing with a configurable secret key.
 * Tokens include the user's email as the subject and their roles as a custom claim.</p>
 *
 * <p>Token expiry is configurable via {@code jwt.expiration} (milliseconds).
 * Default is 86400000ms (24 hours).</p>
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Builds the HMAC signing key from the configured secret.
     *
     * @return a {@link Key} suitable for HS256 signing/verification
     */
    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token for the given user.
     *
     * <p>The token contains:
     * <ul>
     *   <li>{@code sub} — user's email address</li>
     *   <li>{@code roles} — list of role names (e.g. ["CUSTOMER", "ADMIN"])</li>
     *   <li>{@code iat} — issued-at timestamp</li>
     *   <li>{@code exp} — expiry timestamp (iat + configured expiration)</li>
     * </ul>
     * </p>
     *
     * @param email the user's email, used as the JWT subject
     * @param roles the set of roles to embed in the token
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(String email, Set<Role> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles.stream().map(Role::name).collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses and returns all claims from a JWT token.
     *
     * @param token the compact JWT string
     * @return the {@link Claims} payload
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token the compact JWT string
     * @return the email address embedded in the token subject
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Checks whether a JWT token is valid and not expired.
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is valid and not expired; {@code false} otherwise
     */
    public boolean isValid(String token) {
        try {
            return extractClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
