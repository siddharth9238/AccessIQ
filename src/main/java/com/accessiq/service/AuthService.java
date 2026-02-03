package com.accessiq.service;

import com.accessiq.dto.LoginRequest;
import com.accessiq.dto.TokenResponse;
import com.accessiq.model.RefreshToken;
import com.accessiq.model.User;
import com.accessiq.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService,
            RefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userService.getByEmail(request.getEmail());
        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken.getToken());
    }

    public TokenResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = stored.getUser();
        refreshTokenService.revoke(stored);
        RefreshToken newToken = refreshTokenService.createRefreshToken(user);
        String accessToken = jwtUtil.generateAccessToken(user);
        return new TokenResponse(accessToken, newToken.getToken());
    }
}
