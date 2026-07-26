# Security Policy

## Supported Versions

| Version | Supported |
|---------|----------|
| 1.x.x   | ✅ Yes    |
| < 1.0   | ❌ No     |

## Reporting a Vulnerability

We take the security of AccessIQ seriously. If you believe you have found a
security vulnerability, please report it through one of the following methods:

### Preferred Method: GitHub Security Advisory
1. Go to the [Security tab](https://github.com/AccessIQ/accessiq/security)
2. Click "Report a vulnerability"
3. Fill out the form with details

### Email
Send details to [security@accessiq.com] (replace with actual email)

### What to Include

- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

## Security Best Practices

### For Users

1. **Environment Variables**: Never commit `.env` files to version control
2. **JWT Secret**: Use a strong, unique secret (minimum 256 characters)
3. **Passwords**: Use strong passwords for database and admin accounts
4. **Updates**: Keep dependencies updated for security patches
5. **HTTPS**: Always use HTTPS in production

### For Developers

1. **Input Validation**: All inputs must be validated
2. **SQL Injection**: Use parameterized queries (Spring Data JPA handles this)
3. **XSS Protection**: Spring Security provides CSRF protection
4. **Authentication**: Use JWT tokens securely
5. **Authorization**: Implement proper RBAC

## Known Security Measures

### Authentication
- JWT-based authentication with BCrypt password hashing
- Token expiration and refresh token mechanism
- Secure token storage

### Authorization
- Role-Based Access Control (RBAC)
- Method-level security with @PreAuthorize
- Endpoint-level security configuration

### Data Protection
- Password encryption with BCrypt (strength 12)
- Secure session management
- Audit logging for security events

### Network Security
- CORS configuration
- CSRF protection disabled for stateless API
- Security headers configured

## Security Configuration

The application uses the following security configurations:

```yaml
# application.yml
spring:
  security:
    cors:
      allowed-origin-patterns: "*"
    csrf:
      enabled: false  # Stateless API
```

### JWT Configuration

```yaml
accessiq:
  jwt:
    secret: ${JWT_SECRET}
    access-token-minutes: 15
    refresh-token-days: 7
```

## Security Testing

Run security tests:

```bash
# Run all tests including security tests
./mvnw test

# Run with security profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Disclosure Policy

We ask that you:

- Give us reasonable time to fix the issue
- Don't publicly disclose until we've had a chance to respond
- Provide as much detail as possible

## Contact

For security concerns, contact:
- Email: security@accessiq.com
- GitHub Security: [Security Policy](https://github.com/AccessIQ/accessiq/security/policy)

Thank you for helping keep AccessIQ secure!