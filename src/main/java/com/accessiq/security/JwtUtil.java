package com.accessiq.security;

import com.accessiq.model.Role;
import com.accessiq.model.User;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Deprecated JWT utility class. Use JwtTokenProvider instead.
 *
 * @deprecated Use {@link JwtTokenProvider} for JWT operations
 */
@Deprecated
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final String secret;

    private final long accessTokenMinutes;

    private final long refreshTokenDays;

    /**
     * Constructs a new JwtUtil.
     *
     * @param secret the JWT secret key
     * @param accessTokenMinutes the access token time-to-live in minutes
     * @param refreshTokenDays the refresh token time-to-live in days
     */
    public JwtUtil(
            @Value("${accessiq.jwt.secret}") final String secret,
            @Value("${accessiq.jwt.access-token-minutes}") final long accessTokenMinutes,
            @Value("${accessiq.jwt.refresh-token-days}") final long refreshTokenDays
    ) {
        this.secret = secret;
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    /**
     * Generates an access token for the given user.
     *
     * @param user the user to generate token for
     * @return the generated JWT access token
     * @deprecated Use {@link JwtTokenProvider#generateAccessToken(String, Map)} instead
     */
    @Deprecated
    public String generateAccessToken(final User user) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet()));
        return buildToken(user.getEmail(), claims, accessTokenMinutes * 60 * 1000);
    }

    /**
     * Generates a refresh token for the given user.
     *
     * @param user the user to generate token for
     * @return the generated JWT refresh token
     * @deprecated Use {@link JwtTokenProvider#generateRefreshToken(String)} instead
     */
    @Deprecated
    public String generateRefreshToken(final User user) {
        return buildToken(user.getEmail(), new HashMap<>(),
                refreshTokenDays * 24 * 60 * 60 * 1000);
    }

    /**
     * Builds a JWT token.
     *
     * @param subject the subject
     * @param claims the claims
     * @param ttlMillis time to live in milliseconds
     * @return the generated JWT token
     */
    @Deprecated
    private String buildToken(final String subject, final Map<String, Object> claims,
            final long ttlMillis) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .addClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username from the JWT token.
     *
     * @param token the JWT token
     * @return the username
     * @deprecated Use {@link JwtTokenProvider#extractUsername(String)} instead
     */
    @Deprecated
    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validates the JWT token.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     * @deprecated Use {@link JwtTokenProvider#isTokenValid(String)} instead
     */
    @Deprecated
    public boolean isTokenValid(final String token) {
        try {
            final Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (final JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Parses the claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims
     */
    @Deprecated
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
    @Deprecated
    private Key getSigningKey() {
        final byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}