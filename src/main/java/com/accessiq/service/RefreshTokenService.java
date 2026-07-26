package com.accessiq.service;

import com.accessiq.exception.BadRequestException;
import com.accessiq.model.RefreshToken;
import com.accessiq.model.User;
import com.accessiq.repository.RefreshTokenRepository;
import com.accessiq.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for refresh token management.
 */
@Service
@Transactional
public class RefreshTokenService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(RefreshTokenService.class);

    /** The refresh token repository. */
    private final RefreshTokenRepository refreshTokenRepository;

    /** The JWT token provider. */
    private final JwtTokenProvider jwtTokenProvider;

    /** The refresh token time to live in days. */
    private final long refreshTokenDays;

    /**
     * Constructs a new RefreshTokenService.
     *
     * @param refreshTokenRepository the refresh token repository
     * @param jwtTokenProvider the JWT token provider
     * @param refreshTokenDays the refresh token TTL in days
     */
    public RefreshTokenService(final RefreshTokenRepository refreshTokenRepository,
            final JwtTokenProvider jwtTokenProvider,
            @Value("${accessiq.jwt.refresh-token-days}") final long refreshTokenDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenDays = refreshTokenDays;
    }

    /**
     * Creates a refresh token for a user.
     *
     * @param user the user
     * @return the refresh token
     */
    public RefreshToken createRefreshToken(final User user) {
        LOG.info("Creating refresh token for user: {}", user.getEmail());

        final String token = jwtTokenProvider.generateRefreshToken(user.getEmail());
        final RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));

        final RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        LOG.debug("Refresh token created for user: {}", user.getEmail());
        return savedToken;
    }

    /**
     * Verifies a refresh token.
     *
     * @param token the token
     * @return the refresh token
     */
    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(final String token) {
        LOG.debug("Verifying refresh token");

        final RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (stored.isRevoked()) {
            throw new BadRequestException("Refresh token revoked");
        }
        if (stored.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token expired");
        }
        if (!jwtTokenProvider.isTokenValid(token)) {
            throw new BadRequestException("Refresh token signature invalid");
        }
        return stored;
    }

    /**
     * Revokes a refresh token.
     *
     * @param token the token
     */
    public void revoke(final RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        LOG.info("Refresh token revoked for user: {}", token.getUser().getEmail());
    }

    /**
     * Counts active refresh tokens.
     *
     * @return the count
     */
    @Transactional(readOnly = true)
    public long countActiveTokens() {
        return refreshTokenRepository.countByRevokedFalse();
    }
}