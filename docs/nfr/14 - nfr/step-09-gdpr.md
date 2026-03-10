# Step 9 — NFR-SEC-13: GDPR Compliance

**NFR IDs:** NFR-SEC-13
**Effort:** ~2 hrs | **Dependencies:** None
**Commit:** `feat(auth): add GDPR data export/deletion (NFR-SEC-13)`

---

## SRS Requirement

> GDPR compliance: user data export and deletion capability.

## Current State

No GDPR endpoints exist. No data export or right-to-be-forgotten functionality.

## Implementation

### New File: `GdprController.java`

**Path:** `src/main/java/com/digiwork/taskhive/module/auth/controller/GdprController.java`

Endpoints:

```
GET  /api/v1/users/{userId}/data-export    → Export all user data as JSON
POST /api/v1/users/{userId}/gdpr-delete    → Anonymize user data (right to be forgotten)
```

Both endpoints should be restricted to:
- The user themselves (own data)
- ADMIN role (for any user)

### New File: `GdprService.java`

**Path:** `src/main/java/com/digiwork/taskhive/module/auth/service/GdprService.java`

**Data Export** — collects data from:

| Table | Data Exported |
|-------|---------------|
| `users` | Email, name, role, created_at |
| `employees` | Department, position, phone, join_date |
| `tasks` | Tasks created by or assigned to user |
| `task_comments` | Comments by user |
| `task_attachments` | Attachments uploaded by user |
| `notifications` | Notifications for user |
| `audit_logs` | Actions performed by user |

**Data Deletion (Anonymization)** — replaces PII:

| Field | Before | After |
|-------|--------|-------|
| `email` | `john@example.com` | `deleted_<uuid>@anonymized.local` |
| `first_name` | `John` | `[DELETED]` |
| `last_name` | `Doe` | `[DELETED]` |
| `phone` | `+1234567890` | `null` |
| `address` | `123 Main St` | `null` |

### New File: `GdprExportResponse.java`

**Path:** `src/main/java/com/digiwork/taskhive/module/auth/dto/GdprExportResponse.java`

```java
public record GdprExportResponse(
    UserData user,
    EmployeeData employee,
    List<TaskData> tasks,
    List<CommentData> comments,
    int notificationCount,
    int auditLogCount,
    LocalDateTime exportedAt
) {}
```

### Event: `UserDataDeletedEvent`

Publish after anonymization so Notification and Audit modules can clean up:

```java
public record UserDataDeletedEvent(UUID userId, LocalDateTime deletedAt) {}
```

## Verification

```bash
# Export user data (as admin)
curl -X GET http://localhost:8081/api/v1/users/{userId}/data-export \
  -H "Cookie: accessToken=<jwt>"

# GDPR delete (as admin)
curl -X POST http://localhost:8081/api/v1/users/{userId}/gdpr-delete \
  -H "Cookie: accessToken=<jwt>"

# Verify user is anonymized in DB
SELECT email, first_name, last_name FROM users WHERE id = '<userId>';
# Expected: deleted_xxx@anonymized.local, [DELETED], [DELETED]
```
