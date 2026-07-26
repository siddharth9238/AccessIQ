# Deployment Guide

## Overview

AccessIQ can be deployed in multiple environments using Docker, Kubernetes, or traditional cloud platforms.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- Kubernetes 1.24+ (optional)
- PostgreSQL 12+ or MySQL 8+ (for production)

## Local Development Deployment

### Using Docker Compose

```bash
# Clone the repository
git clone https://github.com/AccessIQ/accessiq.git
cd accessiq

# Set environment variables
cp .env.example .env
# Edit .env with your values

# Start services
docker-compose up -d

# View logs
docker-compose logs -f accessiq-app
```

### Services Started

| Service | Port | URL |
|---------|------|-----|
| Application | 8080 | http://localhost:8080 |
| Actuator | 8080 | http://localhost:8080/actuator |
| Swagger UI | 8080 | http://localhost:8080/swagger-ui.html |
| PostgreSQL | 5432 | localhost:5432 |

## Production Deployment

### Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| DB_URL | Yes | Database JDBC URL |
| DB_USERNAME | Yes | Database username |
| DB_PASSWORD | Yes | Database password |
| JWT_SECRET | Yes | JWT secret key (min 256 chars) |
| ADMIN_EMAIL | Yes | Admin user email |
| ADMIN_PASSWORD | Yes | Admin user password |
| SAMPLE_PASSWORD | Yes | Sample user password |
| SPRING_PROFILES_ACTIVE | Yes | Active profile (prod) |

### Docker Production

```bash
# Build the image
docker build -t accessiq:1.0.0 .

# Run with environment variables
docker run -d \
  -p 8080:8080 \
  --name accessiq \
  -e DB_URL=jdbc:postgresql://db:5432/accessiq \
  -e DB_USERNAME=accessiq_user \
  -e DB_PASSWORD=secure_password \
  -e JWT_SECRET=your-256-character-secret-key-here \
  -e ADMIN_EMAIL=admin@accessiq.com \
  -e ADMIN_PASSWORD=Admin@123456 \
  -e SAMPLE_PASSWORD=Sample@123456 \
  -e SPRING_PROFILES_ACTIVE=prod \
  accessiq:1.0.0
```

### Kubernetes Deployment

```yaml
# k8s-deployment.yaml
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
        image: accessiq:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: accessiq-secret
              key: db-url
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: accessiq-secret
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: accessiq-secret
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: accessiq-secret
              key: jwt-secret
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
---
apiVersion: v1
kind: Service
metadata:
  name: accessiq-service
spec:
  selector:
    app: accessiq
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

Apply:
```bash
kubectl apply -f k8s-deployment.yaml
```

## Database Setup

### PostgreSQL

```sql
CREATE DATABASE accessiq;
CREATE USER accessiq_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE accessiq TO accessiq_user;
```

### MySQL

```sql
CREATE DATABASE accessiq CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'accessiq_user'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON accessiq.* TO 'accessiq_user'@'%';
FLUSH PRIVILEGES;
```

## Database Migrations

The application uses Hibernate's `ddl-auto=validate` in production. Ensure your schema matches the entity definitions.

### Initial Schema

```sql
-- Run on application startup to create tables
-- Tables are created automatically by Hibernate
```

## Monitoring Setup

### Prometheus

Prometheus scrapes metrics from `/actuator/prometheus`:

```yaml
scrape_configs:
  - job_name: 'accessiq'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['accessiq:8080']
```

### Grafana Dashboard

Import the dashboard from `grafana/dashboard.json`:
1. Open Grafana
2. Go to Dashboards → Import
3. Upload the JSON file

## Health Checks

### Liveness Probe

```bash
curl -f http://localhost:8080/actuator/health/liveness
```

### Readiness Probe

```bash
curl -f http://localhost:8080/actuator/health/readiness
```

## Scaling

### Horizontal Scaling

```bash
# Scale to 3 instances
docker-compose up -d --scale accessiq=3
```

### Connection Pool

The HikariCP pool is configured for 20 connections:
- Maximum: 20
- Minimum: 5
- Timeout: 30 seconds

## Backup Strategy

### Database Backup

```bash
# PostgreSQL
pg_dump -U accessiq_user accessiq > backup.sql

# MySQL
mysqldump -u accessiq_user -p accessiq > backup.sql
```

### Application Backup

No persistent state in application layer - only database needs backup.

## Rollback Strategy

1. Keep previous Docker images
2. Use Kubernetes deployment revision history
3. Database migrations are forward-only

## Security Hardening

### TLS/SSL

```yaml
server:
  port: 8443
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: tomcat
```

### Network Security

- Use firewall rules to restrict port access
- Enable HTTPS in production
- Use VPN or private networks for database access

## Troubleshooting

See [Troubleshooting Guide](Troubleshooting.md) for common issues.