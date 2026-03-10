package com.digiwork.taskhive.module.ml.exception;

/**
 * MLServiceUnavailableException
 * ──────────────────────────────
 * Thrown internally when the ML server is unreachable or returns an error.
 * This exception is caught by the Resilience4j circuit breaker fallback
 * and NEVER propagates to the frontend — caller always gets a graceful MEDIUM
 * default.
 */
public class MLServiceUnavailableException extends RuntimeException {

    public MLServiceUnavailableException(String message) {
        super(message);
    }

    public MLServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
