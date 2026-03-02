package com.digiwork.taskhive.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Logback converter that redacts sensitive data from log messages (NFR-SEC-12).
 * Patterns: email addresses, JWT tokens, password/secret JSON fields.
 *
 * Register in logback-spring.xml with:
 * <conversionRule conversionWord="redact"
 * converterClass="com.digiwork.taskhive.common.logging.LogRedactionConverter"/>
 * Then use %redact instead of %msg in patterns.
 */
public class LogRedactionConverter extends ClassicConverter {

    // Email: user@domain.com → u***@domain.com
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("([a-zA-Z0-9._%+-])[a-zA-Z0-9._%+-]*@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");

    // JWT token: eyJ<header>.eyJ<payload>.<signature> → [REDACTED_TOKEN]
    private static final Pattern JWT_PATTERN = Pattern
            .compile("eyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");

    // JSON sensitive fields: "password":"value" → "password":"[REDACTED]"
    private static final Pattern SENSITIVE_FIELD_PATTERN = Pattern
            .compile("(\"(?:password|secret|token|apiKey|apiSecret)\"\\s*:\\s*\")([^\"]+)(\")");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null)
            return "";

        message = EMAIL_PATTERN.matcher(message).replaceAll("$1***@$2");
        message = JWT_PATTERN.matcher(message).replaceAll("[REDACTED_TOKEN]");
        message = SENSITIVE_FIELD_PATTERN.matcher(message).replaceAll("$1[REDACTED]$3");

        return message;
    }
}
