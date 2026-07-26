# Docker Deployment

This document describes how to deploy AccessIQ using Docker.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Building Docker Image](#building-docker-image)
- [Local Development](#local-development)
- [Production Deployment](#production-deployment)
- [Environment Variables](#environment-variables)
- [Multi-stage Build](#multi-stage-build)
- [Docker Hub](#docker-hub)
- [Troubleshooting](#troubleshooting)

## Prerequisites

- Docker Engine 24.0+
- Docker Compose 2.20+
- Java 17 JDK (for local builds)
- Docker Hub account (for image push)

## Building Docker Image

### Using Docker CLI

```bash
# Build image
docker build -t accessiq:latest .

# Build with specific version
docker build -t accessiq:1.0.0 .

# Build for multiple platforms
docker buildx build --platform linux/amd64,linux/arm64 -t accessiq:latest .
```

### Using GitHub Actions

Push to `main` branch to trigger automatic Docker build:

```yaml
# .github/workflows/docker.yml
# Automatically builds and pushes to Docker Hub
```

## Local Development

### Using Docker Compose

```bash
# Start development environment
docker-compose up -d

# View logs
docker-compose logs -f

# Stop environment
docker-compose down

# Rebuild
docker-compose up -d --build
```

### Development Environment

The `docker-compose.yml` includes:
- PostgreSQL database
- Prometheus for metrics
- Grafana for visualization

Access the application at: `http://localhost:8080`

## Production Deployment

### Using Production Compose File

```bash
# Use production configuration
docker-compose -f deployment/docker-compose.prod.yml up -d
```

### Using ECS (AWS)

1. Push image to ECR or Docker Hub
2. Create ECS task definition
3. Create ECS service
4. Configure load balancer

### Using Kubernetes

```yaml
# Example Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: accessiq
spec:
  replicas: 3
  selector:
    matchLabels:
      app: accessiq
  template:
    metadata:
      labels:
        app: accessiq
    spec:
      containers:
      - name: accessiq
        image: accessiq:latest
        ports:
        - containerPort: 8080
        envFrom:
        - secretRef:
            name: accessiq-secrets
---
apiVersion: v1
kind: Service
metadata:
  name: accessiq
spec:
  selector:
    app: accessiq
  ports:
  - port: 80
    targetPort: 8080
```

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | Database JDBC URL | `jdbc:postgresql://postgres:5432/accessiq` |
| `DB_USERNAME` | Database username | `accessiq_user` |
| `DB_PASSWORD` | Database password | `secret123` |
| `JWT_SECRET` | JWT signing secret | `your-secret-key-min-32-chars` |
| `ADMIN_EMAIL` | Admin user email | `admin@accessiq.com` |
| `ADMIN_PASSWORD` | Admin password | `SecurePass123!` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `prod` |
| `SERVER_PORT` | Server port | `8080` |
| `LOGGING_LEVEL_ROOT` | Logging level | `INFO` |

### Using .env File

Create a `.env` file:

```bash
# .env
DB_PASSWORD=your-db-password
JWT_SECRET=your-jwt-secret-key
ADMIN_EMAIL=admin@accessiq.com
ADMIN_PASSWORD=SecurePass123!
```

Then reference in docker-compose:

```yaml
environment:
  - DB_PASSWORD=${DB_PASSWORD}
  - JWT_SECRET=${JWT_SECRET}
```

## Multi-stage Build

The Dockerfile uses multi-stage builds for optimal image size:

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Benefits

- Smaller image size (~100MB vs ~500MB)
- Faster build times
- Reduced attack surface
- Better layer caching

## Docker Hub

### Tagging Strategy

```
accessiq:latest          # Latest version
accessiq:1.0.0          # Specific version
accessiq:1.0            # Major.minor version
accessiq:v1.0.0         # With v prefix
accessiq:sha-xxx        # Commit SHA
```

### Push to Docker Hub

```bash
# Login
docker login

# Tag image
docker tag accessiq:latest yourusername/accessiq:1.0.0

# Push
docker push yourusername/accessiq:1.0.0
docker push yourusername/accessiq:latest
```

### GitHub Actions Auto-push

The `docker.yml` workflow automatically:
1. Builds the image
2. Tags with branch, PR, and version
3. Pushes to Docker Hub

## Health Checks

### Docker Health Check

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### Kubernetes Liveness/Readiness

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 30

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
```

## Monitoring

### Prometheus Metrics

Access metrics at: `http://localhost:8080/actuator/prometheus`

### Grafana Dashboard

Import the dashboard from `grafana/dashboard.json`

## Troubleshooting

### Container Not Starting

```bash
# Check container status
docker ps -a

# View logs
docker logs accessiq-app

# Check health
docker inspect accessiq-app --format='{{json .State.Health}}'
```

### Memory Issues

```bash
# Check memory usage
docker stats accessiq-app

# Increase memory limit
docker run -m 1g --memory-swap 1g accessiq:latest
```

### Database Connection

```bash
# Test database connection
docker exec -it accessiq-app bash
java -cp app.jar com.accessiq.DatabaseTest
```

### Application Logs

```bash
# View logs
docker logs -f accessiq-app

# Tail logs
docker logs --tail 100 accessiq-app
```