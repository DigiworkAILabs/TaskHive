# Step 1 — NFR-SEC-14: Security Headers

**NFR IDs:** NFR-SEC-14
**Effort:** ~15 min | **Dependencies:** None
**Commit:** `feat(security): add security headers (NFR-SEC-14)`

---

## SRS Requirement

> Content Security Policy, X-Frame-Options, HSTS headers enabled on all responses.

## Current State

No security headers configured. `SecurityConfig.java` uses a custom `SecurityFilterChain` without a `.headers()` block, so Spring Security's defaults are not applied.

## Implementation

### Modify: `SecurityConfig.java`

Add `.headers()` block inside the `securityFilterChain` method, between `.csrf()` and `.sessionManagement()`:

```java
.headers(headers -> headers
    // Prevents clickjacking — browser will refuse to render page inside iframe
    .frameOptions(frame -> frame.deny())

    // Content Security Policy — restricts allowed sources for scripts, styles, etc.
    .contentSecurityPolicy(csp -> csp
        .policyDirectives(
            "default-src 'self'; " +
            "script-src 'self'; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: blob:; " +
            "connect-src 'self' ws: wss:; " +
            "frame-ancestors 'none'"
        ))

    // HSTS — ensures browsers only connect via HTTPS for 1 year
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
)
```

### CSP Directives Explained

| Directive | Value | Why |
|-----------|-------|-----|
| `default-src` | `'self'` | Only load resources from same origin |
| `script-src` | `'self'` | No external scripts (XSS protection) |
| `style-src` | `'self' 'unsafe-inline' fonts.googleapis.com` | Allow Tailwind + Google Fonts |
| `font-src` | `'self' fonts.gstatic.com` | Google Fonts CDN |
| `img-src` | `'self' data: blob:` | Allow base64 and blob images (avatars) |
| `connect-src` | `'self' ws: wss:` | Allow WebSocket connections |
| `frame-ancestors` | `'none'` | Extra clickjacking protection |

## Verification

```bash
# After starting the backend, check response headers:
curl -I http://localhost:8081/api/v1/auth/login

# Should see:
# X-Frame-Options: DENY
# Content-Security-Policy: default-src 'self'; ...
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# X-Content-Type-Options: nosniff
```
