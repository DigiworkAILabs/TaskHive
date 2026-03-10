# Step 6 — NFR-SEC-12: Log Redaction

**NFR IDs:** NFR-SEC-12
**Effort:** ~30 min | **Dependencies:** Step 2 (logback-spring.xml), Step 3 (prod profile)
**Commit:** `feat(security): add log redaction for PII (NFR-SEC-12)`

---

## SRS Requirement

> No sensitive data (passwords, tokens, PII) in logs.

## Current State

No log redaction in place. While the code doesn't explicitly log passwords, there's no programmatic guarantee against accidental PII logging.

## Implementation

### New File: `LogRedactionConverter.java`

**Path:** `src/main/java/com/digiwork/taskhive/common/logging/LogRedactionConverter.java`

```java
package com.digiwork.taskhive.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Logback converter that redacts sensitive data from log messages.
 * Patterns: email addresses, JWT tokens, password fields, credit card numbers.
 */
public class LogRedactionConverter extends ClassicConverter {

    // Email pattern: user@domain.com → u***@domain.com
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("([a-zA-Z0-9._%+-])[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    // JWT token pattern: eyJ... → [REDACTED_TOKEN]
    private static final Pattern JWT_PATTERN =
        Pattern.compile("eyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");

    // Password field in JSON: "password":"..." → "password":"[REDACTED]"
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("(\"(?:password|secret|token|apiKey|apiSecret)\"\\s*:\\s*\")([^\"]+)(\")");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null) return "";

        message = EMAIL_PATTERN.matcher(message).replaceAll("$1***@$2");
        message = JWT_PATTERN.matcher(message).replaceAll("[REDACTED_TOKEN]");
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1[REDACTED]$3");

        return message;
    }
}
```

### Modify: `logback-spring.xml`

Add the converter declaration at the top and use `%redact` in patterns:

```xml
<configuration>
    <!-- Register the redaction converter -->
    <conversionRule conversionWord="redact"
                    converterClass="com.digiwork.taskhive.common.logging.LogRedactionConverter"/>

    <!-- Then use %redact instead of %msg in patterns -->
    <!-- Dev console pattern example: -->
    <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] [%X{correlationId:-N/A}] %logger{36} - %redact%n</pattern>
</configuration>
```

### Code Audit Checklist

Review these files for accidental PII logging:
- [ ] `AuthService.java` — ensure passwords are never logged
- [ ] `TokenService.java` — ensure raw tokens are never logged
- [ ] `JwtAuthenticationFilter.java` — ensure JWT values are not logged
- [ ] `EmailQueueService.java` — email addresses may appear in logs (acceptable for queuing)

## Verification

```bash
# Send a login request and check server logs
# The email should appear as "u***@domain.com"
# Any JWT token should appear as "[REDACTED_TOKEN]"
```
