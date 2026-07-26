package com.accessiq.service;

import com.accessiq.dto.LoginRequest;
import com.accessiq.dto.TokenResponse;
import com.accessiq.model.RefreshToken;
import com.accessiq.model.User;
import com.accessiq.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for authentication operations.
 */
@Service
public class AuthService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    /** The authentication manager. */
    private final AuthenticationManager authenticationManager;

    /** The JWT token provider. */
    private final JwtTokenProvider jwtTokenProvider;

    /** The user service. */
    private final UserService userService;

    /** The refresh token service. */
    private final RefreshTokenService refreshTokenService;

    /**
     * Constructs a new AuthService.
     *
     * @param authenticationManager the authentication manager
     * @param jwtTokenProvider the JWT token provider
     * @param userService the user service
     * @param refreshTokenService the refresh token service
     */
    public AuthService(final AuthenticationManager authenticationManager,
            final JwtTokenProvider jwtTokenProvider,
            final UserService userService,
            final RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticates a user and returns tokens.
     *
     * @param request the login request
     * @return the token response
     */
    public TokenResponse login(final LoginRequest request) {
        LOG.info("Login attempt for user: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        final User user = userService.getByEmail(request.getEmail());

        final Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());

        final String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), claims);
        final RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        LOG.info("Login successful for user: {}", user.getEmail());
        return new TokenResponse(accessToken, refreshToken.getToken());
    }

    /**
     * Refreshes an access token.
     *
     * @param refreshToken the refresh token
     * @return the token response
     */
    public TokenResponse refresh(final String refreshToken) {
        LOG.info("Refresh token request");

        final RefreshToken stored = refreshTokenService.verifyRefreshToken(refreshToken);
        final User user = stored.getUser();
        refreshTokenService.revoke(stored);
        final RefreshToken newToken = refreshTokenService.createRefreshToken(user);

        final Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());

        final String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(), claims);

        LOG.info("Token refreshed for user: {}", user.getEmail());
        return new TokenResponse(accessToken, newToken.getToken());
    }
}