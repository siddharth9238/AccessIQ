package com.accessiq.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String secret = "test-secret-key-for-testing-purposes-only-minimum-256-bits-long-secret-key-for-jwt-token";
    private long accessTokenMinutes = 15;
    private long refreshTokenDays = 7;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, accessTokenMinutes, refreshTokenDays);
    }

    @Test
    void testGenerateAccessToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", java.util.List.of("EMPLOYEE"));

        String token = jwtTokenProvider.generateAccessToken("test@example.com", claims);
        assertNotNull(token);
        assertTrue(token.length() > 0);

        Claims parsedClaims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test@example.com", parsedClaims.getSubject());
        assertEquals(claims.get("roles"), parsedClaims.get("roles"));
    }

    @Test
    void testGenerateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken("test@example.com");
        assertNotNull(token);
        assertTrue(token.length() > 0);

        Claims parsedClaims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test@example.com", parsedClaims.getSubject());
    }

    @Test
    void testExtractUsername() {
        String token = jwtTokenProvider.generateAccessToken("test@example.com", new HashMap<>());
        String username = jwtTokenProvider.extractUsername(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void testIsTokenValid() {
        String validToken = jwtTokenProvider.generateAccessToken("test@example.com", new HashMap<>());
        assertTrue(jwtTokenProvider.isTokenValid(validToken));

        String invalidToken = "invalid.token.here";
        assertFalse(jwtTokenProvider.isTokenValid(invalidToken));
    }

    @Test
    void testIsTokenExpired() {
        Map<String, Object> claims = new HashMap<>();
        String token = jwtTokenProvider.generateAccessToken("test@example.com", claims);
        assertTrue(jwtTokenProvider.isTokenValid(token));
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}