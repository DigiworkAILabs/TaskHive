# TaskHive — Master Context Document
## v2.4 → v2.5 → v3.x Evolution Guide
### Company: Digiwork | Base Package: `com.digiwork.taskhive` | First Tenant: NSG Academy

---

## 🗺️ Version Roadmap

| Phase | Version | Focus | Output |
|---|---|---|---|
| Phase 1 | **v2.5** | Foundation Hardening — 5 fixes | NSG Academy goes live |
| Phase 2 | **v3.0** | Multi-Tenant Foundation | Any business can be onboarded |
| Phase 3 | **v3.1** | Custom Task Engine + Recurring Tasks | Fully configurable task types |
| Phase 4 | **v3.2** | Handover + Performance + Broadcast | Employee accountability system |
| Phase 5 | **v3.3** | Social Media Plugin | NSG social task management live |
| Phase 6 | **v3.4** | Checklist/Ops Plugin + Holiday/Leave | Physical operations tracked |
| Phase 7 | **v3.5** | Telecalling Plugin | Admissions + fees calling managed |
| Phase 8 | **v3.6** | ML/AI Intelligence | Intelligent, proactive platform |
| Phase 9 | **v3.7** | Flutter Mobile | Mobile app updated for all v3.x features |

> **Versioning logic:** v2.5 = first release (fixes on v2.4 base). v3.0 = major architectural evolution (multi-tenant). v3.1–v3.7 = feature additions on the v3.0 foundation.

---

## 🎯 Purpose of This Document

This is the **single source of truth** for working on TaskHive v2.4 → v3.7 evolution.
Read this entire document before writing a single line of code.

It contains:
1. What is already built in v2.4 (do NOT rebuild this)
2. Every real bug that was hit in v2.4 (do NOT repeat these)
3. ML features already built (what to preserve and extend)
4. Exact phase-wise tasks for all 9 phases
5. Critical rules and patterns established during v2.4 development

---

## 📦 Section 1 — What Is Already Built in v2.4

### Platform Scope

TaskHive v2.4 is a **full multi-platform product**, not just a web app.

| Layer | Technology | Status |
|---|---|---|
| Backend API | Spring Boot 3.5.10 + Java 21 | ✅ Complete (Phases 1–6) |
| Web Frontend | Next.js 16 + React 19 + TypeScript | ✅ Complete (Phases 1–6) |
| Mobile | Flutter 3.24+ / Dart 3.4+ | ✅ Complete (Phases 1–6) — Primary mobile app |
| Mobile Alt | React Native 0.84.1 | ❌ Abandoned — not completed, not maintained |
| Android Wrapper | WebView (Native Android/Java) | ✅ Complete |
| ML Service | Python 3.14 + FastAPI | ✅ Complete (v2.4 Phase 7 — 4 features) |
| Database | PostgreSQL 16 + Flyway | ✅ Complete (V1–V7) |

> ⚠️ **React Native is abandoned.** Flutter is the sole mobile platform going forward. Do not reference or integrate React Native in any v3.x work. All mobile features (scorecard, leaderboard, plugins) are Flutter-only — covered in Phase 9 (v3.7).

### Backend Modules — ALL Complete, Do NOT Rebuild

| Module | Package | What it does |
|---|---|---|
| Auth | `module/auth/` | JWT HttpOnly Cookie, login/logout/refresh, activate account, forgot/reset password, RBAC, account lock |
| Employee | `module/employee/` | Employee CRUD, manager hierarchy, profile photo, search, activate/deactivate |
| Task | `module/task/` | Task CRUD, comments, attachments, status history, overdue scheduler, full-text search |
| Notification | `module/notification/` | WebSocket (STOMP/SockJS), email queue, in-app notifications, daily digest |
| Analytics | `module/analytics/` | Admin dashboard, employee dashboard, task distribution, completion trends, performance table, CSV export |
| Audit | `module/audit/` | Audit logs, security events, compliance report, CSV export |
| ML | `module/ml/` | 4 ML features — priority suggestion, completion time, workload balance, productivity score |

### Database Migrations — Already Applied (V1–V7)

```
V1.0  users
V1.1  roles
V1.2  user_roles
V1.3  refresh_tokens
V1.4  password_reset_tokens
V1.5  account_activation_tokens
V1.6  password_history
V2.0  employees
V2.1  employee_status_history
V3.0  tasks
V3.1  task_comments
V3.2  task_attachments
V3.3  task_status_history
V4.0  notifications
V4.1  notification_preferences
V4.2  email_queue
V5.0  audit_logs
V5.1  security_events
V6.0  analytics tables (daily_metrics, employee_performance_cache)
V6.1  file_metadata
V7.0  ml_prediction_logs
V7.1  indexes
```

**⚠️ Flyway versioning rule:** Use simple integers only — V8, V9, V10. NEVER dot notation (V8.1, V8.2). Dot notation causes lexical ordering bugs.

### ML Features Already Built (v2.4 — Phase 7)

All 4 ML features are complete in v2.4:

| Feature | Algorithm | FastAPI Endpoint | Spring Boot Endpoint |
|---|---|---|---|
| Task Priority Suggestion | Random Forest Classifier | `POST /predict/priority` | `POST /api/v1/ml/predict/task-priority` |
| Completion Time Estimation | Gradient Boosting Regressor | `POST /predict/completion-time` | `POST /api/v1/ml/predict/completion-time` |
| Workload Balance | Scoring Model | `POST /recommend/workload-balance` | `POST /api/v1/ml/recommend/workload-balance` |
| Productivity Score | Weighted Ensemble | `GET /productivity-score/{employeeId}` | `GET /api/v1/ml/score/employee/{id}` |

**Key ML architecture facts:**
- Frontend NEVER talks to FastAPI directly — always via Spring Boot
- FastAPI ML server runs on port 8000 (internal Docker network only — never exposed)
- Spring Boot is a thin HTTP client — no ML logic in Java
- All 4 features use Resilience4j Circuit Breaker — app NEVER fails if ML is down
- Toggle: `ml.enabled=false` in properties (flip to `true` to enable)
- Fallback responses exist for all 4 features — sane defaults when ML is down
- Models trained on synthetic data (5000 records) until real data accumulates (6+ months)
- Models stored in `inference-server/artifacts/` as `.pkl` files — NOT inside `models/` folder

### Actual ML Folder Structure (Implemented — v2.4)

> ⚠️ SRS v2.4 had a placeholder ML structure. The **actual implemented structure** from the ML docs is below. Use this as the source of truth.

```
ml/
├── pipeline/
│   └── run_pipeline.py                     # Master orchestration — runs data gen + training end-to-end
│
├── training/
│   ├── .gitignore                           # Excludes *.csv (generated data not committed)
│   ├── requirements.txt                     # Training-only dependencies
│   ├── data/
│   │   ├── priority_data.csv               # Synthetic data — Feature 1
│   │   ├── completion_data.csv             # Synthetic data — Feature 2
│   │   ├── workload_data.csv               # Synthetic data — Feature 3
│   │   └── productivity_data.csv           # Synthetic data — Feature 4
│   ├── training_generator/
│   │   ├── generate_priority_data.py       # Generates priority_data.csv (5000 records)
│   │   ├── generate_completion_data.py     # Generates completion_data.csv (5000 records)
│   │   ├── generate_workload_data.py       # Generates workload_data.csv (2000 records)
│   │   └── generate_productivity_data.py   # Generates productivity_data.csv (3000 records)
│   └── scripts/
│       ├── train_priority.py               # Trains Random Forest → saves priority_model.pkl
│       ├── train_completion.py             # Trains Gradient Boosting → saves completion_model.pkl
│       ├── train_workload.py               # Trains scoring model → saves workload_model.pkl
│       └── train_productivity.py           # Trains weighted ensemble → saves productivity_model.pkl
│
└── inference-server/
    ├── main.py                             # FastAPI entry point (port 8000)
    ├── check_health.py                     # CLI health check utility — run before connecting Spring Boot
    ├── requirements.txt                    # Production-only dependencies
    ├── .gitignore                          # Excludes *.pkl (models not committed to git)
    ├── Dockerfile
    ├── artifacts/                          # ← PRODUCTION MODELS STORED HERE (not in models/)
    │   ├── priority_model.pkl
    │   ├── completion_model.pkl
    │   ├── workload_model.pkl
    │   └── productivity_model.pkl
    ├── api/
    │   ├── routes/
    │   │   ├── health.py
    │   │   ├── predict_priority.py
    │   │   ├── predict_completion.py
    │   │   ├── recommend_workload.py
    │   │   └── productivity_score.py
    │   └── schemas/
    │       ├── request.py                  # Pydantic request models for all 4 features
    │       └── response.py                 # Pydantic response models for all 4 features
    ├── models/
    │   └── loader.py                       # Loads .pkl files from ../artifacts/
    ├── preprocessing/
    │   └── transformers.py                 # Shared feature transformers (TF-IDF, encoders)
    └── config/
        └── settings.py                     # ML server config (port, artifact paths, timeouts)
```

**Key folder rules:**
- `training/` = offline work (data generation + model training) — never runs in production
- `inference-server/` = production server — only loads pre-trained `.pkl` files
- `artifacts/` = where trained models are stored — `.gitignore`'d, generated by `run_pipeline.py`
- `pipeline/run_pipeline.py` = one command to regenerate all data + retrain all models

**Model training workflow:**
```bash
cd ml/pipeline
python run_pipeline.py
# This: generates synthetic data → trains all 4 models → saves .pkl to artifacts/
```

**Testing FastAPI before connecting to Spring Boot:**
```bash
cd ml/inference-server
python main.py                    # Start server on port 8000
python check_health.py            # Verify all 4 models loaded
# Expected: ✅ ML Server is healthy! Models Loaded: ['priority', 'completion', 'workload', 'productivity']
```

---

## 🐛 Section 2 — Known Bugs from v2.4 (DO NOT REPEAT)

### Backend Bugs

#### B1 — JDK Compatibility (Phase 1)
**Never mismatch Maven and JDK versions.**
```xml
<properties>
    <java.version>21</java.version>
</properties>
```

#### B2 — Flyway Baseline on Migrate (Phase 1)
If schema already exists manually before Flyway takes over:
```properties
spring.flyway.baseline-on-migrate=true
```

#### B3 — JWT Secret Too Short (Phase 1)
JJWT requires minimum 256-bit key for HS256. Always use 32+ character secret.

#### B4 — CORS + allowCredentials (Phase 1)
`allowCredentials(true)` requires explicit origins — NEVER use `*`:
```java
config.setAllowedOrigins(List.of("http://localhost:3000"));
config.setAllowCredentials(true);
// Never: config.setAllowedOriginPatterns(List.of("*")) with credentials
```

#### B5 — Android Emulator API URL (Phase 1)
`localhost` inside Android Emulator = guest device, not host machine:
```
API_BASE_URL=http://10.0.2.2:8080/api/v1  // Emulator
API_BASE_URL=http://localhost:8080/api/v1   // Physical device + adb reverse
```

#### B6 — Employee Self-Referencing JSON Infinite Recursion (Phase 2)
`Employee.manager` → `manager.manager` → StackOverflowError:
```java
@JsonIgnore
@ManyToOne
private Employee manager;

// In DTO — flat string only
private String managerName;
```

#### B7 — Profile Photo 404 (Phase 2)
Spring Boot does not auto-serve files from disk paths. Add resource handler:
```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:/var/taskhive/uploads/");
```

#### B8 — Upload Directory Not Created (Phase 2)
```java
@PostConstruct
public void init() throws IOException {
    Files.createDirectories(Paths.get(uploadPath));
}
```

#### B9 — @EnableScheduling Missing (Phase 3)
Scheduled jobs NEVER run without this:
```java
@SpringBootApplication
@EnableScheduling
public class TaskHiveApplication { ... }
```

#### B10 — File Saved Despite DB Rollback (Phase 3)
File I/O must happen AFTER successful DB commit:
```java
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            storageService.save(file);
        }
    }
);
```

#### B11 — Duplicate Bean Name EmailService (Phase 4)
`auth` and `notification` packages both had `EmailService`. Spring couldn't resolve.
**Rule:** Notification module's email service = `NotificationEmailService`.

#### B12 — JSONB Casting on email_queue (Phase 4)
```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private String templateData;
```

#### B13 — Double Email on Password Events (Phase 4)
Both `auth` and `notification` module listeners fired on same event.
**Rule:** `auth` module owns password event emails. Notification module must NOT duplicate.

#### B14 — WebSocket Handshake 401 (Phase 4)
WebSocket upgrade cannot pass Bearer token in header:
```java
.requestMatchers("/ws/**").permitAll()
// WebSocket auth handled via STOMP headers after connection
```

#### B15 — Admins Not Receiving Task Notifications (Phase 4)
Fan-out to admins was missing. Always notify both assignee AND all admins:
```java
List<UUID> adminIds = userRoleRepository.findUserIdsByRoleName("ADMIN");
for (UUID adminId : adminIds) {
    if (adminId.equals(assignedEmployeeUserId)) continue; // skip if already notified
    notificationService.send(adminId, notification);
}
```

#### B16 — PostgreSQL Untyped Null Params — SQLState 42P18 (Phase 5, recurred from Phase 2)
**GOLDEN RULE: NEVER use `:param IS NULL` in JPQL with PostgreSQL.**
This works on H2, BREAKS on PostgreSQL every time.

```java
// ❌ WRONG
(:action IS NULL OR a.action = :action)
(:startDate IS NULL OR a.createdAt >= :startDate)

// ✅ CORRECT — String params
(COALESCE(:action, '') = '' OR a.action = :action)

// ✅ CORRECT — Timestamp params
(CAST(:startDate AS timestamp) IS NULL OR a.createdAt >= :startDate)
```
Apply to ALL optional filter params in repositories: AuditLogRepository, SecurityEventRepository, EmployeeRepository, and any new v3.0 repositories with optional filters.

#### B17 — Security Events Lost on Failed Login (Phase 5)
Audit inserts inside `@Transactional` methods that throw exceptions get rolled back:
```java
// ❌ WRONG — rolls back with parent transaction
@Transactional
public void logSecurityEvent(...) { ... }

// ✅ CORRECT — own independent transaction
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logSecurityEvent(...) { ... }
```
**Rule:** Any audit/logging operation called from within a transactional method that may throw = MUST use `Propagation.REQUIRES_NEW`.

#### B18 — Employee Names Show as "Unknown" in Analytics (Phase 6)
Native SQL joined on wrong column:
```sql
-- ❌ WRONG
JOIN employees e ON t.assigned_to = e.user_id

-- ✅ CORRECT
JOIN employees e ON t.assigned_to = e.id

-- Standard name format across all native queries
CONCAT(e.first_name, ' ', e.last_name) AS employee_name
```

### Frontend (Next.js) Bugs

#### F1 — AI Dropdown Locked to Suggestion
Priority dropdown snapped back to ML suggestion after manual change.
Root cause: `useEffect` had `formData.priority` in dependency array causing re-run loop.
Fix: Remove `formData.priority` from useEffect deps. Clear "accepted" flag on manual change.

#### F2 — Stale ML Suggestion State After Form Reset
ML prediction state not cleared after task submission.
Fix: Call `resetML()` in form success handler.

#### F3 — ML Sections Visible When ML Toggled OFF
Fix: Wrap ALL ML components in `{isMlEnabled && <Component />}` checks.

#### F4 — ML Toggle Visible to Employees
ML Global Toggle must be in Admin layout ONLY, never Employee layout.

### Frontend (Flutter) Bugs

#### FL1 — Physical Device Connection Refused
Use `adb reverse tcp:8080 tcp:8080` + `localhost` in `.env.dev` (not `10.0.2.2`).

#### FL2 — Cookie Dropped on Physical Device
Backend `app.cookie.domain=localhost` rejected when accessed via IP.
Fix: Use `adb reverse` so phone accesses via `localhost`.

#### FL3 — Flutter Web CookieManager Assertion Error
Use conditional imports — browser handles cookies natively, skip `CookieManager` on web:
```dart
if (!kIsWeb) {
    dio.interceptors.add(CookieManager(cookieJar));
}
```
**NEVER import `dart:io` at top level of files that compile for web.**

#### FL4 — GoRouter Not Redirecting After Login
GoRouter needs `refreshListenable` bridge from Riverpod state:
```dart
final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    refreshListenable: ref.watch(routerRefreshProvider),
    ...
  );
});
```

#### FL5 — Web Session Lost on Tab Reload
`CookieUtil.java` was not passing domain into `ResponseCookie` builder.
Fix: Explicitly set `.domain(domain)` in `ResponseCookie`.

---

## 📐 Section 3 — Established Code Standards

### Error Codes

All exceptions map to `ApiResponse<T>` with module-prefixed error codes. Full set from v3.0 SRS:

| Module | Range | Examples |
|---|---|---|
| Auth | `AUTH_1xxx` | AUTH_1001: Invalid credentials, AUTH_1002: Account locked, AUTH_1003: Token expired |
| Employee | `EMP_2xxx` | EMP_2001: Not found, EMP_2002: Email exists, EMP_2003: Cannot delete with active tasks |
| Task | `TASK_3xxx` | TASK_3001: Not found, TASK_3002: Invalid status transition, TASK_3003: Proof required, TASK_3004: Skip reason required, TASK_3005: Pending approval exists |
| Notification | `NOTIF_4xxx` | NOTIF_4001: Not found |
| Analytics | `ANA_5xxx` | ANA_5001: Invalid date range, ANA_5002: Report not ready |
| Audit | `AUDIT_6xxx` | AUDIT_6001: Unauthorized |
| Tenant | `TENANT_7xxx` | TENANT_7001: Plugin not enabled, TENANT_7002: Task type not found, TENANT_7003: Custom field validation failed |
| Social Media | `SM_8xxx` | SM_8001: Platform not found, SM_8002: Account not found, SM_8003: Proof attachment missing |
| Telecalling | `TC_9xxx` | TC_9001: Campaign not found, TC_9002: Excel parse error, TC_9003: Lead not found |
| Checklist | `CL_10xxx` | CL_10001: Template not found, CL_10002: Critical item not checked |
| Common | `COMMON_9xxx` | COMMON_9001: Validation failed, COMMON_9002: Not found, COMMON_9003: Access denied, COMMON_9004: Internal error |

### Cross-Module Interaction
Modules communicate ONLY via `ApplicationEvent`s.
NEVER inject a service or repository from another module directly.

### Native SQL Name Format
Always: `CONCAT(e.first_name, ' ', e.last_name) AS employee_name`

### Flyway Versioning
Use integers: V8, V9, V10, V11... NEVER V8.1, V8.2.

---

## 🚀 Section 4 — Phase-wise Implementation Plan

Complete 9-phase roadmap from v2.4 → v3.7. Each phase builds on the previous. Do not skip phases.

---

### Phase 1 — Foundation Hardening · Output: **v2.5** (Week 1)

#### Task P1.1 — Proof Enforcement

**Backend changes:**

New file: `module/task/exception/ProofEnforcementException.java`

Modified: `TaskService.java` — in `updateTaskStatus()`:
```java
if (newStatus == TaskStatus.IN_REVIEW && taskType.isProofRequired()) {
    boolean hasProof = attachmentRepository
        .existsByTaskIdAndPurpose(task.getId(), AttachmentPurpose.PROOF);
    if (!hasProof) {
        throw new ProofEnforcementException("TK-010",
            "Proof attachment required before submitting this task type");
    }
}
```

New endpoint: None (validation inside existing status update endpoint)

**Frontend changes:**

New file: `features/task/components/ProofUploadSection.tsx`
- Show when `task.taskType.proofRequired === true`
- Disable submit button if no proof attachment uploaded
- Show "Upload proof to submit" indicator

Modified: `TaskDetail.tsx` — render `<ProofUploadSection />` conditionally

---

#### Task P1.2 — Approval Workflow

**New status flow:**
```
TODO → IN_PROGRESS → IN_REVIEW → PENDING_APPROVAL → DONE
                                       ↓
                                  REJECTED → back to IN_REVIEW
```

**Backend changes:**

Migration: `V8__alter_task_status_add_pending_approval.sql`
```sql
ALTER TYPE task_status ADD VALUE IF NOT EXISTS 'PENDING_APPROVAL';
```

New file: `module/task/service/TaskApprovalService.java`
New file: `module/task/dto/TaskApprovalRequest.java` (with `reason` field for rejection)

Modified: `TaskStatus.java` enum — add `PENDING_APPROVAL`
Modified: `TaskService.java` — when status moves to IN_REVIEW and `taskType.approvalRequired = true`, auto-move to PENDING_APPROVAL
Modified: `TaskController.java` — add 2 new endpoints

New endpoints:
```
PATCH /api/v1/tasks/{id}/approve   — ADMIN only
PATCH /api/v1/tasks/{id}/reject    — ADMIN only, reason required
```

**Frontend changes:**

New file: `features/task/components/TaskApprovalPanel.tsx`
New hooks: `useApproveTask.ts`, `useRejectTask.ts`
Modified: `TaskDetail.tsx` — show approval panel for ADMIN when task is PENDING_APPROVAL
Modified: `TaskStatusBadge.tsx` — add PENDING_APPROVAL badge (amber color)

---

#### Task P1.3 — Mandatory Cancel Reason

**Backend changes:**

Modified: `TaskService.java`:
```java
if (newStatus == TaskStatus.CANCELLED) {
    if (request.getReason() == null || request.getReason().isBlank()) {
        throw new BusinessException("TK-011", "Reason is mandatory when cancelling a task");
    }
}
```

**Frontend changes:**

New file: `features/task/components/CancelReasonModal.tsx`
Modified: `TaskDetail.tsx` — on cancel click, show reason modal before API call

---

#### Task P1.4 — Late Submission Flag

**Migration:** `V9__alter_tasks_add_late_and_analytics_columns.sql`
```sql
-- tasks table
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS is_late         BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS late_by_minutes INTEGER;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS submitted_at    TIMESTAMP;

-- task_attachments: add purpose field for proof enforcement
ALTER TABLE task_attachments ADD COLUMN IF NOT EXISTS attachment_purpose VARCHAR(50) DEFAULT 'GENERAL';

-- daily_metrics: add late_tasks column (NEW in v3.0)
ALTER TABLE daily_metrics ADD COLUMN IF NOT EXISTS late_tasks INTEGER DEFAULT 0;

-- employee_performance_cache: add tasks_late column (NEW in v3.0)
ALTER TABLE employee_performance_cache ADD COLUMN IF NOT EXISTS tasks_late INTEGER DEFAULT 0;
```

**Backend changes:**

New enum: `module/task/enums/AttachmentPurpose.java` — `PROOF`, `GENERAL`

Modified: `Task.java` — add `isLate`, `lateByMinutes`, `submittedAt` fields
Modified: `TaskAttachment.java` — add `attachmentPurpose` field (use `AttachmentPurpose` enum)

Modified: `TaskService.java` — in `updateTaskStatus()`:
```java
if (newStatus == TaskStatus.IN_REVIEW) {
    // Set submitted_at timestamp
    task.setSubmittedAt(LocalDateTime.now());

    // Late flag check
    if (task.getDueDate() != null && !task.isLate()) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(task.getDueDate())) {
            task.setIsLate(true);
            long minutesLate = ChronoUnit.MINUTES.between(task.getDueDate(), now);
            task.setLateByMinutes((int) minutesLate);
        }
    }
}
// is_late is IMMUTABLE once set — never clear it
```

New scheduler: `module/task/scheduler/LateSubmissionScheduler.java`
- Runs hourly via `@Scheduled(cron = "0 0 * * * *")`
- Catches any tasks that are IN_PROGRESS/IN_REVIEW but past due_date and is_late = false
- Safety net in case realtime flag missed somehow
- Sets `is_late = true` + `late_by_minutes` for those tasks

Modified: `TaskResponse.java` — add `isLate`, `lateByMinutes`, `submittedAt` fields

New endpoint:
```
GET /api/v1/tasks/late   — ADMIN only, list of all late tasks
```

**Frontend changes:**

Modified: `TaskCard.tsx` — red "LATE" badge when `isLate === true`
Modified: `TaskDetail.tsx` — show "Late by X hours Y minutes" banner

---

#### Task P1.5 — Today's Overview + Rule-Based Anomaly Detection

**Backend new files:**

`module/analytics/service/TodayOverviewService.java`
`module/analytics/service/AnomalyDetectionService.java`
`module/analytics/dto/TodayOverviewResponse.java`

`TodayOverviewResponse` fields:
- `totalAssignedToday` (int)
- `completedToday` (int)
- `inProgress` (int)
- `pendingApproval` (int)
- `overdueToday` (int)
- `lateSubmissionsToday` (int)
- `activeAnomalies` (int) — count of active anomaly alerts

**Rule-based anomaly detection** (Phase 1 — no ML needed):
```
Rule 1: total completions today < 60% of 7-day average → fire alert
Rule 2: recurring task type has 0 completions for 2+ consecutive days → fire alert
Rule 3: employee has 3+ consecutive days of missed tasks → fire alert
```
`AnomalyDetectionService` runs nightly via `MetricsAggregatorService` scheduler.

New endpoints:
```
GET /api/v1/analytics/dashboard/today   — ADMIN only — today's overview
GET /api/v1/analytics/anomalies         — ADMIN only — active anomaly alerts
GET /api/v1/analytics/tasks/missed      — ADMIN only — missed tasks with reasons
GET /api/v1/analytics/tasks/late        — ADMIN only — late submission patterns
```

**Frontend changes:**

New file: `features/analytics/components/TodayOverviewPanel.tsx`
- 6 stat cards + anomaly alert banner if activeAnomalies > 0
- Auto-refresh every 5 minutes via React Query
- Green = completed, Amber = in progress/pending, Red = overdue/late

Modified: `AdminDashboard.tsx` — add `<TodayOverviewPanel />` at top of page

---

**✅ Phase 1 Complete = TaskHive v2.5 — NSG Academy can go live**

---

### Phase 2 — Multi-Tenant Foundation · Output: **v3.0** (Weeks 2-3)

Goal: Core architectural evolution — multi-tenant support. Any business can now be onboarded as a tenant.
Output = **TaskHive v3.0** — platform foundation complete.

**⚠️ CRITICAL: This is the most dangerous migration in the entire roadmap.**
`tenant_id` is being added to ALL existing tables with backfill of live NSG Academy data.
Do this phase in a **single deployment** — no partial migrations.

---

#### Task P2.1 — Multi-Tenant Foundation (Do this FIRST)

**This is the most critical and most dangerous task in v3.0. Take extra care.**

**New tables:**

Migration: `V10__create_tenants_table.sql`
```sql
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    logo_url VARCHAR(500),
    primary_color VARCHAR(7),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    -- status: ACTIVE, SUSPENDED, INACTIVE
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed NSG Academy as first tenant
INSERT INTO tenants (id, name, slug)
VALUES ('00000000-0000-0000-0000-000000000001', 'NSG Academy', 'nsgacademy');
```

Migration: `V11__create_tenant_features_table.sql`
```sql
CREATE TABLE tenant_features (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    plugin_type VARCHAR(100) NOT NULL,
    -- plugin_type: SOCIAL_MEDIA, TELECALLING, CHECKLIST_OPS
    is_enabled  BOOLEAN DEFAULT FALSE,
    enabled_at  TIMESTAMP,
    enabled_by  UUID REFERENCES users(id),
    -- enabled_by: SuperAdmin who enabled/disabled this plugin
    UNIQUE(tenant_id, plugin_type)
);

-- Enable all 3 plugins for NSG Academy
INSERT INTO tenant_features (tenant_id, plugin_type, is_enabled, enabled_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'SOCIAL_MEDIA', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000001', 'TELECALLING', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000001', 'CHECKLIST_OPS', TRUE, NOW());
```

Migration: `V12__add_tenant_id_to_all_tables.sql`
```sql
-- Add tenant_id to ALL existing core tables
-- NULL allowed initially — will be backfilled below, then enforced NOT NULL
-- Exception: SUPER_ADMIN users will keep tenant_id = NULL (they are platform-level)
ALTER TABLE users ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE employees ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE tasks ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE task_comments ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE task_attachments ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE notifications ADD COLUMN tenant_id UUID REFERENCES tenants(id);
ALTER TABLE audit_logs ADD COLUMN tenant_id UUID REFERENCES tenants(id);

-- Assign all existing data to NSG Academy
UPDATE users SET tenant_id = '00000000-0000-0000-0000-000000000001';
UPDATE employees SET tenant_id = '00000000-0000-0000-0000-000000000001';
UPDATE tasks SET tenant_id = '00000000-0000-0000-0000-000000000001';
UPDATE task_comments SET tenant_id = '00000000-0000-0000-0000-000000000001';
UPDATE task_attachments SET tenant_id = '00000000-0000-0000-0000-000000000001';
UPDATE notifications SET tenant_id = '00000000-0000-0000-0000-000000000001';
UPDATE audit_logs SET tenant_id = '00000000-0000-0000-0000-000000000001';

-- Enforce NOT NULL on all tables EXCEPT users
-- users.tenant_id stays nullable — SUPER_ADMIN has tenant_id = NULL
-- All auth checks must use: user.getTenantId() != null before setting TenantContext
ALTER TABLE employees ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE tasks ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE task_comments ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE task_attachments ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE audit_logs ALTER COLUMN tenant_id SET NOT NULL;
-- NOTE: users.tenant_id remains nullable (SUPER_ADMIN exception)

-- Indexes for performance
CREATE INDEX idx_users_tenant ON users(tenant_id) WHERE tenant_id IS NOT NULL;
CREATE INDEX idx_employees_tenant ON employees(tenant_id);
CREATE INDEX idx_tasks_tenant ON tasks(tenant_id);
CREATE INDEX idx_notifications_tenant ON notifications(tenant_id);
CREATE INDEX idx_audit_logs_tenant ON audit_logs(tenant_id);
```

**SuperAdmin tenant_id rule:**
- `users.tenant_id = NULL` → user is SUPER_ADMIN
- `TenantInterceptor` checks: if `user.getTenantId() == null && user has SUPER_ADMIN role` → skip TenantContext, allow request through
- If `user.getTenantId() == null && NOT SUPER_ADMIN` → 403 (data integrity error)
- All other users must have `tenant_id != NULL`

**New shared kernel files:**

`common/tenant/TenantContext.java` — ThreadLocal to hold current tenant UUID per request
`common/tenant/TenantInterceptor.java` — HandlerInterceptor that resolves tenant from authenticated user's `tenant_id` and stores in `TenantContext`

`common/tenant/TenantAwareRepository.java` — Base repository interface:
```java
// All tenant-scoped repos extend this
// Automatically adds WHERE tenant_id = TenantContext.get() to all queries
// Application code NEVER manually adds tenant_id filter
```

**Register interceptor in WebConfig.java.**

**New Tenant Config module:**

New endpoints:
```
GET    /api/v1/tenant/config
PUT    /api/v1/tenant/config
GET    /api/v1/tenant/features
PATCH  /api/v1/tenant/features/{pluginType}
```

New annotation: `@RequiresPlugin(PluginType.SOCIAL_MEDIA)` — Plugins (Phase 5, 6, 7) will use this.
Implementation: Check `TenantContext.get()` against `tenant_features` table. Return 403 if plugin not enabled.

---

#### Task P2.2 — SuperAdmin Role + Tenant Management

**Why in Phase 2:** Multi-tenant architecture needs someone who can create and manage tenants. SuperAdmin is that person. Without SuperAdmin, new tenants cannot be onboarded after NSG Academy.

**RoleType enum update:**
```java
public enum RoleType {
    SUPER_ADMIN,  // ← NEW — platform-level, above all tenants
    ADMIN,        // existing — tenant-level admin
    EMPLOYEE      // existing — tenant-level employee
}
```

**SuperAdmin scope (Phase 2 only — keep it minimal):**
- Can create new tenants
- Can enable/disable plugins per tenant
- Can view all tenants list
- Does NOT have a tenant_id (or uses a reserved system tenant_id)
- Cannot be created via normal signup — seeded via `AdminSeeder.java` only

**AdminSeeder.java update:**
```java
// On first startup — if no SUPER_ADMIN exists, create one
// Credentials come from application properties: superadmin.email, superadmin.password
@PostConstruct
public void seed() {
    seedSuperAdmin();  // NEW
    seedDefaultAdmin(); // existing — but now scoped to NSG Academy tenant
}
```

**Default task types on tenant creation (FR-TENANT-08):**

When SuperAdmin creates a new tenant, `TaskTypeService` auto-creates a default set of task types for that tenant:

| Slug | Name | proof_required | approval_required |
|---|---|---|---|
| `general` | General Task | false | false |
| `social_media_post` | Social Media Post | true | true |
| `telecall` | Telecalling | false | false |
| `daily_check` | Daily Checklist | false | true |
| `content_creation` | Content Creation | true | true |
| `review_collection` | Review Collection | false | false |

This ensures every new tenant has sensible defaults without manual setup.

**New SuperAdmin API endpoints:**
```
POST   /api/v1/superadmin/tenants               — Create new tenant
GET    /api/v1/superadmin/tenants               — List all tenants
GET    /api/v1/superadmin/tenants/{id}          — Tenant detail
PUT    /api/v1/superadmin/tenants/{id}          — Update tenant (branding, status)
PATCH  /api/v1/superadmin/tenants/{id}/features/{pluginType}  — Toggle plugin
```

All SuperAdmin endpoints secured with `@PreAuthorize("hasRole('SUPER_ADMIN')")`.
SuperAdmin endpoints bypass `TenantInterceptor` — they operate across all tenants.

**New SuperAdmin module:**
```
module/superadmin/
├── controller/SuperAdminController.java
├── service/SuperAdminService.java
├── dto/
│   ├── CreateTenantRequest.java
│   ├── TenantResponse.java
│   └── TenantListResponse.java
```

**Frontend — Basic SuperAdmin dashboard:**
```
app/(superadmin)/
├── layout.tsx       — SuperAdmin layout (separate from admin/employee)
└── tenants/
    ├── page.tsx     — Tenant list table
    ├── new/page.tsx — Create tenant form
    └── [id]/page.tsx — Tenant detail + plugin toggles
```

---

---

**✅ Phase 2 Complete = TaskHive v3.0**
Multi-tenant foundation live. SuperAdmin can onboard new tenants. Core platform ready for Phase 3 features.

---

### Phase 3 — Custom Task Engine + Recurring Tasks · Output: **v3.1** (Weeks 4-6)

Goal: Tenants can define their own task types with custom fields. Recurring tasks auto-generate on schedule. Legacy tasks (no task type) continue working unchanged.

> ⚠️ `task_type_id` is nullable on `tasks` table — legacy v2.4 tasks have no type and must continue working normally. All `taskType` checks in code must null-guard.

---

#### Task P3.1 — Custom Task Type Engine

**New tables:**

`V13__create_task_type_definitions_table.sql`
```sql
CREATE TABLE task_type_definitions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenants(id),
    name              VARCHAR(100) NOT NULL,
    slug              VARCHAR(100) NOT NULL,    -- machine name e.g. 'social_media_post'
    description       VARCHAR(500),
    icon              VARCHAR(50),
    color             VARCHAR(20),
    approval_required BOOLEAN NOT NULL DEFAULT FALSE,
    proof_required    BOOLEAN NOT NULL DEFAULT FALSE,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted        BOOLEAN DEFAULT FALSE,
    created_by        UUID REFERENCES users(id),
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, slug)
);
CREATE INDEX idx_task_type_defs_tenant ON task_type_definitions(tenant_id);
```
```

`V14__create_custom_field_definitions_table.sql`
```sql
CREATE TABLE custom_field_definitions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_type_id UUID NOT NULL REFERENCES task_type_definitions(id) ON DELETE CASCADE,
    field_name   VARCHAR(100) NOT NULL,
    field_label  VARCHAR(200) NOT NULL,
    field_type   VARCHAR(50) NOT NULL,
    -- field_type: TEXT, NUMBER, URL, SELECT, DATE, BOOLEAN, REFERENCE
    is_required  BOOLEAN NOT NULL DEFAULT FALSE,
    options      JSONB,
    -- options: used only for SELECT type — ["Option A", "Option B"]
    sort_order   INT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(task_type_id, field_name)
);
```

`V15__alter_tasks_add_v3_columns.sql`
```sql
-- task_type_id is nullable — legacy tasks have no type
ALTER TABLE tasks ADD COLUMN task_type_id         UUID REFERENCES task_type_definitions(id);

-- Approval workflow columns
ALTER TABLE tasks ADD COLUMN submitted_at         TIMESTAMP;
-- submitted_at: when employee moved task to IN_REVIEW

ALTER TABLE tasks ADD COLUMN proof_required       BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN approval_required    BOOLEAN DEFAULT FALSE;
-- proof_required and approval_required are COPIED from task_type_definition at task CREATE time
-- This way if task_type is later changed, existing tasks are not affected

ALTER TABLE tasks ADD COLUMN approval_status      VARCHAR(50);
-- approval_status: PENDING, APPROVED, REJECTED (separate from task.status)

ALTER TABLE tasks ADD COLUMN approved_by          UUID REFERENCES users(id);
ALTER TABLE tasks ADD COLUMN approved_at          TIMESTAMP;
ALTER TABLE tasks ADD COLUMN rejection_reason     TEXT;

-- Recurring task tracking
ALTER TABLE tasks ADD COLUMN is_recurring         BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN recurring_template_id UUID;
-- recurring_template_id: FK to recurring_task_templates (no hard FK — template may be deleted)

-- New indexes
CREATE INDEX idx_tasks_task_type_id    ON tasks(task_type_id);
CREATE INDEX idx_tasks_is_late         ON tasks(is_late) WHERE is_late = TRUE;
CREATE INDEX idx_tasks_approval_status ON tasks(approval_status) WHERE approval_status = 'PENDING';

-- Custom field values table
CREATE TABLE task_custom_field_values (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id          UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    field_def_id     UUID NOT NULL REFERENCES custom_field_definitions(id),
    value            JSONB NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(task_id, field_def_id)
);
CREATE INDEX idx_task_custom_fields_task ON task_custom_field_values(task_id);
CREATE INDEX idx_task_custom_fields_gin  ON task_custom_field_values USING GIN(value);
```

> **Note on `proof_required` / `approval_required` on tasks table:**
> These are snapshot fields — copied from the task type at task creation time. If a tenant later changes the task type's settings, existing tasks are unaffected. Always read these from `tasks` table, not from `task_type_definitions`, when enforcing business logic.

**New backend files:**
```
module/tasktype/
├── controller/TaskTypeController.java
├── service/TaskTypeService.java
├── dto/
│   ├── CreateTaskTypeRequest.java
│   ├── UpdateTaskTypeRequest.java
│   ├── TaskTypeResponse.java
│   ├── CreateCustomFieldRequest.java
│   └── CustomFieldResponse.java
├── model/
│   ├── TaskTypeDefinition.java
│   └── CustomFieldDefinition.java
├── enums/
│   └── CustomFieldType.java   -- TEXT, NUMBER, URL, SELECT, DATE, BOOLEAN, REFERENCE
└── repository/
    ├── TaskTypeRepository.java
    └── CustomFieldRepository.java
```

**New API endpoints:**
```
POST   /api/v1/task-types                           ADMIN — create task type
GET    /api/v1/task-types                           ADMIN/EMPLOYEE — list task types for tenant
GET    /api/v1/task-types/{id}                      ADMIN/EMPLOYEE — get task type detail
PUT    /api/v1/task-types/{id}                      ADMIN — update task type
DELETE /api/v1/task-types/{id}                      ADMIN — soft delete task type
POST   /api/v1/task-types/{typeId}/fields           ADMIN — add field to type
PUT    /api/v1/task-types/{typeId}/fields/{fieldId} ADMIN — update field
DELETE /api/v1/task-types/{typeId}/fields/{fieldId} ADMIN — remove field
```

**Modified backend:**
- `TaskService.java` — when creating/updating task, if `task_type_id` present, save `task_custom_field_values`
- `TaskResponse.java` — add `taskType` (name, color, icon) and `customFieldValues` (Map<fieldName, value>)
- `CreateTaskRequest.java` — add optional `taskTypeId` + `customFieldValues` map

**Frontend new files:**
```
features/tasktype/
├── components/
│   ├── TaskTypeList.tsx         -- list with edit/delete
│   ├── TaskTypeForm.tsx         -- create/edit form
│   └── CustomFieldBuilder.tsx  -- drag-and-drop field builder
└── hooks/
    ├── useTaskTypes.ts
    ├── useCreateTaskType.ts
    └── useUpdateTaskType.ts

features/task/components/
└── CustomFieldsRenderer.tsx     -- renders dynamic fields based on field_type
                                 -- TEXT → input, NUMBER → number input,
                                 -- SELECT → dropdown, DATE → date picker,
                                 -- BOOLEAN → checkbox, URL → url input,
                                 -- REFERENCE → entity lookup/search input
```

**Frontend modified:**
- `TaskForm.tsx` — add task type selector dropdown; when type selected, render `CustomFieldsRenderer` below
- `(admin)/config/task-types/page.tsx` — new page for task type management

---

#### Task P3.2 — Recurring Task Engine

`V16__create_recurring_task_templates_table.sql`
```sql
CREATE TABLE recurring_task_templates (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    task_type_id     UUID REFERENCES task_type_definitions(id),
    assigned_to      UUID REFERENCES employees(id),
    priority         VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    recurrence_type  VARCHAR(50) NOT NULL,
    -- recurrence_type: DAILY, WEEKLY, MONTHLY
    recurrence_day   INTEGER,
    -- DAILY: null
    -- WEEKLY: 1=Mon, 2=Tue ... 7=Sun
    -- MONTHLY: 1-31 (day of month)
    generation_time  TIME DEFAULT '06:00',
    -- Time of day when task is auto-generated
    due_offset_hours INTEGER DEFAULT 18,
    -- due_date = generation_time + due_offset_hours
    -- Default: task generated at 6 AM, due at midnight (18h later)
    is_active        BOOLEAN DEFAULT TRUE,
    is_deleted       BOOLEAN DEFAULT FALSE,
    last_generated_date DATE,
    -- last_generated_date: set each time a task is generated — prevents duplicate generation
    created_by       UUID NOT NULL REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_recurring_templates_tenant ON recurring_task_templates(tenant_id) WHERE is_active = TRUE;
```

**New backend files:**
```
module/task/scheduler/RecurringTaskGeneratorScheduler.java
module/task/service/RecurringTaskService.java
module/task/dto/
├── CreateRecurringTaskRequest.java
├── UpdateRecurringTaskRequest.java
└── RecurringTaskResponse.java
module/task/model/RecurringTaskTemplate.java
module/task/repository/RecurringTaskTemplateRepository.java
```

**Scheduler logic:**
```java
// Runs every day at 06:00 AM
@Scheduled(cron = "0 0 6 * * *")
public void generateDailyTasks() {
    // For each active tenant
    // For each active template in tenant
    // Check if today matches recurrence pattern
    // Check if task already generated today (last_generated_date == today)
    // If not generated → create task, set last_generated_date = today
    // due_date = today at template.due_time
    // If holiday exists for today in tenant → skip generation
}
```

**New API endpoints:**
```
POST   /api/v1/recurring-tasks           ADMIN — create template
GET    /api/v1/recurring-tasks           ADMIN — list templates
GET    /api/v1/recurring-tasks/{id}      ADMIN — detail
PUT    /api/v1/recurring-tasks/{id}      ADMIN — update
DELETE /api/v1/recurring-tasks/{id}      ADMIN — deactivate (soft)
```

**Frontend new files:**
```
features/task/components/RecurringTaskForm.tsx
(admin)/tasks/recurring/page.tsx   -- recurring templates management
```

**Modified:**
- `TaskCard.tsx` — show "↻ Recurring" badge when task was generated from a template
- `TaskResponse.java` — add `isRecurring` boolean + `recurringTemplateId`

**✅ Phase 3 Complete = TaskHive v3.1**
Tenants have fully configurable task types + automatic recurring task generation.

---

### Phase 4 — Handover + Performance + Broadcast · Output: **v3.2** (Weeks 7-9)

Goal: Employee accountability system — task handovers, daily scorecards, streaks, leaderboard, internal broadcasts. Owner gets full visibility into team performance.

---

#### Task P4.1 — Task Handover Workflow

`V17__create_task_handovers_table.sql`
```sql
CREATE TABLE task_handovers (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- tenant isolation via task_id -> tasks.tenant_id chain — no direct tenant_id needed
    task_id      UUID NOT NULL REFERENCES tasks(id),
    requested_by UUID NOT NULL REFERENCES employees(id),
    requested_to UUID NOT NULL REFERENCES employees(id),
    reason       TEXT NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    -- status: REQUESTED, APPROVED, REJECTED
    admin_comment TEXT,
    decided_by   UUID REFERENCES users(id),
    decided_at   TIMESTAMP,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_task_handovers_status ON task_handovers(status) WHERE status = 'REQUESTED';
```
```

**New backend files:**
```
module/task/service/TaskHandoverService.java
module/task/controller/TaskHandoverController.java
module/task/enums/HandoverStatus.java        -- REQUESTED, APPROVED, REJECTED
module/task/dto/
├── TaskHandoverRequest.java    -- requested_to, reason
├── TaskHandoverResolveRequest.java  -- admin_note (optional)
└── TaskHandoverResponse.java
module/task/model/TaskHandover.java
module/task/repository/TaskHandoverRepository.java
```

**Logic:**
- On approve: `tasks.assigned_to` = `requested_to`, publish `TaskHandoverApprovedEvent`
- On reject: notify requesting employee via notification, publish `TaskHandoverRejectedEvent`
- Only one PENDING handover allowed per task at a time

**New API endpoints:**
```
POST   /api/v1/tasks/{id}/handover-request            EMPLOYEE — request handover
PATCH  /api/v1/tasks/handovers/{handoverId}/approve   ADMIN — approve
PATCH  /api/v1/tasks/handovers/{handoverId}/reject    ADMIN — reject
GET    /api/v1/tasks/handovers/pending                ADMIN — list pending handovers
GET    /api/v1/tasks/{id}/handovers                   ADMIN — handover history for task
```

**New notification triggers (add to TaskEventListener.java):**
- `TaskHandoverApprovedEvent` → in-app + email to requesting employee
- `TaskHandoverRejectedEvent` → in-app + email to requesting employee
- New handover request → in-app + email to all admins

**New email templates:**
- `task-handover-requested.html`
- `task-handover-approved.html`
- `task-handover-rejected.html`

**Frontend new files:**
```
features/task/components/TaskHandoverModal.tsx  -- request form
features/task/components/HandoverHistoryList.tsx
(admin)/tasks/handovers/page.tsx               -- pending handovers list
```

**Modified:**
- `TaskDetail.tsx` — "Request Handover" button for assigned employee when task is IN_PROGRESS

---

#### Task P4.2 — Daily Scorecard + Streak + Leaderboard

`V18__create_performance_tables.sql`
```sql
CREATE TABLE daily_scorecards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    scorecard_date DATE NOT NULL,
    tasks_assigned INT NOT NULL DEFAULT 0,
    tasks_completed INT NOT NULL DEFAULT 0,
    tasks_late INT NOT NULL DEFAULT 0,
    tasks_skipped INT NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5,2),
    score DECIMAL(6,2),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, scorecard_date)
);
CREATE INDEX idx_scorecards_tenant_date ON daily_scorecards(tenant_id, scorecard_date);
CREATE INDEX idx_scorecards_employee ON daily_scorecards(employee_id);

CREATE TABLE employee_streaks (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id    UUID UNIQUE NOT NULL REFERENCES employees(id),
    -- tenant isolation via employees.tenant_id — no direct tenant_id needed here
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    streak_start   DATE,
    -- streak_start: date when current streak began (NULL if streak = 0)
    last_updated   DATE,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Score formula:**
```
completion_rate = tasks_completed / tasks_assigned  (0.0 to 1.0)
score = (completion_rate × 100) - (tasks_late × 5) - (tasks_skipped × 2)
score = MAX(0, score)   -- floor at 0
score = MIN(100, score) -- cap at 100
```

**Streak rules:**
- Streak increments when `completion_rate = 1.0` for that day
- Streak resets to 0 when `completion_rate < 1.0` AND at least one task was assigned
- Days with zero assigned tasks — streak neither increments nor resets
- `longest_streak` always tracks the all-time best

**New backend files:**
```
module/performance/
├── controller/PerformanceController.java
├── service/
│   ├── ScorecardService.java
│   ├── StreakService.java
│   └── LeaderboardService.java
├── scheduler/DailyScorecardScheduler.java
├── dto/
│   ├── ScorecardResponse.java
│   ├── StreakResponse.java
│   └── LeaderboardEntryResponse.java
├── model/
│   ├── DailyScorecard.java
│   └── EmployeeStreak.java
└── repository/
    ├── DailyScorecardRepository.java
    └── EmployeeStreakRepository.java
```

**Scheduler logic:**
```java
// Runs every night at 23:30
@Scheduled(cron = "0 30 23 * * *")
public void calculateDailyScorecards() {
    // For each tenant
    // For each active employee in tenant
    // Count tasks_assigned, tasks_completed, tasks_late, tasks_skipped for today
    // Calculate completion_rate and score
    // Save/update daily_scorecards record
    // Update employee_streaks based on today's completion_rate
}
```

**Leaderboard:** Top 10 by cumulative score — weekly (Mon–Sun) and monthly. Recalculated nightly by `DailyScorecardScheduler`.

**New API endpoints:**
```
GET /api/v1/performance/scorecard/today                ADMIN/EMPLOYEE
GET /api/v1/performance/scorecard/{employeeId}         ADMIN — history
GET /api/v1/performance/scorecard/{employeeId}?from=&to= ADMIN — date range
GET /api/v1/performance/streak/me                      EMPLOYEE
GET /api/v1/performance/streak/{employeeId}            ADMIN
GET /api/v1/performance/leaderboard/weekly             ADMIN/EMPLOYEE
GET /api/v1/performance/leaderboard/monthly            ADMIN/EMPLOYEE
```

**Frontend new files:**
```
features/performance/
├── components/
│   ├── DailyScorecardWidget.tsx   -- today's score card with breakdown
│   ├── StreakBadge.tsx            -- flame icon + streak count
│   └── Leaderboard.tsx           -- ranked table with medal icons
└── hooks/
    ├── useDailyScorecard.ts
    ├── useStreak.ts
    └── useLeaderboard.ts

(admin)/performance/page.tsx     -- admin performance overview
```

**Modified:**
- `(employee)/dashboard/page.tsx` — add `DailyScorecardWidget` + `StreakBadge` at top

---

#### Task P4.3 — Internal Broadcast

`V19__create_broadcasts_table.sql`
```sql
CREATE TABLE broadcasts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent_by UUID NOT NULL REFERENCES users(id),
    -- sent_by references users(id), not employees — admin sends broadcasts, not employees
    send_email BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_broadcasts_tenant ON broadcasts(tenant_id, created_at DESC);
```

**New backend files:**
```
module/notification/service/BroadcastService.java
module/notification/controller/BroadcastController.java
module/notification/dto/
├── CreateBroadcastRequest.java  -- title, message, send_email
└── BroadcastResponse.java
module/notification/model/Broadcast.java
module/notification/repository/BroadcastRepository.java
```

**Delivery mechanism:**
- On broadcast send → push in-app notification to ALL active employees in tenant via WebSocket topic `/topic/system`
- If `send_email = true` → queue email to all employees via email_queue

**New API endpoints:**
```
POST /api/v1/broadcast     ADMIN — send broadcast
GET  /api/v1/broadcast     ADMIN/EMPLOYEE — list broadcasts for tenant (latest 20)
```

> **Weekly Owner Report scheduler** is also added in Phase 4 (notification module):
> `module/notification/scheduler/WeeklyReportScheduler.java`
> - Runs every Sunday at 22:00 via `@Scheduled(cron = "0 0 22 ? * SUN")`
> - Calls `WeeklyReportGeneratorService` in analytics module
> - Emails report to all ADMIN users of each tenant
> - In Phase 4 it generates a simple text/HTML report from DB data
> - In Phase 8 it will be enhanced with Claude API narrative summary
```
features/notification/components/BroadcastBanner.tsx  -- dismissable top banner
features/notification/components/BroadcastForm.tsx    -- admin compose form
```

**Modified:**
- Admin dashboard — "Send Announcement" button opening `BroadcastForm`
- Employee layout — `BroadcastBanner` listens to WebSocket `/topic/system`

**✅ Phase 4 Complete = TaskHive v3.2**
Full employee accountability system live. Owner has complete visibility into team performance.

---

### Phase 5 — Social Media Plugin · Output: **v3.3** (Weeks 10-12)

Goal: NSG Academy's primary use case — manage all social media posting tasks end-to-end.

#### Task P5.1 — Platform + Account Master

> ⚠️ `sm_platforms` is **tenant-specific** — NOT a global table. Each tenant adds their own platforms. No global seed. NSG Academy adds their own (Instagram, YouTube etc.) via UI after onboarding.

New tables:
```sql
CREATE TABLE sm_platforms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,     -- Instagram, YouTube, Telegram etc.
    icon VARCHAR(100),
    color VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, name)
);

CREATE TABLE sm_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    platform_id UUID NOT NULL REFERENCES sm_platforms(id),
    handle VARCHAR(200) NOT NULL,   -- @nsgacademy
    display_name VARCHAR(200),
    category VARCHAR(100),          -- MAIN, EXAM_SPECIFIC, NICHE
    profile_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

New endpoints:
```
POST/GET/PUT/DELETE  /api/v1/plugins/social-media/platforms
POST/GET/PUT/DELETE  /api/v1/plugins/social-media/accounts
```

#### Task P5.2 — Social Media Task Type + Proof

Pre-configured task type for each tenant with custom fields:
- `platform_id` (SELECT — from sm_platforms)
- `account_id` (SELECT — from sm_accounts filtered by platform)
- `post_url` (URL — filled after posting)
- `caption_text` (TEXT)
- `scheduled_date` (DATE)

`approval_required = true`, `proof_required = true` on this task type.
On approval: log `post_url` + `posted_at` timestamp.

#### Task P5.3 — Content Calendar

`V22__create_sm_content_calendar_table.sql`
```sql
CREATE TABLE sm_content_calendar (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenants(id),
    account_id    UUID NOT NULL REFERENCES sm_accounts(id),
    scheduled_date DATE NOT NULL,
    content_type  VARCHAR(100),
    -- content_type: POST, STORY, REEL, VIDEO, COMMUNITY_POST
    title         VARCHAR(300),
    description   TEXT,
    task_id       UUID REFERENCES tasks(id),
    -- task_id: linked Core task (if content is assigned as a task)
    status        VARCHAR(50) DEFAULT 'PLANNED',
    -- status: PLANNED, ASSIGNED, COMPLETED, MISSED
    notes         TEXT,
    created_by    UUID REFERENCES users(id),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_sm_calendar_tenant_date ON sm_content_calendar(tenant_id, scheduled_date);
```

New endpoints: `GET/POST/PUT /api/v1/plugins/social-media/calendar`
Frontend: `(admin)/plugins/social-media/calendar/page.tsx` — FullCalendar view

#### Task P5.4 — Asset Library

`V23__create_sm_asset_library_table.sql`
```sql
CREATE TABLE sm_asset_library (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    file_name    VARCHAR(255) NOT NULL,
    file_url     VARCHAR(500) NOT NULL,
    file_size    BIGINT,
    mime_type    VARCHAR(100),
    content_type VARCHAR(100),
    -- content_type: BANNER, VIDEO, QUOTE, OFFER, FESTIVAL
    tags         VARCHAR(100)[],
    platform_ids UUID[],
    -- platform_ids: which platforms this asset is for
    task_id      UUID REFERENCES tasks(id),
    -- task_id: source task (if asset was submitted as proof from a task)
    uploaded_by  UUID REFERENCES users(id),
    uploaded_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_sm_asset_library_tags ON sm_asset_library USING GIN(tags);
```

New endpoints: `GET/POST/DELETE /api/v1/plugins/social-media/assets`
Frontend: `(admin)/plugins/social-media/assets/page.tsx` — searchable grid

#### Task P5.5 — Festival Reminder + Claude Suggestions + Performance Report

New scheduler: `module/plugins/socialmedia/scheduler/FestivalReminderScheduler.java` — 30 days ahead alerts
New endpoint: `GET /api/v1/plugins/social-media/festivals/upcoming`

Claude API integration: `module/plugins/socialmedia/service/ContentSuggestionService.java`
- `GET /api/v1/plugins/social-media/suggestions`
- Rate limited: 20 calls/hour per tenant, cache 6 hours
- Circuit breaker — graceful fallback if Claude API is down

Per-platform performance report:
- `GET /api/v1/plugins/social-media/performance/by-platform`
- Returns: completion rate, on-time rate per platform per month

**✅ Phase 5 Complete = TaskHive v3.3**
Social Media Plugin live. NSG can manage all social tasks end-to-end.

---

### Phase 6 — Checklist/Ops Plugin + Holiday/Leave · Output: **v3.4** (Weeks 13-15)

Goal: Physical operations management + leave tracking integrated with task engine.

#### Task P6.1 — Checklist Template Builder

`V30__create_cl_templates_table.sql`
```sql
CREATE TABLE cl_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    assigned_to     UUID REFERENCES employees(id),
    recurrence_type VARCHAR(50) NOT NULL DEFAULT 'DAILY',
    -- recurrence_type: DAILY, WEEKLY
    recurrence_day  INTEGER,
    -- WEEKLY: 1=Mon, 2=Tue ... 7=Sun
    is_active       BOOLEAN DEFAULT TRUE,
    is_deleted      BOOLEAN DEFAULT FALSE,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cl_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL REFERENCES cl_templates(id),
    label       VARCHAR(300) NOT NULL,
    item_type   VARCHAR(50) NOT NULL,
    -- item_type: YES_NO, PHOTO_REQUIRED, NUMBER_INPUT, TEXT_INPUT
    is_critical BOOLEAN DEFAULT FALSE,
    -- is_critical: TRUE → immediate admin alert if not checked
    sort_order  INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Critical items trigger immediate admin alert on miss.
New endpoints:
```
POST   /api/v1/plugins/checklist/templates
GET    /api/v1/plugins/checklist/templates
PUT    /api/v1/plugins/checklist/templates/{id}
DELETE /api/v1/plugins/checklist/templates/{id}
```
Frontend: `ChecklistTemplateBuilder.tsx`

#### Task P6.2 — Daily Checklist Instance Generation

`V32__create_cl_task_instances_table.sql`
```sql
CREATE TABLE cl_task_instances (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenants(id),
    template_id   UUID NOT NULL REFERENCES cl_templates(id),
    task_id       UUID REFERENCES tasks(id),
    -- task_id: linked Core task (for notification + scorecard integration)
    instance_date DATE NOT NULL,
    status        VARCHAR(50) DEFAULT 'PENDING',
    -- status: PENDING, IN_PROGRESS, SUBMITTED, APPROVED, FLAGGED
    completed_by  UUID REFERENCES employees(id),
    submitted_at  TIMESTAMP,
    approved_by   UUID REFERENCES users(id),
    approved_at   TIMESTAMP,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_cl_instances_template_date ON cl_task_instances(template_id, instance_date);

CREATE TABLE cl_item_responses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id UUID NOT NULL REFERENCES cl_task_instances(id),
    item_id     UUID NOT NULL REFERENCES cl_items(id),
    response    JSONB,
    -- response: true/false for YES_NO, number for NUMBER_INPUT, text for TEXT_INPUT
    photo_url   VARCHAR(500),
    -- photo_url: used for PHOTO_REQUIRED items
    responded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(instance_id, item_id)
);
```

Integrated with RecurringTaskEngine — generates daily at 8 AM.
New endpoints:
```
GET    /api/v1/plugins/checklist/instances/today
GET    /api/v1/plugins/checklist/instances/{id}
PATCH  /api/v1/plugins/checklist/instances/{id}/items/{itemId}
POST   /api/v1/plugins/checklist/instances/{id}/submit
```

#### Task P6.3 — Vendor Management + Petty Expenses

`V33__create_cl_vendors_table.sql`
```sql
CREATE TABLE cl_vendors (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID NOT NULL REFERENCES tenants(id),
    name           VARCHAR(200) NOT NULL,
    phone          VARCHAR(20),
    category       VARCHAR(100),
    -- category: WATER, CLEANING, IT, STATIONERY, OTHER
    last_contact_at DATE,
    next_due_at     DATE,
    notes          TEXT,
    is_active      BOOLEAN DEFAULT TRUE,
    created_by     UUID REFERENCES users(id),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cl_petty_expenses (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants(id),
    submitted_by     UUID NOT NULL REFERENCES employees(id),
    amount           DECIMAL(10,2) NOT NULL,
    category         VARCHAR(100),
    -- category: PRINTING, STATIONERY, FOOD, TRAVEL, OTHER
    description      TEXT NOT NULL,
    receipt_url      VARCHAR(500),
    status           VARCHAR(50) DEFAULT 'PENDING',
    -- status: PENDING, APPROVED, REJECTED
    decided_by       UUID REFERENCES users(id),
    decided_at       TIMESTAMP,
    rejection_reason TEXT,
    expense_date     DATE NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

New endpoints:
```
POST/GET/PUT         /api/v1/plugins/checklist/vendors
POST/GET             /api/v1/plugins/checklist/expenses
PATCH                /api/v1/plugins/checklist/expenses/{id}/approve
PATCH                /api/v1/plugins/checklist/expenses/{id}/reject
```

#### Task P6.4 — Holiday + Leave Calendar

New tables: `holidays`, `employee_leaves`

```sql
-- holidays are tenant-specific (each institute has their own calendar)
CREATE TABLE holidays (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL REFERENCES tenants(id),
    name         VARCHAR(200) NOT NULL,
    holiday_date DATE NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    -- is_recurring = TRUE: repeats every year on same date (e.g. Diwali)
    created_by   UUID REFERENCES users(id),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, holiday_date)
);
CREATE INDEX idx_holidays_tenant_date ON holidays(tenant_id, holiday_date);

CREATE TABLE employee_leaves (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id         UUID NOT NULL REFERENCES employees(id),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    leave_date          DATE NOT NULL,
    reason              TEXT,
    status              VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- status: PENDING, APPROVED, REJECTED
    backup_employee_id  UUID REFERENCES employees(id),
    -- Optional: who handles this employee's tasks while on leave
    decided_by          UUID REFERENCES users(id),
    decided_at          TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, employee_id, leave_date)
);
```

Integrated with RecurringTaskEngine — `RecurringTaskGeneratorScheduler` checks `holidays` table before generating tasks. If today is a holiday for the tenant, skip all recurring task generation for that tenant.

New endpoints:
```
POST   /api/v1/holidays              ADMIN — add holiday
GET    /api/v1/holidays              ADMIN/EMPLOYEE — list holidays for tenant
DELETE /api/v1/holidays/{id}         ADMIN — remove holiday
POST   /api/v1/leaves                EMPLOYEE — request leave
GET    /api/v1/leaves                ADMIN — all leaves, EMPLOYEE — own leaves
PATCH  /api/v1/leaves/{id}/approve   ADMIN
PATCH  /api/v1/leaves/{id}/reject    ADMIN
```

Frontend: `(admin)/calendar/page.tsx` — combined holiday + leave calendar view

**✅ Phase 6 Complete = TaskHive v3.4**
Physical operations fully tracked. NSG Academy fully operational on TaskHive.

---

### Phase 7 — Telecalling Plugin · Output: **v3.5** (Weeks 16-18)

Goal: Lead and fees calling management — campaigns, call logs, admission pipeline.

#### Task P7.1 — Campaign Management

`V40__create_tc_campaigns_table.sql`
```sql
CREATE TABLE tc_campaigns (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id     UUID NOT NULL REFERENCES tenants(id),
    name          VARCHAR(200) NOT NULL,
    campaign_type VARCHAR(50) NOT NULL,
    -- campaign_type: FEES_CALLING, LEAD_CALLING
    description   TEXT,
    start_date    DATE,
    end_date      DATE,
    status        VARCHAR(50) DEFAULT 'ACTIVE',
    -- status: ACTIVE, PAUSED, COMPLETED
    created_by    UUID REFERENCES users(id),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

New endpoints:
```
POST   /api/v1/plugins/telecalling/campaigns
GET    /api/v1/plugins/telecalling/campaigns
GET    /api/v1/plugins/telecalling/campaigns/{id}
PUT    /api/v1/plugins/telecalling/campaigns/{id}
```

#### Task P7.2 — Excel Lead Import

`V41__create_tc_leads_table.sql`
```sql
CREATE TABLE tc_leads (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenants(id),
    campaign_id       UUID NOT NULL REFERENCES tc_campaigns(id),
    name              VARCHAR(200) NOT NULL,
    phone             VARCHAR(20) NOT NULL,
    email             VARCHAR(255),
    course_interested VARCHAR(200),
    lead_source       VARCHAR(100),
    -- lead_source: INSTAGRAM, YOUTUBE, GOOGLE, REFERRAL, WALK_IN, OTHER
    status            VARCHAR(50) DEFAULT 'NEW',
    -- status: NEW, INTERESTED, DEMO_SCHEDULED, DEMO_DONE, ENROLLED, LOST
    lost_reason       TEXT,
    assigned_to       UUID REFERENCES employees(id),
    attempt_count     INTEGER DEFAULT 0,
    last_call_at      TIMESTAMP,
    next_followup_at  TIMESTAMP,
    extra_data        JSONB,
    -- extra_data: any additional columns from Excel not in standard schema
    is_deleted        BOOLEAN DEFAULT FALSE,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tc_leads_campaign_id  ON tc_leads(campaign_id);
CREATE INDEX idx_tc_leads_assigned_to  ON tc_leads(assigned_to);
CREATE INDEX idx_tc_leads_status       ON tc_leads(status);
```

Apache POI Excel parsing — `LeadImportService.java`
New endpoints:
```
POST  /api/v1/plugins/telecalling/campaigns/{id}/import-leads
GET   /api/v1/plugins/telecalling/campaigns/{id}/leads
GET   /api/v1/plugins/telecalling/leads/{id}
PATCH /api/v1/plugins/telecalling/leads/{id}/status
```
Frontend: Column mapping UI for Excel import.

#### Task P7.3 — Call Logs + Dispositions

`V42__create_tc_call_logs_table.sql`
```sql
CREATE TABLE tc_call_logs (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL REFERENCES tenants(id),
    lead_id               UUID NOT NULL REFERENCES tc_leads(id),
    called_by             UUID NOT NULL REFERENCES employees(id),
    disposition           VARCHAR(50) NOT NULL,
    -- disposition: ANSWERED, NOT_REACHABLE, SWITCHED_OFF, CALLBACK_REQUESTED,
    --              NOT_INTERESTED, CONVERTED
    notes                 TEXT,
    callback_at           TIMESTAMP,
    -- callback_at: set when disposition = CALLBACK_REQUESTED
    call_duration_seconds INTEGER,
    called_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tc_call_logs_lead_id ON tc_call_logs(lead_id);
```

Lead status flow: `NEW` → `INTERESTED` → `DEMO_SCHEDULED` → `DEMO_DONE` → `ENROLLED` / `LOST`
On `CALLBACK_REQUESTED`: auto-create follow-up task via `TaskService`.
New endpoints:
```
POST  /api/v1/plugins/telecalling/call-logs
GET   /api/v1/plugins/telecalling/call-logs/my
GET   /api/v1/plugins/telecalling/call-logs/lead/{leadId}
```

#### Task P7.4 — Campaign Dashboard + Export

New endpoints:
```
GET   /api/v1/plugins/telecalling/campaigns/{id}/dashboard
POST  /api/v1/plugins/telecalling/campaigns/{id}/export
```
Dashboard metrics: total leads, called, pending, conversion rate, per-employee stats.
Export: Excel/PDF via Apache POI.

**✅ Phase 7 Complete = TaskHive v3.5**
Telecalling Plugin live. Admissions + fees calling fully managed.

---

## 📋 Section 5 — New Notification Triggers (per phase)

Add these to `TaskEventListener.java` and `NotificationService.java` in the phase they belong to.

### Phase 1 triggers (add with Phase 1)

| Event | In-App | Email | Template |
|---|---|---|---|
| Task Approved | ✅ assignee | ✅ assignee | `task-approved.html` |
| Task Rejected | ✅ assignee | ✅ assignee | `task-rejected.html` |

### Phase 4 triggers (add with Phase 4)

| Event | In-App | Email | Template |
|---|---|---|---|
| Handover Requested | ✅ all admins | ✅ all admins | `task-handover-requested.html` |
| Handover Approved | ✅ requester | ✅ requester | `task-handover-approved.html` |
| Handover Rejected | ✅ requester | ✅ requester | `task-handover-rejected.html` |
| Admin Broadcast | ✅ all employees | ✅ if send_email=true | — (inline message) |

### Phase 8 triggers (add with Phase 8)

| Event | In-App | Email | Template |
|---|---|---|---|
| Anomaly Detected | ✅ all admins | ✅ all admins | `anomaly-alert.html` |
| Weekly Report Ready | ❌ | ✅ all admins | `weekly-owner-report.html` |

### Email templates to create per phase

**Phase 1:**
- `task-approved.html`
- `task-rejected.html`

**Phase 4:**
- `task-handover-requested.html`
- `task-handover-approved.html`
- `task-handover-rejected.html`

**Phase 8:**
- `anomaly-alert.html`
- `weekly-owner-report.html`

---

## 📋 Section 6 — Flyway Migration Order Summary

```
-- Already applied in v2.4
V1   → V7.1  (users, roles, employees, tasks, notifications, audit, ml, indexes)

-- Phase 1 (v2.5)
V8   alter_task_status_add_pending_approval
V9   alter_tasks_add_late_and_analytics_columns    (is_late, submitted_at, attachment_purpose, daily_metrics.late_tasks)

-- Phase 2 (v3.0 — Multi-Tenant Foundation)
V10  create_tenants_table                      ← seed NSG Academy
V11  create_tenant_features_table              ← seed NSG plugins (all 3 enabled)
V12  add_tenant_id_to_all_tables               ← backfill + users.tenant_id stays nullable

-- Phase 3 (v3.1 — Custom Task Engine + Recurring)
V13  create_task_type_definitions_table
V14  create_custom_field_definitions_table
V15  alter_tasks_add_v3_columns                  (task_type_id + approval cols + recurring cols + custom field values table)
V16  create_recurring_task_templates_table

-- Phase 4 (v3.2 — Handover + Performance + Broadcast)
V17  create_task_handovers_table
V18  create_performance_tables                 (daily_scorecards + employee_streaks)
V19  create_broadcasts_table

-- Phase 5 (v3.3 — Social Media Plugin)
V20  create_sm_platforms_table                 (tenant-specific — each tenant adds their own)
V21  create_sm_accounts_table
V22  create_sm_content_calendar_table
V23  create_sm_asset_library_table

-- Phase 6 (v3.4 — Checklist/Ops + Holiday/Leave)
V30  create_cl_templates_table
V31  create_cl_items_table
V32  create_cl_task_instances_table
V33  create_cl_vendors_table
V34  create_cl_petty_expenses_table
V35  create_holidays_table                     (tenant-specific — has tenant_id)
V36  create_employee_leaves_table

-- Phase 7 (v3.5 — Telecalling Plugin)
V40  create_tc_campaigns_table
V41  create_tc_leads_table
V42  create_tc_call_logs_table

-- Phase 8 (v3.6 — ML/AI Intelligence)
V50  create_ml_event_log_table                 ← deploy first, wait 2-4 weeks, then train
```

> **Numbering gaps are intentional:**
> - V20–V29 = Social Media Plugin
> - V30–V39 = Checklist/Ops Plugin
> - V40–V49 = Telecalling Plugin
> - V50+ = ML/AI
>
> Gaps allow adding new migrations within a phase without renumbering existing ones.

---

## 📋 Section 7 — Phase Execution Order + Dependencies

Single developer workflow — complete each phase before starting the next.

| Phase | Version | Depends on | Can start |
|---|---|---|---|
| Phase 1 | v2.5 | v2.4 base | Now |
| Phase 2 | v3.0 | Phase 1 deployed to production | After Phase 1 |
| Phase 3 | v3.1 | Phase 2 complete | After Phase 2 |
| Phase 4 | v3.2 | Phase 3 complete | After Phase 3 |
| Phase 5 | v3.3 | Phase 4 complete + `@RequiresPlugin` working | After Phase 4 |
| Phase 6 | v3.4 | Phase 4 complete (RecurringTaskEngine needed) | After Phase 4 |
| Phase 7 | v3.5 | Phase 4 complete | After Phase 4 |
| Phase 8 | v3.6 | Phases 5–7 complete (needs real data in all plugins) | After Phase 7 |
| Phase 9 | v3.7 | Phases 2–7 complete (all backend features stable) | After Phase 7 |

**Key deployment checkpoint:** After Phase 1 (v2.5) — deploy to production and let NSG Academy use it before starting Phase 2. Multi-tenant migration on an empty/untested system is safer than on a heavily used one.

**Phases 5, 6, 7 can theoretically run in any order** after Phase 4 — but Social Media first (Phase 5) because it's NSG's primary daily use case.

---

## 📋 Section 8 — Multi-Tenant Onboarding Pattern

TaskHive is a **multi-tenant SaaS product**. NSG Academy is the first tenant — but any business can be onboarded. The architecture must never assume NSG Academy is the only tenant.

### Tenant Onboarding Flow (Generic)

```
1. SuperAdmin creates tenant record (name, slug, logo, branding)
2. SuperAdmin enables plugins for that tenant via tenant_features
3. Tenant admin logs in — sees only their data (enforced via TenantContext)
4. Tenant admin configures their own:
   - Custom task types + custom fields
   - Plugin-specific masters (social accounts, checklist templates, etc.)
   - Recurring task templates
   - Holiday calendar
```

### Plugin Configuration — Tenant-Driven, Not Hardcoded

**Each tenant configures their own plugin data after onboarding.**
No NSG Academy-specific data should be hardcoded in migrations.

| Plugin | What tenant configures | Where stored |
|---|---|---|
| Social Media | Their own platform accounts (Instagram handle, YouTube channel, etc.) | `sm_accounts` table |
| Telecalling | Their own campaigns and lead sources | `tc_campaigns` table |
| Checklist/Ops | Their own checklist templates and vendor list | `cl_templates`, `cl_vendors` tables |

### What SHOULD be seeded in migrations

Only the **first tenant (NSG Academy)** and their plugin enablement. Nothing else is global.

```sql
-- V10: NSG Academy tenant
INSERT INTO tenants (id, name, slug)
VALUES ('00000000-0000-0000-0000-000000000001', 'NSG Academy', 'nsgacademy');

-- V11: Enable all 3 plugins for NSG Academy
INSERT INTO tenant_features (tenant_id, plugin_type, is_enabled, enabled_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'SOCIAL_MEDIA', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000001', 'TELECALLING', TRUE, NOW()),
    ('00000000-0000-0000-0000-000000000001', 'CHECKLIST_OPS', TRUE, NOW());

-- V12: Backfill existing users/employees/tasks to NSG Academy tenant
-- (handled in V12 migration — see Phase 2 detail)
```

> `sm_platforms` is **NOT seeded globally**. It is tenant-specific. NSG Academy admin adds their own platforms (Instagram, YouTube, Telegram etc.) through the UI after logging in.

### What must NOT be seeded

```
❌ NSG Academy's specific social media account handles
❌ NSG Academy's specific checklist templates
❌ NSG Academy's specific batch/course structures
❌ Any tenant-specific business data
```

NSG Academy's admin will configure all of this themselves through the UI after onboarding, exactly like any other tenant would.

### SuperAdmin vs Tenant Admin

| Role | Scope | Can do |
|---|---|---|
| SuperAdmin | Platform-level | Create tenants, enable/disable plugins, view all tenants |
| Tenant Admin (ADMIN role) | Own tenant only | Manage employees, tasks, configure plugins for their tenant |
| Employee | Own tenant only | View/update own tasks, scorecard, notifications |

SuperAdmin is a separate role above the existing ADMIN/EMPLOYEE roles from v2.4. Add `SUPER_ADMIN` to `RoleType` enum in v3.0.

---

## 🔧 Section 9 — Environment & Setup Notes

**Local dev setup order:**
1. PostgreSQL 16 (port 5432)
2. Spring Boot backend (port 8080)
3. Next.js frontend (port 3000)
4. FastAPI ML server (port 8000) — **optional**, app works fully without it (`ml.enabled=false`)
5. Flutter — for mobile development (primary mobile platform)

**ML server setup (when needed):**
```bash
# Step 1 — Train models (only needed once, or after data/algo changes)
cd ml/pipeline
python run_pipeline.py
# Generates synthetic data + trains all 4 models + saves .pkl to inference-server/artifacts/

# Step 2 — Start inference server
cd ml/inference-server
python main.py

# Step 3 — Verify before connecting to Spring Boot
python check_health.py
# Expected: models_loaded: 4

# Step 4 — Enable in Spring Boot
# Set ml.enabled=true in application-dev.properties
```

> ⚠️ React Native setup docs exist in the repo (`docs/setup/06_REACTNATIVE_SETUP.md`) but React Native is **abandoned**. Ignore all React Native docs and setup guides entirely.

**Key property files:**
- `taskhive-backend/src/main/resources/application-dev.properties` — DB password, SMTP, JWT secret
- `taskhive-frontend/.env.local` — API URL
- `ML/inference-server/.env` — ML server config
- `taskhive_flutter/assets/env/.env.dev` — Flutter API URL

**ML server toggle:** `ml.enabled=false` (default) — flip to `true` to enable ML predictions.

**Anthropic Claude API** (Social Media Plugin — Phase 5, v3.3):
```properties
anthropic.api-key=your-key
anthropic.model=claude-sonnet-4-6
anthropic.max-tokens=1000
anthropic.suggestions-cache-hours=6
```
Rate limit: 20 Claude API calls/hour per tenant. Cache responses for 6 hours.

---

---

### Phase 8 — ML/AI Intelligence · Output: **v3.6** (Weeks 19-22)

Goal: Platform becomes intelligent — proactive, data-driven insights. Requires real data from active plugin usage in Phases 5–7.

> ⚠️ Phase 8 has two internal sub-phases. **Deploy P8.1 first, then wait 2-4 weeks** for real data to accumulate before starting P8.2. v2.4 synthetic models continue serving during the wait period.

**Phase 8 internal timeline:**
```
Week 19    → Deploy P8.1 (ML Event Logger) — data collection starts immediately
Weeks 20-21 → Wait — real data accumulates from tasks, scorecards, call logs, checklists
Week 22    → P8.2 (train + deploy new models) + P8.3 (weekly report)
```

#### Task P8.1 — ML Event Logger (Data Collection Foundation)

`V50__create_ml_event_log_table.sql`
```sql
CREATE TABLE ml_event_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants(id),
    event_type  VARCHAR(100) NOT NULL,
    -- event_type: TASK_COMPLETED, TASK_LATE, TASK_SKIPPED, CALL_CONVERTED, etc.
    entity_type VARCHAR(50),
    -- entity_type: TASK, LEAD, CHECKLIST, etc.
    entity_id   UUID,
    employee_id UUID REFERENCES employees(id),
    payload     JSONB NOT NULL,
    -- payload: structured feature data for ML training
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ml_event_log_event_type  ON ml_event_log(event_type);
CREATE INDEX idx_ml_event_log_employee_id ON ml_event_log(employee_id);
CREATE INDEX idx_ml_event_log_created_at  ON ml_event_log(created_at);
CREATE INDEX idx_ml_event_log_payload     ON ml_event_log USING GIN(payload);
```

New shared kernel file: `common/ml/MlEventLogger.java`
- Listens to ALL domain events via `@EventListener`
- Logs structured feature data to `ml_event_log` as `payload`
- Non-blocking — never throw, always catch exceptions internally

> Deploy this first in Phase 8 and let data accumulate for at least 2-4 weeks before training models.

#### Task P8.2 — New ML Models

| Model | Algorithm | Data source |
|---|---|---|
| Anomaly Detection | Isolation Forest | `daily_scorecards` patterns |
| Late Submission Risk | Logistic Regression | `tasks` + `task_status_history` |
| Lead Scoring | XGBoost | `tc_call_logs` outcomes |

Retrain existing 4 v2.4 models on real data (replace synthetic).

New ML folder additions:
```
ml/
├── training/
│   ├── data/
│   │   ├── anomaly_data.csv
│   │   ├── late_risk_data.csv
│   │   └── lead_scoring_data.csv
│   ├── training_generator/
│   │   ├── generate_anomaly_data.py
│   │   ├── generate_late_risk_data.py
│   │   └── generate_lead_scoring_data.py
│   └── scripts/
│       ├── train_anomaly.py
│       ├── train_late_risk.py
│       └── train_lead_scoring.py
└── inference-server/
    ├── artifacts/
    │   ├── anomaly_model.pkl
    │   ├── late_risk_model.pkl
    │   └── lead_scoring_model.pkl
    └── api/routes/
        ├── anomaly_detection.py
        ├── late_risk.py
        └── lead_scoring.py
```

New Spring Boot endpoints:
```
POST /api/v1/ml/detect/anomaly
GET  /api/v1/ml/predict/late-risk/{taskId}
POST /api/v1/ml/score/lead/{leadId}
```

#### Task P8.3 — Weekly Owner Report (Claude API)

New file: `module/analytics/service/WeeklyReportService.java`
- Scheduled every Sunday 10 PM: `@Scheduled(cron = "0 0 22 ? * SUN")`
- Pulls week's performance data per tenant
- Calls Claude API → natural language summary
- Emails all ADMIN users of each tenant
- Rate limit: 1 call per tenant per week

```properties
anthropic.api-key=your-key
anthropic.model=claude-sonnet-4-6
anthropic.max-tokens=1000
app.weekly-report.enabled=true
app.weekly-report.cron=0 0 22 ? * SUN
```

**✅ Phase 8 Complete = TaskHive v3.6**
Platform is now intelligent and proactive.

---

### Phase 9 — Flutter Mobile · Output: **v3.7** (Weeks 23-26)

Goal: Flutter mobile app updated with all v3.x features. Separate detailed phase documents will be created for Flutter (same format as v2.4 Flutter phase docs).

#### Scope of Flutter v3.7 updates

| Feature | v3.x source | Flutter work needed |
|---|---|---|
| PENDING_APPROVAL status | Phase 1 | New status badge + approval actions for admin |
| Late flag indicator | Phase 1 | Red "LATE" badge on task cards |
| Proof upload | Phase 1 | File picker + upload before submission |
| Custom task types + fields | Phase 3 | Dynamic form renderer |
| Recurring task display | Phase 3 | Badge showing "recurring" on task cards |
| Task handover | Phase 4 | Handover request form + pending list |
| Daily scorecard | Phase 4 | Scorecard widget on employee dashboard |
| Streak badge | Phase 4 | Streak counter on profile |
| Leaderboard | Phase 4 | Leaderboard screen |
| Social Media Plugin | Phase 5 | Social task creation with platform/account picker |
| Checklist completion | Phase 6 | Daily checklist screen + photo upload |
| Call logging | Phase 7 | Quick call log form for telecallers |
| ML suggestions | Phase 8 | Priority suggestion + productivity score |

> **SuperAdmin — web only.** SuperAdmin dashboard (tenant management) is a web-only feature. No Flutter screens needed for SuperAdmin. SuperAdmin users are not expected to use the mobile app.

> **Alag Flutter phase documents:** Phase 9 ke liye v2.4 Flutter phase docs jaisi dedicated files banegi — `TaskHive_Flutter_v3_Phase9.md`. Same format: phase summary, deliverable, implementation steps, expected bugs.

**✅ Phase 9 Complete = TaskHive v3.7**
Full platform — web + mobile — all features live.

---

## ✅ Section 10 — Phase-wise Completion Checklists

### Phase 1 (v2.5) Checklist

- [ ] `proof_required = true` tasks cannot be submitted without PROOF attachment
- [ ] PENDING_APPROVAL status exists in enum + DB migration applied
- [ ] Admin can approve and reject PENDING_APPROVAL tasks
- [ ] Rejected tasks return to IN_REVIEW with rejection reason
- [ ] Cancel without reason returns 400 (not 500)
- [ ] `is_late` flag set automatically when task submitted after `due_date`
- [ ] `late_by_minutes` calculated correctly
- [ ] `is_late` is immutable once set — cannot be cleared
- [ ] `GET /api/v1/tasks/late` returns correct list
- [ ] Today's overview panel visible on admin dashboard
- [ ] Auto-refresh every 5 minutes on overview panel
- [ ] NSG Academy deployed to production ✅

### Phase 2 (v3.0) Checklist

- [ ] `tenants` table created, NSG Academy seeded
- [ ] `tenant_features` table created, all 3 plugins enabled for NSG
- [ ] `tenant_id` added to ALL core tables with backfill
- [ ] No existing NSG data lost after migration
- [ ] `TenantContext` resolves correct tenant per request
- [ ] Cross-tenant data leak test: user from tenant A cannot see tenant B data
- [ ] `@RequiresPlugin` returns 403 for disabled plugin
- [ ] Tenant config API working (GET/PUT)
- [ ] `SUPER_ADMIN` role added to `RoleType` enum
- [ ] SuperAdmin can create new tenant via API

### Phase 3 (v3.1) Checklist

- [ ] Admin can create custom task types with fields
- [ ] Custom fields render correctly in task form (all 7 types: TEXT, NUMBER, URL, SELECT, DATE, BOOLEAN, REFERENCE)
- [ ] Custom field values saved as JSONB and retrieved correctly
- [ ] Recurring task template creates task instances on correct schedule
- [ ] Recurring scheduler runs at 6 AM and doesn't duplicate if already generated today
- [ ] Task types are tenant-isolated — Tenant A's types not visible to Tenant B

### Phase 4 (v3.2) Checklist

- [ ] Employee can request task handover with reason
- [ ] Admin sees pending handovers list
- [ ] On handover approve: `assigned_to` updates correctly
- [ ] Daily scorecard calculates correctly nightly
- [ ] Late penalty (-5) and skip penalty (-2) applied correctly
- [ ] Streak resets to 0 on incomplete day
- [ ] Leaderboard shows correct top 10 weekly/monthly
- [ ] Broadcast reaches all employees in tenant via WebSocket
- [ ] Broadcast with `send_email=true` sends email

### Phase 5 (v3.3) Checklist

- [ ] `@RequiresPlugin(SOCIAL_MEDIA)` returns 403 for tenants without plugin
- [ ] Platform CRUD working
- [ ] Account CRUD working, accounts scoped to tenant
- [ ] Social media task type auto-created for tenant on plugin enable
- [ ] Proof required before approval on social media tasks
- [ ] Content calendar displays correctly in FullCalendar
- [ ] Asset library upload + search working
- [ ] Claude content suggestions return with fallback when API unavailable
- [ ] Festival reminders fire 30 days ahead

### Phase 6 (v3.4) Checklist

- [ ] Checklist template builder saves items with correct types
- [ ] Daily instances generated at 8 AM
- [ ] Critical item miss triggers immediate admin notification
- [ ] PHOTO_REQUIRED items cannot be completed without photo
- [ ] Vendor CRUD working
- [ ] Petty expense approve/reject flow working
- [ ] Holiday blocks recurring task generation for that day
- [ ] Leave approved → employee's tasks flagged for reassignment

### Phase 7 (v3.5) Checklist

- [ ] Campaign CRUD working
- [ ] Excel import parses leads correctly with column mapping
- [ ] Call log saves with correct disposition
- [ ] `CALLBACK_REQUESTED` auto-creates follow-up task
- [ ] Lead status flow: NEW → ENROLLED/LOST
- [ ] Campaign dashboard shows correct metrics
- [ ] Export generates correct Excel/PDF

### Phase 8 (v3.6) Checklist

- [ ] `ml_event_log` table created and populating from domain events
- [ ] Anomaly detection model trained and serving
- [ ] Late risk endpoint returns valid risk score
- [ ] Lead scoring endpoint returns probability
- [ ] Weekly report email sends every Sunday
- [ ] Circuit breaker activates when ML server down — no API failures
- [ ] All 4 v2.4 models retrained on real data

### Phase 9 (v3.7) Checklist

- [ ] All Phase 1 features visible in Flutter (late badge, proof upload, approval actions)
- [ ] Custom task type fields render correctly on mobile
- [ ] Scorecard + streak visible on employee dashboard
- [ ] Leaderboard screen working
- [ ] Social media task creation with platform/account picker
- [ ] Daily checklist completion with photo upload
- [ ] Call log quick form working for telecallers

---

**Document Version:** 5.0 — FINAL
**Created:** March 2026
**Last Updated:** March 2026
**Owner:** Digiwork Engineering
**Classification:** Internal — Developer Context
**Covers:** TaskHive v2.4 → v3.7 (9 phases)
