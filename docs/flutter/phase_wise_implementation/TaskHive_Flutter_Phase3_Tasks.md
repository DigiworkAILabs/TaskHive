# TaskHive Flutter — Phase 3: Task Module
## Version 1.0 | SRS v2.4 | Digiwork
## ⚠️ Always provide `TaskHive_Flutter_Master_Context.md` alongside this file

---

## Project Flow Context

Before diving into Phase 3, here is the full picture of what has been built and where this phase fits:

```
Phase 1 — Auth + Core Infrastructure  ✅ COMPLETE
  └── Login, logout, session restore, role-based routing
  └── Cookie-based JWT (HttpOnly), Dio client, Riverpod, GoRouter
  └── Admin shell (5 tabs) + Employee shell (4 tabs)
  └── Core widgets, theme, cache service, secure storage

Phase 2 — Employee Module  ✅ COMPLETE
  └── Admin: create / list / detail / edit / activate / deactivate / delete employees
  └── Employee: view and update own profile (phone + address)
  └── Profile photo upload (native + Web safe)
  └── Hive cache for employee list (stale-while-revalidate)

Phase 3 — Task Module  ← YOU ARE HERE
  └── Admin: create / list / detail / edit / delete / reassign tasks
  └── Admin + Employee: update task status (with transition rules)
  └── Admin + Employee: add comments, upload attachments
  └── Employee: view only own assigned tasks
  └── Overdue indicator + overdue task list
  └── Full status history timeline
  └── Hive cache for tasks_box (admin) and my_tasks_box (employee)

Phase 4 — Notifications + WebSocket  (depends on Phase 3)
Phase 5 — Audit Logs                 (depends on Phase 1)
Phase 6 — Analytics + Dashboards     (depends on Phase 3)
```

### Role Behaviour Summary (Critical for Phase 3)

| Action | ADMIN | EMPLOYEE |
|--------|-------|----------|
| Create task | ✅ | ❌ |
| View all tasks | ✅ | ❌ |
| View own tasks only | ✅ | ✅ (`/tasks/my-tasks`) |
| Update task fields | ✅ | ❌ |
| Update task status | ✅ (any task) | ✅ (own tasks only) |
| Reassign task | ✅ | ❌ |
| Add comment | ✅ | ✅ (own tasks only) |
| Upload attachment | ✅ | ✅ (own tasks only) |
| View overdue tasks | ✅ | ❌ |
| Delete task | ✅ | ❌ |

### Task Status Transition Rules

```
TODO        → IN_PROGRESS, CANCELLED
IN_PROGRESS → IN_REVIEW, CANCELLED
IN_REVIEW   → DONE, IN_PROGRESS, CANCELLED
DONE        → (terminal — no further transitions)
CANCELLED   → (terminal — no further transitions)
```

These rules must be enforced in `status_transition_selector.dart` — only valid next statuses are shown as options. The backend also validates this and will reject invalid transitions.

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 3 of 6 |
| **Name** | Task Module |
| **Scope** | Task models + enums, 3 repositories, 10 providers, 6 screens, 16 widgets, router extension |
| **Depends On** | Phase 1 (Auth) + Phase 2 (Employee — `EmployeeModel`, `employeeSearchProvider` used in assignee picker) |
| **Platforms** | Android ✅ iOS ✅ macOS ✅ Web ✅ Windows ✅ Linux ✅ |

### Deliverable
Admin can create, assign, edit, delete, and manage all tasks. Employees see only their assigned tasks. Both roles can update task status (within allowed transitions), add comments, and upload file attachments. Overdue tasks are flagged visually. Full status history is shown per task. Both task lists are Hive-cached with stale-while-revalidate.

---

## Phase 3 — Complete File List (~32 files)

```
lib/
│
├── features/
│   └── task/
│       ├── data/
│       │   ├── models/
│       │   │   ├── task_model.dart                        # ★ Phase 3
│       │   │   ├── task_comment_model.dart                # ★ Phase 3
│       │   │   ├── task_attachment_model.dart             # ★ Phase 3
│       │   │   ├── task_status_history_model.dart         # ★ Phase 3
│       │   │   ├── create_task_request.dart               # ★ Phase 3
│       │   │   ├── update_task_request.dart               # ★ Phase 3
│       │   │   ├── update_task_status_request.dart        # ★ Phase 3
│       │   │   ├── task_comment_request.dart              # ★ Phase 3
│       │   │   └── pending_action_model.dart              # ★ Phase 3
│       │   └── repositories/
│       │       ├── task_repository.dart                   # ★ Phase 3
│       │       ├── task_comment_repository.dart           # ★ Phase 3
│       │       └── task_attachment_repository.dart        # ★ Phase 3
│       │
│       ├── domain/
│       │   ├── enums/
│       │   │   ├── task_status.dart                       # ★ Phase 3
│       │   │   └── task_priority.dart                     # ★ Phase 3
│       │   └── providers/
│       │       ├── task_list_provider.dart                # ★ Phase 3
│       │       ├── my_tasks_provider.dart                 # ★ Phase 3
│       │       ├── overdue_tasks_provider.dart            # ★ Phase 3
│       │       ├── task_detail_provider.dart              # ★ Phase 3
│       │       ├── task_comments_provider.dart            # ★ Phase 3
│       │       ├── task_attachments_provider.dart         # ★ Phase 3
│       │       ├── task_history_provider.dart             # ★ Phase 3
│       │       ├── task_search_provider.dart              # ★ Phase 3
│       │       ├── task_actions_provider.dart             # ★ Phase 3
│       │       └── pending_actions_provider.dart          # ★ Phase 3
│       │
│       └── presentation/
│           ├── screens/
│           │   ├── admin/
│           │   │   ├── admin_task_list_screen.dart        # ★ Phase 3
│           │   │   ├── admin_task_detail_screen.dart      # ★ Phase 3
│           │   │   ├── create_task_screen.dart            # ★ Phase 3
│           │   │   └── edit_task_screen.dart              # ★ Phase 3
│           │   └── employee/
│           │       ├── my_tasks_screen.dart               # ★ Phase 3
│           │       └── employee_task_detail_screen.dart   # ★ Phase 3
│           └── widgets/
│               ├── task_list_tile.dart                    # ★ Phase 3
│               ├── task_status_badge.dart                 # ★ Phase 3
│               ├── task_priority_badge.dart               # ★ Phase 3
│               ├── task_filter_bar.dart                   # ★ Phase 3
│               ├── task_form.dart                         # ★ Phase 3
│               ├── assignee_picker.dart                   # ★ Phase 3
│               ├── due_date_picker.dart                   # ★ Phase 3
│               ├── priority_selector.dart                 # ★ Phase 3
│               ├── status_transition_selector.dart        # ★ Phase 3
│               ├── task_comment_tile.dart                 # ★ Phase 3
│               ├── task_comments_section.dart             # ★ Phase 3
│               ├── task_attachment_tile.dart              # ★ Phase 3
│               ├── task_attachments_section.dart          # ★ Phase 3
│               ├── task_status_history_timeline.dart      # ★ Phase 3
│               ├── overdue_indicator.dart                 # ★ Phase 3
│               └── tags_input_field.dart                  # ★ Phase 3
│
└── core/
    └── router/
        └── app_router.dart                               # MODIFY — extend Phase 2 router
```

> **Do NOT touch** any Phase 1 or Phase 2 file except `app_router.dart`.
> `app_routes.dart` already has all Phase 3 route constants — no changes needed.
> `api_endpoints.dart` already has all Phase 3 endpoints — no changes needed.
> `cache_service.dart` already has `tasks_box` and `my_tasks_box` methods — no changes needed.

---

## API Endpoints Used in Phase 3

```
POST   /api/v1/tasks                        # Create task (ADMIN only)
GET    /api/v1/tasks                        # All tasks paginated + filters (ADMIN only)
GET    /api/v1/tasks/{id}                  # Task detail (both roles)
PUT    /api/v1/tasks/{id}                  # Update task (ADMIN only)
DELETE /api/v1/tasks/{id}                  # Soft delete (ADMIN only)
PATCH  /api/v1/tasks/{id}/status           # Update status (ADMIN any task, EMPLOYEE own only)
POST   /api/v1/tasks/{id}/comments         # Add comment (both roles)
GET    /api/v1/tasks/{id}/comments         # Get comments (both roles)
POST   /api/v1/tasks/{id}/attachments      # Upload attachment (both roles)
GET    /api/v1/tasks/{id}/attachments      # List attachments (both roles)
GET    /api/v1/tasks/{id}/history          # Status history (both roles)
GET    /api/v1/tasks/my-tasks              # Employee's own tasks only
GET    /api/v1/tasks/overdue               # Overdue tasks (ADMIN only)
GET    /api/v1/tasks/search                # Full-text search by title + description
```

### Response Shapes

```json
// GET /api/v1/tasks — paginated
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "title": "Fix login bug",
        "description": "...",
        "status": "TODO",
        "priority": "HIGH",
        "assignedTo": "uuid",
        "assigneeName": "Jane Doe",
        "dueDate": "2026-04-01",
        "completedAt": null,
        "estimatedHours": 4.0,
        "tags": ["backend", "auth"],
        "isOverdue": false,
        "createdBy": "uuid",
        "createdAt": "2026-03-01T10:00:00Z",
        "updatedAt": "2026-03-01T10:00:00Z"
      }
    ],
    "totalElements": 24,
    "totalPages": 3,
    "size": 10,
    "number": 0
  }
}

// GET /api/v1/tasks/{id}/comments
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "taskId": "uuid",
      "authorId": "uuid",
      "authorName": "Jane Doe",
      "content": "Working on this now.",
      "createdAt": "2026-03-01T11:00:00Z"
    }
  ]
}

// GET /api/v1/tasks/{id}/attachments
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "taskId": "uuid",
      "uploadedBy": "uuid",
      "uploaderName": "Jane Doe",
      "fileName": "report.pdf",
      "fileUrl": "/api/v1/tasks/uuid/attachments/uuid/download",
      "fileSize": 204800,
      "mimeType": "application/pdf",
      "createdAt": "2026-03-01T11:00:00Z"
    }
  ]
}

// GET /api/v1/tasks/{id}/history
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "taskId": "uuid",
      "oldStatus": "TODO",
      "newStatus": "IN_PROGRESS",
      "changedBy": "Jane Doe",
      "comment": "Starting work",
      "changedAt": "2026-03-01T12:00:00Z"
    }
  ]
}

// PATCH /api/v1/tasks/{id}/status — request body
{ "newStatus": "IN_PROGRESS", "comment": "Starting now" }

// POST /api/v1/tasks/{id}/comments — request body
{ "content": "Comment text here" }

// POST /api/v1/tasks/{id}/attachments — multipart/form-data
// field name: "file" (confirm against Postman collection)

// All errors follow global shape:
// { "success": false, "message": "...", "errorCode": "...", "data": null }
// Phase 3 specific error codes: TASK_NOT_FOUND, TASK_ACCESS_DENIED,
// INVALID_STATUS_TRANSITION, ASSIGNEE_NOT_ACTIVE
```

---

## Functional Requirements Covered in Phase 3

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-TASK-01 | Critical | ADMIN creates tasks (title, description, priority, dueDate, assignedTo, estimatedHours optional, tags optional) |
| FR-TASK-02 | Critical | Due date must be a future date — validate on Flutter form before submitting |
| FR-TASK-03 | Critical | Assignee must be an active employee — enforced via `assignee_picker.dart` (only shows ACTIVE employees) |
| FR-TASK-04 | Critical | Tasks support statuses: TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED |
| FR-TASK-05 | Critical | Tasks support priorities: LOW, MEDIUM, HIGH, CRITICAL |
| FR-TASK-06 | High | ADMIN reassigns tasks by updating `assignedTo` field in edit screen |
| FR-TASK-07 | High | EMPLOYEE updates status of own tasks only — enforced by backend; Flutter shows status selector only on own tasks |
| FR-TASK-08 | High | ADMIN updates status of any task |
| FR-TASK-09 | Medium | Both roles add comments — comments are immutable once submitted (no edit/delete UI) |
| FR-TASK-10 | Medium | Both roles upload file attachments (JPEG, PNG, PDF, DOCX) |
| FR-TASK-11 | High | Full status change history shown in task detail (timeline widget) |
| FR-TASK-12 | High | Overdue tasks flagged visually with `overdue_indicator.dart`; `isOverdue` flag comes from backend |
| FR-TASK-14 | Critical | EMPLOYEE sees only own tasks via `/tasks/my-tasks` endpoint |
| FR-TASK-15 | Critical | ADMIN sees all tasks via `/tasks` with filters (status, priority, assignee, tags) |
| FR-TASK-16 | Medium | Full-text task search by title and description |
| FR-TASK-17 | High | Soft delete — Flutter removes from list; backend sets `is_deleted = true` |

---

## Data Models — Specifications

### task_model.dart
`@freezed` model. Fields:
- `id`, `title`, `description` — `String`
- `status` — `TaskStatus` enum
- `priority` — `TaskPriority` enum
- `assignedTo` — `String` (employee ID)
- `assigneeName` — `String?`
- `dueDate` — `String?` (ISO date "YYYY-MM-DD")
- `completedAt` — `String?`
- `estimatedHours` — `double?`
- `tags` — `List<String>` (default `[]`)
- `isOverdue` — `bool` (default `false`)
- `createdBy` — `String?`
- `createdAt`, `updatedAt` — `String?`

Extensions on `TaskModel`:
- `bool get isTerminal` → `status == done || status == cancelled`
- `List<TaskStatus> get allowedNextStatuses` → returns valid transitions per the status flow chart above
- `bool get isDueToday` → compares `dueDate` to today using `AppDateUtils`

### task_comment_model.dart
`@freezed`. Fields: `id`, `taskId`, `authorId`, `authorName`, `content`, `createdAt` — all `String`, `createdAt` nullable.

### task_attachment_model.dart
`@freezed`. Fields: `id`, `taskId`, `uploadedBy`, `uploaderName?`, `fileName`, `fileUrl`, `fileSize` (`int`), `mimeType`, `createdAt?` — all String except `fileSize`.

### task_status_history_model.dart
`@freezed`. Fields: `id`, `taskId`, `oldStatus?`, `newStatus`, `changedBy`, `comment?`, `changedAt` — all `String`.

### create_task_request.dart
`@freezed`. Fields: `title`, `description?`, `priority` (`TaskPriority`), `assignedTo`, `dueDate` — required. `estimatedHours` (`double?`), `tags` (`List<String>`, default `[]`) — optional.

### update_task_request.dart
`@freezed`. All fields nullable (partial update): `title?`, `description?`, `priority?` (`TaskPriority?`), `assignedTo?`, `dueDate?`, `estimatedHours?`, `tags?` (`List<String>?`). `build.yaml` `include_if_null: false` handles omission of nulls automatically.

### update_task_status_request.dart
`@freezed`. Fields: `newStatus` (`TaskStatus`, required), `comment` (`String?`).

### task_comment_request.dart
`@freezed`. Single field: `content` (`String`, required).

### pending_action_model.dart
Plain Dart class (no freezed — not serialized to/from JSON, used only in-memory). Represents an optimistic local action queued while offline. Fields: `String id` (UUID), `String taskId`, `String type` ('status_change' | 'comment'), `Map<String, dynamic> payload`, `DateTime createdAt`. Used by `pending_actions_provider.dart` to replay actions when connectivity restores.

---

## Enums — Specifications

### domain/enums/task_status.dart
```dart
enum TaskStatus {
  @JsonValue('TODO') todo,
  @JsonValue('IN_PROGRESS') inProgress,
  @JsonValue('IN_REVIEW') inReview,
  @JsonValue('DONE') done,
  @JsonValue('CANCELLED') cancelled,
}
```
Extension `TaskStatusX`:
- `String get label` → 'To Do', 'In Progress', 'In Review', 'Done', 'Cancelled'
- `Color get color` → todo: grey, inProgress: blue, inReview: orange, done: green, cancelled: red
- `List<TaskStatus> get allowedTransitions` → enforces the status flow chart

### domain/enums/task_priority.dart
```dart
enum TaskPriority {
  @JsonValue('LOW') low,
  @JsonValue('MEDIUM') medium,
  @JsonValue('HIGH') high,
  @JsonValue('CRITICAL') critical,
}
```
Extension `TaskPriorityX`:
- `String get label` → 'Low', 'Medium', 'High', 'Critical'
- `Color get color` → low: grey, medium: blue, high: orange, critical: red
- `IconData get icon` → use `Icons.arrow_downward`, `Icons.remove`, `Icons.arrow_upward`, `Icons.priority_high`

> **Important**: Enums use `@JsonValue` so they serialize correctly with freezed's `json_serializable`. These enums are used inside `@freezed` models so they must be in separate files imported by the model files.

---

## Repositories — Specifications

### task_repository.dart
All methods use `dioClientProvider`. Key methods:

| Method | Endpoint | Notes |
|--------|----------|-------|
| `getTasks({page, size, status, priority, assignedTo, tags, search})` | `GET /tasks` | Returns raw `Map` — provider parses pagination |
| `getTaskById(id)` | `GET /tasks/{id}` | Returns `TaskModel` |
| `createTask(CreateTaskRequest)` | `POST /tasks` | Returns `TaskModel` |
| `updateTask(id, UpdateTaskRequest)` | `PUT /tasks/{id}` | Returns `TaskModel` |
| `deleteTask(id)` | `DELETE /tasks/{id}` | Returns `void` |
| `updateTaskStatus(id, UpdateTaskStatusRequest)` | `PATCH /tasks/{id}/status` | Returns `TaskModel` |
| `getMyTasks({page, size, status})` | `GET /tasks/my-tasks` | Returns raw `Map` |
| `getOverdueTasks()` | `GET /tasks/overdue` | Returns `List<TaskModel>` |
| `searchTasks(query)` | `GET /tasks/search?q=query` | Returns `List<TaskModel>` |

### task_comment_repository.dart
| Method | Endpoint |
|--------|----------|
| `getComments(taskId)` | `GET /tasks/{id}/comments` → `List<TaskCommentModel>` |
| `addComment(taskId, TaskCommentRequest)` | `POST /tasks/{id}/comments` → `TaskCommentModel` |

### task_attachment_repository.dart
| Method | Endpoint | Notes |
|--------|----------|-------|
| `getAttachments(taskId)` | `GET /tasks/{id}/attachments` → `List<TaskAttachmentModel>` | |
| `uploadAttachment(taskId, File file)` | `POST /tasks/{id}/attachments` multipart | Native only |
| `uploadAttachmentBytes(taskId, bytes, filename)` | `POST /tasks/{id}/attachments` multipart | Web |

> File upload uses same `kIsWeb` + conditional import pattern as `profile_photo_provider.dart` in Phase 2. The multipart field name must be confirmed against the Postman collection (expected `"file"` — verify).

---

## Providers — Specifications

### task_list_provider.dart
`@riverpod` `AsyncNotifier`. Same cache-then-network pattern as `employee_list_provider.dart` from Phase 2 but uses `tasks_box` from `CacheService`. Supports:
- `refresh()` — pull-to-refresh
- `loadMore()` — infinite scroll pagination
- `applyFilter(TaskListFilter)` — filter by status, priority, assignedTo, tags
- `removeTask(id)` — in-place removal after delete
- `updateTask(TaskModel)` — in-place update after status change or edit

`TaskListFilter` class: `status?`, `priority?`, `assignedTo?`, `tags?` (`List<String>?`), `search?`.

### my_tasks_provider.dart
`@riverpod` `AsyncNotifier`. Same pattern but calls `getMyTasks()` and uses `my_tasks_box`. Supports `refresh()`, `loadMore()`, `applyFilter(status?)`, `updateTask(TaskModel)`.

### overdue_tasks_provider.dart
`@riverpod` simple `Future` provider. Calls `getOverdueTasks()`. No caching — always fresh. Invalidated when `task_list_provider` refreshes.

### task_detail_provider.dart
`@riverpod` family provider. `Future<TaskModel> taskDetail(ref, String id)`. Invalidated after status change, edit, or delete.

### task_comments_provider.dart
`@riverpod` family `AsyncNotifier`. `build(String taskId)` fetches comments. `addComment(content)` calls repository, appends to list optimistically. No cache.

### task_attachments_provider.dart
`@riverpod` family `AsyncNotifier`. `build(String taskId)` fetches attachments. `uploadAttachment(taskId, XFile)` handles `kIsWeb` branching internally. Appends on success.

### task_history_provider.dart
`@riverpod` family provider. `Future<List<TaskStatusHistoryModel>> taskHistory(ref, String taskId)`. Simple fetch, no cache.

### task_search_provider.dart
`@riverpod` family provider. `Future<List<TaskModel>> taskSearch(ref, String query)`. Returns `[]` for blank query.

### task_actions_provider.dart
`@riverpod` `AsyncNotifier<void>`. Handles all mutations: `createTask`, `updateTask`, `deleteTask`, `updateTaskStatus`. After each mutation updates `taskListNotifierProvider`, `myTasksNotifierProvider`, and invalidates `taskDetailProvider(id)` as appropriate.

### pending_actions_provider.dart
`@riverpod` `Notifier<List<PendingActionModel>>`. In-memory queue of offline actions. Listens to `connectivityProvider` — when connectivity restores, replays queued actions in order via `task_actions_provider`. On replay success, removes from queue. On replay failure, keeps in queue and shows snackbar. The `PendingActionModel` is never persisted to Hive — queue is lost on app restart (acceptable for Phase 3; persistence is a future enhancement).

---

## Widgets — Specifications

### task_list_tile.dart
`StatelessWidget`. Params: `TaskModel task`, `VoidCallback onTap`, `VoidCallback? onDelete` (admin only). Shows: title, `TaskStatusBadge`, `TaskPriorityBadge`, assignee name, due date. If `task.isOverdue` is true, show `OverdueIndicator` inline. Trailing: `PopupMenuButton` with "View" and optionally "Delete".

### task_status_badge.dart
`StatelessWidget`. Param: `TaskStatus status`. Uses `AppBadge` with `status.color` and `status.label`. Pure display — no providers.

### task_priority_badge.dart
`StatelessWidget`. Param: `TaskPriority priority`. Uses `AppBadge` with `priority.color`. Shows `priority.icon` + `priority.label` side by side inside the badge.

### task_filter_bar.dart
`StatelessWidget`. Params: `TaskListFilter filter`, `ValueChanged<TaskListFilter> onFilterChanged`. Horizontal scrollable row of `FilterChip` groups — one for status (All, Todo, In Progress, In Review, Done, Cancelled), one for priority (All, Low, Medium, High, Critical). Selected chip uses `AppColors.primary.withOpacity(0.15)`.

### task_form.dart
`StatefulWidget`. Params: `TaskModel? initialTask` (null = create), `Future<void> Function(CreateTaskRequest) onSubmit`, `bool isLoading`. Fields: `title*` (max 200), `description` (max 5000, multiline), `priority*` via `PrioritySelector`, `assignedTo*` via `AssigneePicker`, `dueDate*` via `DueDatePicker`, `estimatedHours` (numeric), `tags` via `TagsInputField`. Validation: title not empty, due date must be in the future, assignee must be selected. For edit mode, pre-fill all fields from `initialTask`.

### assignee_picker.dart
`ConsumerStatefulWidget`. Params: `String? initialAssigneeId`, `ValueChanged<String?> onChanged`. Uses `employeeSearchProvider` from Phase 2 (cross-feature import — this is intentional and correct). Shows a read-only `TextFormField` that opens a modal bottom sheet on tap. Bottom sheet has `EmployeeSearchBar` + `ListView` of ACTIVE employees only. Shows employee name + designation in each list item.

### due_date_picker.dart
`StatefulWidget`. Params: `DateTime? initialDate`, `ValueChanged<DateTime?> onChanged`. Read-only `TextFormField` showing formatted date. On tap, opens `showDatePicker` with `firstDate: DateTime.now()` (prevents past dates — FR-TASK-02). Displays selected date using `AppDateUtils.formatDate`.

### priority_selector.dart
`StatelessWidget`. Params: `TaskPriority? selected`, `ValueChanged<TaskPriority> onChanged`. Row of 4 `ChoiceChip` widgets — one per priority. Each shows `priority.icon` + `priority.label`. Selected chip uses priority color as background.

### status_transition_selector.dart
`StatelessWidget`. Params: `TaskStatus currentStatus`, `ValueChanged<TaskStatus> onChanged`. Shows only `currentStatus.allowedTransitions` as selectable options. If `allowedTransitions` is empty (terminal status), shows a disabled message "No further transitions available". Uses `SegmentedButton` or `ChoiceChip` row. This widget is the single source of truth for valid transitions — do not duplicate the logic in screens.

### task_comment_tile.dart
`StatelessWidget`. Param: `TaskCommentModel comment`. Shows: `CircleAvatar` with author initials, author name (bold), comment content, timestamp via `AppDateUtils.timeAgo`. Comments are immutable — no edit/delete controls.

### task_comments_section.dart
`ConsumerStatefulWidget`. Param: `String taskId`. Watches `taskCommentsProvider(taskId)`. Shows `ListView` of `TaskCommentTile` widgets + a text input row at the bottom with a send button. On send: calls `ref.read(taskCommentsProvider(taskId).notifier).addComment(content)`. Clears input on success. Shows loading indicator on send button while submitting.

### task_attachment_tile.dart
`StatelessWidget`. Param: `TaskAttachmentModel attachment`. Shows: file icon (based on mimeType), `fileName`, file size formatted (KB/MB), uploader name, timestamp. Tap opens the `fileUrl` in browser/external app via `url_launcher` (add to pubspec if not present — check first).

### task_attachments_section.dart
`ConsumerStatefulWidget`. Param: `String taskId`. Watches `taskAttachmentsProvider(taskId)`. Shows `ListView` of `TaskAttachmentTile` + an upload button. On upload: uses `file_picker` (already in pubspec) to pick file, then calls `taskAttachmentsProvider(taskId).notifier.uploadAttachment`. Shows upload progress indicator.

### task_status_history_timeline.dart
`ConsumerWidget`. Param: `String taskId`. Watches `taskHistoryProvider(taskId)`. Same timeline layout as `StatusHistoryTimeline` from Phase 2 but uses `TaskStatusHistoryModel`. Each row shows: `oldStatus → newStatus`, actor name, optional comment, timestamp.

### overdue_indicator.dart
`StatelessWidget`. No params — purely presentational, displayed when `task.isOverdue == true`. Small red row with `Icons.warning_amber_rounded` + "Overdue" text in `AppColors.error`. Used inline inside `TaskListTile` and at the top of task detail screens.

### tags_input_field.dart
`StatefulWidget`. Params: `List<String> initialTags`, `ValueChanged<List<String>> onChanged`. Shows existing tags as deletable `Chip` widgets in a `Wrap`. Below chips: a `TextField` — on submit (enter key or comma), adds the trimmed text as a new tag chip, clears the input. Duplicate tags are silently ignored.

---

## Screens — Specifications

### admin/admin_task_list_screen.dart
`ConsumerStatefulWidget`. Watches `taskListNotifierProvider`. AppBar with title "Tasks" + "+" button navigating to `AppRoutes.adminCreateTask`. Body: `TaskFilterBar` + `ListView.builder` with infinite scroll (same `ScrollController` pattern as `EmployeeListScreen` from Phase 2). Each item: `TaskListTile`. Pull-to-refresh. `ref.listen` on `taskActionsProvider` for error snackbars. Shows overdue count badge in AppBar if `overdueTasks` list is non-empty.

### admin/admin_task_detail_screen.dart
`ConsumerWidget`. Param: `String taskId`. Watches `taskDetailProvider(taskId)`. Sections:
1. Title + `OverdueIndicator` (if overdue) + `TaskStatusBadge` + `TaskPriorityBadge`
2. Info rows: assignee, due date, estimated hours, tags, created by
3. `StatusTransitionSelector` — shown to ADMIN always; `TaskActionsProvider.updateTaskStatus` called on selection
4. `TaskCommentsSection(taskId)`
5. `TaskAttachmentsSection(taskId)`
6. `TaskStatusHistoryTimeline(taskId)`
7. Edit button in AppBar → `AppRoutes.adminEditTaskPath(taskId)`
8. Delete in AppBar overflow → `AppConfirmDialog` → delete → back to list

### admin/create_task_screen.dart
`ConsumerWidget`. AppBar "Create Task". Body: `TaskForm(onSubmit: ...)`. On success: snackbar "Task created and assigned to [name]" + `context.go(AppRoutes.adminTasks)`.

### admin/edit_task_screen.dart
`ConsumerWidget`. Param: `String taskId`. Loads `taskDetailProvider(taskId)` first. Body: `TaskForm(initialTask: task, onSubmit: ...)`. Maps `CreateTaskRequest` to `UpdateTaskRequest` via extension (same pattern as Phase 2 employee form). On success: snackbar + `context.go(AppRoutes.adminTaskDetailPath(taskId))`.

### employee/my_tasks_screen.dart
`ConsumerStatefulWidget`. Watches `myTasksNotifierProvider`. AppBar "My Tasks". Filter bar (status only — no assignee filter for employee view). `ListView` of `TaskListTile` — tapping navigates to `AppRoutes.employeeTaskDetailPath(task.id)`. No create/delete buttons. Pull-to-refresh. Infinite scroll.

### employee/employee_task_detail_screen.dart
`ConsumerWidget`. Param: `String taskId`. Watches `taskDetailProvider(taskId)`. Sections:
1. Title + `OverdueIndicator` + `TaskStatusBadge` + `TaskPriorityBadge`
2. Info rows (read-only): assignee (always self), due date, estimated hours, tags
3. `StatusTransitionSelector` — shown only for own tasks, calls `updateTaskStatus`
4. `TaskCommentsSection(taskId)`
5. `TaskAttachmentsSection(taskId)`
6. `TaskStatusHistoryTimeline(taskId)`

> No edit, delete, or reassign controls for EMPLOYEE role.

---

## Router Extension — app_router.dart

Two placeholder branches must be replaced. The `adminTasks` branch and the `employeeTasks` branch.

**Replace `adminTasks` placeholder:**
```dart
// BEFORE (Phase 1 placeholder)
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.adminTasks,
    builder: (_, __) => const _PlaceholderScreen(title: 'Tasks'),
  ),
]),

// AFTER (Phase 3)
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.adminTasks,
    builder: (_, __) => const AdminTaskListScreen(),
    routes: [
      GoRoute(
        path: 'create',
        builder: (_, __) => const CreateTaskScreen(),
      ),
      GoRoute(
        path: ':id',
        builder: (_, state) =>
            AdminTaskDetailScreen(taskId: state.pathParameters['id']!),
        routes: [
          GoRoute(
            path: 'edit',
            builder: (_, state) =>
                EditTaskScreen(taskId: state.pathParameters['id']!),
          ),
        ],
      ),
    ],
  ),
]),
```

**Replace `employeeTasks` placeholder:**
```dart
// BEFORE
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.employeeTasks,
    builder: (_, __) => const _PlaceholderScreen(title: 'My Tasks'),
  ),
]),

// AFTER
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.employeeTasks,
    builder: (_, __) => const MyTasksScreen(),
    routes: [
      GoRoute(
        path: ':id',
        builder: (_, state) =>
            EmployeeTaskDetailScreen(taskId: state.pathParameters['id']!),
      ),
    ],
  ),
]),
```

**Add these imports to `app_router.dart`:**
```dart
import '../../features/task/presentation/screens/admin/admin_task_list_screen.dart';
import '../../features/task/presentation/screens/admin/admin_task_detail_screen.dart';
import '../../features/task/presentation/screens/admin/create_task_screen.dart';
import '../../features/task/presentation/screens/admin/edit_task_screen.dart';
import '../../features/task/presentation/screens/employee/my_tasks_screen.dart';
import '../../features/task/presentation/screens/employee/employee_task_detail_screen.dart';
```

---

## Offline Caching Behaviour (Phase 3)

| Action | Cache Behaviour |
|--------|----------------|
| Open admin task list | Show `tasks_box` instantly → background fetch → update silently |
| Open employee my tasks | Show `my_tasks_box` instantly → background fetch → update silently |
| Pull-to-refresh | Fetch → update Hive |
| Create / edit task | Refresh list (cache updated after) |
| Update status | In-place list update (`updateTask`) — no full re-fetch |
| Delete task | `removeTask(id)` in list state — cache re-synced on next load |
| Comments / attachments | Never cached — always fresh (they are fetched per-task on detail view) |
| Offline status change | Queued in `pendingActionsProvider` — replayed on reconnect |
| Offline + no cache | `AppErrorWidget` with retry |
| Offline + cache exists | Show cached list; offline banner shown globally by `app.dart` |

---

## Cross-Feature Dependencies

Phase 3 has intentional dependencies on Phase 2 — this is by design:

| Phase 3 File | Phase 2 Dependency | Why |
|---|---|---|
| `assignee_picker.dart` | `employeeSearchProvider` | Task assignee must be an active employee |
| `assignee_picker.dart` | `EmployeeModel` | Displays employee name + designation in list |
| `task_form.dart` | `AssigneePicker` widget | Embeds the assignee picker |
| `create_task_screen.dart` | `EmployeeModel` (via form) | Assignee ID comes from employee selection |

These imports cross the `task/` and `employee/` feature boundaries. This is acceptable — the project uses feature-based folders, not strict module isolation. The dependency only flows one way: task → employee. Employee never imports from task.

---

## Sub-Phase Split (Recommended Build Order)

Given Phase 3 has ~32 files, build in 5 sub-phases with `flutter analyze` passing between each:

| Sub-Phase | Files | Dependency |
|-----------|-------|------------|
| **3A** | 2 enums + 9 models → run build_runner | Nothing outside Phase 1+2 |
| **3B** | 3 repositories + 10 providers | Needs 3A generated files |
| **3C** | 16 widgets | Needs 3A + 3B |
| **3D** | 4 admin screens + router (admin branches only) | Needs 3C |
| **3E** | 2 employee screens + router (employee branches) + full verify | Needs 3D |

> **3A note**: Enums must be created before models because models import them. Within 3A, create enums first, then models.
> **3D/3E split**: Same safe boundary as Phase 2 — router branch only added when its screen exists.

---

## Phase 3 File Count

| Layer | Count |
|-------|-------|
| Enums | 2 |
| Models | 9 |
| Repositories | 3 |
| Providers | 10 |
| Widgets | 16 |
| Admin Screens | 4 |
| Employee Screens | 2 |
| Router (modified) | 1 (extends Phase 2) |
| **Total New Files** | **46** |

> Generated files (`.freezed.dart`, `.g.dart`) are not counted.
> No platform config changes needed — file picker and attachment permissions were pre-declared in Phase 1 (`AndroidManifest.xml` and `Info.plist` from Master Context).
> `url_launcher` may be needed for opening attachments — check pubspec before Sub-Phase 3C. If missing, add it then and run `flutter pub get`.
