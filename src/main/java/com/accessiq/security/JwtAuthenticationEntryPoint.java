package com.accessiq.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Authentication entry point for handling authentication failures.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Handles authentication exceptions by sending an error response.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the authentication exception
     * @throws IOException if I/O error occurs
     */
    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException
    ) throws IOException {
        log.warn("Unauthorized request: {}", authException.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized: " + authException.getMessage());
    }
}