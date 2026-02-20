# Software Requirements Specification (SRS)

## TaskHive

------------------------------------------------------------------------

## 1. Introduction

### 1.1 Purpose

This document provides a detailed description of the TaskHive (TH). It defines functional and non-functional requirements, system architecture expectations, constraints, and future scalability considerations.

### 1.2 Scope

TH is a web-based enterprise application designed to manage employee
tasks, monitor productivity, track task history, provide analytics, and
ensure audit compliance.

The system will: - Provide secure authentication - Enable task
assignment and tracking - Maintain complete audit logs - Provide
graphical analytics dashboards - Support real-time notifications -
Maintain soft delete mechanisms - Support future mobile WebView
integration

### 1.3 Technology Stack

Frontend: - Next.js (React Framework) - Responsive Mobile-First Design -
Chart.js / Recharts for Graphs

Backend: - Spring Boot (REST API) - Spring Security (JWT + Refresh
Token) - WebSocket (STOMP) - Scheduler for Deadline Alerts

Database: - PostgreSQL

Testing: - JUnit - Mockito - Spring Boot Integration Testing

------------------------------------------------------------------------

## 2. Overall Description

### 2.1 Product Perspective

The system follows a layered architecture:

Frontend (Next.js) → REST APIs (Spring Boot) → PostgreSQL Database →
WebSocket for Real-Time Updates

Future: Mobile App via WebView using same backend APIs.

### 2.2 User Classes

1.  ADMIN
2.  EMPLOYEE

Future expansion possible (RBAC scalable design).

------------------------------------------------------------------------

## 3. Functional Requirements

------------------------------------------------------------------------

## 3.1 Authentication & Authorization

### 3.1.1 Admin Login

-   Email + Password login
-   JWT-based authentication
-   Refresh token support
-   Role-based authorization

### 3.1.2 Employee Onboarding

-   Admin creates employee
-   System sends secure email verification link
-   Employee sets password
-   Employee logs in securely

### 3.1.3 Password Management

-   Forgot password
-   Token-based reset
-   BCrypt password hashing

------------------------------------------------------------------------

## 3.2 Employee Profile Management

Employees must be able to:

-   View profile details
-   Update mobile number
-   Update address (optional)
-   Upload / update profile photo
-   Change password

### Profile Photo Handling

-   Stored securely (cloud/local storage)
-   File validation required
-   Image size restriction
-   Linked to employee profile

------------------------------------------------------------------------

## 3.3 Employee Management (Admin)

Admin can:

-   Create employee
-   Update employee
-   Activate/Deactivate employee
-   Soft delete employee
-   Search and filter employees

### Soft Delete Mechanism

No permanent deletion.

Database field: is_deleted BOOLEAN DEFAULT false

Deleted records remain for audit integrity.

------------------------------------------------------------------------

## 3.4 Task Management

Admin can:

-   Assign tasks
-   Edit tasks
-   Soft delete tasks
-   Set priority (Low, Medium, High, Critical)
-   Set deadline
-   Add description

Fields: - Title - Description - Priority - Deadline - Status - Created
Date - Updated Date

------------------------------------------------------------------------

## 3.5 Task Status History Tracking

Every task status change must be recorded.

Table: task_status_history

Fields: - task_id - old_status - new_status - changed_by - changed_at -
remarks

Enables: - Timeline view - Audit tracking - Delay analysis

------------------------------------------------------------------------

## 3.6 Deadline-Based Auto Alerts

System must:

-   Automatically detect overdue tasks
-   Update status to "Overdue"
-   Notify admin and employee

Implementation: - Spring Scheduled Job - Periodic deadline scanning

------------------------------------------------------------------------

## 3.7 Real-Time Notifications

Using WebSocket (STOMP)

Admin notified when: - Employee updates task - Task completed

Employee notified when: - New task assigned - Task updated

Notification panel required.

------------------------------------------------------------------------

## 3.8 Analytics & Reporting

Admin Dashboard Graphs:

1.  Task Distribution by Status (Pie Chart)
2.  Task Distribution by Priority (Pie/Bar Chart)
3.  Employee Performance Comparison (Bar Chart)
4.  Task Completion Trend (Line Chart)
5.  Overdue Task Analysis (Bar Chart)
6.  Monthly Task Activity (Line Chart)

Employee Dashboard Graphs:

1.  Completed vs Pending Tasks
2.  Monthly Productivity Trend
3.  Overdue Count

Reports must support: - Date filtering - Employee filtering - Export
(CSV, Excel, PDF)

------------------------------------------------------------------------

## 3.9 Audit Logs

System must record:

-   Login events
-   Employee creation/update
-   Task creation/update
-   Status changes
-   Profile updates

Fields: - action_type - performed_by - timestamp - entity_type -
entity_id - details

------------------------------------------------------------------------

## 4. Non-Functional Requirements

### 4.1 Security

-   JWT + Refresh Tokens
-   BCrypt hashing
-   Input validation
-   Role-based authorization
-   CSRF protection

### 4.2 Performance

-   API response \< 300ms
-   Database indexing
-   Pagination for large datasets

### 4.3 Scalability

-   Modular layered architecture
-   DTO-based communication
-   Separation of concerns

### 4.4 Maintainability

Backend package structure:

-   controller
-   service
-   repository
-   dto
-   mapper
-   security
-   websocket
-   scheduler
-   exception

------------------------------------------------------------------------

## 5. Database Overview

Core Tables:

-   users
-   roles
-   tasks
-   task_status_history
-   audit_logs
-   notifications
-   password_reset_tokens

Soft delete columns: - users.is_deleted - tasks.is_deleted

------------------------------------------------------------------------

## 6. Testing Requirements

### 6.1 Unit Testing

-   Service layer tests
-   Repository mocking (Mockito)
-   Controller testing

### 6.2 Integration Testing

-   API validation
-   Security validation
-   Database interaction testing

------------------------------------------------------------------------

## 7. Future Enhancements

Smart Features (Phase 2+): - AI-based priority suggestion - Workload
balancing - Productivity scoring - Time tracking

Enterprise Features: - Multi-organization support - Advanced search -
Task discussion threads

Authentication Upgrade: - OAuth login (Google / Microsoft)

Mobile: - WebView deployment - Push notifications

------------------------------------------------------------------------

## 8. Conclusion

The TaskHive(TH) is designed as a
scalable, secure, real-time enterprise task management platform with
analytics, audit capability, employee profile management, and structured
extensibility for future AI and enterprise enhancements.
