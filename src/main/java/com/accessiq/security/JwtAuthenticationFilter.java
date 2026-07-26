package com.accessiq.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter for processing JWT tokens in requests.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;

    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructs a new JwtAuthenticationFilter.
     *
     * @param jwtTokenProvider the JWT token provider
     * @param userDetailsService the custom user details service
     */
    public JwtAuthenticationFilter(
            final JwtTokenProvider jwtTokenProvider,
            final CustomUserDetailsService userDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Performs actual filtering - executes in a thread-safe manner.
     *
     * @param request the request
     * @param response the response
     * @param filterChain the filter chain
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        final String token = getJwtFromRequest(request);

        if (token != null && jwtTokenProvider.isTokenValid(token)) {
            final String username = jwtTokenProvider.extractUsername(token);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UserDetails userDetails = userDetailsService
                        .loadUserByUsername(username);
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated user: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the request.
     *
     * @param request the HTTP request
     * @return the JWT token or null if not found
     */
    private String getJwtFromRequest(final HttpServletRequest request) {
        final String bearerToken = request.getHeader(SecurityConstants.JWT_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.JWT_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}