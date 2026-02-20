# Software Requirements Specification (SRS)
## TaskHive - Enterprise Task Management System
### Version 2.0 - Modular Monolithic Architecture

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 2.0 | 2026-02-14 | System Architect | Enhanced with modular architecture, ML integration, microservice readiness |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [System Architecture](#2-system-architecture)
3. [Module Definitions](#3-module-definitions)
4. [Functional Requirements](#4-functional-requirements)
5. [Non-Functional Requirements](#5-non-functional-requirements)
6. [Data Architecture](#6-data-architecture)
7. [Integration Architecture](#7-integration-architecture)
8. [Security Architecture](#8-security-architecture)
9. [ML/AI Architecture](#9-mlai-architecture)
10. [Testing Strategy](#10-testing-strategy)
11. [Deployment Architecture](#11-deployment-architecture)
12. [Migration Path to Microservices](#12-migration-path-to-microservices)

---

## 1. Introduction

### 1.1 Purpose

This document specifies the requirements and architectural design for TaskHive, an enterprise-grade task management system built with a **modular monolithic architecture** that enables seamless migration to microservices as business needs evolve.

### 1.2 Scope

TaskHive provides comprehensive task management capabilities including:
- Employee lifecycle management
- Task assignment and tracking with complete audit trails
- Real-time notifications and collaboration
- Advanced analytics and reporting
- AI-powered insights (future)
- Multi-tenant capabilities (future)

### 1.3 Technology Stack

**Backend (Spring Boot 3.x - Java 17+)**
- Framework: Spring Boot with modular package structure
- Security: Spring Security 6.x (JWT + Refresh Token)
- Real-time: WebSocket (STOMP over SockJS)
- Scheduling: Spring Scheduler
- Data Access: Spring Data JPA
- Validation: Hibernate Validator
- API Documentation: SpringDoc OpenAPI 3
- Event Bus: Spring Events (internal messaging)

**Frontend (Next.js 14+ with App Router)**
- Framework: Next.js 14+ (React 18+)
- Language: TypeScript
- UI Components: Shadcn/ui + Tailwind CSS
- State Management: Zustand / React Query
- Charts: Recharts / Chart.js
- Real-time: SockJS Client
- Form Management: React Hook Form + Zod

**Database**
- Primary: PostgreSQL 15+
- Caching: Redis (future)
- Search: PostgreSQL Full-Text Search (ElasticSearch future option)

**ML/AI Infrastructure (Placeholder)**
- Training: Python 3.11+, PyTorch/TensorFlow, Scikit-learn
- Serving: FastAPI inference server
- Communication: REST API / gRPC (future)

**DevOps & Monitoring**
- Containerization: Docker
- Logging: SLF4J + Logback
- Monitoring: Actuator endpoints (Prometheus/Grafana ready)
- Testing: JUnit 5, Mockito, Testcontainers

### 1.4 Architectural Principles

1. **Module Independence**: Each module is self-contained with minimal coupling
2. **Clear Boundaries**: Well-defined interfaces between modules
3. **Database per Module Pattern**: Logical schema separation (physical separation optional)
4. **Event-Driven Communication**: Modules communicate via domain events
5. **Shared Kernel**: Common utilities and cross-cutting concerns
6. **API Gateway Pattern**: Single entry point with module routing
7. **Strangler Fig Pattern Ready**: Gradual microservice extraction capability

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend Layer (Next.js)                 │
├──────────────┬──────────────┬──────────────┬────────────────┤
│ Auth Module  │ Employee Mod │  Task Module │ Analytics Mod  │
└──────┬───────┴──────┬───────┴──────┬───────┴────────┬───────┘
       │              │              │                │
       └──────────────┴──────────────┴────────────────┘
                          │
              ┌───────────▼───────────┐
              │   API Gateway Layer   │
              │   (Spring MVC)        │
              └───────────┬───────────┘
                          │
       ┌──────────────────┼──────────────────┐
       │                  │                  │
┌──────▼─────┐    ┌──────▼─────┐    ┌──────▼─────┐
│   Auth     │    │  Employee  │    │    Task    │
│   Module   │    │   Module   │    │   Module   │
└──────┬─────┘    └──────┬─────┘    └──────┬─────┘
       │                 │                  │
       └─────────────────┼──────────────────┘
                         │
              ┌──────────▼──────────┐
              │  Shared Kernel      │
              │  - Common Utils     │
              │  - Event Bus        │
              │  - Audit Service    │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │   PostgreSQL DB     │
              │   (Logical Schemas) │
              └─────────────────────┘
```

### 2.2 Backend Module Structure

Each backend module follows this internal structure:

```
com.taskhive.{module-name}/
├── api/                    # REST Controllers (API Layer)
│   ├── controller/
│   ├── dto/               # Request/Response DTOs
│   └── mapper/            # DTO ↔ Domain mapping
├── domain/                # Core Business Logic
│   ├── model/            # Domain entities
│   ├── service/          # Business services
│   ├── event/            # Domain events
│   └── exception/        # Module-specific exceptions
├── infrastructure/        # Technical Implementation
│   ├── repository/       # Data access
│   ├── config/           # Module configuration
│   └── integration/      # External integrations
└── ModuleConfiguration.java  # Spring Configuration
```

### 2.3 Frontend Module Structure

```
src/
├── features/              # Feature modules (aligned with backend)
│   ├── auth/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── types/
│   │   └── store/
│   ├── employee/
│   ├── task/
│   └── analytics/
├── shared/               # Shared kernel (frontend)
│   ├── components/
│   ├── hooks/
│   ├── utils/
│   ├── types/
│   └── services/
├── lib/                  # Third-party configs
└── app/                  # Next.js app router
```

### 2.4 ML Infrastructure Structure

```
ml/
├── inference-server/     # Production serving
│   ├── api/             # FastAPI endpoints
│   ├── models/          # Model loading
│   ├── preprocessing/
│   └── Dockerfile
├── training/            # Model development
│   ├── data/
│   ├── notebooks/
│   ├── experiments/
│   ├── models/
│   └── pipelines/
└── shared/              # Common ML utilities
    ├── features/
    ├── evaluation/
    └── utils/
```

---

## 3. Module Definitions

### 3.1 Core Business Modules

#### 3.1.1 Authentication & Authorization Module (auth-module)

**Responsibility**: User authentication, authorization, session management

**Bounded Context**:
- User credentials and authentication
- JWT token lifecycle
- Password management
- Role-based access control
- Session management

**Key Components**:
- AuthController
- AuthenticationService
- TokenService
- PasswordService
- UserDetailsService

**Database Schema**: `auth_schema`
- Tables: users, roles, user_roles, refresh_tokens, password_reset_tokens

**Domain Events**:
- UserAuthenticatedEvent
- UserLoggedOutEvent
- PasswordChangedEvent
- PasswordResetRequestedEvent

**External Dependencies**: Email service (notification module)

**API Endpoints**:
```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
POST   /api/v1/auth/change-password
GET    /api/v1/auth/me
```

---

#### 3.1.2 Employee Management Module (employee-module)

**Responsibility**: Employee lifecycle, profile management, organizational hierarchy

**Bounded Context**:
- Employee profiles and personal information
- Employee activation/deactivation
- Profile photo management
- Employee search and filtering

**Key Components**:
- EmployeeController
- EmployeeService
- EmployeeRepository
- ProfilePhotoService
- EmployeeSearchService

**Database Schema**: `employee_schema`
- Tables: employees, employee_profiles, profile_photos, employee_status_history

**Domain Events**:
- EmployeeCreatedEvent
- EmployeeUpdatedEvent
- EmployeeActivatedEvent
- EmployeeDeactivatedEvent
- EmployeeDeletedEvent (soft)

**External Dependencies**: 
- Auth module (user account creation)
- Notification module (invitation emails)
- File storage service

**API Endpoints**:
```
POST   /api/v1/employees
GET    /api/v1/employees
GET    /api/v1/employees/{id}
PUT    /api/v1/employees/{id}
DELETE /api/v1/employees/{id}
PATCH  /api/v1/employees/{id}/activate
PATCH  /api/v1/employees/{id}/deactivate
POST   /api/v1/employees/{id}/photo
GET    /api/v1/employees/{id}/photo
GET    /api/v1/employees/search
```

---

#### 3.1.3 Task Management Module (task-module)

**Responsibility**: Task lifecycle, assignment, tracking, status management

**Bounded Context**:
- Task creation and assignment
- Task status transitions
- Task priority and deadlines
- Task comments and collaboration
- Task status history

**Key Components**:
- TaskController
- TaskService
- TaskAssignmentService
- TaskStatusService
- TaskCommentService
- TaskHistoryService
- DeadlineScheduler

**Database Schema**: `task_schema`
- Tables: tasks, task_assignments, task_status_history, task_comments, task_attachments

**Domain Events**:
- TaskCreatedEvent
- TaskAssignedEvent
- TaskStatusChangedEvent
- TaskUpdatedEvent
- TaskDeletedEvent (soft)
- TaskOverdueEvent
- CommentAddedEvent

**External Dependencies**:
- Employee module (assignee validation)
- Notification module (task notifications)
- Audit module (change tracking)

**API Endpoints**:
```
POST   /api/v1/tasks
GET    /api/v1/tasks
GET    /api/v1/tasks/{id}
PUT    /api/v1/tasks/{id}
DELETE /api/v1/tasks/{id}
PATCH  /api/v1/tasks/{id}/status
POST   /api/v1/tasks/{id}/comments
GET    /api/v1/tasks/{id}/history
GET    /api/v1/tasks/{id}/timeline
GET    /api/v1/tasks/my-tasks
GET    /api/v1/tasks/overdue
```

---

#### 3.1.4 Analytics & Reporting Module (analytics-module)

**Responsibility**: Data aggregation, report generation, dashboard metrics

**Bounded Context**:
- Task statistics and metrics
- Employee performance analytics
- Trend analysis
- Report generation and export
- Dashboard data provisioning

**Key Components**:
- AnalyticsController
- AnalyticsService
- ReportGenerator
- MetricsAggregator
- ExportService (CSV, Excel, PDF)

**Database Schema**: `analytics_schema`
- Tables: daily_metrics, employee_performance_cache, report_templates

**Domain Events**:
- ReportGeneratedEvent
- MetricsCalculatedEvent

**External Dependencies**:
- Task module (task data)
- Employee module (employee data)

**API Endpoints**:
```
GET    /api/v1/analytics/dashboard/admin
GET    /api/v1/analytics/dashboard/employee/{id}
GET    /api/v1/analytics/tasks/distribution
GET    /api/v1/analytics/tasks/by-priority
GET    /api/v1/analytics/tasks/completion-trend
GET    /api/v1/analytics/employees/performance
GET    /api/v1/analytics/tasks/overdue-analysis
POST   /api/v1/analytics/reports/export
GET    /api/v1/analytics/reports/{id}/download
```

---

#### 3.1.5 Notification Module (notification-module)

**Responsibility**: Multi-channel notifications, email service, real-time updates

**Bounded Context**:
- Email notifications
- Real-time WebSocket notifications
- Notification preferences
- Notification history
- Push notifications (future)

**Key Components**:
- NotificationController
- NotificationService
- EmailService
- WebSocketNotificationService
- NotificationPreferenceService
- NotificationScheduler

**Database Schema**: `notification_schema`
- Tables: notifications, notification_preferences, email_queue, notification_templates

**Domain Events**:
- NotificationSentEvent
- NotificationReadEvent
- EmailDeliveredEvent

**External Dependencies**:
- SMTP server (email)
- WebSocket broker

**API Endpoints**:
```
GET    /api/v1/notifications
GET    /api/v1/notifications/unread
PATCH  /api/v1/notifications/{id}/read
PATCH  /api/v1/notifications/mark-all-read
GET    /api/v1/notifications/preferences
PUT    /api/v1/notifications/preferences
```

**WebSocket Topics**:
```
/topic/notifications/{userId}
/topic/tasks/{taskId}
/topic/system/announcements
```

---

#### 3.1.6 Audit & Compliance Module (audit-module)

**Responsibility**: Audit logging, compliance tracking, change history

**Bounded Context**:
- Audit log creation and storage
- Compliance reporting
- Security event tracking
- Change history retrieval

**Key Components**:
- AuditController
- AuditService
- AuditEventListener
- ComplianceReportService

**Database Schema**: `audit_schema`
- Tables: audit_logs, security_events, compliance_snapshots

**Domain Events**:
- Subscribes to ALL domain events from other modules

**API Endpoints**:
```
GET    /api/v1/audit/logs
GET    /api/v1/audit/logs/search
GET    /api/v1/audit/logs/entity/{type}/{id}
GET    /api/v1/audit/security-events
GET    /api/v1/audit/compliance/report
```

---

### 3.2 Infrastructure Modules

#### 3.2.1 Shared Kernel Module (shared-kernel)

**Responsibility**: Cross-cutting concerns, common utilities, base classes

**Components**:
- Base entities and DTOs
- Common exceptions
- Utility classes
- Constants and enums
- Domain event infrastructure
- Audit annotations
- Soft delete support

**No Database Schema** (used by all modules)

---

#### 3.2.2 API Gateway Module (api-gateway)

**Responsibility**: Request routing, rate limiting, API versioning, global filters

**Components**:
- GlobalExceptionHandler
- RequestLoggingFilter
- RateLimitingFilter
- CorsConfiguration
- API versioning support

---

#### 3.2.3 File Storage Module (file-storage-module)

**Responsibility**: File upload, storage, retrieval, cloud integration

**Components**:
- FileStorageService
- LocalFileStorageService
- CloudFileStorageService (S3, Azure Blob)
- FileValidationService

**Database Schema**: `storage_schema`
- Tables: file_metadata

---

### 3.3 Future AI/ML Module (ml-module - placeholder)

**Responsibility**: AI-powered features, predictions, recommendations

**Planned Features**:
- Task priority prediction
- Workload balancing recommendations
- Productivity scoring
- Deadline estimation
- Anomaly detection

**Integration**: REST API to Python inference server

---

## 4. Functional Requirements

### 4.1 Authentication & Authorization (FR-AUTH)

#### FR-AUTH-001: User Login
**Priority**: Critical  
**Description**: Users must authenticate using email and password to access the system.

**Acceptance Criteria**:
- System validates credentials against stored hashed passwords
- Successful login generates JWT access token (15 min expiry) and refresh token (7 days)
- Failed login attempts are logged for security monitoring
- Account lockout after 5 failed attempts within 15 minutes
- Support for "Remember Me" functionality

**Business Rules**:
- Only active users can log in
- Inactive/deactivated accounts are denied access
- Deleted accounts cannot authenticate

---

#### FR-AUTH-002: Token Refresh
**Priority**: Critical  
**Description**: System must support token refresh mechanism for seamless user experience.

**Acceptance Criteria**:
- Refresh token can generate new access token without re-authentication
- Refresh tokens are single-use and rotated on each refresh
- Expired refresh tokens require full re-authentication
- Concurrent refresh token usage is detected and flagged

---

#### FR-AUTH-003: Password Reset
**Priority**: High  
**Description**: Users can reset forgotten passwords via email verification.

**Acceptance Criteria**:
- System generates time-limited reset token (1 hour expiry)
- Reset link sent to registered email
- Token can only be used once
- Password must meet complexity requirements (min 8 chars, uppercase, lowercase, number, special char)
- Old password history maintained (prevent reuse of last 3 passwords)

---

#### FR-AUTH-004: Role-Based Access Control
**Priority**: Critical  
**Description**: System enforces role-based permissions for all operations.

**Roles**:
- ADMIN: Full system access
- EMPLOYEE: Limited to assigned tasks and personal profile

**Permission Matrix**:

| Resource | ADMIN | EMPLOYEE |
|----------|-------|----------|
| Create Employee | ✓ | ✗ |
| View All Employees | ✓ | ✗ |
| View Own Profile | ✓ | ✓ |
| Update Own Profile | ✓ | ✓ |
| Create Task | ✓ | ✗ |
| View Assigned Tasks | ✓ | ✓ |
| Update Task Status | ✓ | ✓ (own tasks) |
| Delete Task | ✓ | ✗ |
| View Analytics | ✓ | ✓ (limited) |
| View Audit Logs | ✓ | ✗ |

---

### 4.2 Employee Management (FR-EMP)

#### FR-EMP-001: Employee Creation
**Priority**: Critical  
**Description**: Admin can create employee accounts with automatic invitation.

**Acceptance Criteria**:
- Required fields: First Name, Last Name, Email, Role
- Optional fields: Mobile, Address, Department, Manager
- System validates email uniqueness
- Account created in "Pending Activation" status
- Secure activation email sent with token (24-hour expiry)
- Employee cannot login until activation complete

**Workflow**:
1. Admin submits employee form
2. System creates user account (status: PENDING)
3. Activation email sent with unique token
4. Employee clicks link and sets password
5. Account status changes to ACTIVE
6. Employee can now log in

---

#### FR-EMP-002: Employee Profile Management
**Priority**: High  
**Description**: Employees can view and update their profile information.

**Editable Fields** (by employee):
- Mobile number
- Address
- Profile photo
- Password

**Admin-Only Fields**:
- Email
- Role
- Department
- Status (Active/Inactive)

**Profile Photo**:
- Supported formats: JPG, PNG, WebP
- Max size: 5MB
- Auto-resize to 500x500px
- Stored with versioning (old photos retained for audit)

---

#### FR-EMP-003: Employee Search & Filtering
**Priority**: Medium  
**Description**: Admin can search and filter employee list.

**Search Criteria**:
- Name (partial match)
- Email
- Department
- Status (Active/Inactive/Pending)
- Role

**Features**:
- Pagination (default 20 per page)
- Sorting by name, email, created date
- Export filtered results to CSV

---

#### FR-EMP-004: Soft Delete Employee
**Priority**: High  
**Description**: Admin can deactivate employees without permanent deletion.

**Acceptance Criteria**:
- Soft delete sets `is_deleted = true` and `deleted_at = now()`
- Deleted employee accounts cannot log in
- All historical data and task associations preserved
- Deleted employees visible in audit views (marked as deleted)
- Restore capability (admin can undelete if needed)

**Cascading Effects**:
- Assigned tasks remain visible but show "Former Employee"
- Audit logs preserved
- No new tasks can be assigned

---

### 4.3 Task Management (FR-TASK)

#### FR-TASK-001: Task Creation
**Priority**: Critical  
**Description**: Admin can create and assign tasks to employees.

**Required Fields**:
- Title (max 200 chars)
- Description (rich text, max 5000 chars)
- Assigned To (employee ID)
- Priority (Low, Medium, High, Critical)
- Deadline (date + time)

**Optional Fields**:
- Tags
- Attachments
- Estimated Hours

**Default Values**:
- Status: Pending
- Created By: Current admin
- Created At: Current timestamp

**Validation**:
- Deadline must be future date
- Assignee must be active employee
- Priority must be valid enum

---

#### FR-TASK-002: Task Status Management
**Priority**: Critical  
**Description**: Task status can be updated with complete audit trail.

**Status Workflow**:
```
Pending → In Progress → Completed
   ↓           ↓            ↓
Blocked     On Hold    Reopened
   ↓
Cancelled
```

**Status Change Rules**:
- ADMIN: Can change to any status
- EMPLOYEE: Can only update own assigned tasks
- Status transitions are validated (e.g., can't go from Pending to Completed directly)
- Each status change creates history record

**Required on Status Change**:
- New status
- Optional: Comment/Reason
- Automatic: Changed by, changed at

---

#### FR-TASK-003: Task Status History
**Priority**: High  
**Description**: Complete timeline of all task status changes maintained.

**History Record Contains**:
- Task ID
- Old Status
- New Status
- Changed By (user ID)
- Changed At (timestamp)
- Comment/Reason
- IP Address (security)

**Timeline View**:
- Chronological display of all changes
- Visual timeline with status badges
- User avatars and names
- Relative timestamps (e.g., "2 hours ago")

---

#### FR-TASK-004: Task Comments
**Priority**: Medium  
**Description**: Users can add comments to tasks for collaboration.

**Acceptance Criteria**:
- Comments support markdown formatting
- Comments include timestamp and author
- Comments are immutable (can't be edited or deleted)
- Mentions support (@username notifications)
- File attachments supported in comments

---

#### FR-TASK-005: Deadline Monitoring
**Priority**: High  
**Description**: System automatically detects and flags overdue tasks.

**Scheduler Behavior**:
- Runs every hour
- Scans all tasks with status != Completed
- Compares deadline with current time
- Updates status to "Overdue" if deadline passed

**Notifications**:
- Email to assigned employee (first overdue detection)
- Email to admin (daily digest of overdue tasks)
- WebSocket notification (real-time)

**Escalation**:
- 1 day overdue: Warning notification
- 3 days overdue: Manager notification (if applicable)
- 7 days overdue: Admin escalation

---

#### FR-TASK-006: Task Filtering & Search
**Priority**: High  
**Description**: Users can filter and search tasks based on multiple criteria.

**Filter Options**:
- Status (multiple selection)
- Priority (multiple selection)
- Assigned To (for admin)
- Date Range (created, deadline)
- Tags

**Search**:
- Full-text search on title and description
- Search within filtered results

**Saved Filters** (future):
- Users can save frequently used filter combinations

---

### 4.4 Analytics & Reporting (FR-ANALYTICS)

#### FR-ANALYTICS-001: Admin Dashboard
**Priority**: High  
**Description**: Admin views comprehensive system-wide analytics.

**Key Metrics (Cards)**:
- Total Tasks
- Active Tasks
- Completed Tasks
- Overdue Tasks
- Total Employees
- Active Employees
- Tasks Completed This Month
- Average Completion Time

**Charts**:

1. **Task Distribution by Status** (Pie Chart)
   - Shows percentage of tasks in each status
   - Click-through to filtered task list

2. **Task Distribution by Priority** (Donut Chart)
   - Critical, High, Medium, Low counts
   - Color-coded severity

3. **Employee Performance Comparison** (Bar Chart)
   - X-axis: Employee names
   - Y-axis: Completed tasks count
   - Time period selector (This Week, Month, Quarter, Year)

4. **Task Completion Trend** (Line Chart)
   - X-axis: Date
   - Y-axis: Tasks completed
   - 30-day rolling window

5. **Overdue Task Analysis** (Stacked Bar Chart)
   - X-axis: Employees
   - Y-axis: Overdue task count
   - Color-coded by priority

6. **Monthly Task Activity** (Area Chart)
   - Created vs Completed tasks per month
   - Last 12 months

**Filters**:
- Date range selector
- Employee filter
- Department filter (future)

---

#### FR-ANALYTICS-002: Employee Dashboard
**Priority**: Medium  
**Description**: Employees view personal performance metrics.

**Personal Metrics**:
- My Total Tasks
- In Progress
- Completed
- Overdue
- This Month Completed
- Average Time to Complete

**Charts**:

1. **My Task Status Distribution** (Pie Chart)
2. **Monthly Productivity Trend** (Line Chart)
   - Tasks completed per month (last 6 months)
3. **Completion Rate** (Progress Ring)
   - Percentage of on-time completions

---

#### FR-ANALYTICS-003: Report Export
**Priority**: Medium  
**Description**: Users can export analytics data in multiple formats.

**Export Formats**:
- CSV (raw data)
- Excel (formatted with charts)
- PDF (executive summary with charts)

**Report Types**:
- Task Summary Report
- Employee Performance Report
- Overdue Tasks Report
- Completion Trend Report
- Custom Date Range Report

**Export Process**:
1. User selects report type and parameters
2. System generates report asynchronously
3. User receives notification when ready
4. Report available for download (24-hour retention)

---

### 4.5 Notifications (FR-NOTIF)

#### FR-NOTIF-001: Real-Time WebSocket Notifications
**Priority**: High  
**Description**: Users receive instant notifications for relevant events.

**Notification Triggers**:

**For Employees**:
- New task assigned
- Task updated by admin
- Task comment added
- Task deadline approaching (1 day, 1 hour)
- Task marked overdue

**For Admins**:
- Employee updates task status
- Task completed by employee
- Task becomes overdue
- Employee comments on task
- System alerts (quota limits, errors)

**WebSocket Topics**:
- `/topic/notifications/{userId}` - Personal notifications
- `/topic/tasks/{taskId}` - Task-specific updates
- `/topic/system` - System-wide announcements

**Notification Payload**:
```json
{
  "id": "uuid",
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "Admin assigned you 'Update Documentation'",
  "data": {
    "taskId": 123,
    "taskTitle": "Update Documentation"
  },
  "timestamp": "2026-02-14T10:30:00Z",
  "read": false
}
```

---

#### FR-NOTIF-002: Email Notifications
**Priority**: High  
**Description**: Critical events trigger email notifications.

**Email Templates**:

1. **Employee Invitation**
   - Subject: "Welcome to TaskHive - Activate Your Account"
   - Contains: Activation link, company info, support contact

2. **Task Assignment**
   - Subject: "New Task Assigned: {Task Title}"
   - Contains: Task details, deadline, priority, view task link

3. **Task Overdue**
   - Subject: "Urgent: Task Overdue - {Task Title}"
   - Contains: Task details, days overdue, action required

4. **Password Reset**
   - Subject: "Password Reset Request"
   - Contains: Reset link (1-hour expiry), security notice

5. **Daily Digest** (optional, preference-based)
   - Subject: "Your Daily TaskHive Summary"
   - Contains: Tasks due today, overdue tasks, new assignments

**Email Queue**:
- Asynchronous processing
- Retry logic (3 attempts with exponential backoff)
- Failure logging
- Bounce handling

---

#### FR-NOTIF-003: Notification Preferences
**Priority**: Low  
**Description**: Users can customize notification settings.

**Configurable Preferences**:
- Email notifications (On/Off per category)
- WebSocket notifications (On/Off)
- Daily digest (On/Off, time preference)
- Notification sound (On/Off)
- Desktop notifications (browser permission)

**Default Settings**:
- All critical notifications enabled
- Daily digest disabled
- Sound enabled

---

### 4.6 Audit & Compliance (FR-AUDIT)

#### FR-AUDIT-001: Comprehensive Audit Logging
**Priority**: Critical  
**Description**: All significant system actions are logged for compliance and security.

**Logged Events**:

**Authentication**:
- Login success/failure
- Logout
- Password change
- Password reset
- Token refresh

**Employee Management**:
- Employee created
- Employee updated
- Employee activated/deactivated
- Employee deleted (soft)
- Profile photo changed

**Task Management**:
- Task created
- Task updated
- Task status changed
- Task deleted (soft)
- Task comment added

**System Events**:
- Configuration changes
- Data exports
- Failed authorization attempts
- API errors (500 series)

**Audit Log Schema**:
```json
{
  "id": "uuid",
  "timestamp": "2026-02-14T10:30:00Z",
  "action_type": "TASK_CREATED",
  "entity_type": "TASK",
  "entity_id": "123",
  "performed_by": "user-uuid",
  "ip_address": "192.168.1.100",
  "user_agent": "Mozilla/5.0...",
  "before_state": null,
  "after_state": { "status": "Pending", ... },
  "metadata": { "source": "web_app" }
}
```

---

#### FR-AUDIT-002: Audit Trail Retrieval
**Priority**: High  
**Description**: Admins can search and view audit logs.

**Search Criteria**:
- Date range
- Action type
- Entity type
- Performed by (user)
- IP address

**Audit Log Views**:
- Chronological list (newest first)
- Entity-specific timeline (all changes to a task/employee)
- User activity log (all actions by a user)
- Security events (login attempts, access denials)

**Retention Policy**:
- Audit logs retained for 7 years (compliance)
- Archived to cold storage after 1 year
- Never deleted (regulatory requirement)

---

#### FR-AUDIT-003: Compliance Reporting
**Priority**: Medium  
**Description**: Generate compliance reports for regulatory requirements.

**Report Types**:
- User Access Report (who accessed what, when)
- Data Modification Report (what changed, by whom)
- Security Incident Report (failed logins, unauthorized access attempts)
- Data Retention Report (data age, deletion logs)

---

## 5. Non-Functional Requirements

### 5.1 Performance (NFR-PERF)

#### NFR-PERF-001: API Response Time
- **Target**: 95th percentile < 300ms for standard operations
- **Maximum**: 500ms for complex analytics queries
- **Measurement**: Spring Actuator metrics, APM tools

**Specific Targets**:
- Login: < 200ms
- Task list (paginated): < 150ms
- Task creation: < 250ms
- Dashboard analytics: < 500ms
- Search queries: < 300ms

---

#### NFR-PERF-002: Database Query Performance
- All queries must use appropriate indexes
- No N+1 query problems (use JOIN FETCH)
- Complex analytics use materialized views or caching
- Pagination required for lists > 20 items

**Database Optimization**:
- Indexes on foreign keys
- Composite indexes on frequently filtered columns
- EXPLAIN ANALYZE for slow queries
- Connection pooling (HikariCP, max 20 connections)

---

#### NFR-PERF-003: Concurrent User Support
- **Minimum**: 100 concurrent users
- **Target**: 500 concurrent users
- **Load Testing**: JMeter/Gatling scenarios
- **Graceful Degradation**: System remains functional under 2x expected load

---

#### NFR-PERF-004: Scalability
- Horizontal scaling capability (stateless application)
- Database read replicas support (future)
- Caching layer for frequently accessed data (Redis)
- CDN for static assets

**Vertical Scaling Limits** (before horizontal scaling required):
- 4 vCPU, 8GB RAM: ~500 users
- 8 vCPU, 16GB RAM: ~1000 users

---

### 5.2 Security (NFR-SEC)

#### NFR-SEC-001: Authentication Security
- Passwords hashed with BCrypt (cost factor 12)
- JWT tokens signed with RS256 (asymmetric)
- Refresh token rotation (single-use tokens)
- Account lockout: 5 failed attempts, 15-minute lockout
- Session timeout: 15 minutes inactivity (access token expiry)

---

#### NFR-SEC-002: Authorization
- Role-based access control (RBAC) enforced at API and service layer
- Principle of least privilege
- No client-side permission checks (server authoritative)
- API endpoint protection with Spring Security

---

#### NFR-SEC-003: Data Protection
- Sensitive data encrypted at rest (database encryption)
- TLS 1.3 for all communications
- No sensitive data in logs (PII, passwords)
- SQL injection prevention (parameterized queries)
- XSS prevention (input sanitization, CSP headers)
- CSRF protection (Double Submit Cookie pattern)

---

#### NFR-SEC-004: API Security
- Rate limiting: 100 requests/minute per user
- API versioning (/api/v1/)
- CORS configuration (whitelist domains)
- Request validation (Hibernate Validator)
- File upload validation (type, size, content scanning)

**Rate Limiting Strategy**:
- Anonymous: 20 req/min
- Authenticated: 100 req/min
- Admin: 200 req/min
- Burst allowance: 20 extra requests

---

#### NFR-SEC-005: Compliance
- GDPR compliance (data export, deletion, consent)
- SOC 2 readiness (audit logs, access controls)
- Password complexity enforcement
- Data retention policies
- Right to be forgotten (user data export/deletion)

---

### 5.3 Reliability (NFR-REL)

#### NFR-REL-001: Availability
- **Target**: 99.5% uptime (43.8 hours downtime/year)
- **Measurement**: Uptime monitoring, health checks
- **Maintenance Windows**: Planned downtime < 2 hours/month

---

#### NFR-REL-002: Data Durability
- Database backups: Daily full, hourly incremental
- Backup retention: 30 days online, 1 year archived
- Point-in-time recovery (PITR) capability
- Backup testing: Monthly restore validation

---

#### NFR-REL-003: Error Handling
- Graceful degradation (non-critical features can fail independently)
- User-friendly error messages (no stack traces to users)
- Automatic retry for transient failures (email sending, external APIs)
- Circuit breaker pattern for external dependencies

---

#### NFR-REL-004: Monitoring & Alerting
- Application health checks (Spring Actuator)
- Database connection monitoring
- API error rate monitoring (> 1% triggers alert)
- WebSocket connection monitoring
- Disk space, CPU, memory alerts

**Alert Channels**:
- Email to ops team
- Slack integration (future)
- PagerDuty integration (production)

---

### 5.4 Maintainability (NFR-MAINT)

#### NFR-MAINT-001: Code Quality
- Test coverage > 80% (unit + integration)
- SonarQube quality gate: A rating
- No critical or blocker issues
- Documentation for complex algorithms
- Consistent code style (Checkstyle, ESLint)

---

#### NFR-MAINT-002: Architecture
- Modular monolith (clear module boundaries)
- Dependency injection (Spring IoC)
- DTO pattern (API ↔ Domain separation)
- Event-driven inter-module communication
- Database schema versioning (Flyway/Liquibase)

---

#### NFR-MAINT-003: Documentation
- OpenAPI 3.0 specification for all APIs
- Architecture Decision Records (ADRs)
- Module dependency diagram
- Database ER diagram
- Deployment runbook
- Developer onboarding guide

---

#### NFR-MAINT-004: Logging
- Structured logging (JSON format)
- Log levels: ERROR, WARN, INFO, DEBUG
- Correlation IDs for request tracing
- No sensitive data in logs
- Centralized log aggregation (ELK stack ready)

**Log Retention**:
- ERROR: 90 days
- WARN/INFO: 30 days
- DEBUG: 7 days

---

### 5.5 Usability (NFR-USE)

#### NFR-USE-001: User Interface
- Responsive design (mobile, tablet, desktop)
- WCAG 2.1 Level AA compliance (accessibility)
- Consistent UI/UX (design system)
- Loading states for async operations
- Error messages with actionable guidance

---

#### NFR-USE-002: Browser Support
- Chrome (latest 2 versions)
- Firefox (latest 2 versions)
- Safari (latest 2 versions)
- Edge (latest 2 versions)

---

#### NFR-USE-003: Mobile Support
- Touch-friendly interface (min 44x44px tap targets)
- Progressive Web App (PWA) capabilities
- Offline mode for basic operations (future)
- WebView embedding support (iOS, Android)

---

### 5.6 Operational (NFR-OPS)

#### NFR-OPS-001: Deployment
- Containerized (Docker)
- Environment parity (dev, staging, production)
- Zero-downtime deployments (blue-green)
- Automated database migrations
- Rollback capability

---

#### NFR-OPS-002: Configuration Management
- Externalized configuration (environment variables)
- Secret management (HashiCorp Vault ready)
- Feature flags for gradual rollouts
- No hardcoded credentials or secrets

---

#### NFR-OPS-003: Observability
- Distributed tracing (OpenTelemetry ready)
- Metrics export (Prometheus format)
- Custom business metrics (tasks created, completion rate)
- Performance profiling (actuator/metrics)

---

## 6. Data Architecture

### 6.1 Database Schema Design Principles

1. **Logical Schema Separation**: Each module has its own schema namespace
2. **Soft Delete Pattern**: Use `is_deleted` + `deleted_at` for all core entities
3. **Audit Columns**: All tables include `created_at`, `updated_at`, `created_by`, `updated_by`
4. **UUID Primary Keys**: Use UUIDs for distributed system readiness
5. **Optimistic Locking**: Use `@Version` for concurrency control
6. **Referential Integrity**: Foreign keys enforced at database level

---

### 6.2 Schema Breakdown

#### auth_schema

**users**
```sql
CREATE TABLE auth_schema.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

CREATE INDEX idx_users_email ON auth_schema.users(email) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_status ON auth_schema.users(status) WHERE is_deleted = FALSE;
```

**roles**
```sql
CREATE TABLE auth_schema.roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO auth_schema.roles (name) VALUES ('ADMIN'), ('EMPLOYEE');
```

**user_roles**
```sql
CREATE TABLE auth_schema.user_roles (
    user_id UUID REFERENCES auth_schema.users(id),
    role_id UUID REFERENCES auth_schema.roles(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);
```

**refresh_tokens**
```sql
CREATE TABLE auth_schema.refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth_schema.users(id),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_user ON auth_schema.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON auth_schema.refresh_tokens(expires_at);
```

**password_reset_tokens**
```sql
CREATE TABLE auth_schema.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth_schema.users(id),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### employee_schema

**employees**
```sql
CREATE TABLE employee_schema.employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth_schema.users(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(20),
    department VARCHAR(100),
    manager_id UUID REFERENCES employee_schema.employees(id),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INTEGER DEFAULT 0
);

CREATE INDEX idx_employees_user ON employee_schema.employees(user_id);
CREATE INDEX idx_employees_manager ON employee_schema.employees(manager_id);
CREATE INDEX idx_employees_department ON employee_schema.employees(department);
CREATE INDEX idx_employees_name ON employee_schema.employees(first_name, last_name);
```

**employee_profiles**
```sql
CREATE TABLE employee_schema.employee_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID REFERENCES employee_schema.employees(id),
    address TEXT,
    profile_photo_url VARCHAR(500),
    bio TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**profile_photos** (versioning for audit)
```sql
CREATE TABLE employee_schema.profile_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID REFERENCES employee_schema.employees(id),
    file_path VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(50),
    is_current BOOLEAN DEFAULT TRUE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### task_schema

**tasks**
```sql
CREATE TABLE task_schema.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    priority VARCHAR(50) NOT NULL,
    assigned_to UUID REFERENCES employee_schema.employees(id),
    deadline TIMESTAMP NOT NULL,
    estimated_hours DECIMAL(5,2),
    tags VARCHAR(255)[],
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version INTEGER DEFAULT 0
);

CREATE INDEX idx_tasks_assigned_to ON task_schema.tasks(assigned_to) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_status ON task_schema.tasks(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_priority ON task_schema.tasks(priority);
CREATE INDEX idx_tasks_deadline ON task_schema.tasks(deadline);
CREATE INDEX idx_tasks_created_at ON task_schema.tasks(created_at);
```

**task_status_history**
```sql
CREATE TABLE task_schema.task_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID REFERENCES task_schema.tasks(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comment TEXT,
    ip_address VARCHAR(45)
);

CREATE INDEX idx_task_history_task ON task_schema.task_status_history(task_id);
CREATE INDEX idx_task_history_changed_at ON task_schema.task_status_history(changed_at);
```

**task_comments**
```sql
CREATE TABLE task_schema.task_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID REFERENCES task_schema.tasks(id),
    user_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_comments_task ON task_schema.task_comments(task_id);
```

**task_attachments**
```sql
CREATE TABLE task_schema.task_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID REFERENCES task_schema.tasks(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

#### notification_schema

**notifications**
```sql
CREATE TABLE notification_schema.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    data JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notification_schema.notifications(user_id);
CREATE INDEX idx_notifications_read ON notification_schema.notifications(is_read);
CREATE INDEX idx_notifications_created ON notification_schema.notifications(created_at);
```

**notification_preferences**
```sql
CREATE TABLE notification_schema.notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    email_enabled BOOLEAN DEFAULT TRUE,
    websocket_enabled BOOLEAN DEFAULT TRUE,
    daily_digest BOOLEAN DEFAULT FALSE,
    digest_time TIME DEFAULT '09:00',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**email_queue**
```sql
CREATE TABLE notification_schema.email_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    to_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    template_name VARCHAR(100),
    template_data JSONB,
    status VARCHAR(50) DEFAULT 'PENDING',
    attempts INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    error_message TEXT,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

CREATE INDEX idx_email_queue_status ON notification_schema.email_queue(status);
```

---

#### audit_schema

**audit_logs**
```sql
CREATE TABLE audit_schema.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    performed_by UUID,
    ip_address VARCHAR(45),
    user_agent TEXT,
    before_state JSONB,
    after_state JSONB,
    metadata JSONB
);

CREATE INDEX idx_audit_timestamp ON audit_schema.audit_logs(timestamp);
CREATE INDEX idx_audit_action_type ON audit_schema.audit_logs(action_type);
CREATE INDEX idx_audit_entity ON audit_schema.audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_performed_by ON audit_schema.audit_logs(performed_by);
```

**security_events**
```sql
CREATE TABLE audit_schema.security_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    user_id UUID,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN,
    details JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_security_events_timestamp ON audit_schema.security_events(timestamp);
CREATE INDEX idx_security_events_type ON audit_schema.security_events(event_type);
```

---

#### analytics_schema

**daily_metrics**
```sql
CREATE TABLE analytics_schema.daily_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_date DATE NOT NULL,
    total_tasks INTEGER,
    completed_tasks INTEGER,
    overdue_tasks INTEGER,
    active_employees INTEGER,
    tasks_created INTEGER,
    avg_completion_time_hours DECIMAL(10,2),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(metric_date)
);

CREATE INDEX idx_daily_metrics_date ON analytics_schema.daily_metrics(metric_date);
```

**employee_performance_cache**
```sql
CREATE TABLE analytics_schema.employee_performance_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    tasks_assigned INTEGER,
    tasks_completed INTEGER,
    tasks_overdue INTEGER,
    avg_completion_time_hours DECIMAL(10,2),
    on_time_completion_rate DECIMAL(5,2),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, period_start, period_end)
);
```

---

#### storage_schema

**file_metadata**
```sql
CREATE TABLE storage_schema.file_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    storage_type VARCHAR(50) DEFAULT 'LOCAL',
    uploaded_by UUID,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);
```

---

### 6.3 Database Migration Strategy

**Tool**: Flyway (versioned migrations)

**Migration Naming Convention**:
```
V{version}__{description}.sql

Examples:
V1.0__create_auth_schema.sql
V1.1__create_employee_schema.sql
V1.2__create_task_schema.sql
V2.0__add_task_tags_column.sql
```

**Migration Process**:
1. Migrations run automatically on application startup
2. Each migration is transactional
3. Checksum validation prevents tampering
4. Rollback scripts maintained separately (R__ prefix)

---

## 7. Integration Architecture

### 7.1 Internal Module Communication

**Primary Pattern**: Event-Driven Architecture using Spring Events

**Event Bus Implementation**:
```java
@Component
public class DomainEventPublisher {
    private final ApplicationEventPublisher publisher;
    
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
```

**Example Event Flow**:
```
Task Module publishes TaskAssignedEvent
    ↓
Notification Module listens and sends email
    ↓
Audit Module listens and logs action
```

**Benefits**:
- Loose coupling between modules
- Easy to add new listeners
- Testable (can mock event publisher)
- Synchronous by default (async option available)

---

### 7.2 External Integrations

#### 7.2.1 Email Service Integration

**Provider Options**:
- SendGrid
- AWS SES
- Mailgun
- SMTP server

**Implementation**:
```java
public interface EmailService {
    void sendEmail(EmailRequest request);
    void sendTemplatedEmail(String template, Map<String, Object> data);
}

// Implementations: SendGridEmailService, SesEmailService, SmtpEmailService
```

**Configuration**:
```yaml
email:
  provider: sendgrid
  from: noreply@taskhive.com
  fromName: TaskHive
  sendgrid:
    apiKey: ${SENDGRID_API_KEY}
```

---

#### 7.2.2 File Storage Integration

**Provider Options**:
- Local filesystem (development)
- AWS S3
- Azure Blob Storage
- MinIO (self-hosted S3-compatible)

**Implementation**:
```java
public interface FileStorageService {
    String store(MultipartFile file, String directory);
    Resource load(String filename);
    void delete(String filename);
    String getUrl(String filename);
}
```

**Configuration Strategy Pattern**:
```yaml
storage:
  type: ${STORAGE_TYPE:local} # local, s3, azure
  local:
    uploadDir: /var/taskhive/uploads
  s3:
    bucket: taskhive-files
    region: us-east-1
```

---

#### 7.2.3 ML Service Integration (Future)

**Communication**: REST API to Python inference server

**Endpoints**:
```
POST /ml/predict/task-priority
POST /ml/predict/completion-time
POST /ml/recommend/workload-balance
```

**Request/Response**:
```json
// Request
{
  "taskTitle": "Update documentation",
  "taskDescription": "...",
  "employeeId": "uuid",
  "historicalData": {...}
}

// Response
{
  "predictedPriority": "HIGH",
  "confidence": 0.87,
  "estimatedHours": 4.5,
  "reasoning": "Similar tasks took 4-5 hours"
}
```

**Circuit Breaker**:
- Timeout: 5 seconds
- Failure threshold: 5 consecutive failures
- Fallback: Default heuristic-based priority

---

### 7.3 API Versioning Strategy

**URL Versioning**: `/api/v1/`, `/api/v2/`

**Version Support Policy**:
- Current version (v1): Fully supported
- Previous version (v0): Deprecated, 6-month sunset
- Older versions: Unsupported

**Breaking Changes** (require new version):
- Removing endpoints
- Changing response structure
- Changing required fields
- Changing authentication method

**Non-Breaking Changes** (same version):
- Adding optional fields
- Adding new endpoints
- Deprecating fields (with notice)

---

## 8. Security Architecture

### 8.1 Authentication Flow

**JWT Token Structure**:

**Access Token** (15-minute expiry):
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "roles": ["EMPLOYEE"],
  "iat": 1707912000,
  "exp": 1707912900
}
```

**Refresh Token** (7-day expiry):
```json
{
  "sub": "user-uuid",
  "type": "REFRESH",
  "jti": "token-uuid",
  "iat": 1707912000,
  "exp": 1708516800
}
```

**Token Refresh Flow**:
```
1. Client detects access token expiry
2. Client sends refresh token to /auth/refresh
3. Server validates refresh token:
   - Check not revoked
   - Check not expired
   - Check user still active
4. Server generates new access + refresh token pair
5. Server revokes old refresh token
6. Client stores new tokens
```

---

### 8.2 Authorization Model

**Spring Security Configuration**:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf().disable() // Using JWT
            .cors().and()
            .sessionManagement()
                .sessionCreationPolicy(STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/employees/{id}/**")
                    .access("@securityService.canAccessEmployee(#id)")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Method-Level Security**:
```java
@PreAuthorize("hasRole('ADMIN') or @securityService.isTaskAssignee(#taskId)")
public TaskDTO updateTask(UUID taskId, TaskUpdateRequest request) {
    // Implementation
}
```

---

### 8.3 Data Protection

**Sensitive Data Handling**:

1. **Passwords**: BCrypt with salt (cost factor 12)
2. **Tokens**: SHA-256 hashed in database
3. **Personal Data**: Encrypted at rest (database-level encryption)
4. **File Uploads**: Virus scanning (ClamAV integration optional)

**PII Data Classification**:
- **Highly Sensitive**: Passwords, tokens
- **Sensitive**: Email, mobile, address
- **Public**: Name, department (within organization)

**Data Masking in Logs**:
```java
@ToString(exclude = {"password", "token"})
public class User {
    private String email;
    
    @JsonIgnore
    private String password;
}
```

---

### 8.4 Input Validation

**Validation Layers**:

1. **DTO Validation** (Bean Validation):
```java
public class TaskCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
    
    @NotNull
    @Future(message = "Deadline must be in future")
    private LocalDateTime deadline;
    
    @NotNull
    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL")
    private String priority;
}
```

2. **Service Layer Validation** (Business Rules):
```java
public void assignTask(UUID taskId, UUID employeeId) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new EmployeeNotFoundException());
    
    if (!employee.isActive()) {
        throw new InactiveEmployeeException();
    }
    
    // Proceed with assignment
}
```

3. **Database Constraints** (Last Defense):
- NOT NULL constraints
- CHECK constraints
- Unique constraints
- Foreign key constraints

---

### 8.5 API Security Headers

**Security Headers** (Spring Security):
```java
http.headers()
    .contentSecurityPolicy("default-src 'self'")
    .and()
    .xssProtection()
    .and()
    .frameOptions().deny()
    .and()
    .httpStrictTransportSecurity()
        .maxAgeInSeconds(31536000)
        .includeSubDomains(true);
```

**CORS Configuration**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "https://app.taskhive.com",
        "http://localhost:3000"
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

---

## 9. ML/AI Architecture

### 9.1 ML Infrastructure Overview

**Phase 1** (Current): Placeholder structure, no active ML
**Phase 2** (Future): Inference server for predictions
**Phase 3** (Long-term): Automated retraining pipeline

---

### 9.2 Inference Server Architecture

**Technology Stack**:
- Framework: FastAPI
- ML Libraries: Scikit-learn, PyTorch/TensorFlow
- Model Serving: Joblib/ONNX Runtime
- Deployment: Docker container

**Inference Server Structure**:
```
ml/inference-server/
├── api/
│   ├── main.py                 # FastAPI app
│   ├── routes/
│   │   ├── health.py
│   │   ├── predict_priority.py
│   │   └── predict_completion.py
│   └── schemas/
│       ├── request.py
│       └── response.py
├── models/
│   ├── loader.py               # Model loading logic
│   ├── priority_model.pkl
│   └── completion_model.pkl
├── preprocessing/
│   ├── feature_engineering.py
│   └── text_processing.py
├── config/
│   └── settings.py
├── requirements.txt
└── Dockerfile
```

**Inference API Example**:
```python
from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class TaskPredictionRequest(BaseModel):
    title: str
    description: str
    employee_id: str
    historical_completion_rate: float

class PriorityPredictionResponse(BaseModel):
    predicted_priority: str
    confidence: float
    reasoning: str

@app.post("/predict/task-priority")
async def predict_priority(request: TaskPredictionRequest):
    # Feature extraction
    features = extract_features(request)
    
    # Model inference
    prediction = priority_model.predict(features)
    confidence = priority_model.predict_proba(features).max()
    
    return PriorityPredictionResponse(
        predicted_priority=prediction,
        confidence=confidence,
        reasoning=generate_explanation(features, prediction)
    )
```

---

### 9.3 Training Pipeline Architecture

**Training Folder Structure**:
```
ml/training/
├── data/
│   ├── raw/                    # Raw exports from database
│   ├── processed/              # Cleaned, feature-engineered
│   └── datasets/               # Train/test splits
├── notebooks/
│   ├── 01_exploratory_analysis.ipynb
│   ├── 02_feature_engineering.ipynb
│   └── 03_model_experiments.ipynb
├── experiments/
│   ├── priority_classification/
│   │   ├── config.yaml
│   │   ├── train.py
│   │   └── evaluate.py
│   └── completion_time_regression/
├── models/
│   ├── checkpoints/
│   └── production/
└── pipelines/
    ├── data_extraction.py
    ├── preprocessing.py
    ├── training.py
    └── evaluation.py
```

**Training Workflow** (Future):
```
1. Data Extraction (from PostgreSQL)
   ↓
2. Preprocessing & Feature Engineering
   ↓
3. Model Training (cross-validation)
   ↓
4. Evaluation (test set)
   ↓
5. Model Versioning (MLflow/DVC)
   ↓
6. Deployment (if performance > threshold)
```

---

### 9.4 ML Feature Ideas (Future Roadmap)

#### Priority Prediction
**Input Features**:
- Task title (TF-IDF embeddings)
- Task description length
- Assigned employee historical performance
- Department workload
- Current system workload

**Output**: Predicted priority (LOW, MEDIUM, HIGH, CRITICAL)

**Model**: Random Forest Classifier / BERT fine-tuned

---

#### Completion Time Estimation
**Input Features**:
- Task complexity (derived from description)
- Employee average completion time
- Current workload
- Task priority
- Historical similar tasks

**Output**: Estimated hours to completion

**Model**: Gradient Boosting Regressor / Neural Network

---

#### Workload Balancing
**Input Features**:
- Current tasks per employee
- Employee skill profiles
- Task requirements
- Priority distribution

**Output**: Recommended employee for new task assignment

**Model**: Multi-armed Bandit / Reinforcement Learning

---

#### Productivity Scoring
**Input Features**:
- Completion rate
- On-time completion percentage
- Task complexity handled
- Peer comparison

**Output**: Productivity score (0-100)

**Model**: Weighted scoring algorithm / Ensemble

---

### 9.5 ML Integration with Backend

**Backend ML Service Client**:
```java
@Service
public class MlPredictionService {
    
    private final RestTemplate restTemplate;
    
    @Value("${ml.inference.url}")
    private String mlServiceUrl;
    
    @CircuitBreaker(name = "mlService", fallbackMethod = "fallbackPriority")
    public PriorityPrediction predictPriority(TaskPredictionRequest request) {
        String url = mlServiceUrl + "/predict/task-priority";
        
        ResponseEntity<PriorityPrediction> response = restTemplate.postForEntity(
            url,
            request,
            PriorityPrediction.class
        );
        
        return response.getBody();
    }
    
    // Fallback: Rule-based priority
    public PriorityPrediction fallbackPriority(TaskPredictionRequest request, Exception e) {
        return new PriorityPrediction(
            determineRuleBasedPriority(request),
            0.5,
            "ML service unavailable, using fallback"
        );
    }
}
```

**Configuration**:
```yaml
ml:
  inference:
    url: http://ml-inference:8000
    enabled: ${ML_ENABLED:false}
  
resilience4j:
  circuitbreaker:
    instances:
      mlService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
```

---

## 10. Testing Strategy

### 10.1 Testing Pyramid

```
       /\
      /  \  E2E Tests (5%)
     /____\
    /      \  Integration Tests (25%)
   /________\
  /          \  Unit Tests (70%)
 /__________\
```

---

### 10.2 Unit Testing

**Coverage Target**: 80%+ line coverage

**Framework**: JUnit 5 + Mockito + AssertJ

**Test Structure** (Given-When-Then):
```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private EmployeeRepository employeeRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private TaskService taskService;
    
    @Test
    @DisplayName("Should create task when valid request provided")
    void shouldCreateTask_whenValidRequest() {
        // Given
        TaskCreateRequest request = new TaskCreateRequest(
            "Update docs",
            "Update API documentation",
            employeeId,
            "HIGH",
            futureDeadline
        );
        
        Employee employee = new Employee();
        employee.setActive(true);
        
        when(employeeRepository.findById(employeeId))
            .thenReturn(Optional.of(employee));
        
        when(taskRepository.save(any(Task.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        TaskDTO result = taskService.createTask(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Update docs");
        assertThat(result.getPriority()).isEqualTo("HIGH");
        
        verify(taskRepository).save(any(Task.class));
        verify(eventPublisher).publish(any(TaskCreatedEvent.class));
    }
    
    @Test
    @DisplayName("Should throw exception when employee not found")
    void shouldThrowException_whenEmployeeNotFound() {
        // Given
        when(employeeRepository.findById(any()))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> taskService.createTask(request))
            .isInstanceOf(EmployeeNotFoundException.class)
            .hasMessage("Employee not found");
    }
}
```

---

### 10.3 Integration Testing

**Framework**: Spring Boot Test + Testcontainers

**Database**: PostgreSQL container for realistic testing

**Example**:
```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("taskhive_test");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateTask_whenAdminAuthenticated() throws Exception {
        // Given
        TaskCreateRequest request = new TaskCreateRequest(...);
        
        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Update docs"))
            .andExpect(jsonPath("$.id").exists());
        
        // Verify database
        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Update docs");
    }
    
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldReturn403_whenEmployeeTriesToCreateTask() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
```

---

### 10.4 End-to-End Testing

**Framework**: Playwright (frontend) + API tests

**Scenarios**:
1. Complete user journey: Login → View tasks → Update status
2. Admin workflow: Create employee → Assign task → View analytics
3. Real-time notification flow

**Example** (Playwright):
```javascript
test('Employee can update task status', async ({ page }) => {
  // Login
  await page.goto('/login');
  await page.fill('[name="email"]', 'employee@test.com');
  await page.fill('[name="password"]', 'password123');
  await page.click('button[type="submit"]');
  
  // Navigate to tasks
  await page.click('a[href="/tasks"]');
  
  // Update task status
  await page.click('[data-testid="task-123"]');
  await page.selectOption('[name="status"]', 'IN_PROGRESS');
  await page.click('button:has-text("Update")');
  
  // Verify notification
  await expect(page.locator('.notification'))
    .toHaveText('Task status updated successfully');
  
  // Verify status on list
  await page.goto('/tasks');
  await expect(page.locator('[data-testid="task-123-status"]'))
    .toHaveText('In Progress');
});
```

---

### 10.5 Performance Testing

**Tool**: JMeter / Gatling

**Test Scenarios**:
1. **Load Test**: 100 concurrent users, 10-minute duration
2. **Stress Test**: Gradually increase to 500 users
3. **Spike Test**: Sudden surge from 50 to 300 users
4. **Endurance Test**: 100 users for 2 hours

**Performance Criteria**:
- 95th percentile response time < 300ms
- Error rate < 1%
- No memory leaks (heap stable)

---

### 10.6 Security Testing

**Static Analysis**: SonarQube, OWASP Dependency Check

**Dynamic Analysis**:
- Penetration testing (manual)
- OWASP ZAP automated scans

**Security Test Cases**:
1. SQL injection attempts
2. XSS payloads
3. CSRF token validation
4. JWT token tampering
5. Unauthorized access attempts
6. Rate limiting enforcement

---

## 11. Deployment Architecture

### 11.1 Environment Strategy

**Environments**:
1. **Development** (local): Docker Compose
2. **Staging**: Kubernetes (mimics production)
3. **Production**: Kubernetes (cloud or on-premise)

---

### 11.2 Docker Containerization

**Backend Dockerfile**:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile**:
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=build /app/.next ./.next
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/package.json ./package.json

EXPOSE 3000
CMD ["npm", "start"]
```

**ML Inference Dockerfile**:
```dockerfile
FROM python:3.11-slim
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000
CMD ["uvicorn", "api.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

---

### 11.3 Docker Compose (Local Development)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: taskhive
      POSTGRES_USER: taskhive
      POSTGRES_PASSWORD: taskhive123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/taskhive
      SPRING_DATASOURCE_USERNAME: taskhive
      SPRING_DATASOURCE_PASSWORD: taskhive123
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis
  
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080/api/v1
      NEXT_PUBLIC_WS_URL: ws://localhost:8080/ws
    depends_on:
      - backend
  
  ml-inference:
    build: ./ml/inference-server
    ports:
      - "8000:8000"
    environment:
      MODEL_PATH: /app/models
    volumes:
      - ./ml/training/models/production:/app/models:ro

volumes:
  postgres_data:
```

---

### 11.4 Kubernetes Deployment (Production)

**Namespace Structure**:
```
taskhive-prod/
├── backend-deployment
├── backend-service
├── frontend-deployment
├── frontend-service
├── postgres-statefulset
├── postgres-service
├── redis-deployment
├── redis-service
└── ingress
```

**Backend Deployment**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: taskhive-backend
  namespace: taskhive-prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: taskhive-backend
  template:
    metadata:
      labels:
        app: taskhive-backend
    spec:
      containers:
      - name: backend
        image: taskhive/backend:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

**Ingress (NGINX)**:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: taskhive-ingress
  namespace: taskhive-prod
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - app.taskhive.com
    - api.taskhive.com
    secretName: taskhive-tls
  rules:
  - host: app.taskhive.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend
            port:
              number: 3000
  - host: api.taskhive.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: backend
            port:
              number: 8080
```

---

### 11.5 CI/CD Pipeline

**Tool**: GitHub Actions / GitLab CI

**Pipeline Stages**:
```
1. Build
   - Compile backend (Maven)
   - Build frontend (npm)
   - Run linters

2. Test
   - Unit tests (80% coverage required)
   - Integration tests
   - Security scans (OWASP)

3. Package
   - Build Docker images
   - Tag with commit SHA
   - Push to container registry

4. Deploy (Staging)
   - Deploy to staging cluster
   - Run E2E tests
   - Smoke tests

5. Deploy (Production)
   - Manual approval
   - Blue-green deployment
   - Health checks
   - Rollback if needed
```

**GitHub Actions Example**:
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  backend-build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Build with Maven
        run: mvn clean verify
      
      - name: Run tests
        run: mvn test
      
      - name: SonarQube Scan
        run: mvn sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}
      
      - name: Build Docker image
        run: docker build -t taskhive/backend:${{ github.sha }} .
      
      - name: Push to registry
        run: docker push taskhive/backend:${{ github.sha }}
  
  deploy-staging:
    needs: [backend-build, frontend-build]
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: |
          kubectl set image deployment/backend \
            backend=taskhive/backend:${{ github.sha }} \
            -n taskhive-staging
```

---

### 11.6 Database Migration in Production

**Zero-Downtime Strategy**:

1. **Backward-Compatible Changes First**:
   - Add new columns (nullable)
   - Add new tables
   - Create indexes concurrently

2. **Deploy Application** (supports both old and new schema)

3. **Data Migration** (background job)

4. **Remove Old Schema** (next release)

**Example Migration**:
```sql
-- V2.5__add_task_tags.sql

-- Step 1: Add column (nullable)
ALTER TABLE task_schema.tasks 
ADD COLUMN tags VARCHAR(255)[];

-- Step 2: Create index concurrently (won't lock table)
CREATE INDEX CONCURRENTLY idx_tasks_tags 
ON task_schema.tasks USING GIN(tags);

-- Step 3: Backfill data (separate script, run as background job)
-- UPDATE task_schema.tasks SET tags = '{}' WHERE tags IS NULL;

-- Step 4: (Next release) Make NOT NULL if desired
-- ALTER TABLE task_schema.tasks ALTER COLUMN tags SET NOT NULL;
```

---

## 12. Migration Path to Microservices

### 12.1 When to Consider Microservices

**Triggers**:
- Module becomes performance bottleneck
- Team size exceeds 15 developers
- Independent scaling needed (e.g., ML module CPU-intensive)
- Technology diversification required
- Organizational structure changes (Conway's Law)

**Anti-Patterns to Avoid**:
- Premature microservice extraction
- Creating microservices just for technology trends
- Over-fragmentation (too many small services)

---

### 12.2 Extraction Strategy (Strangler Fig Pattern)

**Phase 1: Identify Extraction Candidate**
- Choose module with clear boundaries
- Minimal dependencies on other modules
- Good candidate: Notification Module (low coupling)

**Phase 2: Prepare Module**
- Ensure module uses events (not direct calls)
- Extract module database schema
- Create separate deployment package

**Phase 3: Deploy as Microservice**
- Deploy module alongside monolith
- Route new traffic to microservice
- Keep monolith functionality as fallback

**Phase 4: Gradual Migration**
- Feature flag to control routing
- Monitor performance and errors
- Gradually increase traffic to microservice

**Phase 5: Decommission Monolith Code**
- Remove module from monolith
- Delete unused database schema
- Update documentation

---

### 12.3 Example: Notification Module → Microservice

**Step 1: Current State** (Monolith)
```
Backend (Spring Boot)
├── auth-module
├── employee-module
├── task-module
└── notification-module  ← Extract this
```

**Step 2: Add API Gateway**
```
API Gateway (Kong/NGINX)
       │
       ├──→ Backend Monolith (handles auth, employee, task)
       └──→ Notification Service (new microservice)
```

**Step 3: Communication Changes**

**Before** (Direct call):
```java
@Autowired
private NotificationService notificationService;

public void assignTask(Task task) {
    taskRepository.save(task);
    notificationService.sendTaskAssignedNotification(task); // Direct
}
```

**After** (Event-based):
```java
@Autowired
private DomainEventPublisher eventPublisher;

public void assignTask(Task task) {
    taskRepository.save(task);
    eventPublisher.publish(new TaskAssignedEvent(task)); // Event
}

// Notification microservice listens to event via message queue
```

**Step 4: Message Queue Integration**
```
Task Module → Kafka/RabbitMQ → Notification Microservice
```

---

### 12.4 Inter-Service Communication Patterns

**Synchronous** (REST):
```java
// Use for immediate responses
@FeignClient(name = "employee-service")
public interface EmployeeServiceClient {
    @GetMapping("/api/v1/employees/{id}")
    EmployeeDTO getEmployee(@PathVariable UUID id);
}
```

**Asynchronous** (Message Queue):
```java
// Use for non-blocking operations
@Service
public class TaskEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, TaskEvent> kafkaTemplate;
    
    public void publishTaskCreated(Task task) {
        TaskCreatedEvent event = new TaskCreatedEvent(task);
        kafkaTemplate.send("task-events", event);
    }
}
```

---

### 12.5 Database per Service

**Current** (Shared Database):
```
PostgreSQL
├── auth_schema
├── employee_schema
├── task_schema
└── notification_schema
```

**After Extraction**:
```
PostgreSQL (Main)          PostgreSQL (Notification)
├── auth_schema            └── notification_schema
├── employee_schema
└── task_schema
```

**Data Consistency**: Saga pattern or Event Sourcing

---

### 12.6 Microservice Readiness Checklist

**Module Characteristics**:
- ✅ Clear bounded context
- ✅ Minimal dependencies
- ✅ Own database schema
- ✅ Event-based communication
- ✅ Independent testing
- ✅ Separate configuration

**Infrastructure Requirements**:
- ✅ Service discovery (Eureka/Consul)
- ✅ API Gateway (Kong/NGINX)
- ✅ Message queue (Kafka/RabbitMQ)
- ✅ Distributed tracing (Jaeger)
- ✅ Centralized logging (ELK)
- ✅ Monitoring (Prometheus/Grafana)

---

### 12.7 Microservice Architecture (Future State)

```
                    API Gateway (Kong)
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
   Auth Service    Employee Service    Task Service
        │                 │                 │
   PostgreSQL        PostgreSQL        PostgreSQL
        │                 │                 │
        └─────────────────┼─────────────────┘
                          │
                  Kafka Message Bus
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
 Notification Service  Analytics Service  ML Service
        │                 │                 │
   PostgreSQL        TimescaleDB        FastAPI
```

---

## Appendix A: Technology Decision Records

### ADR-001: Choice of Modular Monolith

**Context**: Need to balance speed of development with future scalability.

**Decision**: Start with modular monolith, not microservices.

**Rationale**:
- Faster initial development
- Simpler deployment
- Easier debugging
- Team size doesn't justify microservices overhead
- Clear module boundaries allow future extraction

**Consequences**:
- Must maintain strict module boundaries
- Event-driven communication enforced
- Database schema separation required

---

### ADR-002: JWT over Session-Based Auth

**Context**: Need authentication for web and future mobile apps.

**Decision**: Use JWT with refresh token mechanism.

**Rationale**:
- Stateless (no server-side session storage)
- Mobile-friendly
- Horizontal scaling easier
- API-first architecture

**Consequences**:
- Token revocation requires additional logic
- Larger payload size
- Refresh token storage needed

---

### ADR-003: PostgreSQL over NoSQL

**Context**: Need database for structured relational data.

**Decision**: Use PostgreSQL as primary database.

**Rationale**:
- ACID compliance critical for task/employee management
- Complex queries (analytics) easier with SQL
- Mature ecosystem
- JSONB support for flexibility
- Full-text search built-in

**Consequences**:
- Vertical scaling limits
- Schema migrations required
- Read replicas for horizontal read scaling

---

### ADR-004: Event-Driven Inter-Module Communication

**Context**: Need loose coupling between modules.

**Decision**: Use domain events for module communication.

**Rationale**:
- Loose coupling (modules don't know about each other)
- Easy to add new listeners
- Testable in isolation
- Prepares for future message queue integration

**Consequences**:
- Eventual consistency (not immediate)
- Debugging more complex
- Event versioning required

---

## Appendix B: API Endpoint Summary

### Authentication APIs
```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
POST   /api/v1/auth/change-password
GET    /api/v1/auth/me
```

### Employee APIs
```
POST   /api/v1/employees
GET    /api/v1/employees
GET    /api/v1/employees/{id}
PUT    /api/v1/employees/{id}
DELETE /api/v1/employees/{id}
PATCH  /api/v1/employees/{id}/activate
PATCH  /api/v1/employees/{id}/deactivate
POST   /api/v1/employees/{id}/photo
GET    /api/v1/employees/{id}/photo
GET    /api/v1/employees/search
```

### Task APIs
```
POST   /api/v1/tasks
GET    /api/v1/tasks
GET    /api/v1/tasks/{id}
PUT    /api/v1/tasks/{id}
DELETE /api/v1/tasks/{id}
PATCH  /api/v1/tasks/{id}/status
POST   /api/v1/tasks/{id}/comments
GET    /api/v1/tasks/{id}/comments
POST   /api/v1/tasks/{id}/attachments
GET    /api/v1/tasks/{id}/history
GET    /api/v1/tasks/my-tasks
GET    /api/v1/tasks/overdue
GET    /api/v1/tasks/search
```

### Analytics APIs
```
GET    /api/v1/analytics/dashboard/admin
GET    /api/v1/analytics/dashboard/employee
GET    /api/v1/analytics/tasks/distribution
GET    /api/v1/analytics/tasks/by-priority
GET    /api/v1/analytics/tasks/completion-trend
GET    /api/v1/analytics/employees/performance
POST   /api/v1/analytics/reports/export
GET    /api/v1/analytics/reports/{id}/download
```

### Notification APIs
```
GET    /api/v1/notifications
GET    /api/v1/notifications/unread
PATCH  /api/v1/notifications/{id}/read
PATCH  /api/v1/notifications/mark-all-read
GET    /api/v1/notifications/preferences
PUT    /api/v1/notifications/preferences
```

### Audit APIs
```
GET    /api/v1/audit/logs
GET    /api/v1/audit/logs/search
GET    /api/v1/audit/logs/entity/{type}/{id}
GET    /api/v1/audit/security-events
```

---

## Appendix C: Environment Variables

### Backend Configuration
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/taskhive
SPRING_DATASOURCE_USERNAME=taskhive
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET_KEY=your_rsa_private_key
JWT_ACCESS_TOKEN_EXPIRY=900000        # 15 minutes
JWT_REFRESH_TOKEN_EXPIRY=604800000    # 7 days

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=noreply@taskhive.com
SMTP_PASSWORD=your_smtp_password
EMAIL_FROM=noreply@taskhive.com

# File Storage
STORAGE_TYPE=local                    # local | s3 | azure
LOCAL_UPLOAD_DIR=/var/taskhive/uploads
S3_BUCKET_NAME=taskhive-files
S3_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret

# ML Service
ML_INFERENCE_URL=http://ml-inference:8000
ML_ENABLED=false

# Redis (optional)
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Monitoring
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,metrics,prometheus
```

### Frontend Configuration
```bash
NEXT_PUBLIC_API_URL=https://api.taskhive.com/api/v1
NEXT_PUBLIC_WS_URL=wss://api.taskhive.com/ws
NEXT_PUBLIC_APP_NAME=TaskHive
NEXT_PUBLIC_ENABLE_ML=false
```

---

## Appendix D: Glossary

**Modular Monolith**: Single deployable unit with clear internal module boundaries

**Bounded Context**: Self-contained domain area with its own models and logic

**Soft Delete**: Marking records as deleted without physical removal

**Domain Event**: Significant occurrence in the domain that other parts care about

**DTO (Data Transfer Object)**: Object that carries data between processes

**Saga**: Pattern for managing distributed transactions

**Strangler Fig**: Gradual migration from monolith to microservices

**Circuit Breaker**: Prevents cascading failures in distributed systems

**Blue-Green Deployment**: Zero-downtime deployment using two environments

**RBAC (Role-Based Access Control)**: Permissions based on user roles

---

## Document Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Product Owner | _________________ | _________________ | ________ |
| System Architect | _________________ | _________________ | ________ |
| Tech Lead (Backend) | _________________ | _________________ | ________ |
| Tech Lead (Frontend) | _________________ | _________________ | ________ |
| QA Lead | _________________ | _________________ | ________ |
| DevOps Lead | _________________ | _________________ | ________ |

---

**END OF DOCUMENT**

---

**Document Version**: 2.0  
**Last Updated**: February 14, 2026  
**Next Review**: Quarterly or on major architectural changes  
**Owner**: System Architecture Team  
**Classification**: Internal - Confidential
