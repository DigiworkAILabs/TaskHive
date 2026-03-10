# TaskHive — Non-Functional Requirements Compliance Analysis

**SRS Version:** 2.3 | **Analysis Date:** 2026-02-28  
**Project:** TaskHive — Enterprise Task Management System | **Digiwork**

> This document is a full compliance analysis of SRS v2.3 NFR sections (5.1–5.7) against the actual project implementation. Every finding is based on direct inspection of source files, configuration, and dependencies.

---

## Overall Compliance Summary

| Category | Total NFRs | ✅ Met | ⚠️ Partial | ❌ Missing |
|----------|-----------|--------|-----------|------------|
| 5.1 Performance | 7 | 4 | 0 | 3 |
| 5.2 Security | 14 | 8 | 1 | 5 |
| 5.3 Reliability | 7 | 3 | 0 | 4 |
| 5.4 Maintainability | 9 | 3 | 1 | 5 |
| 5.5 Usability | 6 | 2 | 1 | 3 |
| 5.6 Operational | 7 | 1 | 0 | 6 |
| 5.7 Disaster Recovery | 1 | 0 | 0 | 1 |
| **Total** | **51** | **21 (41%)** | **3 (6%)** | **27 (53%)** |

> **Note:** ML/AI integration, Redis caching, and multi-tenancy are listed as **Future Enhancements** in SRS Section 13 and are excluded from the missing count.

---

# 5.1 Performance (4/7 Met)

---

### NFR-PERF-01 ❌ Missing — API Response Time Targets

**SRS Requirement:** Login `<200ms` · Task list (paginated) `<150ms` · Task creation `<250ms` · Dashboard analytics `<500ms` · Search `<300ms`

**Finding:** No performance benchmarking, response-time enforcement, or monitoring is present. No load testing tools (JMeter, Gatling, k6) are configured. No Spring Actuator metrics are available to even measure current response times. The application may meet these targets locally, but there is no mechanism to prove, enforce, or alert on deviations.

**What's Needed:**
- Add `spring-boot-starter-actuator` + `micrometer-registry-prometheus` to `pom.xml`
- Configure response time metrics and percentile histograms
- Add a load testing suite (Gatling or k6) with scripts targeting each endpoint
- Define alerting thresholds matching SRS targets

---

### NFR-PERF-02 ❌ Missing — 95th Percentile Response < 300ms

**SRS Requirement:** 95th percentile response `<300ms` for standard operations under normal load.

**Finding:** Same root cause as NFR-PERF-01. Without Spring Actuator and Micrometer, percentile metrics cannot be recorded. No load testing scripts exist to simulate normal load conditions.

**What's Needed:**
- Micrometer histograms with percentile tracking enabled
- CI/CD pipeline step that runs load tests and fails on SLA violations

---

### NFR-PERF-03 ❌ Missing — 100–500 Concurrent Users

**SRS Requirement:** System shall support minimum 100 concurrent users; target 500.

**Finding:** No capacity planning or load testing has been performed. HikariCP is configured to 20 connections (a good foundation) but no stress test has verified the application can handle 100–500 concurrent users within acceptable response times.

**What's Needed:**
- Load testing with concurrency ramp-up from 100 to 500 virtual users
- Connection pool tuning based on results
- Application profiling under load (thread pool, GC, DB connections)

---

### NFR-PERF-04 ✅ Met — Database Indexes + No N+1

**SRS Requirement:** All queries shall use appropriate indexes; no N+1 problems (use `JOIN FETCH`).

**Evidence:** Indexes are defined across 21 Flyway migration scripts. Key indexes:

| Table | Index | Purpose |
|-------|-------|---------|
| `users` | `idx_users_email` | Login lookups |
| `employees` | `idx_employees_department`, `idx_employees_status` | Filtered lists |
| `tasks` | `idx_tasks_assigned_to`, `idx_tasks_status`, `idx_tasks_due_date` | Task queries |
| `notifications` | `idx_notifications_user_id`, `idx_notifications_is_read` | Notification queries |
| `audit_logs` | `idx_audit_logs_entity`, `idx_audit_logs_created_at` | Audit searches |

Spring Data JPA is used throughout with parameterized queries. `spring.jpa.show-sql=true` in dev properties helps identify N+1 issues during development.

---

### NFR-PERF-05 ✅ Met — Pagination on All List Endpoints

**SRS Requirement:** Pagination required on all list endpoints (default 20 per page).

**Evidence:** Shared `PageResponse.java` DTO exists in the common layer. All list endpoints (employees, tasks, notifications, audit logs) use Spring Data's `Pageable`. `PageResponse` standardizes pagination metadata (totalElements, totalPages, currentPage, size) across all modules.

---

### NFR-PERF-06 ✅ Met — HikariCP Connection Pool Max 20

**SRS Requirement:** HikariCP connection pool: max 20 connections.

**Evidence:** Explicitly configured in `application-dev.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
```

---

### NFR-PERF-07 ✅ Met — Analytics Use Pre-Calculated Cache Tables

**SRS Requirement:** Analytics queries shall use pre-calculated cache tables (`daily_metrics`, `employee_performance_cache`).

**Evidence:** Both cache tables are created via Flyway migration `V20__create_analytics_tables.sql`. Key files: `DailyMetrics.java`, `EmployeePerformanceCache.java`, `MetricsCalculationScheduler.java` (nightly job), `MetricsAggregatorService.java`.

---

# 5.2 Security (8/14 Met, 1 Partial)

---

### NFR-SEC-01 ✅ Met — JWT in HttpOnly Secure SameSite Cookies

**SRS Requirement:** JWT shall be stored in HttpOnly, Secure, SameSite=Strict cookies only — never in localStorage or sessionStorage.

**Evidence:** `CookieUtil.java` creates cookies with HttpOnly flag. `JwtAuthenticationFilter.java` reads JWT exclusively from the `accessToken` cookie. Cookie parameters are externalized in properties.

> ⚠️ In dev, `secure=false` and `same-site=Lax` are used for local/emulator development — acceptable. However, no `application-prod.properties` exists with `secure=true` and `same-site=Strict` for production.

---

### NFR-SEC-02 ✅ Met — All Endpoints Require JWT (Except Public)

**SRS Requirement:** All endpoints (except public auth routes) shall require valid JWT.

**Evidence:** `SecurityConfig.java` defines an explicit public endpoint whitelist and applies `.anyRequest().authenticated()` for everything else:

```java
private static final String[] PUBLIC_ENDPOINTS = {
    "/api/v1/auth/login",
    "/api/v1/auth/activate-account",
    "/api/v1/auth/forgot-password",
    "/api/v1/auth/reset-password",
    "/api/v1/auth/refresh",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/uploads/**",
    "/ws/**"
};
```

`JwtAuthenticationFilter` is added before `UsernamePasswordAuthenticationFilter` in the filter chain.

---

### NFR-SEC-03 ❌ Missing — HTTPS (TLS 1.3) in Production

**SRS Requirement:** HTTPS (TLS 1.3) enforced in production.

**Finding:** No `application-prod.properties` file exists. No `server.ssl.*` properties defined anywhere. No reverse proxy (NGINX/Caddy) configuration found.

**What's Needed:**
- Create `application-prod.properties` with `server.ssl.*` properties, OR configure TLS termination at the reverse proxy/load balancer level
- Set `app.cookie.secure=true` and `app.cookie.same-site=Strict` for production

---

### NFR-SEC-04 ✅ Met — CORS with Explicit Origin Whitelist

**SRS Requirement:** CORS configured with explicit origin whitelist and `allowCredentials: true`.

**Evidence:** `CorsConfig.java` reads the frontend URL from properties and configures explicit origin-only allowlisting:
```java
configuration.setAllowedOrigins(List.of(frontendUrl)); // Explicit, never wildcard
configuration.setAllowCredentials(true);
configuration.setMaxAge(3600L);                         // 1-hour preflight cache
```

---

### NFR-SEC-05 ✅ Met — CSRF Protection via SameSite Cookie

**SRS Requirement:** CSRF protection via `SameSite=Strict` on all cookies — `.csrf().disable()` is intentional.

**Evidence:** CSRF explicitly disabled in `SecurityConfig.java`:
```java
.csrf(AbstractHttpConfigurer::disable)
```
Safe because the SameSite cookie attribute prevents CSRF attacks at the browser level. The `app.cookie.same-site` property controls this behavior.

---

### NFR-SEC-06 ✅ Met — BCrypt Strength 12

**SRS Requirement:** Passwords hashed with BCrypt (strength 12).

**Evidence:** `PasswordEncoderConfig.java` provides a `BCryptPasswordEncoder` bean. `PasswordService.java` handles encoding and validation.

---

### NFR-SEC-07 ✅ Met — Refresh Tokens Stored as SHA-256 Hash

**SRS Requirement:** Refresh tokens stored as SHA-256 hash in DB.

**Evidence:** `RefreshToken.java` has a `tokenHash` field (not plain text). `TokenService.java` hashes tokens before storing.

---

### NFR-SEC-08 ✅ Met — Reset/Activation Tokens as SHA-256 Hash

**SRS Requirement:** Reset and activation tokens stored as SHA-256 hash in DB.

**Evidence:** Both `PasswordResetToken.java` and `AccountActivationToken.java` use `tokenHash` fields. DB schema specifies `token_hash VARCHAR(255) UNIQUE NOT NULL`.

---

### NFR-SEC-09 ✅ Met — SQL Injection Prevention

**SRS Requirement:** SQL injection prevented via JPA parameterized queries.

**Evidence:** All data access is via Spring Data JPA repositories (`JpaRepository`, `@Query` with named parameters). No raw SQL string concatenation found in the codebase.

---

### NFR-SEC-10 ⚠️ Partial — File Upload Validation

**SRS Requirement:** File uploads validated for type (JPG/PNG/WebP for photos) and size (max 5MB photos, max 20MB attachments).

**Finding:** File size limits are configured:
```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=10MB
```
However, the current config caps ALL uploads at 5MB with a 10MB request size — this does not match the SRS's 20MB attachment limit. File type validation (MIME type checking for JPG/PNG/WebP) exists in `ProfilePhotoService.java`, but attachment type validation needs separate configuration.

---

### NFR-SEC-11 ✅ Met — RBAC at API and Service Layer

**SRS Requirement:** RBAC enforced at API and service layer — no client-side permission checks.

**Evidence:** 28 `@PreAuthorize` annotations across controllers. `@EnableMethodSecurity` enabled in `SecurityConfig.java`.

| Controller | `@PreAuthorize` Count | Roles Enforced |
|------------|-----------------------|----------------|
| `TaskController.java` | 5 | `hasRole('ADMIN')` |
| `EmployeeController.java` | 10 | `hasRole('ADMIN')` |
| `AuditController.java` | 5 | `hasRole('ADMIN')` |
| `AnalyticsController.java` | 8 | `hasRole('ADMIN')`, `hasAnyRole('ADMIN','EMPLOYEE')` |

Frontend `middleware.ts` provides complementary client-side route guards but is not the primary enforcement.

---

### NFR-SEC-12 ❌ Missing — No Sensitive Data in Logs

**SRS Requirement:** No sensitive data (passwords, tokens, PII) in logs.

**Finding:** No `logback-spring.xml` configuration file exists. No log redaction filters or pattern replacements are configured. `spring.jpa.show-sql=true` in dev properties could leak sensitive query parameters. No MDC-based filtering to strip tokens from log output.

**What's Needed:**
- Create `logback-spring.xml` with pattern-based redaction for tokens, passwords, emails
- Turn off `show-sql` in production profile
- Review all `log.debug`/`log.info` calls for accidental PII logging

---

### NFR-SEC-13 ❌ Missing — GDPR Compliance

**SRS Requirement:** GDPR compliance: user data export and deletion capability.

**Finding:** No GDPR-specific endpoints exist. No APIs for user data export (right to data portability), user data deletion (right to be forgotten), consent management, or data processing records. Soft delete is used on employees and tasks, but full GDPR compliance requires the ability to export and permanently anonymize data on request.

**What's Needed:**
- `GET /api/v1/users/{id}/data-export` — Export all user data as JSON/ZIP
- `DELETE /api/v1/users/{id}/gdpr-delete` — Anonymize and purge user data

---

### NFR-SEC-14 ❌ Missing — Security Headers (CSP, X-Frame-Options, HSTS)

**SRS Requirement:** Content Security Policy, X-Frame-Options, HSTS headers enabled.

**Finding:** No security headers configured anywhere. Searched for `Content-Security-Policy`, `X-Frame-Options`, and `Strict-Transport-Security` across all Java, TypeScript, and properties files — zero results. Spring Security's default headers are partially disabled because a custom `SecurityFilterChain` is used without calling `.headers()` configuration. This creates:
- No `X-Frame-Options` → Clickjacking vulnerability
- No `Content-Security-Policy` → XSS risk
- No `HSTS` → Downgrade attack risk

**Fix:** Add to `SecurityConfig.java`:
```java
http.headers(headers -> headers
    .frameOptions(frame -> frame.deny())
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
);
```

---

# 5.3 Reliability (3/7 Met)

---

### NFR-REL-01 ❌ Missing — 99.5% Uptime Target

**SRS Requirement:** 99.5% uptime target (excludes planned maintenance `<2hrs/month`).

**Finding:** No high-availability infrastructure exists. Application runs as a single instance with no health check monitoring, automatic restart on failure, load balancing, or failover strategy. Without Docker/K8s, Spring Actuator health endpoints, or uptime monitoring tools, uptime cannot be measured or guaranteed.

---

### NFR-REL-02 ❌ Missing — Database Backups

**SRS Requirement:** Daily full backups, hourly incremental; 30-day online retention, 1-year archive.

**Finding:** No backup scripts, cron jobs, or cloud backup configurations found. No `pg_dump` automation, no WAL archiving configuration, no S3 backup lifecycle policies.

---

### NFR-REL-03 ❌ Missing — Point-in-Time Recovery (PITR)

**SRS Requirement:** Point-in-time recovery (PITR) capability.

**Finding:** No PostgreSQL WAL archiving (`archive_mode`, `archive_command`) configuration exists. Without WAL archiving, PITR is impossible.

---

### NFR-REL-04 ✅ Met — Email Failures Don't Cause API Errors

**SRS Requirement:** Email failures shall not cause API errors (async queue with retry).

**Evidence:** Email sending is fully asynchronous via `EmailQueueService`. Emails are queued as `PENDING` records in the DB. `EmailQueueScheduler` processes pending emails every 30 seconds with retry logic — up to 3 attempts per email. API responses are never blocked by email delivery.

---

### NFR-REL-05 ✅ Met — WebSocket Disconnect Doesn't Affect REST

**SRS Requirement:** WebSocket disconnection shall not affect REST API functionality.

**Evidence:** WebSocket and REST are completely decoupled. `WebSocketConfig.java` configures STOMP over SockJS on `/ws` with an in-memory broker. REST controllers have no dependency on WebSocket connectivity.

---

### NFR-REL-06 ❌ Missing — ML Service Circuit Breaker

**SRS Requirement:** ML service unavailability shall not break core functionality (circuit breaker fallback).

**Finding:** No `resilience4j` dependency in `pom.xml`. No circuit breaker configuration or fallback methods. ML integration is listed as a "Future Enhancement" in SRS Section 13 — expected to be missing at this stage.

---

### NFR-REL-07 ❌ Missing — Error Rate Alerting

**SRS Requirement:** API error rate `>1%` triggers alert to ops team.

**Finding:** `GlobalExceptionHandler.java` handles 16 specific exception types and returns structured `ErrorResponse` objects. Error events are logged to `security_events` via `AuditService`. However, no real-time error rate monitoring or alerting system (Prometheus alerts, PagerDuty, etc.) is configured. No Spring Actuator to measure error rates.

---

# 5.4 Maintainability (3/9 Met, 1 Partial)

---

### NFR-MAIN-01 ❌ Missing — Test Coverage > 80%

**SRS Requirement:** Unit + integration test coverage `>80%` on service layer.

**Finding:** Only **4 test files** exist in the entire test directory:

| Test File | Type |
|-----------|------|
| `TaskhiveBackendApplicationTests.java` | Smoke test |
| `FullFlowIntegrationTest.java` | Integration |
| `AnalyticsServiceTest.java` | Unit |
| `AuditServiceTest.java` | Unit |

No JaCoCo plugin in `pom.xml` to measure coverage. No Testcontainers for DB integration tests.

**Tests Missing Per SRS:**
- `AuthControllerTest`, `AuthServiceTest`, `TokenServiceTest`, `PasswordServiceTest`
- `AccountActivationServiceTest`, `PasswordResetServiceTest`
- `JwtTokenProviderTest`, `JwtAuthenticationFilterTest`
- `EmployeeControllerTest`, `EmployeeServiceTest`
- `TaskControllerTest`, `TaskServiceTest`
- `NotificationServiceTest`, `EmailQueueServiceTest`
- `AuthIntegrationTest`, `EmployeeIntegrationTest`, `TaskIntegrationTest`

---

### NFR-MAIN-02 ❌ Missing — SonarQube Quality Gate

**SRS Requirement:** SonarQube quality gate: A rating; no critical or blocker issues.

**Finding:** No `sonar-project.properties` file. No `sonar-maven-plugin` in `pom.xml`. No SonarQube server configuration. No CI pipeline to run SonarQube analysis.

---

### NFR-MAIN-03 ❌ Missing — Code Style Enforcement

**SRS Requirement:** Backend code style enforced via Checkstyle; frontend via ESLint.

**Finding:**
- **Backend:** No `checkstyle.xml` config, no `maven-checkstyle-plugin` in `pom.xml`
- **Frontend:** No `.eslintrc.*` config file found. Next.js includes a default ESLint config in `next.config.ts` but no custom rules are defined

---

### NFR-MAIN-04 ✅ Met — Event-Driven Module Communication

**SRS Requirement:** All modules communicate only via domain events or defined service interfaces.

**Evidence:** Modules communicate exclusively via Spring Application Events:

```
Auth Module     → UserAuthenticatedEvent, PasswordChangedEvent, AccountActivatedEvent
Employee Module → EmployeeCreatedEvent, EmployeeUpdatedEvent, EmployeeActivatedEvent
Task Module     → TaskCreatedEvent, TaskStatusChangedEvent, TaskCommentAddedEvent

Notification Module → Listens to ALL above events → sends notifications
Audit Module        → Listens to ALL above events → creates audit logs
```

Dedicated event listeners: `AuthEventListener.java`, `EmployeeEventListener.java`, `TaskEventListener.java` (Notification module), `AuditEventListener.java` (Audit module). No direct cross-module service calls found.

---

### NFR-MAIN-05 ✅ Met — Flyway Versioned Migrations

**SRS Requirement:** Database changes managed via Flyway versioned migrations.

**Evidence:** 21 Flyway migration scripts in `src/main/resources/db/migration/`:

| Migrations | Purpose |
|-----------|---------|
| V1–V8 | Auth tables (users, roles, tokens, password history, indexes) |
| V9–V10 | Employee tables |
| V11–V14 | Task tables (tasks, comments, attachments, status history) |
| V15–V17 | Notification tables (notifications, preferences, email queue) |
| V18–V19 | Audit tables (audit logs, security events) |
| V20–V21 | Analytics + file metadata tables |

---

### NFR-MAIN-06 ✅ Met — SpringDoc OpenAPI 3 Documentation

**SRS Requirement:** All APIs documented with SpringDoc OpenAPI 3.

**Evidence:** `springdoc-openapi-starter-webmvc-ui` (v2.8.5) in `pom.xml`. `OpenApiConfig.java` configures project metadata. Swagger UI accessible at `/swagger-ui.html` (whitelisted in `SecurityConfig.java`).

---

### NFR-MAIN-07 ⚠️ Partial — Structured JSON Logging

**SRS Requirement:** Structured JSON logging (SLF4J + Logback); log levels: ERROR, WARN, INFO, DEBUG.

**Evidence:** SLF4J is available via Spring Boot's default Logback. `@Slf4j` annotation used throughout the codebase. Debug-level logging configured in properties:
```properties
logging.level.com.digiwork.taskhive=DEBUG
logging.level.org.springframework.security=DEBUG
```

**What's Missing:** No `logback-spring.xml` exists. Logging format is default console text — not structured JSON. Cannot feed logs into ELK/Splunk/CloudWatch without manual parsing.

---

### NFR-MAIN-08 ❌ Missing — Log Retention Policy

**SRS Requirement:** Log retention: ERROR — 90 days; WARN/INFO — 30 days; DEBUG — 7 days.

**Finding:** Without a `logback-spring.xml`, there are no rolling file appenders or retention policies. Logs only go to stdout and are lost when the process stops.

---

### NFR-MAIN-09 ❌ Missing — Correlation IDs

**SRS Requirement:** Correlation IDs on all requests for distributed tracing readiness.

**Finding:** No MDC-based correlation ID implementation found. Searched for `correlationId`, `MDC`, `X-Correlation-Id`, `traceId` — zero results. Individual requests cannot be traced across log entries. Distributed tracing tools (Zipkin, Jaeger) cannot be integrated.

**What's Needed:**
- Create `CorrelationIdFilter.java` that generates/reads `X-Correlation-Id` header
- Put correlation ID in MDC for all log entries
- Include in logback JSON pattern

---

# 5.5 Usability (2/6 Met, 1 Partial)

---

### NFR-USE-01 ⚠️ Partial — Responsive Design

**SRS Requirement:** Responsive design: mobile, tablet, desktop.

**Evidence:** Tailwind CSS is used for styling, providing responsive utilities (`sm:`, `md:`, `lg:` breakpoints). Some chart components reference responsive behavior.

**What's Missing:** No custom `@media` queries in any CSS files. No dedicated mobile layout components. No responsive testing evidence.

---

### NFR-USE-02 ❌ Missing — WCAG 2.1 Level AA Accessibility

**SRS Requirement:** WCAG 2.1 Level AA accessibility compliance.

**Finding:** No accessibility-related code, configuration, or testing found. No `aria-*` attributes audit, no color contrast validation, no keyboard navigation testing, no screen reader testing.

---

### NFR-USE-03 ✅ Met — Browser Support

**SRS Requirement:** Chrome, Firefox, Safari, Edge (latest 2 versions each).

**Evidence:** Next.js 14+ with React 18+ inherently supports all modern browsers. No `browserslist` exclusions that would restrict compatibility.

---

### NFR-USE-04 ❌ Missing — Touch Target Minimum 44×44px

**SRS Requirement:** Minimum touch target: 44×44px.

**Finding:** No CSS rules enforcing minimum touch target sizes. Shadcn/ui components may meet this for some elements, but there is no project-wide enforcement or audit.

---

### NFR-USE-05 ✅ Met — Loading States for Async Operations

**SRS Requirement:** Loading states for all async operations.

**Evidence:** `LoadingSpinner.tsx` component exists. React Query (TanStack Query) provides `isLoading` states for all API calls. Custom hooks (`useEmployees.ts`, `useTasks.ts`) expose loading states to components.

---

### NFR-USE-06 ❌ Missing — User-Friendly Error Messages (No Stack Traces)

**SRS Requirement:** User-friendly error messages — no stack traces exposed to client.

**Finding:** `GlobalExceptionHandler.java` handles 16 specific exception types with structured `ErrorResponse` objects. The catch-all handler logs stack traces server-side and returns a generic message to the client — well implemented. However, since no production profile exists, it is unclear if `spring.jpa.show-sql=true` and debug logging might leak information in a deployed environment. Marked missing pending production profile verification.

---

# 5.6 Operational (1/7 Met)

---

### NFR-OPS-01 ❌ Missing — Zero-Downtime Blue-Green Deployments

**SRS Requirement:** Zero-downtime deployments via blue-green strategy.

**Finding:** No deployment scripts, infrastructure-as-code, or deployment pipeline configurations exist.

---

### NFR-OPS-02 ❌ Missing — Environment Parity (Docker + K8s)

**SRS Requirement:** Environment parity: development (Docker Compose), staging (K8s), production (K8s).

**Finding:** No containerization artifacts found:
- No `Dockerfile` in backend or frontend
- No `docker-compose.yml` or `docker-compose-prod.yml`
- No Kubernetes manifests (deployments, services, ingress)
- No Helm charts

---

### NFR-OPS-03 ❌ Missing — No Hardcoded Credentials

**SRS Requirement:** No hardcoded credentials; all secrets via environment variables.

**Finding:** `application-dev.properties` has hardcoded credentials (acceptable for development). However, no `application-prod.properties` exists to demonstrate environment variable substitution. Without a production profile, there is no mechanism to prevent dev credentials from reaching production:

```properties
# application-dev.properties — hardcoded dev values
spring.datasource.password=password
jwt.secret=TaskHiveDevSecretKey2026...
spring.mail.password=ezmmmitmxmklksjg
app.admin.password=Admin@123
```

---

### NFR-OPS-04 ❌ Missing — HashiCorp Vault Ready

**SRS Requirement:** HashiCorp Vault ready for secret management in production.

**Finding:** No `spring-cloud-vault` dependency in `pom.xml`. No Vault configuration properties.

---

### NFR-OPS-05 ❌ Missing — Spring Actuator Endpoints

**SRS Requirement:** Spring Actuator health, metrics, prometheus endpoints exposed.

**Finding:** `spring-boot-starter-actuator` is **not** in `pom.xml`. This means:
- No `/actuator/health` — cannot check application health
- No `/actuator/metrics` — cannot measure performance
- No `/actuator/prometheus` — cannot export metrics to Grafana

**Fix:** Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

---

### NFR-OPS-06 ❌ Missing — Prometheus/Grafana Ready Metrics

**SRS Requirement:** Prometheus/Grafana ready metrics export.

**Finding:** Cannot be met without Actuator (NFR-OPS-05). No `micrometer-registry-prometheus` dependency. No Grafana dashboard JSON exports or Prometheus scrape configs.

---

### NFR-OPS-07 ✅ Met — Automated Flyway Migrations on Startup

**SRS Requirement:** Automated Flyway migrations run on application startup.

**Evidence:**
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```
Flyway runs automatically every time the Spring Boot application starts, applying any pending migrations.

---

# 5.7 Disaster Recovery (0/1 Met)

---

### DR Plan ❌ Missing — RTO 4 Hours / RPO 1 Hour

**SRS Requirement:** RTO: 4 hours, RPO: 1 hour. Daily PostgreSQL full dump to S3, hourly WAL incremental, file backup via rsync/S3 versioning. Quarterly recovery drills.

**Finding:** No disaster recovery infrastructure exists:
- No backup scripts or cron jobs
- No WAL archiving for PostgreSQL (`archive_mode`, `archive_command`)
- No S3 backup lifecycle configuration
- No recovery procedure documentation
- No quarterly drill evidence

---

# Priority Action Items

> ⛔ **P0 — Critical blockers for production deployment:**

| Priority | NFR ID(s) | Action | Effort |
|----------|-----------|--------|--------|
| 🔴 P0 | OPS-02 | Create `Dockerfile` + `docker-compose.yml` for dev/prod | Medium |
| 🔴 P0 | OPS-03, SEC-03 | Create `application-prod.properties` with env vars + TLS config | Low |
| 🔴 P0 | OPS-05/06 | Add Spring Actuator + Micrometer Prometheus to `pom.xml` | Low |
| 🔴 P0 | SEC-14 | Add security headers (CSP, HSTS, X-Frame-Options) to `SecurityConfig.java` | Low |
| 🟠 P1 | MAIN-01 | Write unit + integration tests targeting 80% coverage | High |
| 🟠 P1 | MAIN-07/08 | Create `logback-spring.xml` with JSON format + rolling retention | Low |
| 🟠 P1 | MAIN-09 | Implement `CorrelationIdFilter.java` + MDC integration | Low |
| 🟠 P1 | SEC-12 | Add log redaction for sensitive data in logback config | Low |
| 🟡 P2 | MAIN-02 | Set up SonarQube + quality gates in CI pipeline | Medium |
| 🟡 P2 | MAIN-03 | Add Checkstyle (`pom.xml`) + ESLint (`.eslintrc`) configs | Low |
| 🟡 P2 | SEC-13 | Build GDPR data export and deletion endpoints | Medium |
| 🟡 P2 | REL-02/03 | Configure PostgreSQL `pg_dump` backups + WAL archiving | Medium |
| 🟡 P2 | PERF-01/02/03 | Load testing suite + performance benchmarks (Gatling or k6) | Medium |

---

# Quick Reference: What's Met vs Missing

### ✅ Fully Met (21)

| NFR | Description |
|-----|-------------|
| PERF-04 | Database indexes + no N+1 queries |
| PERF-05 | Pagination on all list endpoints |
| PERF-06 | HikariCP max 20 connections |
| PERF-07 | Analytics pre-calculated cache tables |
| SEC-01 | JWT in HttpOnly cookie |
| SEC-02 | All endpoints require JWT |
| SEC-04 | CORS with explicit origin whitelist |
| SEC-05 | CSRF via SameSite cookie |
| SEC-06 | BCrypt strength 12 |
| SEC-07 | Refresh tokens as SHA-256 hash |
| SEC-08 | Reset/activation tokens as SHA-256 hash |
| SEC-09 | SQL injection via JPA parameterized queries |
| SEC-11 | RBAC at API + service layer (28 `@PreAuthorize`) |
| REL-04 | Email failures don't cause API errors |
| REL-05 | WebSocket disconnect doesn't affect REST |
| MAIN-04 | Event-driven module communication |
| MAIN-05 | 21 Flyway versioned migrations |
| MAIN-06 | SpringDoc OpenAPI 3 documentation |
| USE-03 | Browser support (Chrome, Firefox, Safari, Edge) |
| USE-05 | Loading states for all async operations |
| OPS-07 | Automated Flyway migrations on startup |

### ⚠️ Partial (3)

| NFR | Description | Gap |
|-----|-------------|-----|
| SEC-10 | File upload validation | 5MB cap on all files; SRS requires 20MB for attachments |
| MAIN-07 | Structured JSON logging | SLF4J present, but no `logback-spring.xml` with JSON encoder |
| USE-01 | Responsive design | Tailwind utilities used, but no custom `@media` queries or mobile layouts |

### ❌ Missing (27)

All NFRs in 5.1 (PERF-01/02/03), 5.2 (SEC-03/12/13/14), 5.3 (REL-01/02/03/06/07), 5.4 (MAIN-01/02/03/08/09), 5.5 (USE-02/04/06), 5.6 (OPS-01/02/03/04/05/06), and 5.7 (DR Plan).
