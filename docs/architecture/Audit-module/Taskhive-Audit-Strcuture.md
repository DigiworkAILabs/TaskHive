# TaskHive — Phase 5: Audit Module
## SRS v2.3 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 5 of 6 |
| **Name** | Audit Module |
| **Modules** | Audit Module (Backend + Frontend) |
| **DB Migrations** | V5.0 + V5.1 |
| **Duration** | ~1 week |
| **Depends On** | Phase 1 (Auth) + Phase 2 (Employee) + Phase 3 (Task) + Phase 4 (Notification) |

### Deliverable
All domain events from Auth, Employee, Task, and Notification modules are persisted to `audit_logs`. Security events (failed logins, lockouts, unauthorized access attempts) are persisted separately to `security_events`. Admin can browse, filter, and search audit logs. Compliance reports can be generated. Audit logs are never physically deleted (7-year retention).

---

## API Endpoints (Audit Module)

```
GET    /api/v1/audit/logs                       # All logs (ADMIN, paginated)
GET    /api/v1/audit/logs/search                # Filter by date, action, entity, user, IP
GET    /api/v1/audit/logs/entity/{type}/{id}    # Entity-specific timeline
GET    /api/v1/audit/security-events            # Failed logins, lockouts
GET    /api/v1/audit/compliance/report          # Compliance report generation
```

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-AUDIT-01 | Critical | All significant actions shall be logged in audit_logs (actor, action, entity, before/after state, IP, userAgent) |
| FR-AUDIT-02 | Critical | Security events (failed logins, lockouts, unauthorized access) shall be logged in security_events (separate table) |
| FR-AUDIT-03 | High | ADMIN shall search/filter audit logs by date, action type, entity, user, IP |
| FR-AUDIT-04 | High | ADMIN shall view entity-specific timeline (all changes to a task or employee) |
| FR-AUDIT-05 | Medium | System shall generate compliance reports: User Access, Data Modification, Security Incident |
| FR-AUDIT-06 | Critical | Audit logs shall never be physically deleted (7-year retention) |

**Retention**: Audit logs retained 7 years (compliance requirement). Never physically deleted.

---

## Backend File & Folder Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/digiwork/taskhive/
│   │       └── module/
│   │           └── audit/                                 # AUDIT MODULE
│   │               ├── controller/
│   │               │   └── AuditController.java
│   │               │
│   │               ├── service/
│   │               │   ├── AuditService.java
│   │               │   └── ComplianceReportService.java
│   │               │
│   │               ├── dto/
│   │               │   ├── AuditLogResponse.java
│   │               │   ├── SecurityEventResponse.java
│   │               │   └── AuditSearchRequest.java
│   │               │
│   │               ├── model/
│   │               │   ├── AuditLog.java                  # id, actorId, actorEmail, action, entityType, entityId, beforeState, afterState, ipAddress, userAgent, createdAt
│   │               │   └── SecurityEvent.java             # id, eventType, userId, ipAddress, success, details, timestamp
│   │               │
│   │               ├── repository/
│   │               │   ├── AuditLogRepository.java
│   │               │   └── SecurityEventRepository.java
│   │               │
│   │               └── listener/
│   │                   └── AuditEventListener.java        # Listens to ALL domain events
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V5.0__create_audit_logs_table.sql
│               └── V5.1__create_security_events_table.sql
│
└── test/
    └── java/
        └── com/digiwork/taskhive/
            └── module/
                └── audit/
                    └── service/
                        └── AuditServiceTest.java
```

---

## Frontend File & Folder Structure

```
src/
└── app/
    └── (admin)/
        └── audit/page.tsx
```

> **Note**: The Audit module has no dedicated `src/features/audit/` folder.  
> `audit/page.tsx` fetches directly from `AuditController` via the shared `apiClient`.  
> Data is displayed in the shared `DataTable` component. React Query handles server-side pagination inline — no Zustand store required.

---

## DB Migrations — Phase 5

| File | Description |
|------|-------------|
| `V5.0__create_audit_logs_table.sql` | audit_logs — id, actor_id (FK users nullable), actor_email, action (e.g. EMPLOYEE_CREATED), entity_type (e.g. EMPLOYEE), entity_id (UUID), before_state (JSONB), after_state (JSONB), ip_address, user_agent, created_at. **No soft-delete — immutable.** |
| `V5.1__create_security_events_table.sql` | security_events — id, event_type (LOGIN_FAILED, ACCOUNT_LOCKED, UNAUTHORIZED_ACCESS), user_id (FK users nullable), ip_address, success (BOOLEAN), details (JSONB), timestamp. **No soft-delete — immutable.** |

---

## Events Consumed (from ALL Phases)

The `AuditEventListener` listens to every domain event published across all modules:

| Event | Source Module | Audit Action |
|-------|--------------|--------------|
| `UserAuthenticatedEvent` | Auth | `USER_LOGIN` |
| `UserLoggedOutEvent` | Auth | `USER_LOGOUT` |
| `PasswordChangedEvent` | Auth | `PASSWORD_CHANGED` |
| `PasswordResetRequestedEvent` | Auth | `PASSWORD_RESET_REQUESTED` |
| `AccountActivatedEvent` | Auth | `ACCOUNT_ACTIVATED` |
| `EmployeeCreatedEvent` | Employee | `EMPLOYEE_CREATED` |
| `EmployeeUpdatedEvent` | Employee | `EMPLOYEE_UPDATED` |
| `EmployeeActivatedEvent` | Employee | `EMPLOYEE_ACTIVATED` |
| `EmployeeDeactivatedEvent` | Employee | `EMPLOYEE_DEACTIVATED` |
| `EmployeeDeletedEvent` | Employee | `EMPLOYEE_DELETED` |
| `TaskCreatedEvent` | Task | `TASK_CREATED` |
| `TaskUpdatedEvent` | Task | `TASK_UPDATED` |
| `TaskStatusChangedEvent` | Task | `TASK_STATUS_CHANGED` |
| `TaskAssignedEvent` | Task | `TASK_ASSIGNED` |
| `TaskCommentAddedEvent` | Task | `TASK_COMMENT_ADDED` |

Security events (written directly by `AuthService`, not via Spring Events):

| Security Event Type | Written By | Table |
|--------------------|-----------|-------|
| `LOGIN_FAILED` | `AuthService` | `security_events` |
| `ACCOUNT_LOCKED` | `AuthService` | `security_events` |
| `UNAUTHORIZED_ACCESS` | `GlobalExceptionHandler` | `security_events` |

---

## Compliance Reports (ComplianceReportService)

| Report | Description |
|--------|-------------|
| User Access Report | All login events, role assignments, account activations for a time range |
| Data Modification Report | All entity create/update/delete events for a time range |
| Security Incident Report | All failed logins, lockouts, unauthorized access events |

---

## Phase 5 File Count Summary

| Layer | Backend | Frontend |
|-------|---------|----------|
| Controller | 1 | — |
| Service | 2 | — |
| DTO | 3 | — |
| Model | 2 | — |
| Repository | 2 | — |
| Listener | 1 | — |
| DB Migrations | 2 | — |
| Tests | 1 | — |
| App Pages | — | 1 |
