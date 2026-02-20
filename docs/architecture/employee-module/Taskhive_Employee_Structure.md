# TaskHive — Phase 2: Employee Module
## SRS v2.3 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 2 of 6 |
| **Name** | Employee Module |
| **Modules** | Employee Module (Backend + Frontend) |
| **DB Migrations** | V2.0 + V2.1 |
| **Duration** | ~1.5 weeks |
| **Depends On** | Phase 1 (Auth Module must be complete) |

### Deliverable
Admin can create employees, manage their status (activate/deactivate/delete), upload profile photos, and search employees. Employee creation automatically triggers account activation email. Manager hierarchy is stored via `manager_id` self-reference.

---

## API Endpoints (Employee Module)

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
```

---

## Functional Requirements Covered

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

---

## Backend File & Folder Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/digiwork/taskhive/
│   │       └── module/
│   │           └── employee/                              # EMPLOYEE MODULE
│   │               ├── controller/
│   │               │   └── EmployeeController.java
│   │               │
│   │               ├── service/
│   │               │   ├── EmployeeService.java
│   │               │   ├── EmployeeSearchService.java
│   │               │   └── ProfilePhotoService.java
│   │               │
│   │               ├── dto/
│   │               │   ├── CreateEmployeeRequest.java
│   │               │   ├── UpdateEmployeeRequest.java
│   │               │   ├── EmployeeResponse.java
│   │               │   ├── EmployeeListResponse.java
│   │               │   └── EmployeeSearchRequest.java
│   │               │
│   │               ├── model/
│   │               │   ├── Employee.java                  # id, userId, name, email, phone, dept, designation, managerId, joinDate, photoUrl, status, version, createdBy, updatedBy
│   │               │   └── EmployeeStatusHistory.java     # id, employeeId, oldStatus, newStatus, changedBy, reason, changedAt
│   │               │
│   │               ├── repository/
│   │               │   ├── EmployeeRepository.java
│   │               │   └── EmployeeStatusHistoryRepository.java
│   │               │
│   │               ├── event/
│   │               │   ├── EmployeeCreatedEvent.java
│   │               │   ├── EmployeeUpdatedEvent.java
│   │               │   ├── EmployeeActivatedEvent.java
│   │               │   ├── EmployeeDeactivatedEvent.java
│   │               │   └── EmployeeDeletedEvent.java
│   │               │
│   │               ├── exception/
│   │               │   ├── EmployeeNotFoundException.java
│   │               │   └── EmployeeAlreadyExistsException.java
│   │               │
│   │               └── mapper/
│   │                   └── EmployeeMapper.java
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V2.0__create_employees_table.sql
│               └── V2.1__create_employee_status_history_table.sql
│
└── test/
    └── java/
        └── com/digiwork/taskhive/
            ├── module/
            │   └── employee/
            │       ├── controller/
            │       │   └── EmployeeControllerTest.java
            │       └── service/
            │           └── EmployeeServiceTest.java
            │
            └── integration/
                └── EmployeeIntegrationTest.java
```

---

## Frontend File & Folder Structure

```
src/
├── app/
│   └── (admin)/                               # Admin routes (ADMIN role only)
│       ├── layout.tsx
│       ├── dashboard/page.tsx
│       └── employees/
│           ├── page.tsx                       # Employee list
│           ├── new/page.tsx                   # Create employee
│           └── [id]/page.tsx                  # Employee detail
│
└── features/
    └── employee/
        ├── components/
        │   ├── EmployeeList.tsx
        │   ├── EmployeeCard.tsx
        │   ├── EmployeeForm.tsx
        │   ├── EmployeeDetail.tsx
        │   └── ProfilePhotoUpload.tsx
        ├── hooks/
        │   ├── useEmployees.ts
        │   ├── useEmployee.ts
        │   ├── useCreateEmployee.ts
        │   ├── useUpdateEmployee.ts
        │   ├── useDeleteEmployee.ts
        │   └── useEmployeeSearch.ts
        ├── services/
        │   └── employeeService.ts
        └── types/
            └── employee.types.ts
```

---

## DB Migrations — Phase 2

| File | Description |
|------|-------------|
| `V2.0__create_employees_table.sql` | employees — id, user_id (FK users), first_name, last_name, email, phone, department, designation, manager_id (self-ref FK), join_date, photo_url, status, is_deleted, deleted_at, version, created_at, updated_at, created_by, updated_by |
| `V2.1__create_employee_status_history_table.sql` | employee_status_history — id, employee_id (FK), old_status, new_status, changed_by (FK users), reason, changed_at |

---

## Events Published (consumed by Notification Module in Phase 4)

| Event Class | Trigger | Consumer |
|-------------|---------|----------|
| `EmployeeCreatedEvent` | Admin creates new employee | NotificationModule → sends activation email |
| `EmployeeUpdatedEvent` | Admin updates employee profile | AuditModule → audit log |
| `EmployeeActivatedEvent` | Admin activates employee | AuditModule → audit log |
| `EmployeeDeactivatedEvent` | Admin deactivates employee | AuditModule → audit log |
| `EmployeeDeletedEvent` | Admin soft-deletes employee | AuditModule → audit log |

---

## Phase 2 File Count Summary

| Layer | Backend | Frontend |
|-------|---------|----------|
| Controller | 1 | — |
| Service | 3 | — |
| DTO | 5 | — |
| Model | 2 | — |
| Repository | 2 | — |
| Event | 5 | — |
| Exception | 2 | — |
| Mapper | 1 | — |
| DB Migrations | 2 | — |
| Tests | 3 | — |
| App Pages | — | 4 |
| Feature Components | — | 5 |
| Feature Hooks | — | 6 |
| Feature Services | — | 1 |
| Feature Types | — | 1 |
