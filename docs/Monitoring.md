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
| Heapdump | `/actuator/heapdump` | Heap dump (dev only) |
| Thread Dump | `/actuator/threaddump` | Thread dump |

### Health Indicators

#### Default Health Indicators

- **db**: Database connectivity
- **diskSpace**: Disk space availability
- **ping**: Basic liveness check
- **refreshScope**: Refresh scope support

#### Custom Health Indicators

- **request**: Request processing health
- **workflow**: Workflow engine status

### Sample Health Response

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

| Metric | Description | Tags |
|--------|-------------|------|
| jvm.memory.used | Memory used by JVM | area, id |
| jvm.memory.max | Maximum memory | area, id |
| jvm.memory.committed | Committed memory | area, id |
| jvm.threads.live | Live thread count | - |
| jvm.threads.daemon | Daemon thread count | - |
| jvm.gc.pause | GC pause times | cause, gc |

### HTTP Metrics

| Metric | Description | Tags |
|--------|-------------|------|
| http.server.requests | HTTP request counts | method, uri, status |
| http.server.requests.duration | Request durations | method, uri |
| http.server.requests.active | Active requests | - |

### Database Metrics

| Metric | Description | Tags |
|--------|-------------|------|
| hikaricp.connections.active | Active connections | - |
| hikaricp.connections.idle | Idle connections | - |
| hikaricp.connections.pending | Pending connections | - |
| hikaricp.connections.max | Max connections | - |

### Custom Business Metrics

```java
@Component
public class RequestMetrics {
    private final Counter requestCounter;
    private final Timer requestTimer;
    
    public RequestMetrics(MeterRegistry registry) {
        this.requestCounter = Counter.builder("accessiq.requests.total")
            .description("Total requests created")
            .register(registry);
        this.requestTimer = Timer.builder("accessiq.requests.duration")
            .description("Request processing time")
            .register(registry);
    }
    
    public void recordRequest() {
        requestCounter.increment();
    }
}
```

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
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

### Sample Metrics Output

```
# HELP jvm_memory_bytes_used The amount of used memory
# TYPE jvm_memory_bytes_used gauge
jvm_memory_bytes_used{area="heap",} 1.534845952E8

# HELP http_server_requests_seconds_count
# TYPE http_server_requests_seconds_count counter
http_server_requests_seconds_count{method="GET",uri="/api/v1/requests",status="200",} 42.0

# HELP hikaricp_connections_active
# TYPE hikaricp_connections_active gauge
hikaricp_connections_active 5.0
```

### Prometheus Configuration

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'accessiq'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['accessiq:8080']
```

## Grafana Dashboard

### Import Dashboard

1. Open Grafana
2. Navigate to Dashboards → Manage → Import
3. Upload `grafana/dashboard.json`
4. Configure Prometheus as data source

### Dashboard Panels

1. **Application Health** - Overall health status
2. **HTTP Request Rate** - Requests per second by endpoint
3. **JVM Memory Usage** - Heap and non-heap memory
4. **Database Connections** - Active and idle connections
5. **HTTP Response Time** - Request duration percentiles
6. **GC Activity** - Garbage collection metrics
7. **Business Metrics** - Custom request counts

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

      - alert: SlowResponses
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: Slow responses
          description: 95th percentile response time is above 2 seconds
```

### Alert Severity Levels

| Level | Condition | Action |
|-------|-----------|--------|
| Critical | > 90% memory, > 5% 5xx errors, service down | PagerDuty/Slack alert |
| Warning | > 80% memory, > 1s response time | Slack notification |
| Info | New deployment, config change | Log event |

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

## Key Metrics to Monitor

### Application Health

| Metric | Target | Alert |
|--------|--------|-------|
| Health Status | UP | Critical |
| Uptime | 99.9% | - |
| Restart Count | < 1/day | Warning |

### Performance

| Metric | Target | Alert |
|--------|--------|-------|
| 95th percentile response | < 500ms | Warning |
| 99th percentile response | < 1000ms | Warning |
| Error rate | < 1% | Critical |
| Database latency | < 100ms | Warning |

### Resources

| Metric | Target | Alert |
|--------|--------|-------|
| Memory usage | < 75% | Warning |
| CPU usage | < 80% | Warning |
| Disk usage | < 85% | Warning |
| Connection pool | < 80% | Warning |

## Troubleshooting

### High Memory Usage

1. Check heap dump: `/actuator/heapdump`
2. Analyze with Eclipse MAT or VisualVM
3. Look for memory leaks in business code

### Slow Response Times

1. Check database query times
2. Analyze slow endpoint with profiling
3. Check for thread blocking

### High Error Rate

1. Check logs for exception stack traces
2. Verify database connectivity
3. Check third-party service availability

## See Also

- [AWS-Deployment.md](AWS-Deployment.md) - AWS monitoring setup
- [Troubleshooting.md](Troubleshooting.md) - Common issues