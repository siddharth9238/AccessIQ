package com.accessiq.controller;

import com.accessiq.dto.ApiResponse;
import com.accessiq.dto.LoginRequest;
import com.accessiq.dto.RefreshRequest;
import com.accessiq.dto.TokenResponse;
import com.accessiq.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for user: {}", request.getEmail());
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        logger.info("Token refresh request");
        TokenResponse tokenResponse = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }
}