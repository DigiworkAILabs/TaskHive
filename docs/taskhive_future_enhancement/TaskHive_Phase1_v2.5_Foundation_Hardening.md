# TaskHive — Phase 1: Foundation Hardening
## SRS v2.5 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 1 of 9 |
| **Version** | v2.5 |
| **Name** | Foundation Hardening |
| **Modules** | Task Module (extended) + Analytics Module (extended) |
| **DB Migrations** | V8, V9 |
| **Duration** | ~1 week |
| **Depends On** | v2.4 fully complete (all 7 phases) |
| **Next Phase** | Phase 2 — Multi-Tenant Foundation (v3.0) |

### Deliverable
5 partial/missing requirements from v2.4 are fully hardened at the API level. Proof upload is enforced before task submission. Approval workflow is formalized with a `PENDING_APPROVAL` status and approve/reject endpoints. Task cancellation requires a mandatory reason. Late submissions are automatically flagged with exact minutes. Admin dashboard has a live today's overview panel with rule-based anomaly detection. After this phase, NSG Academy goes live on TaskHive v2.5.

---

## What Changes in This Phase

| Task | Area | Change |
|---|---|---|
| P1.1 | Task submission | Block IN_REVIEW if proof missing and task type requires it |
| P1.2 | Task approval | Formalize PENDING_APPROVAL status, approve/reject endpoints |
| P1.3 | Task cancellation | Return 400 if reason is missing on CANCELLED |
| P1.4 | Late tracking | Auto-set `is_late`, `late_by_minutes`, `submitted_at` on IN_REVIEW |
| P1.5 | Admin dashboard | Today's overview panel + rule-based anomaly detection |

## What Does NOT Change in This Phase

- Auth module — untouched
- Employee module — untouched
- Notification module — untouched (approval emails added here but module structure unchanged)
- Audit module — untouched
- ML module — untouched
- Flutter — untouched (Flutter updates in Phase 9)
- Multi-tenant — NOT introduced (Phase 2)
- Custom task types — NOT introduced (Phase 3)

---

## Updated Task Status Flow

```
TODO → IN_PROGRESS → IN_REVIEW ──────────────────── → DONE
           ↓               ↓                               ↓
        CANCELLED      CANCELLED    (if approval_required)  ↓
                                          ↓                 ↓
                                   PENDING_APPROVAL ────────┘
                                          ↓
                                     CANCELLED (reason mandatory)
                                          ↓
                                    REJECTED → back to IN_REVIEW
```

**PENDING_APPROVAL triggers when:**
- Employee moves task to IN_REVIEW
- `task.approval_required = true` (copied from task type at task creation)
- AND proof is present if `task.proof_required = true`
- System auto-advances status to PENDING_APPROVAL

**PENDING_APPROVAL does NOT trigger when:**
- Task has no task type (`task_type_id = null` — legacy v2.4 tasks)
- `task.approval_required = false`

---

## Functional Requirements

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-P1-01 | Critical | If `task.proof_required = true`, employee cannot move task to IN_REVIEW without at least one `task_attachment` with `attachment_purpose = PROOF` — API returns 400 |
| FR-P1-02 | Critical | Error code for missing proof: `TASK_3003` |
| FR-P1-03 | Critical | If `task.approval_required = true`, task auto-moves to `PENDING_APPROVAL` when employee submits to IN_REVIEW |
| FR-P1-04 | Critical | ADMIN can approve task: `PENDING_APPROVAL → DONE`, sets `completed_at = now()` |
| FR-P1-05 | Critical | ADMIN can reject task: `PENDING_APPROVAL → IN_REVIEW`, rejection reason mandatory, stored in `task_status_history.comment` |
| FR-P1-06 | Critical | Task CANCELLATION requires non-empty `reason` — API returns 400 if missing. Error code: `TASK_3004` |
| FR-P1-07 | High | `submitted_at` timestamp set when task moves to IN_REVIEW |
| FR-P1-08 | High | `is_late = true` + `late_by_minutes` set when `submitted_at > due_date` |
| FR-P1-09 | High | `is_late` is IMMUTABLE once set — cannot be cleared by any endpoint |
| FR-P1-10 | High | `LateSubmissionScheduler` runs hourly as safety net — catches any IN_PROGRESS/IN_REVIEW tasks past due_date with `is_late = false` |
| FR-P1-11 | High | `GET /api/v1/tasks/late` returns all tasks with `is_late = true` (ADMIN only) |
| FR-P1-12 | High | Admin dashboard shows today's overview: assigned, completed, in_progress, pending_approval, overdue, late_submissions, active_anomalies |
| FR-P1-13 | Medium | Today's overview auto-refreshes every 5 minutes on frontend |
| FR-P1-14 | Medium | Rule-based anomaly detection runs nightly — 3 rules (see P1.5 detail) |
| FR-P1-15 | Medium | `GET /api/v1/analytics/anomalies` returns active anomaly alerts (ADMIN only) |
| FR-P1-16 | Medium | `GET /api/v1/analytics/tasks/missed` returns tasks cancelled/skipped with reasons (ADMIN only) |
| FR-P1-17 | Medium | `GET /api/v1/analytics/tasks/late` returns late submission patterns (ADMIN only) |
| FR-P1-18 | Low | `TaskResponse` includes `isLate`, `lateByMinutes`, `submittedAt` fields |

---

## New API Endpoints

| Method | Path | Role | Description |
|--------|------|------|-------------|
| PATCH | `/api/v1/tasks/{id}/approve` | ADMIN | Approve PENDING_APPROVAL task → DONE |
| PATCH | `/api/v1/tasks/{id}/reject` | ADMIN | Reject PENDING_APPROVAL task → IN_REVIEW |
| GET | `/api/v1/tasks/late` | ADMIN | List all late tasks |
| GET | `/api/v1/analytics/dashboard/today` | ADMIN | Today's overview stats |
| GET | `/api/v1/analytics/anomalies` | ADMIN | Active anomaly alerts |
| GET | `/api/v1/analytics/tasks/missed` | ADMIN | Missed/cancelled tasks with reasons |
| GET | `/api/v1/analytics/tasks/late` | ADMIN | Late submission patterns |

---

## DB Migrations

### V8__alter_task_status_add_pending_approval.sql

```sql
-- Add PENDING_APPROVAL to the task_status enum
ALTER TYPE task_status ADD VALUE IF NOT EXISTS 'PENDING_APPROVAL';
```

> ⚠️ PostgreSQL `ALTER TYPE ... ADD VALUE` cannot run inside a transaction. Flyway handles this automatically — do NOT wrap in BEGIN/COMMIT manually.

---

### V9__alter_tasks_add_late_and_analytics_columns.sql

```sql
-- tasks table: late tracking + submission timestamp
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS is_late          BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS late_by_minutes  INTEGER;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS submitted_at     TIMESTAMP;

-- task_attachments: add purpose field for proof enforcement
ALTER TABLE task_attachments ADD COLUMN IF NOT EXISTS attachment_purpose VARCHAR(50) DEFAULT 'GENERAL';
-- attachment_purpose values: PROOF, GENERAL

-- daily_metrics: late_tasks column (needed by AnomalyDetectionService)
ALTER TABLE daily_metrics ADD COLUMN IF NOT EXISTS late_tasks INTEGER DEFAULT 0;

-- employee_performance_cache: tasks_late column
ALTER TABLE employee_performance_cache ADD COLUMN IF NOT EXISTS tasks_late INTEGER DEFAULT 0;

-- Index for late task queries (partial index — only indexes true rows)
CREATE INDEX IF NOT EXISTS idx_tasks_is_late ON tasks(is_late) WHERE is_late = TRUE;
```

---

## Backend — New Files

### `module/task/enums/AttachmentPurpose.java`

```java
package com.digiwork.taskhive.module.task.enums;

public enum AttachmentPurpose {
    PROOF,    // Mandatory proof of task completion
    GENERAL   // Any other attachment
}
```

### `module/task/exception/ProofEnforcementException.java`

```java
package com.digiwork.taskhive.module.task.exception;

import com.digiwork.taskhive.common.exception.BusinessException;

public class ProofEnforcementException extends BusinessException {
    public ProofEnforcementException() {
        super("TASK_3003", "Proof attachment required before submitting this task type");
    }
}
```

### `module/task/service/TaskApprovalService.java`

```java
package com.digiwork.taskhive.module.task.service;

import java.util.UUID;

public interface TaskApprovalService {
    TaskResponse approveTask(UUID taskId, UUID adminUserId);
    TaskResponse rejectTask(UUID taskId, UUID adminUserId, String rejectionReason);
}
```

**Implementation rules:**
- `approveTask`: task must be in `PENDING_APPROVAL` — else throw `TASK_3002`. Set `status = DONE`, `completed_at = now()`. Publish `TaskApprovedEvent`.
- `rejectTask`: task must be in `PENDING_APPROVAL`. `rejectionReason` must not be blank. Set `status = IN_REVIEW`, store reason in `task_status_history.comment`. Publish `TaskRejectedEvent`.

### `module/task/dto/TaskApprovalRequest.java`

```java
package com.digiwork.taskhive.module.task.dto;

public class TaskApprovalRequest {
    private String reason; // mandatory for rejection, optional for approval
}
```

### `module/task/scheduler/LateSubmissionScheduler.java`

```java
package com.digiwork.taskhive.module.task.scheduler;

@Component
public class LateSubmissionScheduler {

    // Runs every hour — safety net for late flag
    @Scheduled(cron = "0 0 * * * *")
    public void flagLateSubmissions() {
        // Find tasks where:
        //   status IN ('IN_PROGRESS', 'IN_REVIEW')
        //   AND due_date < NOW()
        //   AND is_late = false
        // For each: set is_late = true, late_by_minutes = MINUTES_BETWEEN(due_date, NOW())
        // is_late is immutable — never run this on tasks already flagged
    }
}
```

### `module/analytics/service/TodayOverviewService.java`

```java
package com.digiwork.taskhive.module.analytics.service;

public interface TodayOverviewService {
    TodayOverviewResponse getTodayStats();
}
```

### `module/analytics/service/AnomalyDetectionService.java`

```java
package com.digiwork.taskhive.module.analytics.service;

public interface AnomalyDetectionService {
    void runNightlyDetection(); // called by MetricsAggregatorService scheduler
    List<AnomalyAlertResponse> getActiveAlerts();
}
```

**Rule-based anomaly detection (no ML — Phase 1):**

| Rule | Condition | Alert fired |
|---|---|---|
| Rule 1 | Total completions today < 60% of 7-day average | Yes |
| Rule 2 | A recurring task type has 0 completions for 2+ consecutive days | Yes |
| Rule 3 | An employee has 3+ consecutive days of missed tasks | Yes |

### `module/analytics/dto/TodayOverviewResponse.java`

```java
package com.digiwork.taskhive.module.analytics.dto;

public class TodayOverviewResponse {
    private int totalAssignedToday;
    private int completedToday;
    private int inProgress;
    private int pendingApproval;
    private int overdueToday;
    private int lateSubmissionsToday;
    private int activeAnomalies;
}
```

---

## Backend — Modified Files

### `module/task/model/Task.java`

Add three new fields:

```java
@Column(name = "is_late", nullable = false)
private boolean isLate = false;

@Column(name = "late_by_minutes")
private Integer lateByMinutes;

@Column(name = "submitted_at")
private LocalDateTime submittedAt;
```

### `module/task/model/TaskAttachment.java`

Add one new field:

```java
@Enumerated(EnumType.STRING)
@Column(name = "attachment_purpose")
private AttachmentPurpose attachmentPurpose = AttachmentPurpose.GENERAL;
```

### `module/task/enums/TaskStatus.java`

Add `PENDING_APPROVAL`:

```java
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    PENDING_APPROVAL,  // NEW
    DONE,
    CANCELLED
}
```

### `module/task/service/TaskService.java`

Four changes inside `updateTaskStatus()`:

**Change 1 — Mandatory cancel reason:**
```java
private void validateCancelReason(TaskStatus newStatus, String reason) {
    if (newStatus == TaskStatus.CANCELLED) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("TASK_3004",
                "Reason is mandatory when cancelling a task");
        }
    }
}
```

**Change 2 — Proof enforcement:**
```java
private void enforceProofIfRequired(Task task, TaskStatus newStatus) {
    if (newStatus == TaskStatus.IN_REVIEW && task.isProofRequired()) {
        boolean hasProof = attachmentRepository
            .existsByTaskIdAndAttachmentPurpose(task.getId(), AttachmentPurpose.PROOF);
        if (!hasProof) {
            throw new ProofEnforcementException();
        }
    }
}
```

> ⚠️ Read `proof_required` from `task.isProofRequired()` — NOT from the task type. The field is snapshotted at task creation time. Task type may change later but existing tasks are unaffected.

**Change 3 — submitted_at + late flag:**
```java
private void handleSubmission(Task task, TaskStatus newStatus) {
    if (newStatus == TaskStatus.IN_REVIEW) {
        task.setSubmittedAt(LocalDateTime.now());

        // Late flag — immutable once set
        if (!task.isLate() && task.getDueDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(task.getDueDate())) {
                task.setIsLate(true);
                long minutes = ChronoUnit.MINUTES.between(task.getDueDate(), now);
                task.setLateByMinutes((int) minutes);
            }
        }
    }
}
```

**Change 4 — Auto-advance to PENDING_APPROVAL:**
```java
private TaskStatus resolveActualStatus(Task task, TaskStatus requestedStatus) {
    if (requestedStatus == TaskStatus.IN_REVIEW && task.isApprovalRequired()) {
        return TaskStatus.PENDING_APPROVAL;
    }
    return requestedStatus;
}
```

**Full call order inside `updateTaskStatus()`:**
```java
validateCancelReason(newStatus, request.getReason());
enforceProofIfRequired(task, newStatus);
handleSubmission(task, newStatus);
TaskStatus actualStatus = resolveActualStatus(task, newStatus);
saveStatusHistory(task, actualStatus, request.getReason(), currentUserId);
task.setStatus(actualStatus);
if (actualStatus == TaskStatus.DONE) task.setCompletedAt(LocalDateTime.now());
taskRepository.save(task);
publishStatusChangeEvent(task, actualStatus);
```

### `module/task/controller/TaskController.java`

Add 3 new endpoints:

```java
// Approve task — ADMIN only
@PatchMapping("/{id}/approve")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<TaskResponse>> approveTask(
        @PathVariable UUID id,
        @RequestBody(required = false) TaskApprovalRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        taskApprovalService.approveTask(id, SecurityUtils.getCurrentUserId())
    ));
}

// Reject task — ADMIN only, reason required
@PatchMapping("/{id}/reject")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<TaskResponse>> rejectTask(
        @PathVariable UUID id,
        @Valid @RequestBody TaskApprovalRequest request) {
    return ResponseEntity.ok(ApiResponse.success(
        taskApprovalService.rejectTask(id, SecurityUtils.getCurrentUserId(), request.getReason())
    ));
}

// Late tasks — ADMIN only
@GetMapping("/late")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Page<TaskListResponse>>> getLateTasks(
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(
        taskService.getLateTasks(pageable)
    ));
}
```

### `module/task/dto/TaskResponse.java`

Add three new fields:

```java
private boolean isLate;
private Integer lateByMinutes;
private LocalDateTime submittedAt;
```

### `module/analytics/controller/AnalyticsController.java`

Add 4 new endpoints:

```java
@GetMapping("/dashboard/today")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<TodayOverviewResponse>> getTodayOverview() {
    return ResponseEntity.ok(ApiResponse.success(todayOverviewService.getTodayStats()));
}

@GetMapping("/anomalies")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<List<AnomalyAlertResponse>>> getAnomalies() {
    return ResponseEntity.ok(ApiResponse.success(anomalyDetectionService.getActiveAlerts()));
}

@GetMapping("/tasks/missed")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Page<TaskListResponse>>> getMissedTasks(
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(analyticsService.getMissedTasks(pageable)));
}

@GetMapping("/tasks/late")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Page<TaskListResponse>>> getLatePatterns(
        @PageableDefault(size = 20) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(analyticsService.getLatePatterns(pageable)));
}
```

### `module/task/repository/TaskAttachmentRepository.java`

Add one new method:

```java
boolean existsByTaskIdAndAttachmentPurpose(UUID taskId, AttachmentPurpose purpose);
```

---

## Events Published

| Event | Trigger | Consumer |
|---|---|---|
| `TaskApprovedEvent` | Admin approves task | NotificationModule → in-app + email to assignee |
| `TaskRejectedEvent` | Admin rejects task | NotificationModule → in-app + email to assignee |

### `module/task/event/TaskApprovedEvent.java`

```java
public class TaskApprovedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID assigneeUserId;
    private final UUID approvedByUserId;
}
```

### `module/task/event/TaskRejectedEvent.java`

```java
public class TaskRejectedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID assigneeUserId;
    private final String rejectionReason;
}
```

---

## Email Templates (New in This Phase)

Add to `src/main/resources/templates/email/`:

| File | Trigger |
|---|---|
| `task-approved.html` | `TaskApprovedEvent` → sent to task assignee |
| `task-rejected.html` | `TaskRejectedEvent` → sent to task assignee with rejection reason |

---

## Backend File & Folder Structure (Changes Only)

```
src/
└── main/
    ├── java/
    │   └── com/digiwork/taskhive/
    │       └── module/
    │           ├── task/
    │           │   ├── controller/
    │           │   │   └── TaskController.java               MODIFIED — 3 new endpoints
    │           │   ├── service/
    │           │   │   ├── TaskService.java                  MODIFIED — 4 new private methods
    │           │   │   └── TaskApprovalService.java          NEW
    │           │   ├── dto/
    │           │   │   ├── TaskResponse.java                 MODIFIED — isLate, lateByMinutes, submittedAt
    │           │   │   └── TaskApprovalRequest.java          NEW
    │           │   ├── model/
    │           │   │   ├── Task.java                         MODIFIED — isLate, lateByMinutes, submittedAt
    │           │   │   └── TaskAttachment.java               MODIFIED — attachmentPurpose
    │           │   ├── enums/
    │           │   │   ├── TaskStatus.java                   MODIFIED — PENDING_APPROVAL added
    │           │   │   └── AttachmentPurpose.java            NEW
    │           │   ├── event/
    │           │   │   ├── TaskApprovedEvent.java            NEW
    │           │   │   └── TaskRejectedEvent.java            NEW
    │           │   ├── exception/
    │           │   │   └── ProofEnforcementException.java    NEW
    │           │   ├── repository/
    │           │   │   └── TaskAttachmentRepository.java     MODIFIED — existsByTaskIdAndPurpose
    │           │   └── scheduler/
    │           │       └── LateSubmissionScheduler.java      NEW
    │           │
    │           └── analytics/
    │               ├── controller/
    │               │   └── AnalyticsController.java          MODIFIED — 4 new endpoints
    │               ├── service/
    │               │   ├── TodayOverviewService.java         NEW
    │               │   └── AnomalyDetectionService.java      NEW
    │               └── dto/
    │                   └── TodayOverviewResponse.java        NEW
    │
    └── resources/
        ├── db/migration/
        │   ├── V8__alter_task_status_add_pending_approval.sql   NEW
        │   └── V9__alter_tasks_add_late_and_analytics_columns.sql  NEW
        └── templates/email/
            ├── task-approved.html                               NEW
            └── task-rejected.html                               NEW
```

---

## Request / Response Examples

### Approve Task

**Request:** `PATCH /api/v1/tasks/{id}/approve`
```json
{}
```
**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "status": "DONE",
    "completedAt": "2026-03-19T14:30:00"
  }
}
```
**Error `409` — task not in PENDING_APPROVAL:**
```json
{ "success": false, "errorCode": "TASK_3002", "message": "Invalid status transition" }
```

---

### Reject Task

**Request:** `PATCH /api/v1/tasks/{id}/reject`
```json
{
  "reason": "Screenshot is blurry, please upload a clear proof"
}
```
**Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "status": "IN_REVIEW"
  }
}
```

---

### Today's Overview

**Request:** `GET /api/v1/analytics/dashboard/today`

**Response `200`:**
```json
{
  "success": true,
  "data": {
    "totalAssignedToday": 24,
    "completedToday": 12,
    "inProgress": 6,
    "pendingApproval": 3,
    "overdueToday": 2,
    "lateSubmissionsToday": 1,
    "activeAnomalies": 0
  }
}
```

---

### Submit Without Proof (Error)

**Request:** `PATCH /api/v1/tasks/{id}/status`
```json
{ "status": "IN_REVIEW" }
```
**Response `400` — when `proof_required = true` and no PROOF attachment:**
```json
{
  "success": false,
  "errorCode": "TASK_3003",
  "message": "Proof attachment required before submitting this task type"
}
```

---

### Cancel Without Reason (Error)

**Request:** `PATCH /api/v1/tasks/{id}/status`
```json
{ "status": "CANCELLED" }
```
**Response `400`:**
```json
{
  "success": false,
  "errorCode": "TASK_3004",
  "message": "Reason is mandatory when cancelling a task"
}
```

---

## Frontend — New Files

### `features/task/components/ProofUploadSection.tsx`

```tsx
interface Props {
  taskId: string;
  proofRequired: boolean;
  hasProof: boolean;
  onProofUploaded: () => void;
}

export const ProofUploadSection = ({ taskId, proofRequired, hasProof, onProofUploaded }: Props) => {
  if (!proofRequired) return null;

  return (
    <div className="rounded-lg border border-amber-200 bg-amber-50 p-4">
      <p className="text-sm font-medium text-amber-800 mb-2">
        Proof required before submission
      </p>
      {!hasProof ? (
        <>
          <p className="text-xs text-amber-600 mb-3">
            Upload a screenshot or photo as proof of completion
          </p>
          <FileUploadButton
            taskId={taskId}
            purpose="PROOF"
            onSuccess={onProofUploaded}
            accept="image/*,application/pdf"
          />
        </>
      ) : (
        <p className="text-xs text-green-700 flex items-center gap-1">
          ✓ Proof uploaded — ready to submit
        </p>
      )}
    </div>
  );
};
```

### `features/task/components/TaskApprovalPanel.tsx`

```tsx
// Shown to ADMIN only when task.status === 'PENDING_APPROVAL'
interface Props {
  taskId: string;
  onApproved: () => void;
  onRejected: () => void;
}

export const TaskApprovalPanel = ({ taskId, onApproved, onRejected }: Props) => {
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const { mutate: approve, isPending: approving } = useApproveTask();
  const { mutate: reject, isPending: rejecting } = useRejectTask();

  return (
    <div className="rounded-lg border border-blue-200 bg-blue-50 p-4">
      <p className="text-sm font-semibold text-blue-800 mb-3">
        This task is pending your approval
      </p>
      <div className="flex gap-2">
        <Button onClick={() => approve(taskId, { onSuccess: onApproved })} disabled={approving}>
          Approve
        </Button>
        <Button variant="outline" onClick={() => setShowRejectForm(true)}>
          Reject
        </Button>
      </div>

      {showRejectForm && (
        <div className="mt-3 space-y-2">
          <Textarea
            placeholder="Reason for rejection (required)"
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
          />
          <Button
            variant="destructive"
            disabled={!rejectReason.trim() || rejecting}
            onClick={() => reject({ taskId, reason: rejectReason }, { onSuccess: onRejected })}
          >
            Confirm Reject
          </Button>
        </div>
      )}
    </div>
  );
};
```

### `features/task/components/CancelReasonModal.tsx`

```tsx
interface Props {
  open: boolean;
  onConfirm: (reason: string) => void;
  onClose: () => void;
}

export const CancelReasonModal = ({ open, onConfirm, onClose }: Props) => {
  const [reason, setReason] = useState('');

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Cancel Task</DialogTitle>
        </DialogHeader>
        <p className="text-sm text-muted-foreground">
          Please provide a reason for cancelling this task.
        </p>
        <Textarea
          placeholder="Reason (required)"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          className="min-h-[80px]"
        />
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Back</Button>
          <Button
            variant="destructive"
            disabled={!reason.trim()}
            onClick={() => { onConfirm(reason); onClose(); }}
          >
            Cancel Task
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
```

### `features/analytics/components/TodayOverviewPanel.tsx`

```tsx
export const TodayOverviewPanel = () => {
  const { data, isLoading } = useTodayOverview();

  const stats = [
    { label: 'Assigned Today', value: data?.totalAssignedToday, color: 'text-blue-600', bg: 'bg-blue-50' },
    { label: 'Completed', value: data?.completedToday, color: 'text-green-600', bg: 'bg-green-50' },
    { label: 'In Progress', value: data?.inProgress, color: 'text-amber-600', bg: 'bg-amber-50' },
    { label: 'Pending Approval', value: data?.pendingApproval, color: 'text-purple-600', bg: 'bg-purple-50' },
    { label: 'Overdue', value: data?.overdueToday, color: 'text-red-600', bg: 'bg-red-50' },
    { label: 'Late Submissions', value: data?.lateSubmissionsToday, color: 'text-orange-600', bg: 'bg-orange-50' },
  ];

  return (
    <div className="mb-6 space-y-3">
      {(data?.activeAnomalies ?? 0) > 0 && (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
          ⚠️ {data?.activeAnomalies} active anomaly alert(s) — check Analytics for details
        </div>
      )}
      <div className="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-6">
        {stats.map((stat) => (
          <div key={stat.label} className={`rounded-lg p-3 ${stat.bg}`}>
            <p className={`text-2xl font-bold ${stat.color}`}>
              {isLoading ? '—' : (stat.value ?? 0)}
            </p>
            <p className="mt-1 text-xs text-muted-foreground">{stat.label}</p>
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## Frontend — New Hooks

### `features/task/hooks/useApproveTask.ts`

```ts
export const useApproveTask = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (taskId: string) => api.patch(`/tasks/${taskId}/approve`),
    onSuccess: (_, taskId) => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Task approved');
    },
    onError: () => toast.error('Failed to approve task'),
  });
};
```

### `features/task/hooks/useRejectTask.ts`

```ts
export const useRejectTask = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, reason }: { taskId: string; reason: string }) =>
      api.patch(`/tasks/${taskId}/reject`, { reason }),
    onSuccess: (_, { taskId }) => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      toast.success('Task sent back for revision');
    },
    onError: () => toast.error('Failed to reject task'),
  });
};
```

### `features/analytics/hooks/useTodayOverview.ts`

```ts
export const useTodayOverview = () => {
  return useQuery({
    queryKey: ['analytics', 'today'],
    queryFn: () => api.get('/analytics/dashboard/today').then(r => r.data),
    refetchInterval: 5 * 60 * 1000,  // 5 minutes
    staleTime: 4 * 60 * 1000,
  });
};
```

---

## Frontend — Modified Files

### `features/task/components/TaskStatusBadge.tsx`

Add `PENDING_APPROVAL` case:

```tsx
case 'PENDING_APPROVAL':
  return (
    <Badge className="border-purple-200 bg-purple-100 text-purple-800">
      Pending Approval
    </Badge>
  );
```

### `features/task/components/TaskCard.tsx`

Add late badge next to status badge:

```tsx
{task.isLate && (
  <Badge variant="destructive" className="text-xs">
    LATE
  </Badge>
)}
```

### `features/task/components/TaskDetail.tsx`

Four additions:

**1 — Proof upload section (above submit button):**
```tsx
<ProofUploadSection
  taskId={task.id}
  proofRequired={task.proofRequired}
  hasProof={task.attachments.some(a => a.purpose === 'PROOF')}
  onProofUploaded={refetch}
/>
```

**2 — Approval panel (ADMIN only, when PENDING_APPROVAL):**
```tsx
{isAdmin && task.status === 'PENDING_APPROVAL' && (
  <TaskApprovalPanel taskId={task.id} onApproved={refetch} onRejected={refetch} />
)}
```

**3 — Late submission banner:**
```tsx
{task.isLate && (
  <div className="flex items-center gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
    <span>⏰</span>
    <span>
      Submitted late by{' '}
      {task.lateByMinutes >= 60
        ? `${Math.floor(task.lateByMinutes / 60)}h ${task.lateByMinutes % 60}m`
        : `${task.lateByMinutes}m`}
    </span>
  </div>
)}
```

**4 — Cancel button shows modal before API call:**
```tsx
const [showCancelModal, setShowCancelModal] = useState(false);

// Replace direct cancel call with:
<Button
  variant="outline"
  className="text-red-600 border-red-200"
  onClick={() => setShowCancelModal(true)}
>
  Cancel Task
</Button>

<CancelReasonModal
  open={showCancelModal}
  onConfirm={(reason) => updateStatus({ status: 'CANCELLED', reason })}
  onClose={() => setShowCancelModal(false)}
/>
```

### `app/(admin)/dashboard/page.tsx`

Add `TodayOverviewPanel` at top of page:

```tsx
import { TodayOverviewPanel } from '@/features/analytics/components/TodayOverviewPanel';

export default function AdminDashboardPage() {
  return (
    <div>
      <TodayOverviewPanel />
      {/* existing dashboard content */}
    </div>
  );
}
```

---

## Frontend File & Folder Structure (Changes Only)

```
src/
├── app/
│   └── (admin)/
│       └── dashboard/
│           └── page.tsx                                    MODIFIED — TodayOverviewPanel added
│
└── features/
    ├── task/
    │   ├── components/
    │   │   ├── TaskDetail.tsx                              MODIFIED — proof, approval, late, cancel
    │   │   ├── TaskCard.tsx                                MODIFIED — late badge
    │   │   ├── TaskStatusBadge.tsx                         MODIFIED — PENDING_APPROVAL case
    │   │   ├── ProofUploadSection.tsx                      NEW
    │   │   ├── TaskApprovalPanel.tsx                       NEW
    │   │   └── CancelReasonModal.tsx                       NEW
    │   └── hooks/
    │       ├── useApproveTask.ts                           NEW
    │       └── useRejectTask.ts                            NEW
    └── analytics/
        ├── components/
        │   └── TodayOverviewPanel.tsx                      NEW
        └── hooks/
            └── useTodayOverview.ts                         NEW
```

---

## Known Risk Areas

### Risk 1 — Legacy tasks (no task type)
v2.4 tasks have `task_type_id = null`, `proof_required = false`, `approval_required = false`.
All enforcement must null-guard — NEVER NPE on `task.getTaskType()`.

```java
// ALWAYS check task fields directly — not task type
if (task.isProofRequired()) { ... }
if (task.isApprovalRequired()) { ... }
// NEVER: task.getTaskType().isProofRequired()
```

### Risk 2 — V8 enum migration
`ALTER TYPE task_status ADD VALUE` cannot run inside a transaction. Deploy backend AFTER migration runs. Never wrap in BEGIN/COMMIT.

### Risk 3 — is_late immutability
Once `is_late = true`, no endpoint should clear it. Guard in both `TaskService` and `LateSubmissionScheduler`:
```java
if (!task.isLate()) {
    // only then check and set
}
```

### Risk 4 — Today's overview timezone
Use `LocalDate.now()` consistently — not `Instant` or UTC:
```java
LocalDate today = LocalDate.now();
LocalDateTime startOfDay = today.atStartOfDay();
LocalDateTime endOfDay = today.atTime(23, 59, 59);
```

---

## Phase 1 Completion Checklist

Before calling Phase 1 done and deploying v2.5 to production:

- [ ] V8 migration applied — `PENDING_APPROVAL` in DB enum
- [ ] V9 migration applied — `is_late`, `late_by_minutes`, `submitted_at`, `attachment_purpose` columns exist
- [ ] `daily_metrics.late_tasks` column exists
- [ ] `employee_performance_cache.tasks_late` column exists
- [ ] `AttachmentPurpose` enum exists with `PROOF` and `GENERAL`
- [ ] Proof upload blocked — task with `proof_required = true` cannot move to IN_REVIEW without PROOF attachment — API returns 400 with `TASK_3003`
- [ ] Legacy tasks (no task type) — proof enforcement skipped, no NPE
- [ ] Task with `approval_required = true` auto-moves to `PENDING_APPROVAL` after IN_REVIEW
- [ ] Admin can approve task → status `DONE`, `completed_at` set
- [ ] Admin can reject task → status back to `IN_REVIEW`, reason in status history
- [ ] CANCELLED without reason → 400 with `TASK_3004` (not 500)
- [ ] `submitted_at` set when task moves to IN_REVIEW
- [ ] `is_late = true` + `late_by_minutes` correct when submitted after `due_date`
- [ ] `is_late` cannot be unset after being set
- [ ] `LateSubmissionScheduler` runs hourly (verify via logs)
- [ ] `GET /api/v1/tasks/late` returns correct list
- [ ] Today's overview panel visible on admin dashboard
- [ ] Overview shows correct counts (verify against DB)
- [ ] Panel auto-refreshes every 5 minutes (verify via Network tab)
- [ ] Anomaly banner shows when active anomalies > 0
- [ ] `PENDING_APPROVAL` badge shows purple in UI
- [ ] Late badge shows red on task cards
- [ ] Cancel button opens reason modal (not direct API call)
- [ ] Proof upload section visible on tasks with `proofRequired = true`
- [ ] Task approved email sent to assignee
- [ ] Task rejected email sent to assignee with reason
- [ ] Deployed to production — NSG Academy using v2.5 ✅

---

## Phase 1 File Count Summary

| Layer | New | Modified |
|---|---|---|
| Backend Controller | 0 | 2 (TaskController, AnalyticsController) |
| Backend Service | 2 (TaskApprovalService, TodayOverviewService, AnomalyDetectionService) | 1 (TaskService) |
| Backend DTO | 2 (TaskApprovalRequest, TodayOverviewResponse) | 1 (TaskResponse) |
| Backend Model | 0 | 2 (Task, TaskAttachment) |
| Backend Enum | 2 (AttachmentPurpose, + PENDING_APPROVAL in TaskStatus) | 1 (TaskStatus) |
| Backend Event | 2 (TaskApprovedEvent, TaskRejectedEvent) | 0 |
| Backend Exception | 1 (ProofEnforcementException) | 0 |
| Backend Repository | 0 | 1 (TaskAttachmentRepository) |
| Backend Scheduler | 1 (LateSubmissionScheduler) | 0 |
| DB Migrations | 2 (V8, V9) | 0 |
| Email Templates | 2 (task-approved, task-rejected) | 0 |
| Frontend Components | 3 (ProofUploadSection, TaskApprovalPanel, CancelReasonModal, TodayOverviewPanel) | 3 (TaskDetail, TaskCard, TaskStatusBadge) |
| Frontend Hooks | 3 (useApproveTask, useRejectTask, useTodayOverview) | 0 |
| Frontend Pages | 0 | 1 (AdminDashboardPage) |

---

**Phase** | **1 of 9**
**Version Output** | **v2.5**
**Next Phase** | Phase 2 — Multi-Tenant Foundation (v3.0)
