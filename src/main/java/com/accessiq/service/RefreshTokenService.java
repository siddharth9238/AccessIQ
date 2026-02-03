package com.accessiq.service;

import com.accessiq.exception.BadRequestException;
import com.accessiq.model.RefreshToken;
import com.accessiq.model.User;
import com.accessiq.repository.RefreshTokenRepository;
import com.accessiq.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final long refreshTokenDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil,
            @Value("${accessiq.jwt.refresh-token-days}") long refreshTokenDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.refreshTokenDays = refreshTokenDays;
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(jwtUtil.generateRefreshToken(user));
        token.setExpiryDate(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (stored.isRevoked()) {
            throw new BadRequestException("Refresh token revoked");
        }
        if (stored.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token expired");
        }
        if (!jwtUtil.isTokenValid(token)) {
            throw new BadRequestException("Refresh token signature invalid");
        }
        return stored;
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
}
