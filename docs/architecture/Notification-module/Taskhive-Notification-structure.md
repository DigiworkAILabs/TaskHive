# TaskHive — Phase 4: Notification Module
## SRS v2.3 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 4 of 6 |
| **Name** | Notification Module |
| **Modules** | Notification Module (Backend + Frontend) |
| **DB Migrations** | V4.0 + V4.1 + V4.2 |
| **Duration** | ~1.5 weeks |
| **Depends On** | Phase 1 (Auth) + Phase 2 (Employee) + Phase 3 (Task) |

### Deliverable
Real-time in-app notifications delivered via WebSocket (STOMP over SockJS). Email notifications sent asynchronously via email queue with 3 retry attempts. Users can view, mark read, and configure notification preferences. Daily digest email is optional per user preference.

---

## API Endpoints (Notification Module)

```
GET    /api/v1/notifications                    # List (paginated)
GET    /api/v1/notifications/unread             # Unread list
GET    /api/v1/notifications/unread-count       # Badge count
PATCH  /api/v1/notifications/{id}/read         # Mark single read
PATCH  /api/v1/notifications/mark-all-read     # Mark all read
GET    /api/v1/notifications/preferences        # Get preferences
PUT    /api/v1/notifications/preferences        # Update preferences
```

## WebSocket Topics

```
/topic/notifications/{userId}   # Personal notifications
/topic/tasks/{taskId}           # Task-specific real-time updates
/topic/system                   # System-wide announcements
```

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-NOTIF-01 | High | System shall send real-time in-app notifications via WebSocket (STOMP) |
| FR-NOTIF-02 | High | System shall send email notifications via SMTP Gmail |
| FR-NOTIF-03 | High | Email sending shall be asynchronous via email_queue table (non-blocking) |
| FR-NOTIF-04 | High | Email queue shall retry failed emails 3 times with exponential backoff |
| FR-NOTIF-05 | Medium | Users shall view paginated notification list |
| FR-NOTIF-06 | Medium | Users shall see unread notification count (badge) |
| FR-NOTIF-07 | Medium | Users shall mark individual or all notifications as read |
| FR-NOTIF-08 | Low | Users shall configure notification preferences (per category, email + in-app) |
| FR-NOTIF-09 | Low | System shall support optional daily digest email (configurable time) |

---

## Notification Triggers

| Event | In-App | Email |
|-------|--------|-------|
| Employee Created | ❌ | ✅ (activation link) |
| Task Assigned | ✅ | ✅ |
| Task Status Changed | ✅ | ❌ |
| Task Comment Added | ✅ | ❌ |
| Task Overdue | ✅ | ✅ |
| Password Reset Requested | ❌ | ✅ |
| Password Changed | ❌ | ✅ |
| Daily Digest | ❌ | ✅ (if enabled) |

**Email Queue**: Async processing with 3 retry attempts (exponential backoff). Failures logged in `email_queue` table with `status = FAILED` and `error_message`.

---

## Backend File & Folder Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/digiwork/taskhive/
│   │       └── module/
│   │           └── notification/                          # NOTIFICATION MODULE
│   │               ├── controller/
│   │               │   └── NotificationController.java
│   │               │
│   │               ├── service/
│   │               │   ├── NotificationService.java
│   │               │   ├── EmailService.java              # SMTP Gmail
│   │               │   ├── EmailQueueService.java         # Async email queue processor
│   │               │   ├── WebSocketNotificationService.java
│   │               │   └── NotificationPreferenceService.java
│   │               │
│   │               ├── dto/
│   │               │   ├── NotificationResponse.java
│   │               │   ├── NotificationListResponse.java
│   │               │   └── NotificationPreferenceRequest.java
│   │               │
│   │               ├── model/
│   │               │   ├── Notification.java
│   │               │   ├── NotificationPreference.java
│   │               │   └── EmailQueue.java                # id, toEmail, subject, templateName, templateData, status, attempts, errorMessage, scheduledAt, sentAt
│   │               │
│   │               ├── repository/
│   │               │   ├── NotificationRepository.java
│   │               │   ├── NotificationPreferenceRepository.java
│   │               │   └── EmailQueueRepository.java
│   │               │
│   │               ├── enums/
│   │               │   ├── NotificationType.java
│   │               │   └── EmailStatus.java               # PENDING, SENT, FAILED
│   │               │
│   │               ├── scheduler/
│   │               │   ├── EmailQueueScheduler.java       # Processes pending emails with retry
│   │               │   └── DailyDigestScheduler.java      # Sends daily digest if enabled
│   │               │
│   │               └── listener/
│   │                   ├── AuthEventListener.java
│   │                   ├── EmployeeEventListener.java
│   │                   └── TaskEventListener.java
│   │
│   └── resources/
│       ├── db/
│       │   └── migration/
│       │       ├── V4.0__create_notifications_table.sql
│       │       ├── V4.1__create_notification_preferences_table.sql
│       │       └── V4.2__create_email_queue_table.sql
│       │
│       └── templates/
│           └── email/
│               └── daily-digest.html
│
└── test/
    └── java/
        └── com/digiwork/taskhive/
            └── module/
                └── notification/
                    ├── service/
                    │   ├── NotificationServiceTest.java
                    │   └── EmailQueueServiceTest.java
```

---

## Frontend File & Folder Structure

```
src/
├── app/
│   └── (employee)/
│       └── settings/
│           └── security/page.tsx              # Change password + Notification preferences
│
└── features/
    └── notification/
        ├── components/
        │   ├── NotificationBell.tsx
        │   ├── NotificationList.tsx
        │   └── NotificationItem.tsx
        ├── hooks/
        │   ├── useNotifications.ts
        │   ├── useMarkAsRead.ts
        │   └── useNotificationPreferences.ts
        ├── services/
        │   ├── notificationService.ts
        │   └── webSocketService.ts            # SockJS + STOMP connection manager
        └── types/
            └── notification.types.ts
```

---

## DB Migrations — Phase 4

| File | Description |
|------|-------------|
| `V4.0__create_notifications_table.sql` | notifications — id, user_id (FK), type, title, message, is_read, read_at, entity_type, entity_id, created_at |
| `V4.1__create_notification_preferences_table.sql` | notification_preferences — id, user_id (FK unique), email_enabled, in_app_enabled, task_assigned, task_overdue, daily_digest, digest_time, created_at, updated_at |
| `V4.2__create_email_queue_table.sql` | email_queue — id, to_email, subject, template_name, template_data (JSONB), status, attempts, error_message, scheduled_at, sent_at, created_at |

---

## Events Consumed (from Phase 1, 2, 3)

| Listener | Event Consumed | Action |
|----------|---------------|--------|
| `AuthEventListener` | `PasswordResetRequestedEvent` | Queue password-reset email |
| `AuthEventListener` | `PasswordChangedEvent` | Queue password-changed email |
| `AuthEventListener` | `AccountActivatedEvent` | — (activation link already sent via EmployeeEventListener) |
| `EmployeeEventListener` | `EmployeeCreatedEvent` | Queue account-activation email |
| `TaskEventListener` | `TaskAssignedEvent` | Create in-app notification + queue task-assigned email |
| `TaskEventListener` | `TaskStatusChangedEvent` | Create in-app notification |
| `TaskEventListener` | `TaskCommentAddedEvent` | Create in-app notification |
| `TaskEventListener` | `TaskOverdueEvent` | Create in-app notification + queue task-overdue email |

---

## Email Templates Used in Phase 4

| Template | Trigger | Already Created In |
|----------|---------|-------------------|
| `account-activation.html` | EmployeeCreatedEvent | Phase 1 |
| `password-reset.html` | PasswordResetRequestedEvent | Phase 1 |
| `password-changed.html` | PasswordChangedEvent | Phase 1 |
| `task-assigned.html` | TaskAssignedEvent | Phase 3 |
| `task-overdue.html` | TaskOverdueEvent | Phase 3 |
| `daily-digest.html` | DailyDigestScheduler | **Phase 4** ← new |

---

## Phase 4 File Count Summary

| Layer | Backend | Frontend |
|-------|---------|----------|
| Controller | 1 | — |
| Service | 5 | — |
| DTO | 3 | — |
| Model | 3 | — |
| Repository | 3 | — |
| Enum | 2 | — |
| Scheduler | 2 | — |
| Listener | 3 | — |
| DB Migrations | 3 | — |
| Email Templates | 1 (new) | — |
| Tests | 2 | — |
| App Pages | — | 1 |
| Feature Components | — | 3 |
| Feature Hooks | — | 3 |
| Feature Services | — | 2 |
| Feature Types | — | 1 |
