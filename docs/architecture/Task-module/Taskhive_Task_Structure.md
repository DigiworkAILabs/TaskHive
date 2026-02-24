# TaskHive — Phase 3: Task Module
## SRS v2.3 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 3 of 6 |
| **Name** | Task Module |
| **Modules** | Task Module (Backend + Frontend) |
| **DB Migrations** | V3.0 + V3.1 + V3.2 + V3.3 |
| **Duration** | ~2 weeks |
| **Depends On** | Phase 1 (Auth) + Phase 2 (Employee) |

### Deliverable
Admin can create and assign tasks to active employees. Employees see only their assigned tasks. Status changes are fully tracked in history. Comments and file attachments work. Overdue task detection runs as a daily scheduled job. Task search with full-text is supported.

---

## Task Status Flow

```
TODO → IN_PROGRESS → IN_REVIEW → DONE
         ↓               ↓         ↓
      CANCELLED       CANCELLED  CANCELLED
```

## Task Priority Levels
`LOW` | `MEDIUM` | `HIGH` | `CRITICAL`

---

## API Endpoints (Task Module)

```
POST   /api/v1/tasks                        # Create task (ADMIN only)
GET    /api/v1/tasks                        # All tasks with filters (ADMIN only)
GET    /api/v1/tasks/{id}                  # Task detail
PUT    /api/v1/tasks/{id}                  # Update task (ADMIN only)
DELETE /api/v1/tasks/{id}                  # Soft delete (ADMIN only)
PATCH  /api/v1/tasks/{id}/status           # Update task status
POST   /api/v1/tasks/{id}/comments         # Add comment
GET    /api/v1/tasks/{id}/comments         # Get comments
POST   /api/v1/tasks/{id}/attachments      # Upload attachment
GET    /api/v1/tasks/{id}/attachments      # List attachments
GET    /api/v1/tasks/{id}/history          # Task status history
GET    /api/v1/tasks/my-tasks              # Logged-in employee's tasks
GET    /api/v1/tasks/overdue               # Overdue tasks
GET    /api/v1/tasks/search                # Search tasks
```

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-TASK-01 | Critical | ADMIN shall create tasks (title max 200 chars, description max 5000 chars, priority, dueDate, assignedTo, estimatedHours optional, tags optional) |
| FR-TASK-02 | Critical | Task due date must be a future date at creation time |
| FR-TASK-03 | Critical | Assignee must be an active employee |
| FR-TASK-04 | Critical | Tasks shall support statuses: TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED |
| FR-TASK-05 | Critical | Tasks shall support priorities: LOW, MEDIUM, HIGH, CRITICAL |
| FR-TASK-06 | High | ADMIN shall reassign tasks to different employees |
| FR-TASK-07 | High | EMPLOYEE shall update status of their own assigned tasks only |
| FR-TASK-08 | High | ADMIN shall update status of any task |
| FR-TASK-09 | Medium | Both ADMIN and EMPLOYEE shall add comments to tasks (comments are immutable) |
| FR-TASK-10 | Medium | Users shall upload file attachments to tasks (image-jpeg, image-png, application-pdf, application-docx)|
| FR-TASK-11 | High | System shall maintain full status change history (old status, new status, actor, comment, IP, timestamp) |
| FR-TASK-12 | High | System shall detect and flag overdue tasks via daily scheduled job |
| FR-TASK-13 | Medium | Overdue escalation: 1 day — warning notification; 3 days — manager notification; 7 days — admin escalation |
| FR-TASK-14 | Critical | EMPLOYEE shall view only their assigned tasks |
| FR-TASK-15 | Critical | ADMIN shall view all tasks with filters (status, priority, assignee, date range, tags) |
| FR-TASK-16 | Medium | System shall support task search by title and description (full-text) |
| FR-TASK-17 | High | Soft delete shall be used for tasks |

---

## Backend File & Folder Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/digiwork/taskhive/
│   │       └── module/
│   │           └── task/                                  # TASK MODULE
│   │               ├── controller/
│   │               │   ├── TaskController.java
│   │               │   ├── TaskCommentController.java
│   │               │   └── TaskAttachmentController.java
│   │               │
│   │               ├── service/
│   │               │   ├── TaskService.java
│   │               │   ├── TaskCommentService.java
│   │               │   ├── TaskAttachmentService.java
│   │               │   ├── TaskStatusHistoryService.java
│   │               │   └── TaskSearchService.java
│   │               │
│   │               ├── dto/
│   │               │   ├── CreateTaskRequest.java
│   │               │   ├── UpdateTaskRequest.java
│   │               │   ├── UpdateTaskStatusRequest.java
│   │               │   ├── TaskResponse.java
│   │               │   ├── TaskListResponse.java
│   │               │   ├── TaskCommentRequest.java
│   │               │   ├── TaskCommentResponse.java
│   │               │   ├── TaskAttachmentResponse.java
│   │               │   ├── TaskStatusHistoryResponse.java
│   │               │   └── TaskSearchRequest.java
│   │               │
│   │               ├── model/
│   │               │   ├── Task.java                      # id, title, description, status, priority, assignedTo, dueDate, completedAt, estimatedHours, tags, version, createdBy, updatedBy
│   │               │   ├── TaskComment.java               # id, taskId, authorId, content, createdAt
│   │               │   ├── TaskAttachment.java            # id, taskId, uploadedBy, fileName, fileUrl, fileSize, mimeType, createdAt
│   │               │   └── TaskStatusHistory.java         # id, taskId, oldStatus, newStatus, changedBy, comment, ipAddress, changedAt
│   │               │
│   │               ├── repository/
│   │               │   ├── TaskRepository.java
│   │               │   ├── TaskCommentRepository.java
│   │               │   ├── TaskAttachmentRepository.java
│   │               │   └── TaskStatusHistoryRepository.java
│   │               │
│   │               ├── event/
│   │               │   ├── TaskCreatedEvent.java
│   │               │   ├── TaskUpdatedEvent.java
│   │               │   ├── TaskStatusChangedEvent.java
│   │               │   ├── TaskAssignedEvent.java
│   │               │   ├── TaskCommentAddedEvent.java
│   │               │   └── TaskOverdueEvent.java
│   │               │
│   │               ├── enums/
│   │               │   ├── TaskStatus.java                # TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED
│   │               │   └── TaskPriority.java              # LOW, MEDIUM, HIGH, CRITICAL
│   │               │
│   │               ├── exception/
│   │               │   ├── TaskNotFoundException.java
│   │               │   └── TaskAccessDeniedException.java
│   │               │
│   │               ├── mapper/
│   │               │   └── TaskMapper.java
│   │               │
│   │               └── scheduler/
│   │                   └── TaskOverdueScheduler.java      # Daily job to detect overdue tasks
│   │
│   └── resources/
│       ├── db/
│       │   └── migration/
│       │       ├── V3.0__create_tasks_table.sql
│       │       ├── V3.1__create_task_comments_table.sql
│       │       ├── V3.2__create_task_attachments_table.sql
│       │       └── V3.3__create_task_status_history_table.sql
│       │
│       └── templates/
│           └── email/
│               ├── task-assigned.html
│               └── task-overdue.html
│
└── test/
    └── java/
        └── com/digiwork/taskhive/
            ├── module/
            │   └── task/
            │       ├── controller/
            │       │   └── TaskControllerTest.java
            │       └── service/
            │           └── TaskServiceTest.java
            │
            └── integration/
                └── TaskIntegrationTest.java
```

---

## Frontend File & Folder Structure

```
src/
├── app/
│   ├── (admin)/
│   │   └── tasks/
│   │       ├── page.tsx                       # All tasks (ADMIN)
│   │       ├── new/page.tsx
│   │       └── [id]/page.tsx
│   │
│   └── (employee)/                            # Employee routes
│       ├── layout.tsx
│       ├── dashboard/page.tsx
│       └── tasks/
│           ├── page.tsx                       # My tasks only
│           └── [id]/page.tsx
│  
└── features/
    └── task/
        ├── components/
        │   ├── TaskList.tsx
        │   ├── TaskCard.tsx
        │   ├── TaskForm.tsx
        │   ├── TaskDetail.tsx
        │   ├── TaskStatusBadge.tsx
        │   ├── TaskPriorityBadge.tsx
        │   ├── TaskComments.tsx
        │   ├── TaskAttachments.tsx
        │   └── TaskStatusHistory.tsx
        ├── hooks/
        │   ├── useTasks.ts
        │   ├── useTask.ts
        │   ├── useCreateTask.ts
        │   ├── useUpdateTask.ts
        │   ├── useUpdateTaskStatus.ts
        │   ├── useMyTasks.ts
        │   ├── useOverdueTasks.ts
        │   └── useTaskSearch.ts
        ├── services/
        │   └── taskService.ts
        └── types/
            └── task.types.ts
```

---

## DB Migrations — Phase 3

| File | Description |
|------|-------------|
| `V3.0__create_tasks_table.sql` | tasks — id, title, description, status, priority, assigned_to (FK employees), due_date, completed_at, estimated_hours, tags (array), is_deleted, deleted_at, version, created_at, updated_at, created_by, updated_by |
| `V3.1__create_task_comments_table.sql` | task_comments — id, task_id (FK), author_id (FK users), content, created_at |
| `V3.2__create_task_attachments_table.sql` | task_attachments — id, task_id (FK), uploaded_by (FK users), file_name, file_url, file_size, mime_type, created_at |
| `V3.3__create_task_status_history_table.sql` | task_status_history — id, task_id (FK), old_status, new_status, changed_by (FK users), comment, ip_address, changed_at |

---

## Events Published (consumed by Notification Module in Phase 4)

| Event Class | Trigger | Consumer |
|-------------|---------|----------|
| `TaskCreatedEvent` | Admin creates task | AuditModule → audit log |
| `TaskUpdatedEvent` | Admin updates task | AuditModule → audit log |
| `TaskStatusChangedEvent` | Status updated | NotificationModule → in-app notification |
| `TaskAssignedEvent` | Task assigned to employee | NotificationModule → in-app + email notification |
| `TaskCommentAddedEvent` | Comment added to task | NotificationModule → in-app notification |
| `TaskOverdueEvent` | Daily scheduler detects overdue task | NotificationModule → in-app + email escalation |

---

## Phase 3 File Count Summary

| Layer | Backend | Frontend |
|-------|---------|----------|
| Controller | 3 | — |
| Service | 5 | — |
| DTO | 10 | — |
| Model | 4 | — |
| Repository | 4 | — |
| Event | 6 | — |
| Enum | 2 | — |
| Exception | 2 | — |
| Mapper | 1 | — |
| Scheduler | 1 | — |
| DB Migrations | 4 | — |
| Email Templates | 2 | — |
| Tests | 3 | — |
| App Pages | — | 5 |
| Feature Components | — | 9 |
| Feature Hooks | — | 8 |
| Feature Services | — | 1 |
| Feature Types | — | 1 |
