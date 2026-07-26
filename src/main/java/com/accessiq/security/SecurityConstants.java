package com.accessiq.security;

/**
 * Security constants for JWT and role-based access control.
 */
public final class SecurityConstants {

    /** HTTP header name for JWT token. */
    public static final String JWT_HEADER = "Authorization";

    /** Bearer token prefix. */
    public static final String JWT_PREFIX = "Bearer ";

    /** Token type constant. */
    public static final String TOKEN_TYPE = "Bearer";

    /** Role constant for ADMIN users. */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** Role constant for EMPLOYEE users. */
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";

    /** Role constant for MANAGER users. */
    public static final String ROLE_MANAGER = "ROLE_MANAGER";

    /** Role constant for AUDITOR users. */
    public static final String ROLE_AUDITOR = "ROLE_AUDITOR";

    /** Private constructor to prevent instantiation. */
    private SecurityConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}