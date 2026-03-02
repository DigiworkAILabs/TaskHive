package com.digiwork.taskhive.module.auth.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published after a user's data is anonymized via GDPR deletion (NFR-SEC-13).
 * Other modules (Notification, Audit) can listen to this event to clean up
 * references.
 */
public record UserDataDeletedEvent(UUID userId, LocalDateTime deletedAt) {
}
