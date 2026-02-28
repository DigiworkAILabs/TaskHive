# Software Requirements Specification (SRS)
## TaskHive - Enterprise Task Management System
### Version 2.4 - Modular Monolithic Architecture

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-01 | System Architect | Initial draft |
| 2.0 | 2026-02-14 | System Architect | Enhanced with modular architecture, ML integration placeholder, microservice readiness |
| 2.1 | 2026-02-19 | System Architect | Spring Boot 3.5.10 + Java 21, HttpOnly Cookie auth, Local + S3 file storage, Future Enhancements section |
| 2.2 | 2026-02-19 | System Architect | Role entity added, manager hierarchy, task status finalized, email queue, security events table, analytics cache tables, optimistic locking, created_by/updated_by, detailed NFR, Dockerfiles, K8s, CI/CD, WebSocket topics, ML detail, compliance |
| 2.3 | 2026-02-19 | System Architect | Added Section 5.7 Disaster Recovery (RTO/RPO), Added Section 8.7 Error Code Standardization per module |
| 2.4 | 2026-02-20 | System Architect | Phase-wise cross-check fixes: (1) Auth — added useMe.ts hook to frontend auth hooks; (2) Employee — added PATCH /{id}/profile endpoint, UpdateEmployeeProfileRequest.java DTO, address column to employees table, useUpdateEmployeeProfile.ts hook (FR-EMP-06); (3) Analytics — confirmed docker-compose-prod.yml in root structure |

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
13. [Future Enhancements](#13-future-enhancements)

---

## 1. Introduction

### 1.1 Purpose

This document specifies the requirements and architectural design for TaskHive, an enterprise-grade task management system built by **Digiwork**, using a **modular monolithic architecture** that enables seamless migration to microservices as business needs evolve.

### 1.2 Scope

TaskHive provides comprehensive task management capabilities including:
- Employee lifecycle management with organizational hierarchy
- Task assignment and tracking with complete audit trails
- Real-time notifications and collaboration
- Advanced analytics and reporting
- AI-powered insights (future)
- Multi-tenant capabilities (future)

### 1.3 Technology Stack

**Backend (Spring Boot 3.5.10 - Java 21)**
- Framework: Spring Boot 3.5.10 with modular package structure
- Language: Java 21 (LTS)
- Build Tool: Maven
- Security: Spring Security 6.x (JWT + HttpOnly Cookie)
- Real-time: WebSocket (STOMP over SockJS)
- Scheduling: Spring Scheduler
- Data Access: Spring Data JPA (with Optimistic Locking)
- Validation: Hibernate Validator
- API Documentation: SpringDoc OpenAPI 3
- Event Bus: Spring Events (internal messaging)
- Resilience: Resilience4j (Circuit Breaker for ML service)

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
- Caching: Redis (Future Enhancement — see Section 13)
- Search: PostgreSQL Full-Text Search (ElasticSearch future option)

**File Storage**
- Primary: Local Storage (`/var/taskhive/uploads/`)
- Production-ready: AWS S3 (switchable via config — `STORAGE_TYPE=local|s3`)

**ML/AI Infrastructure (Placeholder — Future)**
- Training: Python 3.11+, PyTorch/TensorFlow, Scikit-learn
- Serving: FastAPI inference server
- Communication: REST API with Resilience4j Circuit Breaker

**DevOps & Monitoring**
- Containerization: Docker + Docker Compose
- Orchestration: Kubernetes (production)
- CI/CD: GitHub Actions
- Logging: SLF4J + Logback (structured JSON)
- Monitoring: Spring Actuator (Prometheus/Grafana ready)
- Code Quality: SonarQube
- Testing: JUnit 5, Mockito, Testcontainers, Playwright (E2E)

### 1.4 Architectural Principles

1. **Module Independence**: Each module is self-contained with minimal coupling
2. **Clear Boundaries**: Well-defined interfaces between modules
3. **Database per Module Pattern**: Logical schema separation (physical separation optional)
4. **Event-Driven Communication**: Modules communicate via domain events
5. **Shared Kernel**: Common utilities and cross-cutting concerns
6. **API Gateway Pattern**: Single entry point with module routing
7. **Strangler Fig Pattern Ready**: Gradual microservice extraction capability
8. **Security First**: HttpOnly Cookie for token storage, no tokens in localStorage
9. **Optimistic Locking**: Concurrency control on all core entities via `@Version`
10. **Audit Trail**: `created_by`, `updated_by`, `created_at`, `updated_at` on all core tables

### 1.5 Company & Project Info

| Field | Value |
|-------|-------|
| Company | Digiwork |
| Project | TaskHive |
| Base Package | `com.digiwork.taskhive` |
| GroupId | `com.digiwork` |
| ArtifactId | `taskhive` |

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Frontend Layer (Next.js 14+)                  │
├──────────────┬──────────────┬──────────────┬────────────────────┤
│ Auth Module  │ Employee Mod │  Task Module │  Analytics Mod     │
└──────┬───────┴──────┬───────┴──────┬───────┴────────┬───────────┘
       │              │              │                │
       └──────────────┴──────────────┴────────────────┘
                          │ (HTTPS + HttpOnly Cookie)
              ┌───────────▼───────────┐
              │   API Gateway Layer   │
              │   (Spring MVC)        │
              └───────────┬───────────┘
                          │
       ┌──────────────────┼──────────────────┬──────────────────┐
       │                  │                  │                  │
┌──────▼─────┐    ┌──────▼─────┐    ┌──────▼─────┐    ┌──────▼──────┐
│   Auth     │    │  Employee  │    │    Task    │    │ Notification │
│   Module   │    │   Module   │    │   Module   │    │   Module    │
└──────┬─────┘    └──────┬─────┘    └──────┬─────┘    └──────┬──────┘
       │                 │                  │                  │
       └─────────────────┼──────────────────┴──────────────────┘
                         │
              ┌──────────▼──────────┐
              │  Shared Kernel      │
              │  - Common Utils     │
              │  - Event Bus        │
              │  - Audit Service    │
              │  - File Storage     │
              └──────────┬──────────┘
                         │
              ┌──────────▼──────────┐
              │   PostgreSQL 15+    │
              │   (Logical Schemas) │
              └─────────────────────┘
```

### 2.2 Backend Project Structure

```
taskhive-backend/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/digiwork/taskhive/
│   │   │       │
│   │   │       ├── TaskHiveApplication.java
│   │   │       │
│   │   │       ├── common/                                    # Shared Kernel
│   │   │       │   ├── config/
│   │   │       │   │   ├── SecurityConfig.java                # Spring Security + JWT Cookie filter chain; csrf().disable() — CSRF handled by SameSite=Strict cookie
│   │   │       │   │   ├── CorsConfig.java                    # CORS allowed origins + allowCredentials
│   │   │       │   │   ├── WebConfig.java                     # Web MVC config
│   │   │       │   │   ├── WebSocketConfig.java               # STOMP over SockJS
│   │   │       │   │   └── OpenApiConfig.java                 # SpringDoc OpenAPI 3
│   │   │       │   │
│   │   │       │   ├── exception/
│   │   │       │   │   ├── GlobalExceptionHandler.java        # @RestControllerAdvice
│   │   │       │   │   ├── ResourceNotFoundException.java
│   │   │       │   │   ├── UnauthorizedException.java
│   │   │       │   │   ├── InvalidTokenException.java
│   │   │       │   │   ├── DuplicateResourceException.java
│   │   │       │   │   └── BusinessException.java
│   │   │       │   │
│   │   │       │   ├── dto/
│   │   │       │   │   ├── ApiResponse.java                   # Generic API response wrapper
│   │   │       │   │   ├── ErrorResponse.java
│   │   │       │   │   └── PageResponse.java
│   │   │       │   │
│   │   │       │   ├── util/
│   │   │       │   │   ├── CookieUtil.java                    # HttpOnly Cookie create/clear helpers
│   │   │       │   │   ├── DateUtil.java
│   │   │       │   │   ├── ValidationUtil.java
│   │   │       │   │   └── StringUtil.java
│   │   │       │   │
│   │   │       │   ├── constants/
│   │   │       │   │   ├── AppConstants.java
│   │   │       │   │   ├── CookieConstants.java               # Cookie names, paths, max-age values
│   │   │       │   │   └── MessageConstants.java
│   │   │       │   │
│   │   │       │   └── storage/
│   │   │       │       ├── StorageService.java                # Interface
│   │   │       │       ├── LocalStorageService.java           # Local disk implementation
│   │   │       │       ├── S3StorageService.java              # AWS S3 implementation
│   │   │       │       └── StorageConfig.java                 # Switches based on STORAGE_TYPE env
│   │   │       │
│   │   │       └── module/
│   │   │           │
│   │   │           ├── auth/                                  # AUTH MODULE
│   │   │           │   ├── controller/
│   │   │           │   │   └── AuthController.java
│   │   │           │   │
│   │   │           │   ├── service/
│   │   │           │   │   ├── AuthService.java               # Login, logout, me, changePassword
│   │   │           │   │   ├── TokenService.java              # JWT generate, validate, refresh
│   │   │           │   │   ├── PasswordService.java           # BCrypt encode, validate, history check
│   │   │           │   │   ├── AccountActivationService.java  # Employee account activation flow
│   │   │           │   │   ├── PasswordResetService.java      # Forgot/reset password flow
│   │   │           │   │   └── AdminSeeder.java               # Creates default admin on first startup
│   │   │           │   │
│   │   │           │   ├── dto/
│   │   │           │   │   ├── LoginRequest.java
│   │   │           │   │   ├── LoginResponse.java             # Only user info (tokens in HttpOnly Cookie)
│   │   │           │   │   ├── ActivateAccountRequest.java    # token + newPassword
│   │   │           │   │   ├── ForgotPasswordRequest.java     # email
│   │   │           │   │   ├── ResetPasswordRequest.java      # token + newPassword
│   │   │           │   │   ├── ChangePasswordRequest.java     # oldPassword + newPassword
│   │   │           │   │   └── UserInfoResponse.java          # id, email, role, firstName, lastName
│   │   │           │   │
│   │   │           │   ├── model/
│   │   │           │   │   ├── User.java                      # id, email, passwordHash, status, failedAttempts, lockedUntil, version
│   │   │           │   │   ├── Role.java                      # id, name (ADMIN/EMPLOYEE), description
│   │   │           │   │   ├── UserRole.java                  # userId, roleId, assignedAt (join entity)
│   │   │           │   │   ├── RefreshToken.java              # id, userId, tokenHash, expiresAt, revoked
│   │   │           │   │   ├── PasswordResetToken.java        # id, userId, tokenHash, expiresAt, used
│   │   │           │   │   ├── AccountActivationToken.java    # id, userId, tokenHash, expiresAt, used
│   │   │           │   │   └── PasswordHistory.java            # id, userId, passwordHash, createdAt
│   │   │           │   │
│   │   │           │   ├── repository/
│   │   │           │   │   ├── UserRepository.java
│   │   │           │   │   ├── RoleRepository.java
│   │   │           │   │   ├── UserRoleRepository.java
│   │   │           │   │   ├── RefreshTokenRepository.java
│   │   │           │   │   ├── PasswordResetTokenRepository.java
│   │   │           │   │   ├── AccountActivationTokenRepository.java
│   │   │           │   │   └── PasswordHistoryRepository.java
│   │   │           │   │
│   │   │           │   ├── security/
│   │   │           │   │   ├── JwtConfig.java                 # Reads jwt.* from application.properties
│   │   │           │   │   ├── JwtTokenProvider.java          # Generate + parse JWT
│   │   │           │   │   ├── JwtAuthenticationFilter.java   # Reads JWT from accessToken HttpOnly Cookie
│   │   │           │   │   ├── CustomUserDetailsService.java  # Loads user from DB by email
│   │   │           │   │   ├── CustomUserDetails.java         # Wraps User model for Spring Security
│   │   │           │   │   └── SecurityUtils.java             # getCurrentUserId(), getCurrentUserRole()
│   │   │           │   │
│   │   │           │   ├── event/
│   │   │           │   │   ├── UserAuthenticatedEvent.java
│   │   │           │   │   ├── UserLoggedOutEvent.java
│   │   │           │   │   ├── PasswordChangedEvent.java
│   │   │           │   │   ├── PasswordResetRequestedEvent.java
│   │   │           │   │   └── AccountActivatedEvent.java
│   │   │           │   │
│   │   │           │   ├── exception/
│   │   │           │   │   ├── InvalidCredentialsException.java
│   │   │           │   │   ├── AccountLockedException.java
│   │   │           │   │   ├── AccountNotActiveException.java
│   │   │           │   │   ├── TokenExpiredException.java
│   │   │           │   │   ├── TokenAlreadyUsedException.java
│   │   │           │   │   └── InvalidTokenException.java
│   │   │           │   │
│   │   │           │   ├── enums/
│   │   │           │   │   ├── RoleType.java                  # ADMIN, EMPLOYEE (enum constants)
│   │   │           │   │   └── UserStatus.java                # PENDING, ACTIVE, INACTIVE, DELETED
│   │   │           │   │
│   │   │           │   ├── mapper/
│   │   │           │   │   └── UserMapper.java                # User → UserInfoResponse
│   │   │           │   │
│   │   │           │   └── config/
│   │   │           │       └── PasswordEncoderConfig.java     # BCryptPasswordEncoder bean (strength 12)
│   │   │           │
│   │   │           ├── employee/                              # EMPLOYEE MODULE
│   │   │           │   ├── controller/
│   │   │           │   │   └── EmployeeController.java
│   │   │           │   │
│   │   │           │   ├── service/
│   │   │           │   │   ├── EmployeeService.java
│   │   │           │   │   ├── EmployeeSearchService.java
│   │   │           │   │   └── ProfilePhotoService.java
│   │   │           │   │
│   │   │           │   ├── dto/
│   │   │           │   │   ├── CreateEmployeeRequest.java
│   │   │           │   │   ├── UpdateEmployeeRequest.java
│   │   │           │   │   ├── UpdateEmployeeProfileRequest.java  # phone + address (EMPLOYEE self-update — FR-EMP-06)
│   │   │           │   │   ├── EmployeeResponse.java
│   │   │           │   │   ├── EmployeeListResponse.java
│   │   │           │   │   └── EmployeeSearchRequest.java
│   │   │           │   │
│   │   │           │   ├── model/
│   │   │           │   │   ├── Employee.java                  # id, userId, firstName, lastName, email, phone, address, dept, designation, managerId, joinDate, photoUrl, status, version, createdBy, updatedBy
│   │   │           │   │   └── EmployeeStatusHistory.java     # id, employeeId, oldStatus, newStatus, changedBy, reason, changedAt
│   │   │           │   │
│   │   │           │   ├── repository/
│   │   │           │   │   ├── EmployeeRepository.java
│   │   │           │   │   └── EmployeeStatusHistoryRepository.java
│   │   │           │   │
│   │   │           │   ├── event/
│   │   │           │   │   ├── EmployeeCreatedEvent.java
│   │   │           │   │   ├── EmployeeUpdatedEvent.java
│   │   │           │   │   ├── EmployeeActivatedEvent.java
│   │   │           │   │   ├── EmployeeDeactivatedEvent.java
│   │   │           │   │   └── EmployeeDeletedEvent.java
│   │   │           │   │
│   │   │           │   ├── exception/
│   │   │           │   │   ├── EmployeeNotFoundException.java
│   │   │           │   │   └── EmployeeAlreadyExistsException.java
│   │   │           │   │
│   │   │           │   └── mapper/
│   │   │           │       └── EmployeeMapper.java
│   │   │           │
│   │   │           ├── task/                                  # TASK MODULE
│   │   │           │   ├── controller/
│   │   │           │   │   ├── TaskController.java
│   │   │           │   │   ├── TaskCommentController.java
│   │   │           │   │   └── TaskAttachmentController.java
│   │   │           │   │
│   │   │           │   ├── service/
│   │   │           │   │   ├── TaskService.java
│   │   │           │   │   ├── TaskCommentService.java
│   │   │           │   │   ├── TaskAttachmentService.java
│   │   │           │   │   ├── TaskStatusHistoryService.java
│   │   │           │   │   └── TaskSearchService.java
│   │   │           │   │
│   │   │           │   ├── dto/
│   │   │           │   │   ├── CreateTaskRequest.java
│   │   │           │   │   ├── UpdateTaskRequest.java
│   │   │           │   │   ├── UpdateTaskStatusRequest.java
│   │   │           │   │   ├── TaskResponse.java
│   │   │           │   │   ├── TaskListResponse.java
│   │   │           │   │   ├── TaskCommentRequest.java
│   │   │           │   │   ├── TaskCommentResponse.java
│   │   │           │   │   ├── TaskAttachmentResponse.java
│   │   │           │   │   ├── TaskStatusHistoryResponse.java
│   │   │           │   │   └── TaskSearchRequest.java
│   │   │           │   │
│   │   │           │   ├── model/
│   │   │           │   │   ├── Task.java                      # id, title, description, status, priority, assignedTo, dueDate, completedAt, estimatedHours, tags, version, createdBy, updatedBy
│   │   │           │   │   ├── TaskComment.java               # id, taskId, authorId, content, createdAt
│   │   │           │   │   ├── TaskAttachment.java            # id, taskId, uploadedBy, fileName, fileUrl, fileSize, mimeType, createdAt
│   │   │           │   │   └── TaskStatusHistory.java         # id, taskId, oldStatus, newStatus, changedBy, comment, ipAddress, changedAt
│   │   │           │   │
│   │   │           │   ├── repository/
│   │   │           │   │   ├── TaskRepository.java
│   │   │           │   │   ├── TaskCommentRepository.java
│   │   │           │   │   ├── TaskAttachmentRepository.java
│   │   │           │   │   └── TaskStatusHistoryRepository.java
│   │   │           │   │
│   │   │           │   ├── event/
│   │   │           │   │   ├── TaskCreatedEvent.java
│   │   │           │   │   ├── TaskUpdatedEvent.java
│   │   │           │   │   ├── TaskStatusChangedEvent.java
│   │   │           │   │   ├── TaskAssignedEvent.java
│   │   │           │   │   ├── TaskCommentAddedEvent.java
│   │   │           │   │   └── TaskOverdueEvent.java
│   │   │           │   │
│   │   │           │   ├── enums/
│   │   │           │   │   ├── TaskStatus.java                # TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED
│   │   │           │   │   └── TaskPriority.java              # LOW, MEDIUM, HIGH, CRITICAL
│   │   │           │   │
│   │   │           │   ├── exception/
│   │   │           │   │   ├── TaskNotFoundException.java
│   │   │           │   │   └── TaskAccessDeniedException.java
│   │   │           │   │
│   │   │           │   ├── mapper/
│   │   │           │   │   └── TaskMapper.java
│   │   │           │   │
│   │   │           │   └── scheduler/
│   │   │           │       └── TaskOverdueScheduler.java      # Daily job to detect overdue tasks
│   │   │           │
│   │   │           ├── notification/                          # NOTIFICATION MODULE
│   │   │           │   ├── controller/
│   │   │           │   │   └── NotificationController.java
│   │   │           │   │
│   │   │           │   ├── service/
│   │   │           │   │   ├── NotificationService.java
│   │   │           │   │   ├── EmailService.java              # SMTP Gmail
│   │   │           │   │   ├── EmailQueueService.java         # Async email queue processor
│   │   │           │   │   ├── WebSocketNotificationService.java
│   │   │           │   │   └── NotificationPreferenceService.java
│   │   │           │   │
│   │   │           │   ├── dto/
│   │   │           │   │   ├── NotificationResponse.java
│   │   │           │   │   ├── NotificationListResponse.java
│   │   │           │   │   └── NotificationPreferenceRequest.java
│   │   │           │   │
│   │   │           │   ├── model/
│   │   │           │   │   ├── Notification.java
│   │   │           │   │   ├── NotificationPreference.java
│   │   │           │   │   └── EmailQueue.java                # id, toEmail, subject, templateName, templateData, status, attempts, errorMessage, scheduledAt, sentAt
│   │   │           │   │
│   │   │           │   ├── repository/
│   │   │           │   │   ├── NotificationRepository.java
│   │   │           │   │   ├── NotificationPreferenceRepository.java
│   │   │           │   │   └── EmailQueueRepository.java
│   │   │           │   │
│   │   │           │   ├── enums/
│   │   │           │   │   ├── NotificationType.java
│   │   │           │   │   └── EmailStatus.java               # PENDING, SENT, FAILED
│   │   │           │   │
│   │   │           │   ├── scheduler/
│   │   │           │   │   ├── EmailQueueScheduler.java       # Processes pending emails with retry
│   │   │           │   │   └── DailyDigestScheduler.java      # Sends daily digest if enabled
│   │   │           │   │
│   │   │           │   └── listener/
│   │   │           │       ├── AuthEventListener.java
│   │   │           │       ├── EmployeeEventListener.java
│   │   │           │       └── TaskEventListener.java
│   │   │           │
│   │   │           ├── analytics/                             # ANALYTICS MODULE
│   │   │           │   ├── controller/
│   │   │           │   │   └── AnalyticsController.java
│   │   │           │   │
│   │   │           │   ├── service/
│   │   │           │   │   ├── AnalyticsService.java
│   │   │           │   │   ├── ReportService.java
│   │   │           │   │   └── MetricsAggregatorService.java  # Calculates and caches daily metrics
│   │   │           │   │
│   │   │           │   ├── dto/
│   │   │           │   │   ├── AdminDashboardResponse.java
│   │   │           │   │   ├── EmployeeDashboardResponse.java
│   │   │           │   │   ├── TaskDistributionResponse.java
│   │   │           │   │   ├── TaskCompletionTrendResponse.java
│   │   │           │   │   ├── EmployeePerformanceResponse.java
│   │   │           │   │   └── ReportExportRequest.java
│   │   │           │   │
│   │   │           │   ├── model/
│   │   │           │   │   ├── DailyMetrics.java              # Pre-calculated daily stats
│   │   │           │   │   └── EmployeePerformanceCache.java  # Cached performance per period
│   │   │           │   │
│   │   │           │   ├── repository/
│   │   │           │   │   ├── AnalyticsRepository.java       # Native queries for reports
│   │   │           │   │   ├── DailyMetricsRepository.java
│   │   │           │   │   └── EmployeePerformanceCacheRepository.java
│   │   │           │   │
│   │   │           │   └── scheduler/
│   │   │           │       └── MetricsCalculationScheduler.java  # Nightly metrics aggregation
│   │   │           │
│   │   │           └── audit/                                 # AUDIT MODULE
│   │   │               ├── controller/
│   │   │               │   └── AuditController.java
│   │   │               │
│   │   │               ├── service/
│   │   │               │   ├── AuditService.java
│   │   │               │   └── ComplianceReportService.java
│   │   │               │
│   │   │               ├── dto/
│   │   │               │   ├── AuditLogResponse.java
│   │   │               │   ├── SecurityEventResponse.java
│   │   │               │   └── AuditSearchRequest.java
│   │   │               │
│   │   │               ├── model/
│   │   │               │   ├── AuditLog.java                  # id, actorId, actorEmail, action, entityType, entityId, beforeState, afterState, ipAddress, userAgent, createdAt
│   │   │               │   └── SecurityEvent.java             # id, eventType, userId, ipAddress, success, details, timestamp
│   │   │               │
│   │   │               ├── repository/
│   │   │               │   ├── AuditLogRepository.java
│   │   │               │   └── SecurityEventRepository.java
│   │   │               │
│   │   │               └── listener/
│   │   │                   └── AuditEventListener.java        # Listens to ALL domain events
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       │
│   │       ├── db/
│   │       │   └── migration/                                 # Flyway migrations
│   │       │       ├── V1.0__create_users_table.sql
│   │       │       ├── V1.1__create_roles_table.sql
│   │       │       ├── V1.2__create_user_roles_table.sql
│   │       │       ├── V1.3__create_refresh_tokens_table.sql
│   │       │       ├── V1.4__create_password_reset_tokens_table.sql
│   │       │       ├── V1.5__create_account_activation_tokens_table.sql
│   │       │       ├── V1.6__create_password_history_table.sql
│   │       │       ├── V2.0__create_employees_table.sql
│   │       │       ├── V2.1__create_employee_status_history_table.sql
│   │       │       ├── V3.0__create_tasks_table.sql
│   │       │       ├── V3.1__create_task_comments_table.sql
│   │       │       ├── V3.2__create_task_attachments_table.sql
│   │       │       ├── V3.3__create_task_status_history_table.sql
│   │       │       ├── V4.0__create_notifications_table.sql
│   │       │       ├── V4.1__create_notification_preferences_table.sql
│   │       │       ├── V4.2__create_email_queue_table.sql
│   │       │       ├── V5.0__create_audit_logs_table.sql
│   │       │       ├── V5.1__create_security_events_table.sql
│   │       │       ├── V6.0__create_analytics_tables.sql
│   │       │       ├── V6.1__create_file_metadata_table.sql
│   │       │       └── V7.0__create_indexes.sql
│   │       │
│   │       └── templates/
│   │           └── email/
│   │               ├── account-activation.html
│   │               ├── password-reset.html
│   │               ├── password-changed.html
│   │               ├── task-assigned.html
│   │               ├── task-overdue.html
│   │               └── daily-digest.html
│   │
│   └── test/
│       └── java/
│           └── com/digiwork/taskhive/
│               ├── module/
│               │   ├── auth/
│               │   │   ├── controller/AuthControllerTest.java
│               │   │   ├── service/
│               │   │   │   ├── AuthServiceTest.java
│               │   │   │   ├── TokenServiceTest.java
│               │   │   │   ├── PasswordServiceTest.java
│               │   │   │   ├── AccountActivationServiceTest.java
│               │   │   │   └── PasswordResetServiceTest.java
│               │   │   └── security/
│               │   │       ├── JwtTokenProviderTest.java
│               │   │       └── JwtAuthenticationFilterTest.java
│               │   ├── employee/
│               │   │   ├── controller/EmployeeControllerTest.java
│               │   │   └── service/EmployeeServiceTest.java
│               │   ├── task/
│               │   │   ├── controller/TaskControllerTest.java
│               │   │   └── service/TaskServiceTest.java
│               │   ├── notification/
│               │   │   ├── service/NotificationServiceTest.java
│               │   │   └── service/EmailQueueServiceTest.java
│               │   └── analytics/
│               │       └── service/AnalyticsServiceTest.java
│               │
│               └── integration/
│                   ├── AuthIntegrationTest.java
│                   ├── EmployeeIntegrationTest.java
│                   ├── TaskIntegrationTest.java
│                   └── FullFlowIntegrationTest.java
│
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── docker-compose-prod.yml
└── README.md
```

### 2.3 Frontend Project Structure

```
taskhive-frontend/
│
├── src/
│   ├── app/                                        # Next.js App Router
│   │   ├── layout.tsx
│   │   ├── page.tsx                                # Root redirect to login or dashboard
│   │   │
│   │   ├── (auth)/                                 # Public routes (no JWT needed)
│   │   │   ├── layout.tsx                          # Centered card layout
│   │   │   ├── login/page.tsx
│   │   │   ├── forgot-password/page.tsx
│   │   │   ├── reset-password/page.tsx             # ?token=xyz
│   │   │   └── activate-account/page.tsx           # ?token=xyz
│   │   │
│   │   ├── (admin)/                                # Admin routes (ADMIN role only)
│   │   │   ├── layout.tsx
│   │   │   ├── dashboard/page.tsx
│   │   │   ├── employees/
│   │   │   │   ├── page.tsx                        # Employee list
│   │   │   │   ├── new/page.tsx                    # Create employee
│   │   │   │   └── [id]/page.tsx                   # Employee detail
│   │   │   ├── tasks/
│   │   │   │   ├── page.tsx                        # All tasks
│   │   │   │   ├── new/page.tsx
│   │   │   │   └── [id]/page.tsx
│   │   │   ├── analytics/page.tsx
│   │   │   └── audit/page.tsx
│   │   │
│   │   └── (employee)/                             # Employee routes
│   │       ├── layout.tsx
│   │       ├── dashboard/page.tsx
│   │       ├── tasks/
│   │       │   ├── page.tsx                        # My tasks only
│   │       │   └── [id]/page.tsx
│   │       └── settings/
│   │           └── security/page.tsx               # Change password
│   │
│   ├── features/
│   │   │
│   │   ├── auth/
│   │   │   ├── components/
│   │   │   │   ├── LoginForm.tsx
│   │   │   │   ├── ForgotPasswordForm.tsx
│   │   │   │   ├── ResetPasswordForm.tsx
│   │   │   │   ├── ActivateAccountForm.tsx
│   │   │   │   └── ChangePasswordForm.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useLogin.ts
│   │   │   │   ├── useLogout.ts
│   │   │   │   ├── useActivateAccount.ts
│   │   │   │   ├── useForgotPassword.ts
│   │   │   │   ├── useResetPassword.ts
│   │   │   │   ├── useChangePassword.ts
│   │   │   │   ├── useCurrentUser.ts
│   │   │   │   └── useMe.ts                        # GET /auth/me — fetches current user from server
│   │   │   ├── services/
│   │   │   │   └── authService.ts                  # API calls (no token handling — cookie auto-sent)
│   │   │   ├── store/
│   │   │   │   └── authStore.ts                    # Zustand: user info + isAuthenticated ONLY (no tokens)
│   │   │   ├── types/
│   │   │   │   ├── auth.types.ts
│   │   │   │   └── user.types.ts
│   │   │   └── utils/
│   │   │       └── authUtils.ts                    # isAdmin(), isEmployee(), isLoggedIn()
│   │   │
│   │   ├── employee/
│   │   │   ├── components/
│   │   │   │   ├── EmployeeList.tsx
│   │   │   │   ├── EmployeeCard.tsx
│   │   │   │   ├── EmployeeForm.tsx
│   │   │   │   ├── EmployeeDetail.tsx
│   │   │   │   └── ProfilePhotoUpload.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useEmployees.ts
│   │   │   │   ├── useEmployee.ts
│   │   │   │   ├── useCreateEmployee.ts
│   │   │   │   ├── useUpdateEmployee.ts
│   │   │   │   ├── useUpdateEmployeeProfile.ts     # PATCH /{id}/profile — employee self-update (FR-EMP-06)
│   │   │   │   ├── useDeleteEmployee.ts
│   │   │   │   └── useEmployeeSearch.ts
│   │   │   ├── services/
│   │   │   │   └── employeeService.ts
│   │   │   └── types/
│   │   │       └── employee.types.ts
│   │   │
│   │   ├── task/
│   │   │   ├── components/
│   │   │   │   ├── TaskList.tsx
│   │   │   │   ├── TaskCard.tsx
│   │   │   │   ├── TaskForm.tsx
│   │   │   │   ├── TaskDetail.tsx
│   │   │   │   ├── TaskStatusBadge.tsx
│   │   │   │   ├── TaskPriorityBadge.tsx
│   │   │   │   ├── TaskComments.tsx
│   │   │   │   ├── TaskAttachments.tsx
│   │   │   │   └── TaskStatusHistory.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useTasks.ts
│   │   │   │   ├── useTask.ts
│   │   │   │   ├── useCreateTask.ts
│   │   │   │   ├── useUpdateTask.ts
│   │   │   │   ├── useUpdateTaskStatus.ts
│   │   │   │   ├── useMyTasks.ts
│   │   │   │   ├── useOverdueTasks.ts
│   │   │   │   └── useTaskSearch.ts
│   │   │   ├── services/
│   │   │   │   └── taskService.ts
│   │   │   └── types/
│   │   │       └── task.types.ts
│   │   │
│   │   ├── notification/
│   │   │   ├── components/
│   │   │   │   ├── NotificationBell.tsx
│   │   │   │   ├── NotificationList.tsx
│   │   │   │   └── NotificationItem.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useNotifications.ts
│   │   │   │   ├── useMarkAsRead.ts
│   │   │   │   └── useNotificationPreferences.ts
│   │   │   ├── services/
│   │   │   │   ├── notificationService.ts
│   │   │   │   └── webSocketService.ts             # SockJS + STOMP connection manager
│   │   │   └── types/
│   │   │       └── notification.types.ts
│   │   │
│   │   └── analytics/
│   │       ├── components/
│   │       │   ├── AdminDashboard.tsx
│   │       │   ├── EmployeeDashboard.tsx
│   │       │   ├── TaskDistributionChart.tsx
│   │       │   ├── TaskCompletionTrend.tsx
│   │       │   ├── EmployeePerformanceTable.tsx
│   │       │   └── ReportExport.tsx
│   │       ├── hooks/
│   │       │   ├── useAdminDashboard.ts
│   │       │   ├── useEmployeeDashboard.ts
│   │       │   └── useReportExport.ts
│   │       ├── services/
│   │       │   └── analyticsService.ts
│   │       └── types/
│   │           └── analytics.types.ts
│   │
│   ├── shared/
│   │   ├── components/
│   │   │   ├── layout/
│   │   │   │   ├── Sidebar.tsx
│   │   │   │   ├── Header.tsx
│   │   │   │   └── PageWrapper.tsx
│   │   │   └── common/
│   │   │       ├── ProtectedRoute.tsx
│   │   │       ├── LoadingSpinner.tsx
│   │   │       ├── ConfirmDialog.tsx
│   │   │       ├── DataTable.tsx
│   │   │       └── EmptyState.tsx
│   │   ├── hooks/
│   │   │   ├── useDebounce.ts
│   │   │   └── usePagination.ts
│   │   ├── services/
│   │   │   └── api/
│   │   │       ├── apiClient.ts                    # Axios with withCredentials: true, 401 interceptor
│   │   │       └── endpoints.ts
│   │   ├── types/
│   │   │   └── common.types.ts
│   │   └── utils/
│   │       └── formatUtils.ts
│   │
│   └── middleware.ts                               # Next.js route protection
│
├── public/
├── .env.local
├── .env.example
├── Dockerfile
├── next.config.ts
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

### 2.4 ML Infrastructure Structure (Placeholder — Future)

```
ml/
├── inference-server/
│   ├── api/
│   │   ├── main.py
│   │   ├── routes/
│   │   │   ├── health.py
│   │   │   ├── predict_priority.py
│   │   │   └── predict_completion.py
│   │   └── schemas/
│   │       ├── request.py
│   │       └── response.py
│   ├── models/
│   │   ├── loader.py
│   │   ├── priority_model.pkl
│   │   └── completion_model.pkl
│   ├── preprocessing/
│   │   ├── feature_engineering.py
│   │   └── text_processing.py
│   ├── config/
│   │   └── settings.py
│   ├── requirements.txt
│   └── Dockerfile
├── training/
│   ├── data/
│   │   ├── raw/
│   │   ├── processed/
│   │   └── datasets/
│   ├── notebooks/
│   │   ├── 01_exploratory_analysis.ipynb
│   │   ├── 02_feature_engineering.ipynb
│   │   └── 03_model_experiments.ipynb
│   ├── experiments/
│   │   ├── priority_classification/
│   │   │   ├── config.yaml
│   │   │   ├── train.py
│   │   │   └── evaluate.py
│   │   └── completion_time_regression/
│   ├── models/
│   │   ├── checkpoints/
│   │   └── production/
│   └── pipelines/
│       ├── data_extraction.py
│       ├── preprocessing.py
│       ├── training.py
│       └── evaluation.py
└── shared/
    ├── features/
    ├── evaluation/
    └── utils/
```

---

## 3. Module Definitions

### 3.1 Auth Module

**Responsibility**: User authentication, authorization, session management via HttpOnly Cookies

**Bounded Context**:
- User credentials and authentication
- JWT access token + refresh token lifecycle (HttpOnly Cookies)
- Role management (Role entity — future-proof for new roles)
- Password management (BCrypt strength 12)
- Account lockout management
- Default admin seeding on startup

**Key Components**:
- `AuthController` — All auth endpoints
- `AuthService` — Login, logout, change password, get current user
- `TokenService` — JWT generate, validate, refresh
- `PasswordService` — BCrypt encode, validate, complexity check, last-3-password history
- `AccountActivationService` — Employee account activation flow
- `PasswordResetService` — Forgot/reset password flow
- `AdminSeeder` — Creates default admin on first startup
- `JwtAuthenticationFilter` — Reads JWT from `accessToken` HttpOnly Cookie
- `CookieUtil` — Creates/clears HttpOnly Cookies

**Cookie Strategy**:
- `accessToken` — HttpOnly, Secure, SameSite=Strict, Max-Age=900 (15 min)
- `refreshToken` — HttpOnly, Secure, SameSite=Strict, Max-Age=604800 (7 days), Path=/api/v1/auth/refresh

**API Endpoints**:
```
POST   /api/v1/auth/login               # Sets accessToken + refreshToken cookies
POST   /api/v1/auth/logout              # Clears both cookies, revokes refreshToken in DB
POST   /api/v1/auth/refresh             # Issues new accessToken cookie
POST   /api/v1/auth/activate-account    # Employee sets password via email token
POST   /api/v1/auth/forgot-password     # Sends reset email (no info leak on unknown email)
POST   /api/v1/auth/reset-password      # Resets password using email token
POST   /api/v1/auth/change-password     # Change password (JWT required)
GET    /api/v1/auth/me                  # Get current user info (JWT required)
```

---

### 3.2 Employee Module

**Responsibility**: Employee lifecycle management, profile management, organizational hierarchy

**Bounded Context**:
- Employee profiles and personal information
- Employee activation/deactivation
- Manager hierarchy (`manager_id` self-reference)
- Profile photo upload (Local/S3 via StorageService)
- Employee search and filtering
- Employee status history tracking

**Key Components**:
- `EmployeeController`
- `EmployeeService`
- `EmployeeSearchService`
- `ProfilePhotoService`
- `EmployeeMapper`

**API Endpoints**:
```
POST   /api/v1/employees                    # Create employee (ADMIN only)
GET    /api/v1/employees                    # List with pagination + filters (ADMIN only)
GET    /api/v1/employees/{id}              # Get employee detail
PUT    /api/v1/employees/{id}              # Update employee (ADMIN only)
DELETE /api/v1/employees/{id}              # Soft delete (ADMIN only)
PATCH  /api/v1/employees/{id}/activate     # Activate (ADMIN only)
PATCH  /api/v1/employees/{id}/deactivate   # Deactivate (ADMIN only)
POST   /api/v1/employees/{id}/photo        # Upload profile photo (max 5MB, JPG/PNG/WebP)
GET    /api/v1/employees/{id}/photo        # Get profile photo URL
GET    /api/v1/employees/search            # Search by name, email, department, designation
PATCH  /api/v1/employees/{id}/profile      # Employee updates own mobile + address (EMPLOYEE role — FR-EMP-06)
```

---

### 3.3 Task Module

**Responsibility**: Task creation, assignment, tracking, comments, attachments, status history

**Task Status Flow**:
```
TODO → IN_PROGRESS → IN_REVIEW → DONE
         ↓               ↓         ↓
      CANCELLED       CANCELLED  CANCELLED
```

**Task Priority**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

**Key Components**:
- `TaskController`, `TaskCommentController`, `TaskAttachmentController`
- `TaskService`, `TaskCommentService`, `TaskAttachmentService`
- `TaskStatusHistoryService`
- `TaskSearchService`
- `TaskOverdueScheduler` — Daily job

**API Endpoints**:
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

### 3.4 Notification Module

**Responsibility**: Real-time and email notifications with reliable email queue

**Notification Triggers**:
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

**WebSocket Topics**:
```
/topic/notifications/{userId}   # Personal notifications
/topic/tasks/{taskId}           # Task-specific real-time updates
/topic/system                   # System-wide announcements
```

**Email Queue**: Async processing with 3 retry attempts (exponential backoff). Failures logged in `email_queue` table.

**API Endpoints**:
```
GET    /api/v1/notifications                    # List (paginated)
GET    /api/v1/notifications/unread             # Unread list
GET    /api/v1/notifications/unread-count       # Badge count
PATCH  /api/v1/notifications/{id}/read         # Mark single read
PATCH  /api/v1/notifications/mark-all-read     # Mark all read
GET    /api/v1/notifications/preferences        # Get preferences
PUT    /api/v1/notifications/preferences        # Update preferences
```

---

### 3.5 Analytics Module

**Responsibility**: Dashboards, reports, pre-calculated metrics

**Key Components**:
- `AnalyticsService`, `ReportService`
- `MetricsAggregatorService` — calculates and caches daily metrics (nightly scheduler)
- `DailyMetrics` + `EmployeePerformanceCache` models — avoid heavy real-time queries

**API Endpoints**:
```
GET    /api/v1/analytics/dashboard/admin            # Admin overview
GET    /api/v1/analytics/dashboard/employee         # Employee personal stats
GET    /api/v1/analytics/tasks/distribution         # By status (pie chart data)
GET    /api/v1/analytics/tasks/by-priority          # By priority (donut chart data)
GET    /api/v1/analytics/tasks/completion-trend     # 30-day rolling (line chart data)
GET    /api/v1/analytics/employees/performance      # Performance table
POST   /api/v1/analytics/reports/export             # Async report generation
GET    /api/v1/analytics/reports/{id}/download      # Download (24hr retention)
```

---

### 3.6 Audit Module

**Responsibility**: System-wide audit trail + security event tracking + compliance reporting

**Key Components**:
- `AuditService` — Records audit logs
- `ComplianceReportService` — User Access, Data Modification, Security Incident reports
- `AuditEventListener` — Listens to ALL domain events
- `AuditLog` + `SecurityEvent` — Separate models for different concerns

**Retention**: Audit logs retained 7 years (compliance requirement). Never physically deleted.

**API Endpoints**:
```
GET    /api/v1/audit/logs                       # All logs (ADMIN, paginated)
GET    /api/v1/audit/logs/search                # Filter by date, action, entity, user
GET    /api/v1/audit/logs/entity/{type}/{id}    # Entity-specific timeline
GET    /api/v1/audit/security-events            # Failed logins, lockouts
GET    /api/v1/audit/compliance/report          # Compliance report generation
```

---

## 4. Functional Requirements

### 4.1 Authentication & Authorization

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-AUTH-01 | Critical | System shall create a default ADMIN user on first startup if no admin exists |
| FR-AUTH-02 | Critical | Users shall login with email + password |
| FR-AUTH-03 | Critical | On successful login, server shall set `accessToken` and `refreshToken` as HttpOnly, Secure, SameSite=Strict cookies |
| FR-AUTH-04 | Critical | Access token shall expire in 15 minutes |
| FR-AUTH-05 | Critical | Refresh token shall expire in 7 days |
| FR-AUTH-06 | Critical | Frontend shall send requests with `withCredentials: true`; cookies attach automatically |
| FR-AUTH-07 | Critical | `JwtAuthenticationFilter` shall read JWT from `accessToken` cookie only |
| FR-AUTH-08 | Critical | On 401, frontend shall call `/auth/refresh`; server issues new `accessToken` cookie |
| FR-AUTH-09 | Critical | System shall lock account for 15 minutes after 5 consecutive failed login attempts |
| FR-AUTH-10 | High | Employee shall receive activation email with link to set password (24hr expiry) |
| FR-AUTH-11 | High | Activation token shall be single-use and expire in 24 hours |
| FR-AUTH-12 | High | Password reset token shall be single-use and expire in 1 hour |
| FR-AUTH-13 | High | Forgot password shall not reveal if email exists (prevent enumeration) |
| FR-AUTH-14 | Critical | Logout shall revoke refresh token in DB and clear both cookies |
| FR-AUTH-15 | High | Password change and reset shall revoke all active refresh tokens for that user |
| FR-AUTH-16 | Critical | Passwords shall be encoded with BCrypt (strength 12) |
| FR-AUTH-17 | High | System shall prevent reuse of last 3 passwords |
| FR-AUTH-18 | Critical | ADMIN and EMPLOYEE roles shall have distinct access permissions enforced server-side |

### 4.2 Employee Management

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-EMP-01 | Critical | ADMIN shall create employees (firstName, lastName, email, phone, department, designation, joinDate, managerId optional) |
| FR-EMP-02 | Critical | Employee creation shall trigger account activation email automatically |
| FR-EMP-03 | Critical | New employee account shall be created in PENDING status; cannot login until activated |
| FR-EMP-04 | High | ADMIN shall view paginated, filterable employee list (filter by name, email, department, status, role) |
| FR-EMP-05 | High | ADMIN shall update employee profile information |
| FR-EMP-06 | High | Employee shall be able to update own mobile and address |
| FR-EMP-07 | High | ADMIN shall soft-delete employees (is_deleted = true); historical data preserved |
| FR-EMP-08 | High | Deleted employee accounts shall not be able to login |
| FR-EMP-09 | High | ADMIN shall activate and deactivate employees |
| FR-EMP-10 | Medium | Employee or ADMIN shall upload profile photo (max 5MB, JPG/PNG/WebP, auto-resize 500x500) |
| FR-EMP-11 | Medium | System shall support employee search by name, email, department, designation |
| FR-EMP-12 | Medium | Employee list shall be exportable to CSV |
| FR-EMP-13 | Low | System shall support manager hierarchy (manager_id self-reference on employees table) |
| FR-EMP-14 | High | Status change history shall be maintained with actor and reason |

### 4.3 Task Management

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
| FR-TASK-10 | Medium | Users shall upload file attachments to tasks |
| FR-TASK-11 | High | System shall maintain full status change history (old status, new status, actor, comment, IP, timestamp) |
| FR-TASK-12 | High | System shall detect and flag overdue tasks via daily scheduled job |
| FR-TASK-13 | Medium | Overdue escalation: 1 day — warning notification; 3 days — manager notification; 7 days — admin escalation |
| FR-TASK-14 | Critical | EMPLOYEE shall view only their assigned tasks |
| FR-TASK-15 | Critical | ADMIN shall view all tasks with filters (status, priority, assignee, date range, tags) |
| FR-TASK-16 | Medium | System shall support task search by title and description (full-text) |
| FR-TASK-17 | High | Soft delete shall be used for tasks |

### 4.4 Notifications

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

### 4.5 Analytics

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-ANA-01 | High | Admin dashboard: total tasks, active tasks, overdue tasks, completion rate, total employees, avg completion time |
| FR-ANA-02 | High | Admin charts: task distribution by status (pie), by priority (donut), employee performance (bar), completion trend 30-day (line), monthly activity (area) |
| FR-ANA-03 | Medium | Employee dashboard: my tasks count by status, monthly productivity trend, on-time completion rate |
| FR-ANA-04 | Medium | System shall pre-calculate and cache daily metrics nightly (no heavy real-time queries) |
| FR-ANA-05 | Medium | Reports exportable as CSV, Excel, PDF (async generation + download link) |
| FR-ANA-06 | Low | Report download links expire after 24 hours |

### 4.6 Audit

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-AUDIT-01 | Critical | All significant actions shall be logged in audit_logs (actor, action, entity, before/after state, IP, userAgent) |
| FR-AUDIT-02 | Critical | Security events (failed logins, lockouts, unauthorized access) shall be logged in security_events (separate table) |
| FR-AUDIT-03 | High | ADMIN shall search/filter audit logs by date, action type, entity, user, IP |
| FR-AUDIT-04 | High | ADMIN shall view entity-specific timeline (all changes to a task or employee) |
| FR-AUDIT-05 | Medium | System shall generate compliance reports: User Access, Data Modification, Security Incident |
| FR-AUDIT-06 | Critical | Audit logs shall never be physically deleted (7-year retention) |

---

## 5. Non-Functional Requirements

### 5.1 Performance

| ID | Requirement |
|----|-------------|
| NFR-PERF-01 | Login: < 200ms; Task list (paginated): < 150ms; Task creation: < 250ms; Dashboard analytics: < 500ms; Search: < 300ms |
| NFR-PERF-02 | 95th percentile response < 300ms for standard operations under normal load |
| NFR-PERF-03 | System shall support minimum 100 concurrent users; target 500 concurrent users |
| NFR-PERF-04 | All queries shall use appropriate indexes; no N+1 problems (use JOIN FETCH) |
| NFR-PERF-05 | Pagination required on all list endpoints (default 20 per page) |
| NFR-PERF-06 | HikariCP connection pool: max 20 connections |
| NFR-PERF-07 | Analytics queries shall use pre-calculated cache tables (daily_metrics, employee_performance_cache) |

### 5.2 Security

| ID | Requirement |
|----|-------------|
| NFR-SEC-01 | JWT shall be stored in HttpOnly, Secure, SameSite=Strict cookies only — never in localStorage or sessionStorage |
| NFR-SEC-02 | All endpoints (except public auth routes) shall require valid JWT |
| NFR-SEC-03 | HTTPS (TLS 1.3) enforced in production |
| NFR-SEC-04 | CORS configured with explicit origin whitelist and `allowCredentials: true` |
| NFR-SEC-05 | CSRF protection provided by `SameSite=Strict` on all cookies — no separate CSRF token required; Spring Security `.csrf().disable()` is intentional and safe in this setup |
| NFR-SEC-06 | Passwords hashed with BCrypt (strength 12) |
| NFR-SEC-07 | Refresh tokens stored as SHA-256 hash in DB |
| NFR-SEC-08 | Reset and activation tokens stored as SHA-256 hash in DB |
| NFR-SEC-09 | SQL injection prevented via JPA parameterized queries |
| NFR-SEC-10 | File uploads validated for type (JPG/PNG/WebP for photos) and size (max 5MB photos, max 20MB attachments) |
| NFR-SEC-11 | RBAC enforced at API and service layer — no client-side permission checks |
| NFR-SEC-12 | No sensitive data (passwords, tokens, PII) in logs |
| NFR-SEC-13 | GDPR compliance: user data export and deletion capability |
| NFR-SEC-14 | Content Security Policy, X-Frame-Options, HSTS headers enabled |

### 5.3 Reliability

| ID | Requirement |
|----|-------------|
| NFR-REL-01 | 99.5% uptime target (excludes planned maintenance < 2hrs/month) |
| NFR-REL-02 | Database backups: daily full, hourly incremental; 30-day online retention, 1-year archive |
| NFR-REL-03 | Point-in-time recovery (PITR) capability |
| NFR-REL-04 | Email failures shall not cause API errors (async queue with retry) |
| NFR-REL-05 | WebSocket disconnection shall not affect REST API functionality |
| NFR-REL-06 | ML service unavailability shall not break core functionality (circuit breaker fallback) |
| NFR-REL-07 | API error rate > 1% triggers alert to ops team |

### 5.4 Maintainability

| ID | Requirement |
|----|-------------|
| NFR-MAIN-01 | Unit + integration test coverage > 80% on service layer |
| NFR-MAIN-02 | SonarQube quality gate: A rating; no critical or blocker issues |
| NFR-MAIN-03 | Backend code style enforced via Checkstyle; frontend via ESLint |
| NFR-MAIN-04 | All modules communicate only via domain events or defined service interfaces |
| NFR-MAIN-05 | Database changes managed via Flyway versioned migrations |
| NFR-MAIN-06 | All APIs documented with SpringDoc OpenAPI 3 |
| NFR-MAIN-07 | Structured JSON logging (SLF4J + Logback); log levels: ERROR, WARN, INFO, DEBUG |
| NFR-MAIN-08 | Log retention: ERROR — 90 days; WARN/INFO — 30 days; DEBUG — 7 days |
| NFR-MAIN-09 | Correlation IDs on all requests for distributed tracing readiness |

### 5.5 Usability

| ID | Requirement |
|----|-------------|
| NFR-USE-01 | Responsive design: mobile, tablet, desktop |
| NFR-USE-02 | WCAG 2.1 Level AA accessibility compliance |
| NFR-USE-03 | Browser support: Chrome, Firefox, Safari, Edge (latest 2 versions each) |
| NFR-USE-04 | Minimum touch target: 44x44px |
| NFR-USE-05 | Loading states for all async operations |
| NFR-USE-06 | User-friendly error messages (no stack traces exposed) |

### 5.6 Operational

| ID | Requirement |
|----|-------------|
| NFR-OPS-01 | Zero-downtime deployments via blue-green strategy |
| NFR-OPS-02 | Environment parity: development (Docker Compose), staging (K8s), production (K8s) |
| NFR-OPS-03 | No hardcoded credentials; all secrets via environment variables |
| NFR-OPS-04 | HashiCorp Vault ready for secret management in production |
| NFR-OPS-05 | Spring Actuator health, metrics, prometheus endpoints exposed |
| NFR-OPS-06 | Prometheus/Grafana ready metrics export |
| NFR-OPS-07 | Automated Flyway migrations run on application startup |

### 5.7 Disaster Recovery

| Metric | Definition | Target |
|--------|-----------|--------|
| **RTO** (Recovery Time Objective) | Maximum acceptable downtime after a failure | 4 hours |
| **RPO** (Recovery Point Objective) | Maximum acceptable data loss window | 1 hour |

**Backup Strategy**:

| Backup Type | Frequency | Destination | Retention |
|-------------|-----------|-------------|-----------|
| PostgreSQL full dump | Daily (2 AM) | AWS S3 (encrypted) | 30 days daily, 12 months monthly |
| PostgreSQL WAL / incremental | Hourly | AWS S3 | 7 days |
| Uploaded files (Local) | Daily rsync | AWS S3 | 30 days |
| Uploaded files (S3) | S3 versioning enabled | Same bucket | Lifecycle: 90 days |

**Recovery Procedure**:
1. Restore latest PostgreSQL dump from S3
2. Apply WAL logs up to last consistent point
3. Restore uploaded files from S3 backup
4. Run Flyway migrations to verify schema integrity
5. Smoke test critical flows (login, task creation)
6. DNS/load balancer cutover

**Recovery Testing**: Quarterly restore drills in staging environment. Results documented and signed off by DevOps lead.

---

## 6. Data Architecture

### 6.1 Design Principles

1. **Soft Delete**: `is_deleted` + `deleted_at` on all core entities
2. **Audit Columns**: `created_at`, `updated_at`, `created_by`, `updated_by` on all core tables
3. **Optimistic Locking**: `version INTEGER DEFAULT 0` on all core entities (`@Version` in JPA)
4. **UUID Primary Keys**: All tables use UUID for distributed system readiness
5. **Referential Integrity**: Foreign keys enforced at DB level
6. **Logical Schema Grouping**: Tables grouped by module for future physical separation

### 6.2 Database Schema

```sql
-- =============================================
-- AUTH TABLES
-- =============================================

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255),                          -- NULL until account activated
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, ACTIVE, INACTIVE, DELETED
    failed_attempts INTEGER DEFAULT 0,
    locked_until    TIMESTAMP,
    is_deleted      BOOLEAN DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) UNIQUE NOT NULL,               -- ADMIN, EMPLOYEE (and future roles)
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Full system access'),
    ('EMPLOYEE', 'Limited to assigned tasks and personal profile');

CREATE TABLE user_roles (
    user_id     UUID NOT NULL REFERENCES users(id),
    role_id     UUID NOT NULL REFERENCES roles(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_reset_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE account_activation_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_history (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id),
    password_hash VARCHAR(255) NOT NULL,                   -- BCrypt hash of old password
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- Only last 3 entries per user are kept (cleaned up after each password change)
);

-- =============================================
-- EMPLOYEE TABLES
-- =============================================

CREATE TABLE employees (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID UNIQUE NOT NULL REFERENCES users(id),
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    phone       VARCHAR(20),
    address     VARCHAR(255),                                    -- Employee self-update via PATCH /{id}/profile (FR-EMP-06)
    department  VARCHAR(100),
    designation VARCHAR(100),
    manager_id  UUID REFERENCES employees(id),             -- Self-reference for hierarchy
    join_date   DATE,
    photo_url   VARCHAR(500),
    status      VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_deleted  BOOLEAN DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    version     INTEGER DEFAULT 0,
    created_by  UUID REFERENCES users(id),
    updated_by  UUID REFERENCES users(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_status_history (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES employees(id),
    old_status  VARCHAR(50),
    new_status  VARCHAR(50) NOT NULL,
    changed_by  UUID NOT NULL REFERENCES users(id),
    reason      VARCHAR(500),
    changed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- TASK TABLES
-- =============================================

CREATE TABLE tasks (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title            VARCHAR(200) NOT NULL,
    description      TEXT,                                  -- Max 5000 chars enforced at app layer
    status           VARCHAR(50) NOT NULL DEFAULT 'TODO',   -- TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED
    priority         VARCHAR(50) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    assigned_to      UUID REFERENCES employees(id),
    estimated_hours  DECIMAL(5, 2),
    tags             VARCHAR(100)[],
    due_date         TIMESTAMP,
    completed_at     TIMESTAMP,
    is_deleted       BOOLEAN DEFAULT FALSE,
    deleted_at       TIMESTAMP,
    version          INTEGER DEFAULT 0,
    created_by       UUID NOT NULL REFERENCES users(id),
    updated_by       UUID REFERENCES users(id),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task_comments (
    task_id    UUID NOT NULL REFERENCES tasks(id),
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id  UUID NOT NULL REFERENCES users(id),
    content    TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- Immutable: no updated_at, no is_deleted
);

CREATE TABLE task_attachments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id      UUID NOT NULL REFERENCES tasks(id),
    uploaded_by  UUID NOT NULL REFERENCES users(id),
    file_name    VARCHAR(255) NOT NULL,
    file_url     VARCHAR(500) NOT NULL,
    file_size    BIGINT,
    mime_type    VARCHAR(100),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE task_status_history (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID NOT NULL REFERENCES tasks(id),
    changed_by UUID NOT NULL REFERENCES users(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    comment    TEXT,                                        -- Optional reason for status change
    ip_address VARCHAR(45),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- NOTIFICATION TABLES
-- =============================================

CREATE TABLE notifications (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID NOT NULL REFERENCES users(id),
    title          VARCHAR(200) NOT NULL,
    message        TEXT NOT NULL,
    type           VARCHAR(100) NOT NULL,
    data           JSONB,                                   -- Extra payload (taskId, taskTitle, etc.)
    reference_id   UUID,
    reference_type VARCHAR(50),
    is_read        BOOLEAN DEFAULT FALSE,
    read_at        TIMESTAMP,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_preferences (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID UNIQUE NOT NULL REFERENCES users(id),
    email_enabled        BOOLEAN DEFAULT TRUE,
    in_app_enabled       BOOLEAN DEFAULT TRUE,
    task_assigned        BOOLEAN DEFAULT TRUE,
    task_status_changed  BOOLEAN DEFAULT TRUE,
    task_commented       BOOLEAN DEFAULT TRUE,
    task_overdue         BOOLEAN DEFAULT TRUE,
    daily_digest         BOOLEAN DEFAULT FALSE,
    digest_time          TIME DEFAULT '09:00',
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_queue (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    to_email       VARCHAR(255) NOT NULL,
    subject        VARCHAR(255) NOT NULL,
    template_name  VARCHAR(100),
    template_data  JSONB,
    status         VARCHAR(50) DEFAULT 'PENDING',           -- PENDING, SENT, FAILED
    attempts       INTEGER DEFAULT 0,
    max_attempts   INTEGER DEFAULT 3,
    error_message  TEXT,
    scheduled_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at        TIMESTAMP
);

-- =============================================
-- AUDIT TABLES
-- =============================================

CREATE TABLE audit_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id    UUID,
    actor_email VARCHAR(255),
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id   UUID,
    before_state JSONB,
    after_state  JSONB,
    metadata     JSONB,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- Never deleted. Retained 7 years.
);

CREATE TABLE security_events (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,                       -- LOGIN_FAILED, ACCOUNT_LOCKED, UNAUTHORIZED_ACCESS
    user_id    UUID,
    ip_address VARCHAR(45),
    user_agent TEXT,
    success    BOOLEAN,
    details    JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- ANALYTICS TABLES (Pre-calculated cache)
-- =============================================

CREATE TABLE daily_metrics (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_date              DATE NOT NULL UNIQUE,
    total_tasks              INTEGER,
    completed_tasks          INTEGER,
    overdue_tasks            INTEGER,
    active_employees         INTEGER,
    tasks_created            INTEGER,
    avg_completion_time_hours DECIMAL(10, 2),
    calculated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_performance_cache (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id              UUID NOT NULL REFERENCES employees(id),
    period_start             DATE NOT NULL,
    period_end               DATE NOT NULL,
    tasks_assigned           INTEGER,
    tasks_completed          INTEGER,
    tasks_overdue            INTEGER,
    avg_completion_time_hours DECIMAL(10, 2),
    on_time_completion_rate  DECIMAL(5, 2),
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, period_start, period_end)
);

-- =============================================
-- FILE METADATA TABLE (Centralized file tracking)
-- =============================================

CREATE TABLE file_metadata (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_name VARCHAR(255) NOT NULL,
    stored_name   VARCHAR(255) NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    file_size     BIGINT,
    mime_type     VARCHAR(100),
    storage_type  VARCHAR(50) DEFAULT 'LOCAL',              -- LOCAL, S3
    entity_type   VARCHAR(50),                              -- TASK_ATTACHMENT, PROFILE_PHOTO
    entity_id     UUID,
    uploaded_by   UUID REFERENCES users(id),
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata      JSONB
);

-- =============================================
-- INDEXES
-- =============================================

-- Auth
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

CREATE INDEX idx_password_history_user_id ON password_history(user_id);

-- Employee
CREATE INDEX idx_employees_user_id ON employees(user_id);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);
CREATE INDEX idx_employees_status ON employees(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_employees_name ON employees(first_name, last_name);

-- Task
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_status ON tasks(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_by ON tasks(created_by);
CREATE INDEX idx_task_comments_task_id ON task_comments(task_id);
CREATE INDEX idx_task_attachments_task_id ON task_attachments(task_id);
CREATE INDEX idx_task_status_history_task_id ON task_status_history(task_id);

-- Notification
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_email_queue_status ON email_queue(status);
CREATE INDEX idx_email_queue_scheduled_at ON email_queue(scheduled_at);

-- Audit
CREATE INDEX idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_security_events_user_id ON security_events(user_id);
CREATE INDEX idx_security_events_event_type ON security_events(event_type);
CREATE INDEX idx_security_events_created_at ON security_events(created_at);

-- Analytics
CREATE INDEX idx_daily_metrics_date ON daily_metrics(metric_date);
CREATE INDEX idx_emp_perf_cache_employee ON employee_performance_cache(employee_id);

-- File
CREATE INDEX idx_file_metadata_entity ON file_metadata(entity_type, entity_id);
```

### 6.3 Database Migration Strategy

**Tool**: Flyway (versioned migrations)

**Naming Convention**: `V{major}.{minor}__{description}.sql`

**Migration Process**:
- Runs automatically on application startup
- Each migration is transactional
- Checksum validation prevents tampering
- Zero-downtime strategy: add nullable columns first → deploy app → backfill → add constraints in next release

---

## 7. Integration Architecture

### 7.1 Event-Driven Communication

All inter-module communication via Spring Application Events. No module directly calls another module's service.

```
Auth Module       → UserAuthenticatedEvent, UserLoggedOutEvent, PasswordChangedEvent, AccountActivatedEvent
Employee Module   → EmployeeCreatedEvent, EmployeeUpdatedEvent, EmployeeActivatedEvent, EmployeeDeactivatedEvent, EmployeeDeletedEvent
Task Module       → TaskCreatedEvent, TaskAssignedEvent, TaskStatusChangedEvent, TaskUpdatedEvent, TaskCommentAddedEvent, TaskOverdueEvent

Notification Module → listens to all above → sends emails (via email_queue) + in-app notifications
Audit Module        → listens to all above → creates audit_logs and security_events entries
```

### 7.2 Email Integration

- Provider: SMTP Gmail (`spring-boot-starter-mail`)
- Templates: Thymeleaf HTML
- Delivery: Asynchronous via `email_queue` table
- Retry: 3 attempts, exponential backoff (1 min, 5 min, 15 min)
- Failure: Logged in `email_queue.error_message`

### 7.3 WebSocket Integration

- Protocol: STOMP over SockJS
- Endpoint: `/ws`
- Topics:
  - `/topic/notifications/{userId}` — Personal notifications
  - `/topic/tasks/{taskId}` — Task-specific real-time updates
  - `/topic/system` — System-wide announcements
- Authentication: JWT cookie validated at WebSocket handshake

### 7.4 File Storage Integration

Strategy pattern — switchable via `STORAGE_TYPE` env:

```
STORAGE_TYPE=local → LocalStorageService  → /var/taskhive/uploads/{module}/{entityId}/{filename}
STORAGE_TYPE=s3    → S3StorageService     → AWS S3 bucket (pre-signed URLs for access)
```

All file metadata tracked in `file_metadata` table regardless of storage type.

### 7.5 ML Service Integration (Future)

```java
@CircuitBreaker(name = "mlService", fallbackMethod = "fallbackPriority")
public PriorityPrediction predictPriority(TaskPredictionRequest request) {
    return restTemplate.postForObject(mlServiceUrl + "/predict/task-priority",
        request, PriorityPrediction.class);
}

public PriorityPrediction fallbackPriority(TaskPredictionRequest request, Exception e) {
    return new PriorityPrediction("MEDIUM", 0.5, "ML service unavailable, using default");
}
```

Circuit breaker: timeout 5s, failure threshold 5 consecutive, wait 30s before retry.

### 7.6 API Versioning

URL versioning: `/api/v1/`. Breaking changes require new version. Non-breaking changes (new optional fields, new endpoints) stay on same version.

---

## 8. Security Architecture

### 8.1 Authentication Flow (HttpOnly Cookie)

```
LOGIN:
──────
1.  Client  →  POST /api/v1/auth/login { email, password }
2.  Server  →  Validate credentials
3.  Server  →  Generate accessToken (JWT, 15min) + refreshToken (UUID)
4.  Server  →  Store SHA-256(refreshToken) in refresh_tokens table
5.  Server  →  Set cookies:
                 accessToken:  HttpOnly, Secure, SameSite=Strict, Max-Age=900
                 refreshToken: HttpOnly, Secure, SameSite=Strict, Max-Age=604800, Path=/api/v1/auth/refresh
6.  Server  →  Return { user: { id, email, role, firstName, lastName } }
7.  Frontend →  Store user info in Zustand only (NO tokens in JS)

REQUEST:
────────
1.  Client  →  API call with withCredentials: true (browser auto-attaches cookies)
2.  Filter  →  JwtAuthenticationFilter reads accessToken from cookie
3.  Filter  →  Validates JWT → sets SecurityContext
4.  Request →  Proceeds to controller

REFRESH:
────────
1.  Server  →  Returns 401 (accessToken expired)
2.  Axios   →  Interceptor catches 401, calls POST /api/v1/auth/refresh
3.  Browser →  Auto-sends refreshToken cookie (Path matches)
4.  Server  →  Validates refreshToken hash in DB → issues new accessToken cookie
5.  Axios   →  Retries original request

LOGOUT:
───────
1.  Client  →  POST /api/v1/auth/logout
2.  Server  →  Reads refreshToken from cookie
3.  Server  →  Sets refresh_tokens.revoked = true
4.  Server  →  Clears both cookies (Max-Age=0)
5.  Frontend →  Clears Zustand store → redirect to /login
```

### 8.2 JWT Token Structure

```json
// Access Token (15 min)
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "role": "EMPLOYEE",
  "iat": 1707912000,
  "exp": 1707912900
}
```

### 8.3 Role-Based Access Control

| Resource | ADMIN | EMPLOYEE |
|----------|-------|----------|
| Auth (login, logout, me, change-password) | ✅ | ✅ |
| Employee CRUD | ✅ | ❌ |
| Employee view own profile | ✅ | ✅ |
| Employee update own mobile/address | ✅ | ✅ |
| Task Create / Update / Delete | ✅ | ❌ |
| Task Update Status | ✅ | ✅ (own tasks only) |
| Task View All | ✅ | ❌ |
| Task View Own | ✅ | ✅ |
| Task Comments & Attachments | ✅ | ✅ (own tasks) |
| Analytics Admin Dashboard | ✅ | ❌ |
| Analytics Employee Dashboard | ❌ | ✅ |
| Audit Logs | ✅ | ❌ |
| Notifications | ✅ | ✅ (own) |

### 8.4 Password Security

- Algorithm: BCrypt strength 12
- Minimum: 8 characters, uppercase, lowercase, number, special character
- Password history: last 3 passwords cannot be reused
- Reset/Activation tokens: UUID v4, stored as SHA-256 hash

### 8.5 Account Lockout

- Max failed attempts: 5
- Lockout duration: 15 minutes
- Counter resets on successful login
- Stored in `users.locked_until`

### 8.6 Security Headers

```java
http.headers()
    .contentSecurityPolicy("default-src 'self'")
    .xssProtection()
    .frameOptions().deny()
    .httpStrictTransportSecurity()
        .maxAgeInSeconds(31536000)
        .includeSubDomains(true);
```

---

### 8.7 Error Code Standardization

All API error responses follow a consistent structure:

```json
{
  "success": false,
  "errorCode": "AUTH_1001",
  "message": "Invalid email or password",
  "timestamp": "2026-02-19T10:30:00Z",
  "path": "/api/v1/auth/login"
}
```

**Error Code Ranges by Module**:

| Module | Range | Example Codes |
|--------|-------|--------------|
| Auth |  | AUTH_1001: Invalid credentials, AUTH_1002: Account locked, AUTH_1003: Token expired, AUTH_1004: Token already used, AUTH_1005: Account not active |
| Employee |  | EMP_2001: Employee not found, EMP_2002: Email already exists, EMP_2003: Cannot delete active employee with tasks |
| Task |  | TASK_3001: Task not found, TASK_3002: Invalid status transition, TASK_3003: Assignee not active, TASK_3004: Due date must be in future |
| Notification |  | NOTIF_4001: Notification not found, NOTIF_4002: Preference not found |
| Analytics |  | ANA_5001: Invalid date range, ANA_5002: Report not ready, ANA_5003: Report expired |
| Audit |  | AUDIT_6001: Unauthorized access to audit logs |
| Common |  | COMMON_9001: Validation failed, COMMON_9002: Resource not found, COMMON_9003: Access denied, COMMON_9004: Internal server error |

**HTTP Status Mapping**:

| HTTP Status | When Used |
|-------------|-----------|
| 200 OK | Successful GET, PATCH |
| 201 Created | Successful POST (resource created) |
| 204 No Content | Successful DELETE |
| 400 Bad Request | Validation errors () |
| 401 Unauthorized | Missing/expired token () |
| 403 Forbidden | Valid token but insufficient role () |
| 404 Not Found | Resource not found () |
| 409 Conflict | Duplicate resource () |
| 423 Locked | Account locked () |
| 500 Internal Server Error | Unexpected errors () |

**Frontend Handling Strategy**:
-  → Trigger silent token refresh
-  → Show lockout timer with countdown
-  → Redirect to appropriate dashboard (not generic 403 page)
-  → Map field-level errors to form inputs
- All others → Show user-friendly toast with  field

---

## 9. ML/AI Architecture (Future Placeholder)

**Current Status**: Not implemented. Infrastructure placeholder only.

### 9.1 Planned ML Features

| Feature | Model Type | Status |
|---------|-----------|--------|
| Task Priority Suggestion | Random Forest / BERT fine-tuned | Future |
| Completion Time Estimation | Gradient Boosting Regressor | Future |
| Workload Balancing | Multi-armed Bandit / RL | Future |
| Productivity Scoring | Weighted ensemble | Future |

### 9.2 ML Integration Points (When Enabled)

**Inference API**:
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
  "historicalCompletionRate": 0.87
}

// Response
{
  "predictedPriority": "HIGH",
  "confidence": 0.87,
  "estimatedHours": 4.5,
  "reasoning": "Similar tasks historically took 4-5 hours"
}
```

### 9.3 Training Pipeline (Future)

```
1. Data Extraction (from PostgreSQL)
        ↓
2. Preprocessing & Feature Engineering
        ↓
3. Model Training (cross-validation)
        ↓
4. Evaluation (test set, threshold check)
        ↓
5. Model Versioning (MLflow/DVC)
        ↓
6. Deploy to inference server (if performance > threshold)
```

**Prerequisite**: Minimum 6 months of task data required for meaningful training.

**Config**: `ML_ENABLED=false` by default. Toggle to enable.

---

## 10. Testing Strategy

### 10.1 Testing Pyramid

```
        /\
       /  \   E2E Tests — 5% (Playwright)
      /____\
     /      \  Integration Tests — 25% (Testcontainers + PostgreSQL)
    /________\
   /          \  Unit Tests — 70% (JUnit 5 + Mockito + AssertJ)
  /____________\
```

**Coverage Target**: 80%+ on service layer

### 10.2 Unit Testing (Given-When-Then)

**Tools**: JUnit 5, Mockito, AssertJ

Key scenarios per module:
- **Auth**: Login success/failure, cookie set correctly, account lockout, token refresh, logout revokes DB token, single-use token enforcement, BCrypt validation
- **Employee**: ADMIN creates employee, activation email triggered, EMPLOYEE cannot access CRUD, soft delete preserves data
- **Task**: EMPLOYEE can only see own tasks, status transitions valid, history recorded on every change
- **Notification**: Email queue created on event, retry logic on failure
- **Analytics**: Cached metrics returned, not live queries

### 10.3 Integration Testing

**Tools**: Spring Boot Test + Testcontainers (PostgreSQL 15)

```java
@SpringBootTest
@Testcontainers
class AuthIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("taskhive_test");
    // Full flow: login → cookie set → request with cookie → 401 → refresh → retry
}
```

### 10.4 End-to-End Testing

**Tool**: Playwright

Key scenarios:
1. Admin creates employee → activation email → employee sets password → employee logs in
2. Admin creates task → employee gets notification → employee updates status → admin sees update
3. Token refresh flow: wait for access token expiry → auto-refresh → original request succeeds

### 10.5 Performance Testing

**Tool**: JMeter / Gatling

| Scenario | Users | Duration |
|----------|-------|---------|
| Load Test | 100 concurrent | 10 minutes |
| Stress Test | Ramp to 500 | Gradual |
| Spike Test | 50 → 300 sudden | Instant |
| Endurance Test | 100 | 2 hours |

**Pass Criteria**: P95 < 300ms, error rate < 1%, stable heap

### 10.6 Security Testing

- SonarQube static analysis
- OWASP Dependency Check
- OWASP ZAP dynamic scanning
- Manual pen test: SQL injection, XSS, JWT tampering, CSRF, unauthorized access

---

## 11. Deployment Architecture

### 11.1 Dockerfiles

**Backend (`taskhive-backend/Dockerfile`)**:
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S taskhive && adduser -S taskhive -G taskhive
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /var/taskhive/uploads && chown taskhive:taskhive /var/taskhive/uploads
USER taskhive

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend (`taskhive-frontend/Dockerfile`)**:
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app
RUN addgroup -S taskhive && adduser -S taskhive -G taskhive
COPY --from=build /app/.next ./.next
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/package.json ./package.json
USER taskhive

EXPOSE 3000
CMD ["npm", "start"]
```

**ML Inference (`ml/inference-server/Dockerfile`)**:
```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .

EXPOSE 8000
CMD ["uvicorn", "api.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 11.2 Docker Compose (Development)

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
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U taskhive"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: ./taskhive-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/taskhive
      SPRING_DATASOURCE_USERNAME: taskhive
      SPRING_DATASOURCE_PASSWORD: taskhive123
      STORAGE_TYPE: local
      ML_ENABLED: "false"
    depends_on:
      postgres:
        condition: service_healthy
    volumes:
      - uploads_data:/var/taskhive/uploads

  frontend:
    build: ./taskhive-frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080/api/v1
      NEXT_PUBLIC_WS_URL: ws://localhost:8080/ws
      NEXT_PUBLIC_APP_NAME: TaskHive
    depends_on:
      - backend

volumes:
  postgres_data:
  uploads_data:
```

### 11.3 Environment Configuration

**Backend (`application.properties`)**:
```properties
# DATABASE
spring.datasource.url=jdbc:postgresql://localhost:5432/taskhive
spring.datasource.username=taskhive
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.datasource.hikari.maximum-pool-size=20

# FLYWAY
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# JWT
jwt.secret=your-256-bit-minimum-secret-key-here
jwt.access-token-expiry=900000
jwt.refresh-token-expiry=604800000

# COOKIE
app.cookie.secure=true
app.cookie.same-site=Strict
app.cookie.domain=yourdomain.com

# DEFAULT ADMIN
app.admin.email=admin@taskhive.com
app.admin.password=Admin@123
app.admin.first-name=System
app.admin.last-name=Admin

# TOKEN EXPIRY
app.auth.activation-token-expiry=86400000
app.auth.reset-token-expiry=3600000

# ACCOUNT LOCKOUT
app.auth.max-failed-attempts=5
app.auth.lockout-duration=900000

# MAIL (SMTP Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=noreply@taskhive.com
spring.mail.password=your-smtp-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# FILE STORAGE
storage.type=local
storage.local.upload-dir=/var/taskhive/uploads
storage.s3.bucket-name=taskhive-files
storage.s3.region=ap-south-1
storage.s3.access-key=your-aws-access-key
storage.s3.secret-key=your-aws-secret-key

# ML SERVICE
ml.enabled=false
ml.inference-url=http://ml-inference:8000

# RESILIENCE4J (Circuit Breaker for ML)
resilience4j.circuitbreaker.instances.mlService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.mlService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.mlService.waitDurationInOpenState=30s

# ACTUATOR
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=when-authorized

# FRONTEND URL (for email links)
app.frontend.url=http://localhost:3000
```

**Frontend (`.env.local`)**:
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
NEXT_PUBLIC_APP_NAME=TaskHive
NEXT_PUBLIC_ENABLE_ML=false
```

### 11.4 Kubernetes (Production)

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
        image: taskhive/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: prod
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

**Ingress**:
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: taskhive-ingress
  namespace: taskhive-prod
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
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

### 11.5 CI/CD Pipeline (GitHub Actions)

```yaml
name: TaskHive CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Maven
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Build & Test
        run: mvn clean verify
        working-directory: taskhive-backend

      - name: SonarQube
        run: mvn sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}
        working-directory: taskhive-backend

      - name: Build Docker Image
        run: docker build -t taskhive/backend:${{ github.sha }} .
        working-directory: taskhive-backend

      - name: Push to Registry
        if: github.ref == 'refs/heads/main'
        run: |
          echo ${{ secrets.REGISTRY_PASSWORD }} | docker login -u ${{ secrets.REGISTRY_USER }} --password-stdin
          docker push taskhive/backend:${{ github.sha }}

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: taskhive-frontend/package-lock.json

      - name: Install & Build
        run: |
          npm ci
          npm run build
        working-directory: taskhive-frontend

      - name: Lint
        run: npm run lint
        working-directory: taskhive-frontend

  deploy-staging:
    needs: [backend, frontend]
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: |
          kubectl set image deployment/taskhive-backend \
            backend=taskhive/backend:${{ github.sha }} \
            -n taskhive-staging

  deploy-production:
    needs: [backend, frontend]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment: production                        # Requires manual approval
    steps:
      - name: Deploy to Production (Blue-Green)
        run: |
          kubectl set image deployment/taskhive-backend \
            backend=taskhive/backend:${{ github.sha }} \
            -n taskhive-prod
```

---

## 12. Migration Path to Microservices

### 12.1 When to Consider

**Triggers**:
- Team size exceeds 15 developers
- A specific module becomes a performance bottleneck
- Independent scaling needed (e.g., ML module is CPU-intensive)
- Technology diversification required

**Recommended Extraction Order**:
1. ML/AI Service (already in separate language/repo)
2. Notification Service (least dependencies, event-only)
3. Analytics Service (read-only, no writes to core tables)
4. Auth Service
5. Employee Service
6. Task Service

### 12.2 Strangler Fig Pattern

```
Phase 1: Current Monolith
        Backend (Spring Boot) — all modules

Phase 2: Extract Notification
        API Gateway (NGINX)
              │
              ├──→ Backend Monolith (auth, employee, task, analytics, audit)
              └──→ Notification Microservice

Phase 3: Spring Events → Kafka
        Task Module  →  Kafka Topic: task-events  →  Notification Microservice

Phase 4: Separate Databases
        PostgreSQL (main)          PostgreSQL (notification)
        ├── users                  └── notifications
        ├── employees                  notification_preferences
        └── tasks                      email_queue
```

### 12.3 Microservice Readiness Checklist

- ✅ Clear bounded context per module
- ✅ Minimal cross-module dependencies
- ✅ Logical schema separation (ready for physical split)
- ✅ Event-driven communication (Spring Events → Kafka swap)
- ✅ Independent test suites per module
- ✅ Separate module configuration

**Infrastructure needed before extraction**:
- API Gateway (Kong/NGINX)
- Message queue (Kafka/RabbitMQ)
- Distributed tracing (Micrometer + Zipkin)
- Centralized logging (ELK or Loki)
- Monitoring (Prometheus + Grafana)
- Service discovery (Consul/Eureka)

### 12.4 Future Microservice Architecture

```
                    API Gateway (Kong)
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
   Auth Service    Employee Service     Task Service
        │                 │                 │
   PostgreSQL         PostgreSQL        PostgreSQL
        │                 │                 │
        └─────────────────┼─────────────────┘
                          │
                  Kafka Message Bus
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
 Notification Service  Analytics Service  ML Service
        │                 │                 │
   PostgreSQL          PostgreSQL        FastAPI
```

---

## 13. Future Enhancements

### 13.1 Redis Integration
**Priority**: High | **Target**: v3.0

JWT access token blacklisting on logout (currently access token valid until natural expiry after logout), caching for `/auth/me` and employee lists, distributed lock support.

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 13.2 Rate Limiting on Auth Endpoints
**Priority**: High | **Target**: v3.0

Bucket4j (Redis-backed) for per-IP limits:

| Endpoint | Limit |
|----------|-------|
| POST /auth/login | 10 req/min/IP |
| POST /auth/forgot-password | 3 req/min/IP |
| POST /auth/reset-password | 5 req/min/IP |
| POST /auth/activate-account | 5 req/min/IP |

### 13.3 Refresh Token Rotation Hardening
**Priority**: Medium | **Target**: v2.3

Token family tracking — if a revoked refresh token is reused, invalidate all sessions for that user and alert via email (replay attack detection).

### 13.4 Multi-Tenancy Support
**Priority**: Medium | **Target**: v4.0

Schema-per-tenant isolation. `tenant_id` on all core tables. Tenant resolution via subdomain. Super Admin role for managing organizations. Major refactor — deferred intentionally.

### 13.5 Production S3 Setup Checklist
**Priority**: Medium | **Target**: Before first production deployment

- [ ] Create dedicated S3 bucket
- [ ] Private bucket policy (no public access)
- [ ] IAM user with least-privilege S3 permissions
- [ ] Enable S3 versioning for attachments
- [ ] Lifecycle policy for old files
- [ ] Pre-signed URL generation (1-hour expiry)
- [ ] CloudFront CDN for profile photos (optional)

### 13.6 ElasticSearch
**Priority**: Low | **Target**: v4.0

PostgreSQL full-text search sufficient until 100k+ records. ElasticSearch for fuzzy matching, relevance scoring, autocomplete at scale.

### 13.7 Spring Events → Kafka Migration
**Priority**: Low | **Target**: During microservices migration

Current Spring Events are in-memory synchronous. Kafka provides guaranteed delivery, replay, consumer groups — required when modules become separate services.

### 13.8 ML/AI Features
**Priority**: Low | **Target**: v5.0

Minimum 6 months of production data required before training. See Section 9 for full ML architecture.

### 13.9 Contract Testing
**Priority**: Medium | **Target**: v2.3

Spring Cloud Contract for backend. Pact for frontend-backend API contracts. Prevents breaking changes between frontend and backend.

### 13.10 Load Balancing Design
**Priority**: Low | **Target**: When traffic exceeds 1000 concurrent users or 99.99% uptime required

Single instance deployment sufficient for MVP. When horizontal scaling is needed:
- **Load Balancer**: NGINX or AWS ALB in front of multiple backend instances
- **Session Affinity**: Not required — application is fully stateless (JWT in cookie, no server-side session)
- **Health Checks**:  used by LB to route traffic only to healthy instances
- **Trigger**: Traffic > 10k concurrent users OR business SLA > 99.9% uptime

### 13.11 Horizontal Scaling WebSocket Strategy
**Priority**: Low | **Target**: When multiple backend instances are deployed (see 13.10)

Current single-instance WebSocket works fine for MVP. When running multiple backend replicas:
- **Problem**: WebSocket connections are instance-specific — a user connected to Instance A won't receive events published on Instance B
- **Solution**: Redis Pub/Sub as shared WebSocket message broker
- **Implementation**: Spring WebSocket + STOMP + Redis Pub/Sub ( + )
- **Config**: All instances subscribe to Redis channels; any instance publishes → all instances receive → correct WebSocket session notified
- **Dependency**: Requires Redis (already planned in Section 13.1)
- **Trigger**: Add this when deploying 2+ backend replicas behind a load balancer

### 13.12 Database Partitioning Plan
**Priority**: Low | **Target**: When individual tables exceed 10 million rows

PostgreSQL with proper indexes sufficient for current scale (target: <100k employees, <1M tasks). Partitioning when needed:

| Table | Partition Strategy | Partition Key | Trigger |
|-------|-------------------|---------------|---------|
|  | Range partitioning by month |  | > 10M rows |
|  | Range partitioning by month |  | > 5M rows |
|  | Range partitioning by month |  | > 5M rows |
|  | Range partitioning by month |  | > 5M rows |

**Implementation**: PostgreSQL native declarative partitioning (no application code change needed). Old partitions archived to cold storage after 1 year (aligns with audit retention policy).

**Do NOT partition prematurely** — adds operational complexity with no benefit at small scale.

---

## Appendix A: Architecture Decision Records

### ADR-001: Modular Monolith over Microservices
**Decision**: Start with modular monolith. Team size and complexity don't justify microservices overhead. Clear module boundaries allow future extraction via Strangler Fig pattern.

### ADR-002: HttpOnly Cookie over localStorage for JWT
**Decision**: JWT stored in HttpOnly, Secure, SameSite=Strict cookies only. localStorage is XSS-vulnerable. Cookie approach requires `withCredentials: true`, configured CORS with `allowCredentials`, server-side cookie clear on logout. `SameSite=Strict` also provides CSRF protection — no separate CSRF token mechanism needed; `.csrf().disable()` in Spring Security is intentional and safe.

### ADR-003: Role as Alag Entity (not enum-only)
**Decision**: `Role` is a DB entity (`roles` table + `user_roles` join table), not just an enum. `RoleType` enum exists as constants. Rationale: Future multi-tenancy requires Super Admin and potentially Manager/Team Lead roles. Adding a row to `roles` table is far cheaper than a code deploy + enum change.

### ADR-004: PostgreSQL over NoSQL
**Decision**: PostgreSQL 15+. ACID compliance critical for task/employee data. Complex analytics easier with SQL. JSONB for flexibility (audit logs, notification data). Full-text search built-in for current scale.

### ADR-005: Event-Driven Inter-Module Communication
**Decision**: Spring Application Events for all inter-module communication. No direct service-to-service calls across module boundaries. Prepares for Kafka migration when extracting microservices.

### ADR-006: Local + S3 Storage via Strategy Pattern
**Decision**: `StorageService` interface with `LocalStorageService` and `S3StorageService` implementations. Switch via `STORAGE_TYPE` env variable. Zero code change to switch environments.

### ADR-007: Spring Boot 3.5.10 + Java 21
**Decision**: Latest stable Spring Boot with Java 21 LTS. Virtual Threads (Project Loom) available, GraalVM native support, pattern matching, records — all production-ready.

### ADR-008: Separate `task_status_history` Table
**Decision**: Dedicated `task_status_history` table (not generic `task_history`). Status changes are the most queried history type. Includes `comment` and `ip_address` — specific to status change semantics. Generic history tables become unqueryable at scale.

### ADR-009: Email Queue Table
**Decision**: `email_queue` table for async email delivery with retry. Emails failing should never cause API errors. Queue allows retry with backoff, failure tracking, and future migration to a proper message queue.

### ADR-010: Separate `security_events` Table
**Decision**: Security events (failed logins, lockouts, unauthorized access) in dedicated `security_events` table, separate from `audit_logs`. Security monitoring queries are high-frequency and have different retention/alerting needs. Mixing with general audit logs degrades query performance.

---

## Appendix B: API Endpoint Summary

### Auth
```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/activate-account
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
POST   /api/v1/auth/change-password
GET    /api/v1/auth/me
```

### Employee
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
PATCH  /api/v1/employees/{id}/profile
```

### Task
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
GET    /api/v1/tasks/{id}/attachments
GET    /api/v1/tasks/{id}/history
GET    /api/v1/tasks/my-tasks
GET    /api/v1/tasks/overdue
GET    /api/v1/tasks/search
```

### Analytics
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

### Notification
```
GET    /api/v1/notifications
GET    /api/v1/notifications/unread
GET    /api/v1/notifications/unread-count
PATCH  /api/v1/notifications/{id}/read
PATCH  /api/v1/notifications/mark-all-read
GET    /api/v1/notifications/preferences
PUT    /api/v1/notifications/preferences
```

### Audit
```
GET    /api/v1/audit/logs
GET    /api/v1/audit/logs/search
GET    /api/v1/audit/logs/entity/{type}/{id}
GET    /api/v1/audit/security-events
GET    /api/v1/audit/compliance/report
```

---

## Appendix C: Glossary

| Term | Definition |
|------|-----------|
| Modular Monolith | Single deployable unit with clear internal module boundaries |
| Bounded Context | Self-contained domain area with its own models and logic |
| HttpOnly Cookie | Browser cookie inaccessible to JavaScript — XSS protection |
| SameSite=Strict | Cookie sent only for same-origin requests — CSRF protection |
| Soft Delete | `is_deleted=true` + `deleted_at` — no physical removal |
| Optimistic Locking | `@Version` field on JPA entity — prevents lost updates on concurrent writes |
| Domain Event | Significant occurrence that other modules react to |
| DTO | Data Transfer Object — data carrier between layers |
| Flyway | Versioned DB migration tool |
| Strangler Fig | Gradual microservice extraction from monolith |
| BCrypt | Password hashing with configurable cost factor |
| SHA-256 | Hash function for storing tokens in DB |
| RBAC | Role-Based Access Control |
| STOMP | Simple Text Oriented Messaging Protocol over WebSocket |
| SockJS | WebSocket fallback library |
| Circuit Breaker | Prevents cascading failures when downstream service is unavailable |
| Email Queue | Async email delivery table with retry logic |
| Blue-Green | Zero-downtime deployment with two identical environments |

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

**Document Version**: 2.3
**Last Updated**: February 19, 2026
**Next Review**: Quarterly or on major architectural changes
**Owner**: System Architecture Team — Digiwork
**Classification**: Internal - Confidential

---

**END OF DOCUMENT**
