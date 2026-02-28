# Step 5 — NFR-USE-06: User-Friendly Error Messages

**NFR IDs:** NFR-USE-06
**Effort:** ~20 min | **Dependencies:** Step 3 (prod profile)
**Commit:** `feat(security): verify error message safety (NFR-USE-06)`

---

## SRS Requirement

> User-friendly error messages (no stack traces exposed to clients).

## Current State

`GlobalExceptionHandler.java` already handles 16 exception types and returns structured `ErrorResponse` objects. The catch-all handler logs the stack trace server-side but returns a generic message to the client. **This is mostly correct**, but needs verification that prod profile suppresses debug info.

## Implementation

### Verify: `GlobalExceptionHandler.java`

The existing catch-all handler is already safe:
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error: ", ex);  // Stack trace logged server-side only
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(MessageConstants.INTERNAL_ERROR, request.getRequestURI()));
}
```

### Verify: `ErrorResponse.java`

Ensure `ErrorResponse` does NOT contain any stack trace field. It should only have:
- `message` — user-friendly message
- `errors` — validation error list (optional)
- `path` — request URI
- `timestamp` — when the error occurred

### Verify: Prod Profile Guards (from Step 3)

These settings in `application-prod.properties` prevent info leakage:

| Setting | Value | Prevents |
|---------|-------|----------|
| `spring.jpa.show-sql` | `false` | SQL query leakage |
| `logging.level.com.digiwork.taskhive` | `INFO` | Debug info leakage |
| `server.error.include-stacktrace` | `never` | Stack traces in error responses |
| `server.error.include-message` | `never` | Raw exception messages |

### Modify: `application-prod.properties`

Add Spring Boot error configuration:

```properties
# ── Error Handling (NFR-USE-06) ──────────────
server.error.include-stacktrace=never
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-exception=false
```

## Verification

```bash
# Hit an endpoint that causes an error and verify no stack trace:
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bad","password":"bad"}'

# Expected: Clean error response, NO Java stack trace
# {"message":"Invalid credentials","path":"/api/v1/auth/login","timestamp":"..."}
```
