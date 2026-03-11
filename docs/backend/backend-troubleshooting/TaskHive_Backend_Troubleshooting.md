# TaskHive Backend — Development Troubleshooting Reference

**Digiwork | `com.digiwork.taskhive` | Phases 1–6**  
Spring Boot 3.5 · Java 21 · PostgreSQL · Hibernate 6 · Flyway

> This document is a consolidated, phase-ordered record of every real error, configuration issue, and architectural decision encountered during TaskHive backend development. All issues are sourced from actual error logs — nothing speculative.

---

## How to Read This Document

Each issue contains three fields:

- **Symptom** — What you see: the error message or broken behavior.
- **Root Cause** — Why it is happening at the technical level.
- **Resolution** — The exact steps or code changes that fixed it.

---

# Phase 1 — Authentication Module

> Focus: User security, JWT, RBAC, and system initialization.

---

### Issue 1.1 — JDK Compatibility Error

**Symptom:** Project fails to build or run — Maven/compiler mismatch errors on startup.

**Root Cause:** Project was initialized with Maven 4.x patterns but running on a JDK 21 environment. The compiler plugin version was mismatched.

**Resolution:** Update `pom.xml` with the correct Java version and compiler plugin:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

---

### Issue 1.2 — Flyway Migration Failure

**Symptom:** Application fails to start with a Flyway error — migration blocked because the `users` table already exists.

**Root Cause:** The `users` table was created manually via SQL scripts before Flyway took over. Flyway detected the schema was not empty and refused to run.

**Resolution:** Set the baseline flag in `application-dev.properties`:

```properties
spring.flyway.baseline-on-migrate=true
```

---

### Issue 1.3 — JWT Secret Too Short

**Symptom:** Application throws an exception on startup related to the JWT signing key being too weak.

**Root Cause:** The JJWT library requires a minimum 256-bit key for the HS256 algorithm. The configured secret was shorter than this threshold.

**Resolution:** Generate and configure a cryptographically secure 256-bit (32+ character) secret string in the environment/properties file.

---

### Issue 1.4 — CORS Policy Denials

**Symptom:** Frontend (`localhost:3000`) requests are blocked by the browser with CORS errors. API calls never reach the backend.

**Root Cause:** No CORS configuration was in place. The browser blocked cross-origin requests from the frontend port to the backend port.

**Resolution:** Implement `CorsConfig.java` with explicit allowed origins, methods, and credentials:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowCredentials(true);
    // ...
}
```

> ⚠️ `allowCredentials(true)` requires `allowedOrigins` to be explicit — never `*`.

---

### Issue 1.5 — Emulator Connectivity: Login Failure

**Symptom:** Android Emulator cannot reach the backend. Login requests time out or connection is refused.

**Root Cause:** `localhost` inside the Android Emulator/WebView points to the guest device itself, not the host machine. The backend running on the host is unreachable via `localhost`.

**Resolution:** Update the API URL to use the standard Android-to-host bridge address and update Backend CORS/Security config to allow this origin:

```
API_BASE_URL=http://10.0.2.2:8080/api/v1
```

---

# Phase 2 — Employee Module

> Focus: Employee profile management, hierarchy, and photo storage.

---

### Issue 2.1 — Infinite Recursion in JSON Serialization

**Symptom:** `GET /employees` throws a `StackOverflowError`. The response never completes.

**Root Cause:** The `Employee` entity has a self-referencing `manager_id` foreign key. Jackson attempted to serialize the `manager` → `manager.manager` → ... chain recursively until it crashed.

**Resolution:** Annotate the manager field with `@JsonIgnore` and expose it as a flat string in the response DTO instead:

```java
// Employee.java
@JsonIgnore
@ManyToOne
private Employee manager;

// EmployeeResponse.java
private String managerName; // flat, safe string
```

---

### Issue 2.2 — Profile Photo Returns 404

**Symptom:** Uploaded photos are saved to disk but accessing them via `/uploads/**` returns `404 Not Found`.

**Root Cause:** Spring Boot does not automatically serve files from arbitrary disk paths. There was no resource handler mapping `/uploads/**` to the physical storage location.

**Resolution:** Add a `WebMvcConfigurer` resource handler:

```java
@Override
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:/var/taskhive/uploads/");
}
```

---

### Issue 2.3 — Photo Upload Fails: Directory Not Found

**Symptom:** Photo upload throws an exception — destination directory does not exist.

**Root Cause:** The upload path `/var/taskhive/uploads` was not created before the application ran. Java's file write operations do not auto-create parent directories.

**Resolution:** Add directory creation logic in `LocalStorageService` on startup:

```java
@PostConstruct
public void init() throws IOException {
    Files.createDirectories(Paths.get(uploadPath));
}
```

---

### Issue 2.4 — Unique Constraint Violation on Duplicate Employee

**Symptom:** Creating an employee with an already-used email throws an unhandled database exception — the client receives a generic `500` error.

**Root Cause:** No application-level check for duplicate emails before attempting the insert. The DB constraint fired and was not caught gracefully.

**Resolution:** Implement `EmployeeAlreadyExistsException` and check for duplicates before insert, returning a clean `409 Conflict` response.

---

# Phase 3 — Task Module

> Focus: Task assignment, status tracking, and automated scheduling.

---

### Issue 3.1 — Overdue Task Scheduler Never Runs

**Symptom:** The scheduled job for marking overdue tasks never executes — overdue tasks remain in their original status indefinitely.

**Root Cause:** The `@Scheduled` annotation was present on the method but `@EnableScheduling` was missing from the application class. Without it, Spring does not activate any scheduled jobs.

**Resolution:** Add `@EnableScheduling` to the main application class:

```java
@SpringBootApplication
@EnableScheduling
public class TaskHiveApplication { ... }
```

---

### Issue 3.2 — Full-Text Search Index Migration Fails

**Symptom:** Flyway migration fails when creating a `tsvector` index on columns that already have complex data.

**Root Cause:** The migration syntax was not standardized for PostgreSQL's full-text search index creation. The index creation was attempted after data was already loaded, causing conflicts.

**Resolution:** Standardize the migration syntax and ensure indexes are created before large data loads in the migration script order.

---

### Issue 3.3 — File Attachments Saved Despite Transaction Rollback

**Symptom:** Physical files are written to disk even when the associated database transaction for the task fails and rolls back — leaving orphaned files with no database record.

**Root Cause:** File I/O was executed before the transaction committed. When the DB transaction rolled back, the file was already on disk with no way to undo it.

**Resolution:** Use `TransactionSynchronizationManager` to defer file saving until after a successful DB commit:

```java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            storageService.save(file); // only runs if DB commits
        }
    }
);
```

---

### Issue 3.4 — Task Access Denied (403) for Assigned Employee

**Symptom:** Employees receive `403 Forbidden` when trying to view their own tasks.

**Root Cause:** The custom `@PreAuthorize` security expression was not correctly resolving task ownership from the `SecurityContext`. The employee's identity was not being matched against the task's `assignedTo` field.

**Resolution:** Fix the security expression in `@PreAuthorize` to correctly identify task ownership using the authenticated principal's employee ID.

---

# Phase 4 — Notification Module

> Focus: Real-time WebSocket updates, async email queue, and daily digests.

---

### Issue 4.1 — Duplicate Bean Name: `EmailService`

**Symptom:** Application fails to start with a `ConflictingBeanDefinitionException` — two beans named `EmailService` detected.

**Root Cause:** An `EmailService` class existed in both the `auth` and `notification` packages. Spring could not determine which to inject.

**Resolution:** Rename the notification module's component to `NotificationEmailService` to give it a unique bean name.

---

### Issue 4.2 — JSONB Casting Failure on Email Queue

**Symptom:** Inserting into the `email_queue` table fails with a PostgreSQL type casting error — `jsonb` column rejected a Java `String` input.

**Root Cause:** Hibernate was treating the `templateData` field as a plain `varchar`. PostgreSQL's `jsonb` column requires proper JSON type binding, not a raw string cast.

**Resolution:** Add the Hibernate JSON type annotation to the entity field:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private String templateData;
```

---

### Issue 4.3 — Double Email Delivery for Password Events

**Symptom:** Users receive duplicate emails for password-related events (e.g., password reset, password changed).

**Root Cause:** `AuthEventListener` in the `notification` module duplicated logic already present in the `auth` module's own event handlers. Both listeners were firing on the same event.

**Resolution:** Remove the redundant listeners from the `notification` module. The `auth` module's handlers own password event emails.

---

### Issue 4.4 — WebSocket Handshake Returns 401

**Symptom:** WebSocket connection fails at the handshake step with a `401 Unauthorized`.

**Root Cause:** The `/ws/**` endpoint was protected by the default JWT filter chain. WebSocket connections cannot pass a Bearer token in the standard `Authorization` header during the HTTP upgrade handshake.

**Resolution:** Whitelist `/ws/**` in `SecurityConfig`'s permitted paths. WebSocket authentication is handled internally via STOMP headers after the connection is established:

```java
.requestMatchers("/ws/**").permitAll()
```

---

### Issue 4.5 — Admins Not Receiving Task Notifications

**Symptom:** Admins have no visibility into task activity — status changes, completions, overdue alerts, and comments only notify the assigned employee.

**Root Cause:** `TaskEventListener` only sent notifications to the task's assigned employee. No logic existed to fan out notifications to admin users.

**Resolution:** Add a repository query to find all admin user IDs and include them in every task event notification dispatch, with a self-notification skip guard:

```java
List<UUID> adminIds = userRoleRepository.findUserIdsByRoleName("ADMIN");

for (UUID adminId : adminIds) {
    if (adminId.equals(assignedEmployeeUserId)) continue; // skip if already notified
    notificationService.send(adminId, notification);
}
```

---

# Phase 5 — Audit Module

> Focus: Audit trail logging, security event tracking, compliance reports, and CSV export.

---

### Issue 5.1 — PostgreSQL: `could not determine data type of parameter`

**Symptom:** `GET /audit/logs/search` with any filter parameter (e.g., `?action=TASK_CREATED`) throws a database error.

**Error:**
```
SQL Error: 0, SQLState: 42P18
ERROR: could not determine data type of parameter $1
```

**Root Cause:** When nullable parameters (`startDate`, `endDate`, `action`, etc.) are passed to JPQL using the `:param IS NULL` pattern, Hibernate 6 sends them as untyped `null` values. PostgreSQL is stricter than H2 or MySQL — it cannot infer the data type of `?` in `? IS NULL` and throws an error.

This is a known **PostgreSQL + Hibernate 6** incompatibility that fails silently on H2 but crashes on PostgreSQL.

**Resolution:** Two strategies based on parameter type:

**For String parameters** — use `COALESCE`:
```java
// Before
(:action IS NULL OR a.action = :action)

// After
(COALESCE(:action, '') = '' OR a.action = :action)
```

**For Timestamp parameters** — use `CAST`:
```java
// Before
(:startDate IS NULL OR a.createdAt >= :startDate)

// After
(CAST(:startDate AS timestamp) IS NULL OR a.createdAt >= :startDate)
```

**Files fixed:**

| File | Methods |
|------|---------|
| `AuditLogRepository.java` | `searchLogs()` — 4 string + 2 timestamp params |
| `AuditLogRepository.java` | `findByActionInAndDateRange()` — 2 timestamp params |
| `SecurityEventRepository.java` | `findByDateRange()` — 2 timestamp params |

> 💡 This same error was previously hit in Phase 2 (`EmployeeRepository`). The fix was the same COALESCE pattern.

---

### Issue 5.2 — Security Events Not Persisting After Failed Login

**Symptom:** `GET /audit/security-events` returns an empty list after triggering a `LOGIN_FAILED` event (wrong password). The security event is never saved.

**Root Cause:** The call chain on a failed login looked like this:

```
AuthService.login()                    ← @Transactional (opens transaction)
  ├─ Password mismatch detected
  ├─ handleFailedLogin(user)
  │     └─ auditService.logSecurityEvent("LOGIN_FAILED", ...)  ← INSERT
  └─ throw InvalidCredentialsException
                                        ← Spring rolls back entire transaction
                                        ← Security event INSERT is UNDONE
```

`logSecurityEvent()` used the default `@Transactional` (`Propagation.REQUIRED`), which joins the caller's existing transaction. When `AuthService.login()` threw an exception, Spring rolled back everything — including the security event insert.

**Resolution:** Change `logSecurityEvent()` to `Propagation.REQUIRES_NEW` so it runs in its own independent transaction that commits regardless of the caller's outcome:

```java
// Before
@Transactional
public void logSecurityEvent(...) { ... }

// After
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logSecurityEvent(...) { ... }
```

> ⚠️ **Key Takeaway:** Any audit or logging operation called from within a transactional method that may throw exceptions must use `Propagation.REQUIRES_NEW`. Otherwise, the audit record will be silently lost on rollback.

---

# Phase 6 — Analytics Module

> Focus: Performance tracking, data aggregation, and CSV report generation.

---

### Issue 6.1 — "Unknown" Employee Names in Analytics

**Symptom:** Analytics reports and CSV exports show `"Unknown"` instead of employee names for task-related metrics.

**Root Cause:** Native SQL queries were joining `tasks.assigned_to` (which stores the Employee Primary Key) with `employees.user_id` (which is the User UUID). These are different columns — the join produced no matches, causing the name lookup to fail for every record.

**Resolution:** Correct all native queries in `AnalyticsService` and `ReportService` to join on `employees.id` (Employee PK), and standardize name formatting:

```sql
-- Before (wrong join column)
JOIN employees e ON t.assigned_to = e.user_id

-- After (correct join column)
JOIN employees e ON t.assigned_to = e.id

-- Standardized name format
CONCAT(e.first_name, ' ', e.last_name) AS employee_name
```

---

# Global Backend Decisions & Lessons Learned

These are project-wide rules established as a result of issues that recurred or had cross-module impact.

---

### Flyway Versioning Convention

**Problem:** Early confusion between dot notation (`V1.1`) and integer notation (`V11`) caused lexical ordering issues in migration history.

**Rule Established:** Use simple integer increments only — `V10`, `V11`, `V12`, etc. No dot notation.

---

### Error Code Standardization

**Problem:** Error responses were inconsistent across phases — some returned plain strings, some returned partial objects, none had a module prefix.

**Rule Established:** All exceptions map to `ApiResponse<T>` with a structured `ErrorDetail` containing module-specific error codes:

```
AUTH_401, AUTH_403
TASK_404, TASK_409
EMP_404, EMP_409
```

---

### Cross-Module Interaction Pattern

**Problem:** Direct repository access across module boundaries was identified as a coupling risk — changes in one module's schema broke other modules.

**Rule Established:** Modules interact only via published `ApplicationEvent`s, never via direct service or repository injection across module boundaries.

---

### PostgreSQL Null Parameter Handling (Recurring)

**Problem:** The `:param IS NULL` JPQL pattern failed in Phase 2 (Employee) and again in Phase 5 (Audit). Each time it was rediscovered rather than applied proactively.

**Rule Established:** PostgreSQL is stricter than H2 or MySQL about untyped null parameters. For all optional filter parameters in JPQL:

| Parameter Type | Pattern to Use |
|---------------|---------------|
| `String` | `COALESCE(:param, '') = ''` |
| `Timestamp` / `LocalDateTime` | `CAST(:param AS timestamp) IS NULL` |

Never use `:param IS NULL` in any JPQL query targeting PostgreSQL.

---

### Native SQL Name Concatenation Style

**Problem:** Full name strings were built inconsistently across services — some used `+`, some used `||`, some used `CONCAT`.

**Rule Established:** Always use `CONCAT(e.first_name, ' ', e.last_name)` in all native SQL queries for full name formatting. This applies to both analytics queries and CSV export reports.

---

# Quick Reference: Most Common Issues

| Symptom | Phase | Root Cause | Resolution |
|---------|-------|-----------|------------|
| Flyway blocks startup — table exists | 1 | Manual schema conflicts with Flyway | `spring.flyway.baseline-on-migrate=true` |
| CORS errors on all frontend requests | 1 | No CORS config | `CorsConfig.java` with explicit origins + `allowCredentials(true)` |
| Android Emulator can't reach backend | 1 | `localhost` resolves to guest device | Use `10.0.2.2` as the API host |
| `StackOverflowError` on `GET /employees` | 2 | Self-referencing entity causes infinite JSON recursion | `@JsonIgnore` on manager field + flat DTO |
| Uploaded photos return 404 | 2 | No resource handler for disk path | `WebMvcConfigurer` mapping `/uploads/**` |
| Upload throws — directory not found | 2 | Storage path not created at startup | `Files.createDirectories()` in `@PostConstruct` |
| Scheduled job never runs | 3 | `@EnableScheduling` missing | Add to `TaskHiveApplication` |
| Files saved despite DB rollback | 3 | File I/O before transaction commit | `TransactionSynchronizationManager.afterCommit()` |
| Employees get 403 on own tasks | 3 | `@PreAuthorize` expression wrong | Fix ownership resolution in security expression |
| Duplicate bean on startup | 4 | Two classes named `EmailService` | Rename to `NotificationEmailService` |
| WebSocket handshake 401 | 4 | `/ws/**` blocked by JWT filter | Whitelist `/ws/**` in `SecurityConfig` |
| Admins miss task notifications | 4 | Fan-out to admins not implemented | Query all admin IDs + send with self-skip guard |
| `SQLState: 42P18` on search endpoints | 5 | Untyped null params in JPQL on PostgreSQL | `COALESCE` for strings, `CAST` for timestamps |
| Security events lost on failed login | 5 | Audit insert rolls back with parent transaction | `Propagation.REQUIRES_NEW` on `logSecurityEvent()` |
| Employee names show as "Unknown" | 6 | Join on `user_id` instead of `id` | Fix join column to `employees.id` in native queries |

---

> 🏆 **Golden Rule for PostgreSQL:** Never use `:param IS NULL` in JPQL with optional filters. Always use `COALESCE` for strings and `CAST(...) IS NULL` for timestamps. It works on H2, it breaks on PostgreSQL — every time.
