# Step 3 — NFR-SEC-03 + NFR-OPS-03: Production Profile

**NFR IDs:** NFR-SEC-03 (HTTPS/TLS), NFR-OPS-03 (No hardcoded creds), NFR-SEC-12 (No sensitive data in logs)
**Effort:** ~30 min | **Dependencies:** None
**Commit:** `feat(config): add production profile (NFR-SEC-03, NFR-OPS-03)`

---

## SRS Requirements

> **NFR-SEC-03:** HTTPS (TLS 1.3) enforced in production.
> **NFR-OPS-03:** No hardcoded credentials; all secrets via environment variables.
> **NFR-SEC-12:** No sensitive data (passwords, tokens, PII) in logs.

## Current State

- Only `application-dev.properties` exists with hardcoded credentials
- No production profile → no TLS, no env var substitution
- `spring.jpa.show-sql=true` in dev could leak sensitive data

## Implementation

### New File: `application-prod.properties`

**Path:** `src/main/resources/application-prod.properties`

```properties
# =============================================
# TaskHive Production Configuration
# =============================================

# ── Database (from environment variables) ────
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20

# ── JPA ──────────────────────────────────────
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.format_sql=false

# ── Flyway ───────────────────────────────────
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=false
spring.flyway.locations=classpath:db/migration

# ── JWT ──────────────────────────────────────
jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000

# ── Cookie (production-secure) ───────────────
app.cookie.secure=true
app.cookie.same-site=Strict
app.cookie.domain=${COOKIE_DOMAIN}

# ── Mail ─────────────────────────────────────
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from-email=${MAIL_FROM:noreply@taskhive.com}

# ── Server ───────────────────────────────────
server.port=${SERVER_PORT:8081}
app.frontend.url=${FRONTEND_URL}

# ── Admin ────────────────────────────────────
app.admin.email=${ADMIN_EMAIL}
app.admin.password=${ADMIN_PASSWORD}
app.admin.first-name=Admin
app.admin.last-name=User

# ── Logging (no debug in prod) ───────────────
logging.level.com.digiwork.taskhive=INFO
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN

# ── File Upload ──────────────────────────────
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=25MB
app.storage.type=${STORAGE_TYPE:local}
app.storage.local.upload-dir=${UPLOAD_DIR:./uploads}
```

### New File: `application-staging.properties`

**Path:** `src/main/resources/application-staging.properties`

```properties
# Staging mirrors production but with more logging
# All values come from environment variables (same as prod)

# Import prod config as base
spring.config.import=classpath:application-prod.properties

# Override logging for debugging
logging.level.com.digiwork.taskhive=DEBUG
logging.level.org.springframework.security=INFO
```

### Environment Variables Reference

| Variable | Example | Required |
|----------|---------|----------|
| `DB_URL` | `jdbc:postgresql://db:5432/taskhive` | ✅ |
| `DB_USERNAME` | `taskhive_user` | ✅ |
| `DB_PASSWORD` | `<strong-password>` | ✅ |
| `JWT_SECRET` | `<64-char-random-string>` | ✅ |
| `COOKIE_DOMAIN` | `taskhive.com` | ✅ |
| `MAIL_HOST` | `smtp.gmail.com` | ✅ |
| `MAIL_PORT` | `587` | ❌ (default: 587) |
| `MAIL_USERNAME` | `noreply@taskhive.com` | ✅ |
| `MAIL_PASSWORD` | `<app-password>` | ✅ |
| `FRONTEND_URL` | `https://app.taskhive.com` | ✅ |
| `ADMIN_EMAIL` | `admin@taskhive.com` | ✅ |
| `ADMIN_PASSWORD` | `<strong-password>` | ✅ |
| `SERVER_PORT` | `8081` | ❌ (default: 8081) |
| `STORAGE_TYPE` | `local` or `s3` | ❌ (default: local) |

## Verification

```bash
# Test prod profile locally with env vars:
DB_URL=jdbc:postgresql://localhost:5432/taskhive \
DB_USERNAME=postgres \
DB_PASSWORD=password \
JWT_SECRET=TestSecretKeyForLocalProdProfileVerification2026 \
COOKIE_DOMAIN=localhost \
MAIL_HOST=smtp.gmail.com \
MAIL_USERNAME=test@gmail.com \
MAIL_PASSWORD=test \
FRONTEND_URL=http://localhost:3000 \
ADMIN_EMAIL=admin@test.com \
ADMIN_PASSWORD=Admin@123 \
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Verify no SQL in logs
# Verify cookie has Secure=true, SameSite=Strict
```
