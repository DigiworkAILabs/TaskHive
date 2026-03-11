# Step 4 — NFR-OPS-05 + NFR-OPS-06: Spring Actuator + Prometheus

**NFR IDs:** NFR-OPS-05, NFR-OPS-06
**Effort:** ~30 min | **Dependencies:** None
**Commit:** `feat(monitoring): add Spring Actuator + Prometheus (NFR-OPS-05, NFR-OPS-06)`

---

## SRS Requirements

> **NFR-OPS-05:** Spring Actuator health, metrics, prometheus endpoints exposed.
> **NFR-OPS-06:** Prometheus/Grafana ready metrics export.

## Current State

No `spring-boot-starter-actuator` or `micrometer-registry-prometheus` in `pom.xml`. No health check or metrics endpoints available.

## Implementation

### Modify: `pom.xml`

Add two dependencies:

```xml
<!-- Monitoring & Metrics (NFR-OPS-05, NFR-OPS-06) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Modify: `application-dev.properties`

Add actuator configuration:

```properties
# =============================================
# ACTUATOR & MONITORING (NFR-OPS-05, NFR-OPS-06)
# =============================================
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
management.metrics.tags.application=${spring.application.name}
```

### Modify: `application-prod.properties`

```properties
# ── Actuator (expose only needed endpoints) ──
management.endpoints.web.exposure.include=health,prometheus,info
management.endpoint.health.show-details=when-authorized
management.endpoint.health.probes.enabled=true
management.metrics.tags.application=${spring.application.name}
```

### Modify: `SecurityConfig.java`

Add actuator endpoints to PUBLIC_ENDPOINTS:

```java
private static final String[] PUBLIC_ENDPOINTS = {
    // ... existing endpoints ...
    "/actuator/health/**",
    "/actuator/prometheus"
};
```

### Available Endpoints After Implementation

| Endpoint | Purpose | Example |
|----------|---------|---------|
| `/actuator/health` | Application health check | `{"status":"UP"}` |
| `/actuator/health/liveness` | K8s liveness probe | `{"status":"UP"}` |
| `/actuator/health/readiness` | K8s readiness probe | `{"status":"UP"}` |
| `/actuator/prometheus` | Prometheus metrics scrape | Metrics in Prometheus format |
| `/actuator/metrics` | All available metrics | List of metric names |
| `/actuator/info` | Application info | App name, version |

## Verification

```bash
# Health check
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"}}}

# Prometheus metrics
curl http://localhost:8081/actuator/prometheus
# Expected: Lines like:
# http_server_requests_seconds_count{method="GET",uri="/api/v1/tasks",...} 42
# jvm_memory_used_bytes{area="heap",...} 123456789

# Specific metric
curl http://localhost:8081/actuator/metrics/http.server.requests
```
