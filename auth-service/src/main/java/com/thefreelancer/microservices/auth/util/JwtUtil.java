package com.thefreelancer.microservices.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long jwtExpiration, // 24 hours default
            @Value("${jwt.refresh-expiration:604800000}") long refreshExpiration // 7 days default
    ) {
        // Decode the base64 secret properly
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // Generate access token with user ID as subject
    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        claims.put("type", "access");
        return createToken(claims, userId.toString(), jwtExpiration);
    }

    // Generate refresh token
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userId.toString(), refreshExpiration);
    }

    // Create token with custom claims and expiration
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    // Validate token and return true if valid
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user ID from a JWT token.
     * 
     * @param token The JWT token to parse
     * @return The user ID as Long, or null if extraction fails
     * @throws JwtException if token is malformed or invalid
     */
    public Long extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return subject != null ? Long.valueOf(subject) : null;
    }

    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    // Extract role from token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Extract token type (access/refresh)
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract any claim using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Internal method to extract all claims from a JWT token.
     * 
     * Parses and verifies the token signature, then returns all claims.
     * 
     * @param token The JWT token to parse
     * @return Claims object containing all token claims
     * @throws JwtException if token is malformed, invalid, or signature verification fails
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // Validate token for specific user
    public boolean isTokenValid(String token, Long userId) {
        try {
            final Long extractedUserId = extractUserId(token);
            return (extractedUserId.equals(userId)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    // Validate refresh token specifically
    public boolean isRefreshTokenValid(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "refresh".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Get remaining time until expiration in seconds
    public long getExpirationTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return (expiration.getTime() - System.currentTimeMillis()) / 1000;
        } catch (Exception e) {
            return 0;
        }
    }
}