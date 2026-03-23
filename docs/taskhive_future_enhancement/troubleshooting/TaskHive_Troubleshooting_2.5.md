# TaskHive — Troubleshooting Guide

> A living document. Add new issues as they are encountered and resolved.
> Format: one section per issue, ordered by date.

---

## Issue Index

| # | Date | Area | Title | Status |
|---|---|---|---|---|
| 1 | 2026-03-20 | Database / Flyway | V23 migration fails: `type "task_status" does not exist` | ✅ Resolved |
| 2 | 2026-03-20 | Frontend / React | React crash: `Expected static flag was missing` in `ProductivityScoreCard` | ✅ Resolved |
| 3 | 2026-03-20 | Frontend / Analytics | Dashboard Overview and Analytics page show different numbers | ✅ By Design |
| 4 | 2026-03-20 | Frontend + Backend | Task sorting not working on employee side (4-layer bug) | ✅ Resolved |
| 5 | 2026-03-20 | Backend / Compile | Employee file upload fails — backend compile error after signature change | ✅ Resolved |
| 6 | 2026-03-20 | Frontend / UX | Employee cannot see admin's revision feedback when task is sent back | ✅ Resolved |
| 7 | 2026-03-20 | Frontend / UI | Notification preference toggles don't match ML toggle design | ✅ Resolved |
| 8 | 2026-03-23 | Backend + Frontend | Approved proofs not clearing upon task rejection | ✅ Resolved |
| 9 | 2026-03-23 | Frontend / UX | Missing error message (TASK_3003) when submitting without proof | ✅ Resolved |
| 10 | 2026-03-23 | Backend + Frontend | Employees are able to Cancel their own tasks | ✅ Resolved |
| 11 | 2026-03-23 | Backend / Security | Insecure Direct Object Reference (IDOR) on Task sub-resources | ✅ Resolved |

---

## Issue 1 — V23 Migration Fails: `type "task_status" does not exist`

**Date:** 2026-03-20  
**Version:** Phase 1 v2.5 — Foundation Hardening  
**Severity:** 🔴 Critical (application fails to start)

---

### Symptom

Application crashes on startup with the following Flyway error:

```
ERROR [main] o.f.core.internal.command.DbMigrate -
Migration of schema "public" to version
"23 - alter task status add pending approval" failed!
Changes successfully rolled back.

Script V23__alter_task_status_add_pending_approval.sql failed
-------------------------------------------------------------
SQL State  : 42704
Error Code : 0
Message    : ERROR: type "task_status" does not exist
Location   : db/migration/V23__alter_task_status_add_pending_approval.sql
Line       : 1
```

Followed by a cascade of Spring context failures:

```
Error creating bean with name 'flywayInitializer':
Script V23__alter_task_status_add_pending_approval.sql failed
...
Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'
```

---

### Root Cause

The V23 migration file contained:

```sql
ALTER TYPE task_status ADD VALUE IF NOT EXISTS 'PENDING_APPROVAL';
```

`ALTER TYPE ... ADD VALUE` is PostgreSQL syntax for modifying a **native PostgreSQL enum type** (created with `CREATE TYPE task_status AS ENUM (...)`).

However, the `tasks` table was created in **V11** with the `status` column as a plain `VARCHAR`:

```sql
-- V11__create_tasks_table.sql
status VARCHAR(20) NOT NULL DEFAULT 'TODO'
```

There is **no** `CREATE TYPE task_status AS ENUM (...)` anywhere in the migration history. PostgreSQL therefore correctly throws `type "task_status" does not exist` because there is nothing to `ALTER`.

> [!NOTE]
> The `PENDING_APPROVAL` value does **not** require any DDL change because:
> - `VARCHAR(20)` accepts any string value natively
> - Valid status values are enforced at the **Java application layer** via the `TaskStatus` enum in `TaskStatus.java`
> - PostgreSQL has no knowledge of which values are valid — that is intentionally an application concern

---

### Fix

#### Step 1 — Fix the migration script

Replace the failing `ALTER TYPE` statement in `V23__alter_task_status_add_pending_approval.sql` with a documented no-op:

```sql
-- V23: Phase 1 v2.5 — PENDING_APPROVAL status support
--
-- tasks.status is VARCHAR(20) (see V11), NOT a PostgreSQL enum type.
-- VARCHAR columns accept any string value natively, so no DDL change
-- is needed to support 'PENDING_APPROVAL' as a status value.
-- The Java enum TaskStatus.java controls valid values at the application layer.
SELECT 1; -- intentional no-op to satisfy Flyway's requirement for non-empty scripts
```

#### Step 2 — Repair the Flyway schema history table

Because Flyway marked V23 as **FAILED** in its `flyway_schema_history` table, you must repair it before the app can start again:

```bash
cd D:\Internship\TaskHive\taskhive-backend

mvn flyway:repair \
  -Dflyway.url=jdbc:postgresql://localhost:5432/taskhive \
  -Dflyway.user=postgres \
  -Dflyway.password=password \
  -Dflyway.locations=classpath:db/migration \
  --no-transfer-progress
```

Expected output:
```
[INFO] Successfully repaired schema history table
       "public"."flyway_schema_history" (execution time 00:00.132s).
[INFO] BUILD SUCCESS
```

#### Step 3 — Restart the application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev --no-transfer-progress
```

---

### Prevention

> [!IMPORTANT]
> **Before writing `ALTER TYPE` in any new migration**, check how the column is actually defined:
>
> ```sql
> -- Run in psql to check column type
> SELECT column_name, data_type, udt_name
> FROM information_schema.columns
> WHERE table_name = 'tasks' AND column_name = 'status';
> ```
>
> - If `data_type` = `USER-DEFINED` and `udt_name` = `task_status` → use `ALTER TYPE`
> - If `data_type` = `character varying` → just add the Java enum value, no SQL needed

> [!TIP]
> **How to check Flyway migration history state:**
>
> ```sql
> SELECT version, description, type, state, execution_time
> FROM flyway_schema_history
> ORDER BY installed_rank;
> ```
>
> A `state = 'FAILED'` row means `mvn flyway:repair` is needed before the next startup.

---

### Files Changed

| File | Change |
|---|---|
| `src/main/resources/db/migration/V23__alter_task_status_add_pending_approval.sql` | Replaced `ALTER TYPE` with `SELECT 1` no-op |

---

### Related

- **V11** `V11__create_tasks_table.sql` — original tasks table schema (status as VARCHAR)
- **V24** `V24__alter_tasks_add_v25_columns.sql` — companion migration for Phase 1 columns (unaffected)
- `TaskStatus.java` — Java enum that controls valid status values at the application layer

---

*Last updated: 2026-03-20 by Antigravity (AI assistant)*

---

## Issue 2 — React Internal Error: `Expected static flag was missing`

**Date:** 2026-03-20  
**Version:** Phase 1 v2.5  
**Area:** Frontend / React  
**Severity:** 🔴 Critical (crashes the Employee Detail page)

---

### Symptom

Opening any employee detail page (`/admin/employees/:id`) crashes with:

```
Internal React error: Expected static flag was missing.
Please notify the React team.
```

The stack trace pointed to `ProductivityScoreCard.tsx` rendered inside `EmployeeDetail.tsx`.

---

### Root Cause

**Violation of React's Rules of Hooks.** The `useMlStore()` hook was called *after* two early `return` statements inside `ProductivityScoreCard`:

```tsx
// ❌ BEFORE — hooks called after conditional returns
export const ProductivityScoreCard = ({ employeeId }) => {
    if (!employeeId) return null;          // early return #1
    if (someOtherCondition) return null;   // early return #2

    const { isMlEnabled } = useMlStore();  // ← Hook after a return!
```

React requires all hooks to be called **unconditionally on every render** in the same order. If a return fires before the hook call, the hook count changes between renders and React panics.

> [!IMPORTANT]
> This is one of the most common but least obvious React bugs. The error message ("Expected static flag") is not intuitive — it is React's internal way of flagging a hook call order mismatch.

---

### Fix

Move all hook calls to the **very top** of the component, before any conditional logic:

```tsx
// ✅ AFTER — hooks always called first, unconditionally
export const ProductivityScoreCard = ({ employeeId }) => {
    const { isMlEnabled } = useMlStore();  // ← Always called first

    if (!employeeId) return null;
    if (!isMlEnabled) return null;
    // ...rest of component
```

---

### Files Changed

| File | Change |
|---|---|
| `features/ml/components/ProductivityScoreCard.tsx` | Moved `useMlStore()` call to top of component, before all early returns |

---

### Prevention

> [!TIP]
> ESLint's `react-hooks/rules-of-hooks` plugin catches this at write-time. If it isn't flagging violations like this, check that your ESLint config includes `"plugin:react-hooks/recommended"`.
> Always scan for `return` statements that appear *before* any hook call when debugging mysterious React crashes.

---

---

## Issue 3 — Dashboard Overview vs Analytics Page Show Different Numbers

**Date:** 2026-03-20  
**Area:** Frontend / Analytics  
**Severity:** 🟡 Confusion (not a bug — by design)

---

### Symptom

The **Admin Dashboard** "Today's Overview" showed different numbers from the **Analytics** page for the same metrics (e.g. "Completed Tasks" showed `3` on Dashboard but `47` on Analytics).

---

### Root Cause

**Intentional design — two different data scopes:**

| Page | Endpoint | Scope |
|---|---|---|
| Admin Dashboard (`/admin/dashboard`) | `GET /analytics/dashboard/today` | **Real-time, today only** — tasks completed *today* |
| Analytics Page (`/admin/analytics`) | `GET /analytics/dashboard/admin` | **All-time cumulative** — all tasks ever completed |

The `AnalyticsService.java` confirms:
- `getTodayOverview()` runs a real-time query scoped to `LocalDate.now()`
- `getAdminDashboard()` prioritises cached `daily_metrics` snapshots and falls back to a cumulative real-time query

---

### Resolution

**No fix required.** The difference is intentional:
- Dashboard = operational view (what happened today?)
- Analytics = strategic view (cumulative performance)

Document this for future developers to avoid re-investigating.

---

---

## Issue 4 — Task Sorting Not Working on Employee Side (Multi-Layer Bug)

**Date:** 2026-03-20  
**Area:** Frontend + Backend / Task Management  
**Severity:** 🔴 Functional bug (sorting silently does nothing)

---

### Symptom

Clicking column sort headers on the admin task list (`/admin/tasks`) worked correctly. The same sort action on the employee task list (`/employee/tasks`) had no effect — the list was always returned in the same order regardless of what was selected.

---

### Root Cause

The bug existed across **four separate layers**, each silently swallowing the sort parameters:

#### Layer 1 — Frontend: `taskService.getMyTasks` (Frontend Service)

`sortBy` and `sortDir` were never forwarded to the API call, even though they existed in the `TaskFilters` type:

```typescript
// ❌ BEFORE
if (filters?.status) params.status = filters.status;
if (filters?.priority) params.priority = filters.priority;
// sortBy and sortDir: silently dropped!
```

#### Layer 2 — Frontend: Employee Tasks Page Had No Sort UI

The employee tasks page had no clickable sort column headers at all. Admins had sort UI (even if non-functional); employees had none.

#### Layer 3 — Backend Controller: `/my-tasks` Missing `status` and `priority` Params

The `GET /tasks/my-tasks` controller endpoint accepted `sortBy`/`sortDir` but not `status` or `priority`, so filter params sent from the frontend were silently ignored:

```java
// ❌ BEFORE — only sort params, no filter params
public ResponseEntity getMyTasks(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "dueDate") String sortBy,
    @RequestParam(defaultValue = "asc") String sortDir) {
```

#### Layer 4 — Backend Service + Repository: `getMyTasks` Used a Non-Filterable Query

`TaskService.getMyTasks` called `findByAssignedToAndIsDeletedFalse(employeeId, pageable)` — a Spring Data derived method that **does not support** filtering by status or priority:

```java
// ❌ BEFORE — no status/priority filtering possible
Page<Task> taskPage = taskRepository.findByAssignedToAndIsDeletedFalse(employee.getId(), pageable);
```

---

### Fix

Each layer required a separate fix:

**Frontend — `taskService.ts`:** Forward `sortBy` and `sortDir` params:
```typescript
if (filters?.sortBy) params.sortBy = filters.sortBy;
if (filters?.sortDir) params.sortDir = filters.sortDir;
```

**Frontend — Employee tasks page:** Added clickable `SortHeader` column components (Priority, Status, Due Date) with orange highlight and arrow indicators matching the admin side.

**Backend — `TaskController.java`:** Added `status` and `priority` `@RequestParam`s:
```java
@RequestParam(required = false) String status,
@RequestParam(required = false) String priority
```

**Backend — `TaskService.java`:** Added field name validation and called new filterable query:
```java
Page<Task> taskPage = taskRepository.findMyTasksWithFilters(
    employee.getId(), status, priority, pageable);
```

**Backend — `TaskRepository.java`:** Added JPQL query with nullable filters (Spring Data auto-appends `ORDER BY` from `Pageable`):
```java
@Query("SELECT t FROM Task t WHERE t.isDeleted = false " +
       "AND t.assignedTo = :assignedTo " +
       "AND (:status IS NULL OR t.status = :status) " +
       "AND (:priority IS NULL OR t.priority = :priority)")
Page<Task> findMyTasksWithFilters(
    @Param("assignedTo") UUID assignedTo,
    @Param("status") String status,
    @Param("priority") String priority,
    Pageable pageable);
```

> [!NOTE]
> The admin's `findAllWithFilters` is a `nativeQuery = true` (raw SQL) and needs a column-name mapper (`mapToColumnName`) to convert `dueDate` → `due_date`. The employee's `findMyTasksWithFilters` is a JPQL query that uses **entity field names** (camelCase), so no mapping is needed.

---

### Files Changed

| File | Change |
|---|---|
| `features/task/services/taskService.ts` | Added `sortBy`/`sortDir` forwarding in `getMyTasks` |
| `app/employee/tasks/page.tsx` | Rewrote page with `SortHeader` components and sort state |
| `module/task/controller/TaskController.java` | Added `status` + `priority` params to `/my-tasks` |
| `module/task/service/TaskService.java` | Updated `getMyTasks` signature, added field-name mapping, switched to new query |
| `module/task/repository/TaskRepository.java` | Added `findMyTasksWithFilters` JPQL query |
| `module/task/service/TaskServiceTest.java` | Updated test to match new method signature |

---

### Prevention

> [!TIP]
> When a feature works on the admin side but not the employee side, trace the **entire vertical slice**: page → hook → service → API call → controller → service → repository. Silent parameter drops at any layer are a common failure mode.

---

---

## Issue 5 — Employee Upload Fails with "Upload failed. Please try again."

**Date:** 2026-03-20  
**Area:** Backend / Compile  
**Severity:** 🔴 Critical (all API calls fail)

---

### Symptom

Employee received `"Upload failed. Please try again."` when uploading proof files. The issue appeared immediately after changes to `TaskService.java` and `TaskRepository.java` (Issue 4 fixes).

---

### Root Cause

The backend **failed to compile** due to the `getMyTasks` signature change in `TaskService.java`. All API endpoints — including file upload — returned 503/connection errors because Spring Boot never started successfully.

The compile errors were:
```
The method getMyTasks(int, int, String, String) in TaskService is not applicable
for the arguments (int, int, String, String, String, String)
→ TaskController.java line 73
→ TaskServiceTest.java line 261
```

The upload error was a red herring — the actual problem was the backend was completely down.

---

### Fix

1. Updated `TaskController.java` `getMyTasks` call to pass `status` and `priority`
2. Updated `TaskServiceTest.java` `getMyTasks` call to pass `null, null` for filters

**Lesson:** Always check backend compile/startup logs first when all API calls are failing simultaneously.

---

### Prevention

> [!WARNING]
> After changing any method signature in a Spring Boot service, search for all callers (`Ctrl+Shift+F` in IntelliJ, or `grep -r "getMyTasks"`) to avoid broken callers at compile time.

---

---

## Issue 6 — Employee Cannot See Revision Feedback When Admin Requests Changes

**Date:** 2026-03-20  
**Area:** Frontend / Task Detail UX  
**Severity:** 🟠 UX Bug (employee has no actionable information)

---

### Symptom

When an admin rejected a `PENDING_APPROVAL` task (sending it back to `IN_REVIEW`):
1. The task status badge silently changed back to "In Review" — indistinguishable from when the employee originally submitted it
2. The admin's rejection reason/comment was only visible deep inside the "Status History" section at the bottom of the page, styled the same as every other timeline entry
3. The employee had no indication they needed to take action or what specifically to fix

---

### Root Cause

`TaskDetail.tsx` had no logic to distinguish a **admin-rejected IN_REVIEW** from an **employee-submitted IN_REVIEW**. The status history component showed comments, but with no visual distinction for rejection entries — the rejection reason was buried and unstyled.

The backend **did** correctly persist the rejection reason in `TaskStatusHistory` with `PENDING_APPROVAL → IN_REVIEW` as the old/new status transition.

---

### Fix

Two-part frontend fix:

**1. Prominent "Revision Requested" banner in `TaskDetail.tsx`:**
- On mount, if `task.status === 'IN_REVIEW'`, fetches status history
- Detects the most recent `PENDING_APPROVAL → IN_REVIEW` entry
- Renders an **orange banner** directly below the overdue alert with:
  - "Revision Requested" heading, admin name, and timestamp
  - The rejection reason in a styled message box
  - "Please address the feedback above, then resubmit" hint
- Shown to employees only; hidden from admins
- Automatically disappears when the employee resubmits

**2. Highlighted rejection entries in `TaskStatusHistory.tsx`:**
- `PENDING_APPROVAL → IN_REVIEW` entries get an orange-tinted dot and border
- A "Revision Requested" pill badge appears inline with the status badges
- The comment box gets an orange tint instead of plain grey

---

### Files Changed

| File | Change |
|---|---|
| `features/task/components/TaskDetail.tsx` | Added `useEffect` to detect rejection; added orange revision banner |
| `features/task/components/TaskStatusHistory.tsx` | Highlighted rejection entries with orange styling and pill badge |

---

---

## Issue 7 — Notification Preference Toggles Don't Match ML Toggle Design

**Date:** 2026-03-20  
**Area:** Frontend / UI Consistency  
**Severity:** 🟡 Design Inconsistency

---

### Symptom

The notification preference toggles in Employee Settings (`/employee/settings/security`) used Tailwind CSS classes and had slightly different dimensions from the pill toggle in the Admin sidebar ML feature toggle — making the app feel visually inconsistent.

| Property | Notification Toggle (old) | ML Toggle |
|---|---|---|
| Track width | `w-11` (44px) | 42px |
| Knob size | `h-4 w-4` (16px) | 18px |
| Knob start offset | `translate-x-1` (4px) | `left: 3px` |
| Implementation | Tailwind classes on `<button>` | Inline styles on `<div>` |

---

### Fix

Replaced the Tailwind `Toggle` component in `app/employee/settings/security/page.tsx` with the identical inline-style implementation from `MlFeatureToggle.tsx`:

```tsx
// ✅ Exact same dimensions and style as the ML pill toggle
<div
    style={{
        width: '42px', height: '24px', borderRadius: '12px',
        backgroundColor: checked ? '#f97316' : '#3f3f46',
        position: 'relative', cursor: 'pointer',
        transition: 'background-color 0.2s',
    }}
>
    <div style={{
        width: '18px', height: '18px', borderRadius: '50%',
        backgroundColor: '#ffffff', position: 'absolute',
        top: '3px', left: checked ? '21px' : '3px',
        transition: 'left 0.2s',
        boxShadow: '0 1px 3px rgba(0,0,0,0.4)',
    }} />
</div>
```

---

### Files Changed

| File | Change |
|---|---|
| `app/employee/settings/security/page.tsx` | Replaced Tailwind Toggle with inline-style pill toggle |

---

### Prevention

> [!TIP]
> Create a shared `PillToggle` component in `components/ui/PillToggle.tsx` and import it everywhere toggles are used. This avoids per-file duplication and ensures visual consistency is enforced by reuse rather than manual coordination.

---

## Issue 8 — Approved Proofs Not Clearing Upon Task Rejection

**Date:** 2026-03-23  
**Area:** Backend + Frontend / Workflow  
**Severity:** 🟠 Functional Bug (employees submitting old rejected proofs)

---

### Symptom

When an admin rejects a task (moving it from `PENDING_APPROVAL` to `IN_REVIEW`), the old "proof" attachments remained in the database with the active `PROOF` state. If the employee resubmitted the task without uploading new proof, the backend logic incorrectly accepted the submission because the database still found `AttachmentPurpose.PROOF` linked to the `taskId`.

### Root Cause

No logical separation existed between an "active, pending proof" and a "rejected, invalid proof". The system had no `REJECTED_PROOF` purpose mapping.

### Fix

1. **Backend:** Added `REJECTED_PROOF` to `AttachmentPurpose` enum.
2. **Backend:** Updated `TaskAttachmentRepository.java` with a bulk `@Modifying` query to `updatePurposeByTaskId()`.
3. **Backend:** In `TaskService.java` `rejectTask()`, automatically converted all existing `PROOF` attachments to `REJECTED_PROOF` when the admin hits Reject.
4. **Frontend:** Upgraded `ProofUploadSection.tsx` to structurally filter `REJECTED_PROOF` files from `PROOF` files, visually displaying the rejected ones in a red box with strikethrough text to clearly indicate they no longer count for submission.

---

## Issue 9 — Missing Error Message (TASK_3003) When Submitting Without Proof

**Date:** 2026-03-23  
**Area:** Frontend / UX  
**Severity:** 🟠 UX Bug (silent failure leaves users confused)

---

### Symptom

When an employee attempts to change a task status to `IN_REVIEW` on a task that requires proof (but hasn't uploaded one), the status change modal remains open but silent. No error is shown to the user explaining why they can't submit the task.

### Root Cause

The `useUpdateTaskStatus` React Hook successfully caught the `TASK_3003` Business Exception ("Proof attachment required..."). However, the consuming `TaskDetail.tsx` component never destructured or rendered the `error` state from the hook, completely swallowing the feedback.

### Fix

Destructured `error: statusError` from the `useUpdateTaskStatus` hook and embedded an inline red banner using the `AlertCircle` icon immediately above the Submit button in the Status Modal. Now, backend rejections securely inform the employee exactly what is missing.

---

## Issue 10 — Employees Are Able to Cancel Their Own Tasks

**Date:** 2026-03-23  
**Area:** Backend + Frontend / Logic Rules  
**Severity:** 🔴 Functional Bug (unauthorized state changes)

---

### Symptom

Employees saw the "Move to: Cancelled" option in their `TaskDetail.tsx` view and could freely cancel assigned tasks without Admin intervention. 

### Root Cause

The frontend manually defined `CANCELLED` as an available transition inside the static `STATUS_TRANSITIONS` object. The backend's `updateTaskStatus` endpoint had zero role-based security prohibiting `EMPLOYEE` accounts from forcing a task to the `CANCELLED` step.

### Fix

1. **Frontend:** In `TaskDetail.tsx`'s `availableTransitions` method, added `if (s === 'CANCELLED' && !isAdmin) return false;`. This correctly eliminated the Cancel button via UI permissions.
2. **Backend:** Upgraded `TaskService.updateTaskStatus()` with a direct check: if the user role is `EMPLOYEE` and the target status is `CANCELLED`, throw a `TaskAccessDeniedException("Employees are not allowed to cancel tasks")`.

---

## Issue 11 — Insecure Direct Object Reference (IDOR) on Task Sub-Resources

**Date:** 2026-03-23  
**Area:** Backend / Security  
**Severity:** 🔴 Critical Vulnerability (Data leak across employees)

---

### Symptom

During an overarching logic review, it was discovered that any logged-in user could intercept proof files, internal task comments, and administrative history items of **any** task created in the platform, so long as they knew the task UUID. They were additionally able to upload attachments to tasks that were already declared `DONE` or `CANCELLED`.

### Root Cause

Sub-resource service endpoints like `getAttachments()`, `getHistory()`, and `getComments()` validated whether the task existed, but **did not validate whether the logged-in user had security access to the task** (unlike the main `getTaskById()` endpoint). Furthermore, `uploadAttachment()` never bothered checking the state limit of the task.

### Fix

1. **IDOR Patches:** Patched `TaskAttachmentService.java`, `TaskStatusHistoryService.java`, and `TaskCommentService.java` by injecting the `SecurityUtils` context and the `EmployeeRepository`, verifying explicitly that the authenticated employee is, in fact, the exact `assignedTo` employee on the task.
2. **State Violation Patch:** Appended simple `DONE` and `CANCELLED` checks to `TaskAttachmentService.uploadAttachment()`, throwing `BusinessException` and stopping all file writes to disabled or closed records.

---

*Last updated: 2026-03-23 by Antigravity (AI assistant)*
