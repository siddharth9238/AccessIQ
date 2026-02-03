package com.accessiq.security;

import com.accessiq.model.Role;
import com.accessiq.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String secret;
    private final long accessTokenMinutes;
    private final long refreshTokenDays;

    public JwtUtil(
            @Value("${accessiq.jwt.secret}") String secret,
            @Value("${accessiq.jwt.access-token-minutes}") long accessTokenMinutes,
            @Value("${accessiq.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.secret = secret;
        this.accessTokenMinutes = accessTokenMinutes;
        this.refreshTokenDays = refreshTokenDays;
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet()));
        return buildToken(user.getEmail(), claims, accessTokenMinutes * 60 * 1000);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user.getEmail(), new HashMap<>(), refreshTokenDays * 24 * 60 * 60 * 1000);
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .addClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
