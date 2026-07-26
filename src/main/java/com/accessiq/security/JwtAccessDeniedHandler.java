package com.accessiq.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Access denied handler for handling access denied exceptions.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    /**
     * Handles access denied exceptions by sending an error response.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param accessDeniedException the access denied exception
     * @throws IOException if I/O error occurs
     */
    @Override
    public void handle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AccessDeniedException accessDeniedException
    ) throws IOException {
        log.warn("Access denied: {}", accessDeniedException.getMessage());
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Access Denied: " + accessDeniedException.getMessage());
    }
}