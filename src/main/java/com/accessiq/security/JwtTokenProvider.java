package com.accessiq.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * Token provider for JWT generation and validation.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String secret;

    private final long accessTokenTtlMillis;

    private final long refreshTokenTtlMillis;

    /**
     * Constructs a new JwtTokenProvider.
     *
     * @param secret the JWT secret key
     * @param accessTokenMinutes the access token time-to-live in minutes
     * @param refreshTokenDays the refresh token time-to-live in days
     */
    public JwtTokenProvider(
            @Value("${accessiq.jwt.secret}") final String secret,
            @Value("${accessiq.jwt.access-token-minutes}") final long accessTokenMinutes,
            @Value("${accessiq.jwt.refresh-token-days}") final long refreshTokenDays
    ) {
        this.secret = secret;
        this.accessTokenTtlMillis = accessTokenMinutes * 60 * 1000;
        this.refreshTokenTtlMillis = refreshTokenDays * 24 * 60 * 60 * 1000;
    }

    /**
     * Generates an access token for the given username with claims.
     *
     * @param username the username to encode in the token
     * @param claims additional claims to include in the token
     * @return the generated JWT access token
     */
    public String generateAccessToken(final String username, final Map<String, Object> claims) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + accessTokenTtlMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .addClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates a refresh token for the given username.
     *
     * @param username the username to encode in the token
     * @return the generated JWT refresh token
     */
    public String generateRefreshToken(final String username) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + refreshTokenTtlMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims map
     */
    public Map<String, Object> extractClaims(final String token) {
        return parseClaims(token);
    }

    /**
     * Validates the JWT token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(final String token) {
        try {
            final Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (final JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Parses the claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims
     */
    private Claims parseClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Gets the signing key from the secret.
     *
     * @return the signing key
     */
    private Key getSigningKey() {
        final byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}