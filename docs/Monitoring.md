# Monitoring Documentation

## Overview

AccessIQ provides comprehensive monitoring through Spring Boot Actuator, Micrometer, and Prometheus integration.

## Actuator Endpoints

### Exposed Endpoints

| Endpoint | Path | Description |
|----------|------|-------------|
| Health | `/actuator/health` | Application health status |
| Info | `/actuator/info` | Application information |
| Metrics | `/actuator/metrics` | Metrics data |
| Prometheus | `/actuator/prometheus` | Prometheus format metrics |
| Env | `/actuator/env` | Environment properties |
| Beans | `/actuator/beans` | Spring beans |
| Mappings | `/actuator/mappings` | Request mappings |

### Health Indicators

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 50731170304,
        "free": 38958944256,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

## Micrometer Metrics

### JVM Metrics

| Metric | Description |
|--------|-------------|
| jvm.memory.used | Memory used by JVM |
| jvm.memory.max | Maximum memory |
| jvm.threads.live | Live thread count |
| jvm.gc.pause | GC pause times |

### HTTP Metrics

| Metric | Description |
|--------|-------------|
| http.server.requests | HTTP request counts |
| http.server.requests.duration | Request durations |
| http.server.requests.active | Active requests |

### Database Metrics

| Metric | Description |
|--------|-------------|
| hikaricp.connections.active | Active connections |
| hikaricp.connections.idle | Idle connections |
| hikaricp.connections.pending | Pending connections |

## Prometheus Integration

### Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Sample Metrics Output

```
# HELP jvm_memory_bytes_used The amount of used memory
# TYPE jvm_memory_bytes_used gauge
jvm_memory_bytes_used{area="heap",} 1.534845952E8

# HELP http_server_requests_seconds_count
# TYPE http_server_requests_seconds_count counter
http_server_requests_seconds_count{method="GET",uri="/api/v1/requests",} 42.0

# HELP hikaricp_connections_active
# TYPE hikaricp_connections_active gauge
hikaricp_connections_active 5.0
```

## Grafana Dashboard

Import the dashboard from `grafana/dashboard.json`:

### Panels

1. **Application Health** - Overall health status
2. **HTTP Request Rate** - Requests per second
3. **JVM Memory Usage** - Heap and non-heap memory
4. **Database Connections** - Active and idle connections
5. **HTTP Response Time** - Average response time
6. **GC Activity** - Garbage collection metrics

## Custom Metrics

### Business Metrics

Track custom business metrics using Micrometer:

```java
@Service
public class RequestService {
    private final Counter requestCounter;
    
    public RequestService(MeterRegistry registry) {
        this.requestCounter = Counter.builder("accessiq.requests.created")
            .description("Number of requests created")
            .register(registry);
    }
    
    public Request createRequest(...) {
        requestCounter.increment();
        ...
    }
}
```

## Alerting Rules

### Prometheus Alerting Rules

```yaml
groups:
  - name: accessiq.alerts
    rules:
      - alert: HighErrorRate
        expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: High error rate
          description: Error rate is above 5% for more than 5 minutes
      
      - alert: HighMemoryUsage
        expr: jvm_memory_bytes_used / jvm_memory_bytes_max > 0.9
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: High memory usage
          description: Memory usage is above 90%
      
      - alert: DatabaseDown
        expr: up{job="accessiq"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: Database is down
          description: Database connection is not available
```

## Monitoring Setup

### Local Development

```bash
# Start with Docker Compose
docker-compose up -d

# Access Actuator
curl http://localhost:8080/actuator/health

# Access Prometheus
curl http://localhost:9090/-/healthy

# Access Grafana
open http://localhost:3000
```

### Production

1. Configure Prometheus to scrape `/actuator/prometheus`
2. Import Grafana dashboard
3. Set up alerting rules
4. Configure log aggregation

## Log Monitoring

### Structured Logging

Logs follow a structured format:

```
2024-01-15 10:30:00.000 [http-nio-8080-exec-1] INFO  c.a.service.RequestService - Creating request with title: Leave Request for user: employee@accessiq.com
```

### Log Levels

| Level | Usage |
|-------|-------|
| ERROR | Application errors |
| WARN | Warnings |
| INFO | Business operations |
| DEBUG | Debug information |
| TRACE | Detailed tracing |

## Performance Monitoring

### Key Metrics to Watch

1. **Response Time** - Should be under 200ms
2. **Throughput** - Requests per second
3. **Error Rate** - Should be under 1%
4. **Database Latency** - Query execution time
5. **Connection Pool** - Utilization percentage

### Baseline Metrics

| Metric | Target |
|--------|--------|
| 95th percentile response | < 500ms |
| 99th percentile response | < 1000ms |
| Error rate | < 0.1% |
| Database connections | < 80% pool |
| Memory usage | < 75% heap |

## Troubleshooting

See [Troubleshooting.md](Troubleshooting.md) for common monitoring issues.