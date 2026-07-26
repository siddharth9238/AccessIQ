# Troubleshooting Guide

## Common Issues and Solutions

### Application Won't Start

#### Error: Bean Definition Override Exception

**Problem**: `BeanDefinitionOverrideException` occurs when bean names conflict.

**Solution**: 
- Rename custom beans to avoid conflicts with Spring Boot auto-configured beans
- The `RequestContextFilter` was renamed to `CorrelationIdFilter`

#### Error: H2 Driver Not Found

**Problem**: `ClassNotFoundException: jdbc.h2.Driver`

**Solution**: 
- Ensure H2 dependency is in compile scope
- Check `application.yml` for correct driver class name: `org.h2.Driver`

#### Error: Database Connection Failed

**Problem**: Cannot connect to database

**Solution**:
1. Check database is running
2. Verify connection URL, username, password
3. Check firewall/network settings
4. Verify database user has correct permissions

```bash
# Test database connection
telnet localhost 5432
```

### Authentication Issues

#### Error: Invalid JWT Token

**Problem**: Token validation fails

**Solution**:
- Ensure JWT secret matches between token generation and validation
- Check token hasn't expired
- Verify token format is correct

#### Error: Authentication Failed

**Problem**: Login returns 401

**Solution**:
- Verify email and password are correct
- Check user exists and is enabled
- Verify BCrypt encoding is correct

### Database Issues

#### Error: Schema Validation Failed

**Problem**: Database schema doesn't match entities

**Solution**:
- For development: Set `ddl-auto: update`
- For production: Run database migrations manually
- Check for missing tables or columns

#### Error: Connection Pool Exhausted

**Problem**: No available database connections

**Solution**:
- Increase `maximum-pool-size` in HikariCP config
- Check for connection leaks (unclosed connections)
- Review long-running queries

### Docker Issues

#### Error: Container Fails to Start

**Problem**: Docker container exits immediately

**Solution**:
1. Check container logs: `docker-compose logs accessiq-app`
2. Verify environment variables are set
3. Check database connectivity from container
4. Verify ports are not already in use

```bash
# Check running containers
docker-compose ps

# View logs
docker-compose logs -f
```

#### Error: Port Already in Use

**Problem**: Port 8080 is already in use

**Solution**:
- Change port in `application.yml` or environment
- Stop the conflicting service
- Use a different port

```bash
# Find process using port
netstat -ano | findstr :8080
```

### Testing Issues

#### Error: Test Context Failed

**Problem**: Tests fail to load ApplicationContext

**Solution**:
- Check for missing dependencies
- Verify test configuration
- Clean and rebuild: `./mvnw clean test`

#### Error: Cannot resolve reference to bean

**Problem**: Bean dependency not found

**Solution**:
- Check @MockBean annotations in tests
- Verify all required beans are mocked
- Check for circular dependencies

### Performance Issues

#### Slow Response Times

**Diagnosis**:
- Check `/actuator/metrics/http.server.requests`
- Review slow queries in logs
- Check database connection pool

**Solutions**:
- Add database indexes
- Enable query caching
- Optimize queries
- Increase connection pool size

#### High Memory Usage

**Diagnosis**:
- Check `/actuator/metrics/jvm.memory.used`
- Review heap dump if needed

**Solutions**:
- Adjust JVM heap size
- Fix memory leaks
- Enable G1GC garbage collector

### Security Issues

#### CORS Errors

**Problem**: Cross-origin request blocked

**Solution**:
- Check CORS configuration in `SecurityConfig`
- Verify allowed origins match frontend URL
- Check credentials settings

#### JWT Token Issues

**Problem**: Tokens rejected

**Solution**:
- Verify JWT secret is correct
- Check token expiration
- Verify algorithm matches

### Monitoring Issues

#### Prometheus Not Scraping

**Problem**: Metrics not appearing in Prometheus

**Solution**:
- Verify `/actuator/prometheus` endpoint is accessible
- Check Prometheus configuration
- Verify scrape interval

#### Grafana Dashboard Blank

**Problem**: Dashboard shows no data

**Solution**:
- Verify Prometheus is configured as datasource
- Check metric names match
- Verify time range selection

## Health Check Commands

```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Environment
curl http://localhost:8080/actuator/env
```

## Log Analysis

### Finding Errors

```bash
# Search for ERROR in logs
grep "ERROR" logs/accessiq.log

# Search for specific exception
grep "NullPointerException" logs/accessiq.log

# Tail logs
tail -f logs/accessiq.log
```

### Common Log Patterns

```
# Startup
INFO  com.accessiq.AccessiqApplication - Starting AccessIQ...

# Request processing
INFO  c.a.controller.RequestController - Creating request...

# Security
INFO  c.a.security.JwtTokenProvider - Token validated for user...

# Database
DEBUG org.hibernate.SQL - select * from users...
```

## Recovery Procedures

### Database Recovery

```bash
# Stop application
docker-compose stop accessiq-app

# Backup database
pg_dump -U accessiq_user accessiq > backup.sql

# Restore database
psql -U accessiq_user -d accessiq -f backup.sql

# Start application
docker-compose start accessiq-app
```

### Configuration Recovery

```bash
# Revert to last known good configuration
git checkout HEAD~1 -- src/main/resources/application.yml

# Restart application
docker-compose restart
```

### Rollback Deployment

```bash
# Rollback to previous Docker image
docker service update --image accessiq:previous_tag accessiq_app

# Or with docker-compose
docker-compose down
docker-compose pull accessiq:previous_version
docker-compose up -d
```

## Getting Help

1. Check existing GitHub issues
2. Consult the documentation
3. Open a new issue with:
   - Clear description
   - Steps to reproduce
   - Expected vs actual behavior
   - Environment details
   - Logs and screenshots