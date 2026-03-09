# TaskHive Flutter — Phase 2: Employee Module
## Version 1.0 | SRS v2.4 | Digiwork
## ⚠️ Always provide `TaskHive_Flutter_Master_Context.md` alongside this file

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 2 of 6 |
| **Name** | Employee Module |
| **Scope** | Employee models, repository, providers, admin CRUD screens, employee self-profile screen, photo upload, search, status management, Hive caching |
| **Depends On** | Phase 1 (Auth Module must be complete and compiling on all platforms) |
| **Platforms** | Android ✅ iOS ✅ macOS ✅ Web ✅ Windows ✅ Linux ✅ |

### Deliverable
Admin can create employees, view paginated + filterable employee list, view employee detail, edit employee profile, activate/deactivate/delete employees, upload profile photos, and search employees. Logged-in employee can view and update their own profile (phone + address). Employee list is cached in Hive (`employees_box`) with stale-while-revalidate strategy. Router is extended with real employee screens (replacing Phase 1 placeholders).

---

## Phase 2 — Files To Create (~22 files)

### Complete Folder Structure for This Phase

```
lib/
│
├── features/
│   └── employee/
│       ├── data/
│       │   ├── models/
│       │   │   ├── employee_model.dart                  # ★ Full implementation below
│       │   │   ├── employee_status_history_model.dart   # ★ Full implementation below
│       │   │   ├── create_employee_request.dart         # ★ Full implementation below
│       │   │   ├── update_employee_request.dart         # ★ Full implementation below
│       │   │   └── update_employee_profile_request.dart # ★ Full implementation below
│       │   └── repositories/
│       │       └── employee_repository.dart             # ★ Full implementation below
│       │
│       ├── domain/
│       │   └── providers/
│       │       ├── employee_list_provider.dart          # ★ Full implementation below
│       │       ├── employee_detail_provider.dart        # ★ Full implementation below
│       │       ├── employee_search_provider.dart        # ★ Full implementation below
│       │       ├── employee_actions_provider.dart       # ★ Full implementation below
│       │       └── profile_photo_provider.dart          # ★ Full implementation below
│       │
│       └── presentation/
│           ├── screens/
│           │   ├── admin/
│           │   │   ├── employee_list_screen.dart        # ★ Full implementation below
│           │   │   ├── employee_detail_screen.dart      # ★ Full implementation below
│           │   │   ├── create_employee_screen.dart      # ★ Full implementation below
│           │   │   └── edit_employee_screen.dart        # ★ Full implementation below
│           │   └── employee/
│           │       └── my_profile_screen.dart           # ★ Full implementation below
│           └── widgets/
│               ├── employee_list_tile.dart              # Spec below
│               ├── employee_status_badge.dart           # Spec below
│               ├── employee_filter_bar.dart             # Spec below
│               ├── employee_form.dart                   # Spec below
│               ├── employee_search_bar.dart             # Spec below
│               ├── profile_photo_widget.dart            # Spec below
│               ├── status_history_timeline.dart         # Spec below
│               └── manager_selector_field.dart          # Spec below
│
└── core/
    └── router/
        └── app_router.dart                             # MODIFY — extend Phase 1 router
```

> **Do NOT touch** any Phase 1 files except `app_router.dart` (router extension only — see section below).
> `app_routes.dart` already has all Phase 2 route constants — no changes needed there.
> `api_endpoints.dart` already has all Phase 2 endpoints — no changes needed there.
> `cache_service.dart` already has `employees_box` methods — no changes needed there.

---

## API Endpoints Used in Phase 2

```
POST   /api/v1/employees                    # Create employee (ADMIN only)
GET    /api/v1/employees                    # List with pagination + filters (ADMIN only)
GET    /api/v1/employees/{id}              # Get employee detail (both roles)
PUT    /api/v1/employees/{id}              # Update employee (ADMIN only)
DELETE /api/v1/employees/{id}              # Soft delete (ADMIN only)
PATCH  /api/v1/employees/{id}/activate     # Activate (ADMIN only)
PATCH  /api/v1/employees/{id}/deactivate   # Deactivate (ADMIN only)
POST   /api/v1/employees/{id}/photo        # Upload profile photo (max 5MB, JPG/PNG/WebP)
GET    /api/v1/employees/{id}/photo        # Get profile photo URL
GET    /api/v1/employees/search            # Search by name, email, department, designation
PATCH  /api/v1/employees/{id}/profile      # Employee updates own phone + address (EMPLOYEE role — FR-EMP-06)
```

### Response Shapes

```json
// GET /api/v1/employees — paginated list
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "userId": "uuid",
        "firstName": "Jane",
        "lastName": "Doe",
        "email": "jane.doe@taskhive.com",
        "phone": "+91-9876543210",
        "address": "Pune, MH",
        "department": "Engineering",
        "designation": "Software Engineer",
        "managerId": "uuid-or-null",
        "managerName": "John Smith",
        "joinDate": "2026-01-15",
        "photoUrl": "/api/v1/employees/uuid/photo",
        "status": "ACTIVE",
        "createdAt": "2026-01-15T09:00:00Z",
        "updatedAt": "2026-01-15T09:00:00Z"
      }
    ],
    "totalElements": 42,
    "totalPages": 5,
    "size": 10,
    "number": 0
  },
  "message": "Employees fetched successfully"
}

// GET /api/v1/employees/{id} — single employee detail (same EmployeeResponse fields)
// Status field values: "PENDING" | "ACTIVE" | "INACTIVE" | "DELETED"

// GET /api/v1/employees/search — same paginated shape, filtered

// GET /api/v1/employees/{id}/photo — 200 OK
{
  "success": true,
  "data": { "photoUrl": "https://..." },
  "message": "Photo URL fetched"
}

// POST /api/v1/employees/{id}/photo — multipart/form-data, field name: "photo"
// 200 OK: { "success": true, "data": { "photoUrl": "..." }, "message": "Photo uploaded" }

// POST /api/v1/employees — 201 Created
{
  "success": true,
  "data": { /* EmployeeResponse */ },
  "message": "Employee created successfully"
}

// PUT /api/v1/employees/{id}, PATCH activate/deactivate, PATCH profile — 200 OK
{
  "success": true,
  "data": { /* EmployeeResponse */ },
  "message": "..."
}

// DELETE /api/v1/employees/{id} — 200 OK
{
  "success": true,
  "data": null,
  "message": "Employee deleted successfully"
}

// All errors follow the global shape from Master Context:
// { "success": false, "message": "...", "errorCode": "...", "data": null }
// Phase 2 specific error codes: EMPLOYEE_NOT_FOUND, DUPLICATE_EMAIL, RESOURCE_NOT_FOUND
```

---

## Functional Requirements Covered in Phase 2

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-EMP-01 | Critical | ADMIN creates employees (firstName, lastName, email, phone, department, designation, joinDate, managerId optional) |
| FR-EMP-02 | Critical | Employee creation triggers activation email automatically (backend-driven — Flutter shows success message) |
| FR-EMP-03 | Critical | New account is PENDING; Flutter shows status badge — employee cannot login until activated |
| FR-EMP-04 | High | ADMIN views paginated, filterable employee list (filter: name, email, department, status, role) |
| FR-EMP-05 | High | ADMIN updates employee profile information |
| FR-EMP-06 | High | Employee updates own mobile and address via `PATCH /employees/{id}/profile` |
| FR-EMP-07 | High | ADMIN soft-deletes employees — Flutter removes from list, backend preserves history |
| FR-EMP-09 | High | ADMIN activates and deactivates employees — status badge updates immediately |
| FR-EMP-10 | Medium | Employee or ADMIN uploads profile photo (max 5MB, JPG/PNG/WebP) via `image_picker` |
| FR-EMP-11 | Medium | Employee search by name, email, department, designation |
| FR-EMP-13 | Low | Manager hierarchy — `managerId` field in create/edit form (manager selector widget) |
| FR-EMP-14 | High | Status change history displayed in employee detail screen (timeline widget) |

> FR-EMP-08 (deleted accounts cannot login) and FR-EMP-12 (CSV export) are enforced backend-side.
> Flutter shows DELETED status badge; CSV export is a future enhancement (Phase 6 analytics).

---

## ★ Employee Models

### features/employee/data/models/employee_model.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'employee_model.freezed.dart';
part 'employee_model.g.dart';

/// Employee status values as returned by the backend.
/// PENDING → awaiting account activation
/// ACTIVE  → can login and receive tasks
/// INACTIVE → temporarily deactivated by admin
/// DELETED  → soft-deleted; cannot login
enum EmployeeStatus {
  @JsonValue('PENDING') pending,
  @JsonValue('ACTIVE') active,
  @JsonValue('INACTIVE') inactive,
  @JsonValue('DELETED') deleted,
}

@freezed
class EmployeeModel with _$EmployeeModel {
  const factory EmployeeModel({
    required String id,
    required String userId,
    required String firstName,
    required String lastName,
    required String email,
    String? phone,
    String? address,
    String? department,
    String? designation,
    String? managerId,
    String? managerName,
    String? joinDate,      // ISO date string "2026-01-15"
    String? photoUrl,
    required EmployeeStatus status,
    String? createdAt,
    String? updatedAt,
  }) = _EmployeeModel;

  factory EmployeeModel.fromJson(Map<String, dynamic> json) =>
      _$EmployeeModelFromJson(json);
}

extension EmployeeModelX on EmployeeModel {
  String get fullName => '$firstName $lastName';

  /// Initials for avatar fallback — e.g., "JD"
  String get initials =>
      '${firstName.isNotEmpty ? firstName[0] : ''}${lastName.isNotEmpty ? lastName[0] : ''}'
          .toUpperCase();

  bool get isActive => status == EmployeeStatus.active;
  bool get isPending => status == EmployeeStatus.pending;
  bool get isInactive => status == EmployeeStatus.inactive;
  bool get isDeleted => status == EmployeeStatus.deleted;

  /// Returns true if admin can activate this employee
  bool get canActivate =>
      status == EmployeeStatus.pending || status == EmployeeStatus.inactive;

  /// Returns true if admin can deactivate this employee
  bool get canDeactivate => status == EmployeeStatus.active;
}
```

### features/employee/data/models/employee_status_history_model.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'employee_status_history_model.freezed.dart';
part 'employee_status_history_model.g.dart';

@freezed
class EmployeeStatusHistoryModel with _$EmployeeStatusHistoryModel {
  const factory EmployeeStatusHistoryModel({
    required String id,
    required String employeeId,
    String? oldStatus,
    required String newStatus,
    required String changedBy,      // Display name of actor
    String? reason,
    required String changedAt,      // ISO datetime string
  }) = _EmployeeStatusHistoryModel;

  factory EmployeeStatusHistoryModel.fromJson(Map<String, dynamic> json) =>
      _$EmployeeStatusHistoryModelFromJson(json);
}
```

### features/employee/data/models/create_employee_request.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'create_employee_request.freezed.dart';
part 'create_employee_request.g.dart';

@freezed
class CreateEmployeeRequest with _$CreateEmployeeRequest {
  const factory CreateEmployeeRequest({
    required String firstName,
    required String lastName,
    required String email,
    String? phone,
    String? department,
    String? designation,
    String? joinDate,       // "YYYY-MM-DD"
    String? managerId,      // optional self-reference
  }) = _CreateEmployeeRequest;

  factory CreateEmployeeRequest.fromJson(Map<String, dynamic> json) =>
      _$CreateEmployeeRequestFromJson(json);
}
```

### features/employee/data/models/update_employee_request.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'update_employee_request.freezed.dart';
part 'update_employee_request.g.dart';

/// Used by ADMIN to update any employee profile field.
@freezed
class UpdateEmployeeRequest with _$UpdateEmployeeRequest {
  const factory UpdateEmployeeRequest({
    String? firstName,
    String? lastName,
    String? phone,
    String? address,
    String? department,
    String? designation,
    String? joinDate,
    String? managerId,
  }) = _UpdateEmployeeRequest;

  factory UpdateEmployeeRequest.fromJson(Map<String, dynamic> json) =>
      _$UpdateEmployeeRequestFromJson(json);
}
```

### features/employee/data/models/update_employee_profile_request.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'update_employee_profile_request.freezed.dart';
part 'update_employee_profile_request.g.dart';

/// Used by the logged-in EMPLOYEE to update their own phone and address only.
/// Maps to PATCH /employees/{id}/profile (FR-EMP-06).
@freezed
class UpdateEmployeeProfileRequest with _$UpdateEmployeeProfileRequest {
  const factory UpdateEmployeeProfileRequest({
    String? phone,
    String? address,
  }) = _UpdateEmployeeProfileRequest;

  factory UpdateEmployeeProfileRequest.fromJson(Map<String, dynamic> json) =>
      _$UpdateEmployeeProfileRequestFromJson(json);
}
```

---

## Code Generation — Required After Writing Models

```bash
# Run from project root — taskhive-flutter/
dart run build_runner build --delete-conflicting-outputs
```

Files this generates (do NOT write manually):
- `employee_model.freezed.dart`, `employee_model.g.dart`
- `employee_status_history_model.freezed.dart`, `employee_status_history_model.g.dart`
- `create_employee_request.freezed.dart`, `create_employee_request.g.dart`
- `update_employee_request.freezed.dart`, `update_employee_request.g.dart`
- `update_employee_profile_request.freezed.dart`, `update_employee_profile_request.g.dart`
- `employee_list_provider.g.dart`, `employee_detail_provider.g.dart`
- `employee_search_provider.g.dart`, `employee_actions_provider.g.dart`
- `profile_photo_provider.g.dart`

---

## ★ features/employee/data/repositories/employee_repository.dart

```dart
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../models/create_employee_request.dart';
import '../models/employee_model.dart';
import '../models/employee_status_history_model.dart';
import '../models/update_employee_profile_request.dart';
import '../models/update_employee_request.dart';

final employeeRepositoryProvider = Provider<EmployeeRepository>((ref) {
  return EmployeeRepository(dio: ref.watch(dioClientProvider));
});

class EmployeeRepository {
  final Dio _dio;
  EmployeeRepository({required Dio dio}) : _dio = dio;

  /// Fetch paginated employee list with optional filters.
  /// Query params: page, size, department, status, search
  Future<Map<String, dynamic>> getEmployees({
    int page = 0,
    int size = 20,
    String? department,
    String? status,
    String? search,
  }) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.employees,
        queryParameters: {
          'page': page,
          'size': size,
          if (department != null && department.isNotEmpty)
            'department': department,
          if (status != null && status.isNotEmpty) 'status': status,
          if (search != null && search.isNotEmpty) 'search': search,
        },
      );
      // Returns the raw `data` map — provider parses content + pagination
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Fetch single employee by ID.
  Future<EmployeeModel> getEmployeeById(String id) async {
    try {
      final response = await _dio.get(ApiEndpoints.employeeById(id));
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Create a new employee. ADMIN only.
  /// Backend auto-sends activation email after creation.
  Future<EmployeeModel> createEmployee(CreateEmployeeRequest request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.employees,
        data: request.toJson(),
      );
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Update employee profile. ADMIN only.
  Future<EmployeeModel> updateEmployee(
      String id, UpdateEmployeeRequest request) async {
    try {
      final response = await _dio.put(
        ApiEndpoints.employeeById(id),
        data: request.toJson(),
      );
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Employee updates own phone + address. EMPLOYEE role only.
  Future<EmployeeModel> updateOwnProfile(
      String id, UpdateEmployeeProfileRequest request) async {
    try {
      final response = await _dio.patch(
        ApiEndpoints.employeeProfile(id),
        data: request.toJson(),
      );
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Soft-delete employee. ADMIN only.
  Future<void> deleteEmployee(String id) async {
    try {
      await _dio.delete(ApiEndpoints.employeeById(id));
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Activate employee account. ADMIN only.
  Future<EmployeeModel> activateEmployee(String id) async {
    try {
      final response =
          await _dio.patch(ApiEndpoints.employeeActivate(id));
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Deactivate employee account. ADMIN only.
  Future<EmployeeModel> deactivateEmployee(String id) async {
    try {
      final response =
          await _dio.patch(ApiEndpoints.employeeDeactivate(id));
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Upload profile photo using multipart/form-data.
  /// [filePath] is only available on native platforms. Web passes bytes instead.
  /// The calling provider must handle platform-conditional logic.
  Future<String> uploadPhoto(String id, File file) async {
    try {
      final formData = FormData.fromMap({
        'photo': await MultipartFile.fromFile(
          file.path,
          filename: file.path.split('/').last,
        ),
      });
      final response = await _dio.post(
        ApiEndpoints.employeePhoto(id),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return response.data['data']['photoUrl'] as String;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Upload profile photo from raw bytes (Web-safe).
  Future<String> uploadPhotoBytes(
      String id, List<int> bytes, String filename) async {
    try {
      final formData = FormData.fromMap({
        'photo': MultipartFile.fromBytes(bytes, filename: filename),
      });
      final response = await _dio.post(
        ApiEndpoints.employeePhoto(id),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return response.data['data']['photoUrl'] as String;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Search employees by name, email, department, designation.
  Future<List<EmployeeModel>> searchEmployees(String query) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.employeeSearch,
        queryParameters: {'q': query},
      );
      final list = response.data['data']['content'] as List<dynamic>;
      return list
          .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Fetch status change history for an employee.
  /// Backend returns this inside the employee detail or a dedicated endpoint.
  /// If backend returns history inside GET /employees/{id}, parse from there.
  Future<List<EmployeeStatusHistoryModel>> getStatusHistory(
      String employeeId) async {
    try {
      // Status history is embedded in the employee detail response
      // under data.statusHistory — adjust key if backend differs
      final response =
          await _dio.get('${ApiEndpoints.employeeById(employeeId)}/history');
      final list = response.data['data'] as List<dynamic>;
      return list
          .map((e) => EmployeeStatusHistoryModel.fromJson(
              e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
```

---

## ★ Domain Providers

### features/employee/domain/providers/employee_list_provider.dart

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../../core/storage/cache_service.dart';
import '../../data/models/employee_model.dart';
import '../../data/repositories/employee_repository.dart';

part 'employee_list_provider.g.dart';

/// Filter state for the employee list screen.
class EmployeeListFilter {
  final String? department;
  final String? status;
  final String? search;

  const EmployeeListFilter({this.department, this.status, this.search});

  EmployeeListFilter copyWith({
    String? department,
    String? status,
    String? search,
  }) =>
      EmployeeListFilter(
        department: department ?? this.department,
        status: status ?? this.status,
        search: search ?? this.search,
      );
}

/// Holds the full paginated employee list state.
class EmployeeListState {
  final List<EmployeeModel> employees;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final bool isLoadingMore;
  final bool fromCache;

  const EmployeeListState({
    this.employees = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.currentPage = 0,
    this.isLoadingMore = false,
    this.fromCache = false,
  });

  bool get hasMore => currentPage < totalPages - 1;

  EmployeeListState copyWith({
    List<EmployeeModel>? employees,
    int? totalElements,
    int? totalPages,
    int? currentPage,
    bool? isLoadingMore,
    bool? fromCache,
  }) =>
      EmployeeListState(
        employees: employees ?? this.employees,
        totalElements: totalElements ?? this.totalElements,
        totalPages: totalPages ?? this.totalPages,
        currentPage: currentPage ?? this.currentPage,
        isLoadingMore: isLoadingMore ?? this.isLoadingMore,
        fromCache: fromCache ?? this.fromCache,
      );
}

@riverpod
class EmployeeListNotifier extends _$EmployeeListNotifier {
  final _cacheService = CacheService();
  EmployeeListFilter _filter = const EmployeeListFilter();

  @override
  Future<EmployeeListState> build() async {
    // Step 1: Emit cached data immediately for instant UI
    final cached = _cacheService.getCachedEmployees();
    if (cached != null && cached.isNotEmpty) {
      // Schedule background refresh after emitting cache
      Future.microtask(_refresh);
      return EmployeeListState(
        employees:
            cached.map((e) => EmployeeModel.fromJson(e)).toList(),
        fromCache: true,
      );
    }
    // No cache — fetch from network
    return _fetchPage(0);
  }

  Future<EmployeeListState> _fetchPage(int page) async {
    final repo = ref.read(employeeRepositoryProvider);
    final data = await repo.getEmployees(
      page: page,
      size: 20,
      department: _filter.department,
      status: _filter.status,
      search: _filter.search,
    );

    final content = (data['content'] as List<dynamic>)
        .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
        .toList();

    // Cache first page result
    if (page == 0) {
      await _cacheService.cacheEmployees(
          content.map((e) => e.toJson()).toList());
    }

    return EmployeeListState(
      employees: content,
      totalElements: data['totalElements'] as int? ?? 0,
      totalPages: data['totalPages'] as int? ?? 1,
      currentPage: data['number'] as int? ?? 0,
    );
  }

  /// Background refresh — called after cache is shown
  Future<void> _refresh() async {
    try {
      final fresh = await _fetchPage(0);
      state = AsyncData(fresh);
    } catch (_) {
      // Silently fail — keep showing cache; offline banner shown by app.dart
    }
  }

  /// Pull-to-refresh from UI
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  /// Load next page (infinite scroll)
  Future<void> loadMore() async {
    final current = state.valueOrNull;
    if (current == null || !current.hasMore || current.isLoadingMore) return;

    state = AsyncData(current.copyWith(isLoadingMore: true));
    try {
      final repo = ref.read(employeeRepositoryProvider);
      final data = await repo.getEmployees(
        page: current.currentPage + 1,
        size: 20,
        department: _filter.department,
        status: _filter.status,
        search: _filter.search,
      );
      final more = (data['content'] as List<dynamic>)
          .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
          .toList();

      state = AsyncData(current.copyWith(
        employees: [...current.employees, ...more],
        currentPage: data['number'] as int? ?? current.currentPage + 1,
        totalPages: data['totalPages'] as int? ?? current.totalPages,
        isLoadingMore: false,
      ));
    } catch (_) {
      state = AsyncData(current.copyWith(isLoadingMore: false));
    }
  }

  /// Apply filters and refetch from page 0
  Future<void> applyFilter(EmployeeListFilter filter) async {
    _filter = filter;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  /// Remove a single employee from the list state (after delete)
  void removeEmployee(String id) {
    final current = state.valueOrNull;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      employees: current.employees.where((e) => e.id != id).toList(),
      totalElements: current.totalElements - 1,
    ));
  }

  /// Replace a single employee in the list (after activate/deactivate/update)
  void updateEmployee(EmployeeModel updated) {
    final current = state.valueOrNull;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      employees: current.employees
          .map((e) => e.id == updated.id ? updated : e)
          .toList(),
    ));
  }
}
```

### features/employee/domain/providers/employee_detail_provider.dart

```dart
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/employee_model.dart';
import '../../data/repositories/employee_repository.dart';

part 'employee_detail_provider.g.dart';

/// Fetches a single employee by ID. Invalidated after edit/status change.
@riverpod
Future<EmployeeModel> employeeDetail(EmployeeDetailRef ref, String id) async {
  final repo = ref.watch(employeeRepositoryProvider);
  return repo.getEmployeeById(id);
}
```

### features/employee/domain/providers/employee_search_provider.dart

```dart
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/employee_model.dart';
import '../../data/repositories/employee_repository.dart';

part 'employee_search_provider.g.dart';

/// Debounced search provider — pass the query string as family param.
/// Returns empty list for blank query (no network call).
@riverpod
Future<List<EmployeeModel>> employeeSearch(
    EmployeeSearchRef ref, String query) async {
  if (query.trim().isEmpty) return [];
  final repo = ref.watch(employeeRepositoryProvider);
  return repo.searchEmployees(query.trim());
}
```

### features/employee/domain/providers/employee_actions_provider.dart

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/create_employee_request.dart';
import '../../data/models/employee_model.dart';
import '../../data/models/update_employee_profile_request.dart';
import '../../data/models/update_employee_request.dart';
import '../../data/repositories/employee_repository.dart';
import 'employee_detail_provider.dart';
import 'employee_list_provider.dart';

part 'employee_actions_provider.g.dart';

/// Manages mutation operations (create, update, delete, activate, deactivate).
/// After each mutation, updates both the list and detail providers in-place
/// to avoid a full re-fetch where possible.
@riverpod
class EmployeeActions extends _$EmployeeActions {
  @override
  AsyncValue<void> build() => const AsyncData(null);

  Future<EmployeeModel?> createEmployee(CreateEmployeeRequest request) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.createEmployee(request);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: (e, s) => AsyncError(e, s),
      loading: () => const AsyncLoading(),
    );
    if (result is AsyncData) {
      // Refresh list to include the new employee
      ref.read(employeeListNotifierProvider.notifier).refresh();
      return result.value;
    }
    return null;
  }

  Future<EmployeeModel?> updateEmployee(
      String id, UpdateEmployeeRequest request) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.updateEmployee(id, request);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: (e, s) => AsyncError(e, s),
      loading: () => const AsyncLoading(),
    );
    if (result is AsyncData) {
      ref
          .read(employeeListNotifierProvider.notifier)
          .updateEmployee(result.value!);
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }

  Future<EmployeeModel?> updateOwnProfile(
      String id, UpdateEmployeeProfileRequest request) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.updateOwnProfile(id, request);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: (e, s) => AsyncError(e, s),
      loading: () => const AsyncLoading(),
    );
    if (result is AsyncData) {
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }

  Future<bool> deleteEmployee(String id) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      await repo.deleteEmployee(id);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: (e, s) => AsyncError(e, s),
      loading: () => const AsyncLoading(),
    );
    if (result is AsyncData) {
      ref.read(employeeListNotifierProvider.notifier).removeEmployee(id);
    }
    return result is AsyncData;
  }

  Future<EmployeeModel?> activateEmployee(String id) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.activateEmployee(id);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: (e, s) => AsyncError(e, s),
      loading: () => const AsyncLoading(),
    );
    if (result is AsyncData) {
      ref
          .read(employeeListNotifierProvider.notifier)
          .updateEmployee(result.value!);
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }

  Future<EmployeeModel?> deactivateEmployee(String id) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.deactivateEmployee(id);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: (e, s) => AsyncError(e, s),
      loading: () => const AsyncLoading(),
    );
    if (result is AsyncData) {
      ref
          .read(employeeListNotifierProvider.notifier)
          .updateEmployee(result.value!);
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }
}
```

### features/employee/domain/providers/profile_photo_provider.dart

```dart
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/repositories/employee_repository.dart';
import 'employee_detail_provider.dart';
import 'employee_list_provider.dart';

part 'profile_photo_provider.g.dart';

/// Handles profile photo upload.
/// Uses dart:io on native platforms, bytes on Web (kIsWeb).
/// image_picker is used to pick photos — it returns XFile which works cross-platform.
@riverpod
class ProfilePhotoNotifier extends _$ProfilePhotoNotifier {
  @override
  AsyncValue<String?> build() => const AsyncData(null);

  Future<void> pickAndUpload(String employeeId) async {
    final picker = ImagePicker();
    final picked = await picker.pickImage(
      source: ImageSource.gallery,
      maxWidth: 1024,
      maxHeight: 1024,
      imageQuality: 85,
    );
    if (picked == null) return; // User cancelled

    state = const AsyncLoading();

    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);

      if (kIsWeb) {
        // Web: read as bytes — dart:io is unavailable
        final bytes = await picked.readAsBytes();
        return repo.uploadPhotoBytes(employeeId, bytes, picked.name);
      } else {
        // Native: use File path
        // ignore: avoid_slow_async_io
        // dart:io is only compiled for native — safe here because kIsWeb is false
        final file = dart_io.File(picked.path);
        return repo.uploadPhoto(employeeId, file);
      }
    });

    state = result;

    if (result is AsyncData<String?>) {
      // Invalidate detail so photo refreshes
      ref.invalidate(employeeDetailProvider(employeeId));
      // Refresh list so avatar updates there too
      ref.read(employeeListNotifierProvider.notifier).refresh();
    }
  }
}

// Conditional dart:io import — only compiled on native platforms.
// On Web this import resolves to the stub and is never used (kIsWeb guard above).
import 'dart:io' as dart_io
    if (dart.library.html) 'package:taskhive_flutter/core/network/cookie_jar_factory_stub.dart';
```

> **Note**: The conditional `dart:io` import at the bottom of `profile_photo_provider.dart` uses a stub to satisfy Dart's static analysis on Web. The `kIsWeb` runtime check ensures the `dart:io` code path is never executed on Web. This follows Rule 1 from the Master Context.

---

## ★ Router Extension — app_router.dart

Only the `StatefulShellBranch` for `adminEmployees` changes. Replace the placeholder `GoRoute` for `/admin/employees` and add the three child routes. The employee shell branch (`/employee/profile`) also gets its real screen.

**Find and replace this block in `app_router.dart`:**

```dart
// BEFORE (Phase 1 placeholder — find this exact block)
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.adminEmployees,
    builder: (_, __) =>
        const _PlaceholderScreen(title: 'Employees'),
  ),
]),
```

**Replace with:**

```dart
// AFTER (Phase 2 — real employee screens)
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.adminEmployees,
    builder: (_, __) => const EmployeeListScreen(),
    routes: [
      GoRoute(
        path: 'create',
        builder: (_, __) => const CreateEmployeeScreen(),
      ),
      GoRoute(
        path: ':id',
        builder: (_, state) =>
            EmployeeDetailScreen(employeeId: state.pathParameters['id']!),
        routes: [
          GoRoute(
            path: 'edit',
            builder: (_, state) =>
                EditEmployeeScreen(employeeId: state.pathParameters['id']!),
          ),
        ],
      ),
    ],
  ),
]),
```

**Also replace the employee profile placeholder:**

```dart
// BEFORE (Phase 1 placeholder)
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.employeeProfile,
    builder: (_, __) =>
        const _PlaceholderScreen(title: 'My Profile'),
  ),
]),
```

**Replace with:**

```dart
// AFTER (Phase 2 — real profile screen)
StatefulShellBranch(routes: [
  GoRoute(
    path: AppRoutes.employeeProfile,
    builder: (_, __) => const MyProfileScreen(),
  ),
]),
```

**Add these imports at the top of `app_router.dart`:**

```dart
import '../../features/employee/presentation/screens/admin/employee_list_screen.dart';
import '../../features/employee/presentation/screens/admin/employee_detail_screen.dart';
import '../../features/employee/presentation/screens/admin/create_employee_screen.dart';
import '../../features/employee/presentation/screens/admin/edit_employee_screen.dart';
import '../../features/employee/presentation/screens/employee/my_profile_screen.dart';
```

---

## ★ Presentation Screens

### features/employee/presentation/screens/admin/employee_list_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_empty_state.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../../../core/widgets/app_confirm_dialog.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_list_provider.dart';
import '../../widgets/employee_filter_bar.dart';
import '../../widgets/employee_list_tile.dart';
import '../../widgets/employee_search_bar.dart';

class EmployeeListScreen extends ConsumerStatefulWidget {
  const EmployeeListScreen({super.key});

  @override
  ConsumerState<EmployeeListScreen> createState() =>
      _EmployeeListScreenState();
}

class _EmployeeListScreenState extends ConsumerState<EmployeeListScreen> {
  final _scrollController = ScrollController();
  EmployeeListFilter _filter = const EmployeeListFilter();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      ref.read(employeeListNotifierProvider.notifier).loadMore();
    }
  }

  @override
  Widget build(BuildContext context) {
    final listAsync = ref.watch(employeeListNotifierProvider);
    final actionsState = ref.watch(employeeActionsProvider);

    // Side effects — error/success snackbars
    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: const Text('Employees'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add_alt_1_outlined),
            tooltip: 'Add Employee',
            onPressed: () => context.go(AppRoutes.adminCreateEmployee),
          ),
        ],
      ),
      body: Column(
        children: [
          EmployeeSearchBar(
            onSearch: (q) {
              _filter = _filter.copyWith(search: q);
              ref
                  .read(employeeListNotifierProvider.notifier)
                  .applyFilter(_filter);
            },
          ),
          EmployeeFilterBar(
            filter: _filter,
            onFilterChanged: (f) {
              _filter = f;
              ref
                  .read(employeeListNotifierProvider.notifier)
                  .applyFilter(_filter);
            },
          ),
          Expanded(
            child: listAsync.when(
              loading: () => const AppLoading(),
              error: (e, _) => AppErrorWidget(
                message: e.toString(),
                onRetry: () => ref
                    .read(employeeListNotifierProvider.notifier)
                    .refresh(),
              ),
              data: (state) {
                if (state.employees.isEmpty) {
                  return AppEmptyState(
                    message: 'No employees found.',
                    icon: Icons.people_outline,
                    action: TextButton(
                      onPressed: () =>
                          context.go(AppRoutes.adminCreateEmployee),
                      child: const Text('Create first employee'),
                    ),
                  );
                }
                return RefreshIndicator(
                  onRefresh: () => ref
                      .read(employeeListNotifierProvider.notifier)
                      .refresh(),
                  child: ListView.builder(
                    controller: _scrollController,
                    itemCount: state.employees.length +
                        (state.isLoadingMore ? 1 : 0),
                    itemBuilder: (ctx, index) {
                      if (index == state.employees.length) {
                        return const Padding(
                          padding: EdgeInsets.all(16),
                          child: Center(
                              child: CircularProgressIndicator.adaptive()),
                        );
                      }
                      final employee = state.employees[index];
                      return EmployeeListTile(
                        employee: employee,
                        onTap: () => context.go(
                            AppRoutes.adminEmployeeDetailPath(employee.id)),
                        onDelete: () async {
                          final confirmed =
                              await AppConfirmDialog.show(context,
                            title: 'Delete Employee',
                            message:
                                'Delete ${employee.fullName}? This action cannot be undone.',
                            confirmLabel: 'Delete',
                          );
                          if (confirmed && context.mounted) {
                            await ref
                                .read(employeeActionsProvider.notifier)
                                .deleteEmployee(employee.id);
                            if (context.mounted) {
                              AppSnackbar.showSuccess(context,
                                  '${employee.fullName} has been deleted.');
                            }
                          }
                        },
                      );
                    },
                  ),
                );
              },
            ),
          ),
          if (actionsState is AsyncLoading)
            const LinearProgressIndicator(),
        ],
      ),
    );
  }
}
```

### features/employee/presentation/screens/admin/employee_detail_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_confirm_dialog.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_detail_provider.dart';
import '../../../domain/providers/profile_photo_provider.dart';
import '../../widgets/employee_status_badge.dart';
import '../../widgets/profile_photo_widget.dart';
import '../../widgets/status_history_timeline.dart';

class EmployeeDetailScreen extends ConsumerWidget {
  final String employeeId;
  const EmployeeDetailScreen({super.key, required this.employeeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(employeeDetailProvider(employeeId));
    final actionsState = ref.watch(employeeActionsProvider);
    final photoState = ref.watch(profilePhotoNotifierProvider);

    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: const Text('Employee Detail'),
        actions: [
          detailAsync.whenOrNull(
            data: (employee) => IconButton(
              icon: const Icon(Icons.edit_outlined),
              tooltip: 'Edit',
              onPressed: () =>
                  context.go(AppRoutes.adminEditEmployeePath(employeeId)),
            ),
          ) ??
              const SizedBox.shrink(),
        ],
      ),
      body: detailAsync.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(message: e.toString()),
        data: (employee) => SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Profile photo + name header
              Center(
                child: Column(
                  children: [
                    ProfilePhotoWidget(
                      photoUrl: employee.photoUrl,
                      initials: employee.initials,
                      size: 96,
                      onUpload: () => ref
                          .read(profilePhotoNotifierProvider.notifier)
                          .pickAndUpload(employeeId),
                      isUploading: photoState is AsyncLoading,
                    ),
                    const SizedBox(height: 12),
                    Text(employee.fullName,
                        style: Theme.of(context).textTheme.titleLarge),
                    const SizedBox(height: 4),
                    EmployeeStatusBadge(status: employee.status),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              // Info rows
              _InfoRow(label: 'Email', value: employee.email),
              _InfoRow(label: 'Phone', value: employee.phone ?? '—'),
              _InfoRow(label: 'Address', value: employee.address ?? '—'),
              _InfoRow(
                  label: 'Department', value: employee.department ?? '—'),
              _InfoRow(
                  label: 'Designation', value: employee.designation ?? '—'),
              _InfoRow(label: 'Manager', value: employee.managerName ?? '—'),
              _InfoRow(label: 'Join Date', value: employee.joinDate ?? '—'),
              const SizedBox(height: 24),
              // Action buttons
              if (!employee.isDeleted) ...[
                Row(
                  children: [
                    if (employee.canActivate)
                      Expanded(
                        child: FilledButton.icon(
                          icon: const Icon(Icons.check_circle_outline),
                          label: const Text('Activate'),
                          onPressed: () async {
                            final ok = await ref
                                .read(employeeActionsProvider.notifier)
                                .activateEmployee(employeeId);
                            if (ok != null && context.mounted) {
                              AppSnackbar.showSuccess(
                                  context, 'Employee activated.');
                            }
                          },
                        ),
                      ),
                    if (employee.canDeactivate)
                      Expanded(
                        child: OutlinedButton.icon(
                          icon: const Icon(Icons.block_outlined),
                          label: const Text('Deactivate'),
                          style: OutlinedButton.styleFrom(
                              foregroundColor:
                                  Theme.of(context).colorScheme.error),
                          onPressed: () async {
                            final confirmed =
                                await AppConfirmDialog.show(context,
                              title: 'Deactivate Employee',
                              message:
                                  'Deactivate ${employee.fullName}?',
                              confirmLabel: 'Deactivate',
                            );
                            if (confirmed && context.mounted) {
                              await ref
                                  .read(employeeActionsProvider.notifier)
                                  .deactivateEmployee(employeeId);
                              if (context.mounted) {
                                AppSnackbar.showSuccess(
                                    context, 'Employee deactivated.');
                              }
                            }
                          },
                        ),
                      ),
                  ].map((w) => Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4),
                        child: w,
                      )).toList(),
                ),
                const SizedBox(height: 8),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.delete_outline),
                    label: const Text('Delete Employee'),
                    style: OutlinedButton.styleFrom(
                        foregroundColor:
                            Theme.of(context).colorScheme.error),
                    onPressed: () async {
                      final confirmed = await AppConfirmDialog.show(context,
                        title: 'Delete Employee',
                        message:
                            'Delete ${employee.fullName}? Historical data will be preserved.',
                        confirmLabel: 'Delete',
                      );
                      if (confirmed && context.mounted) {
                        final ok = await ref
                            .read(employeeActionsProvider.notifier)
                            .deleteEmployee(employeeId);
                        if (ok && context.mounted) {
                          AppSnackbar.showSuccess(
                              context, 'Employee deleted.');
                          context.go(AppRoutes.adminEmployees);
                        }
                      }
                    },
                  ),
                ),
              ],
              const SizedBox(height: 24),
              // Status history
              Text('Status History',
                  style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              StatusHistoryTimeline(employeeId: employeeId),
              if (actionsState is AsyncLoading)
                const Padding(
                  padding: EdgeInsets.all(16),
                  child: Center(child: CircularProgressIndicator.adaptive()),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;
  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 110,
              child: Text(label,
                  style: Theme.of(context)
                      .textTheme
                      .bodyMedium
                      ?.copyWith(color: Theme.of(context).hintColor)),
            ),
            Expanded(child: Text(value)),
          ],
        ),
      );
}
```

### features/employee/presentation/screens/admin/create_employee_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../data/models/create_employee_request.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../widgets/employee_form.dart';

class CreateEmployeeScreen extends ConsumerWidget {
  const CreateEmployeeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('Add Employee')),
      body: EmployeeForm(
        onSubmit: (data) async {
          final created = await ref
              .read(employeeActionsProvider.notifier)
              .createEmployee(data);
          if (created != null && context.mounted) {
            AppSnackbar.showSuccess(context,
                '${created.fullName} created. Activation email sent.');
            context.go(AppRoutes.adminEmployees);
          }
        },
        isLoading: ref.watch(employeeActionsProvider) is AsyncLoading,
      ),
    );
  }
}
```

### features/employee/presentation/screens/admin/edit_employee_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_detail_provider.dart';
import '../../widgets/employee_form.dart';

class EditEmployeeScreen extends ConsumerWidget {
  final String employeeId;
  const EditEmployeeScreen({super.key, required this.employeeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(employeeDetailProvider(employeeId));

    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('Edit Employee')),
      body: detailAsync.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(message: e.toString()),
        data: (employee) => EmployeeForm(
          initialEmployee: employee,
          onSubmit: (data) async {
            // EmployeeForm returns CreateEmployeeRequest shape for both create + edit.
            // Map to UpdateEmployeeRequest here.
            final updated = await ref
                .read(employeeActionsProvider.notifier)
                .updateEmployee(
                  employeeId,
                  data.toUpdateRequest(),
                );
            if (updated != null && context.mounted) {
              AppSnackbar.showSuccess(context, 'Employee updated.');
              context.go(AppRoutes.adminEmployeeDetailPath(employeeId));
            }
          },
          isLoading: ref.watch(employeeActionsProvider) is AsyncLoading,
        ),
      ),
    );
  }
}
```

### features/employee/presentation/screens/employee/my_profile_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../core/widgets/app_button.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../../../core/widgets/app_text_field.dart';
import '../../../../../features/auth/domain/providers/auth_provider.dart';
import '../../../../../features/auth/domain/providers/current_user_provider.dart';
import '../../../data/models/update_employee_profile_request.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_detail_provider.dart';
import '../../../domain/providers/profile_photo_provider.dart';
import '../../widgets/employee_status_badge.dart';
import '../../widgets/profile_photo_widget.dart';

/// The employee's own profile screen. Allows updating phone + address only.
/// Profile photo upload is also available here.
class MyProfileScreen extends ConsumerStatefulWidget {
  const MyProfileScreen({super.key});

  @override
  ConsumerState<MyProfileScreen> createState() => _MyProfileScreenState();
}

class _MyProfileScreenState extends ConsumerState<MyProfileScreen> {
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _dirty = false;

  @override
  void dispose() {
    _phoneController.dispose();
    _addressController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final currentUser = ref.watch(currentUserProvider);
    if (currentUser == null) {
      return const Scaffold(body: Center(child: Text('Not logged in.')));
    }

    final profileAsync =
        ref.watch(employeeDetailProvider(currentUser.id));
    final actionsState = ref.watch(employeeActionsProvider);
    final photoState = ref.watch(profilePhotoNotifierProvider);

    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('My Profile')),
      body: profileAsync.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(message: e.toString()),
        data: (employee) {
          // Pre-fill on first load (not on every rebuild)
          if (!_dirty) {
            _phoneController.text = employee.phone ?? '';
            _addressController.text = employee.address ?? '';
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Center(
                    child: Column(
                      children: [
                        ProfilePhotoWidget(
                          photoUrl: employee.photoUrl,
                          initials: employee.initials,
                          size: 96,
                          onUpload: () => ref
                              .read(profilePhotoNotifierProvider.notifier)
                              .pickAndUpload(employee.id),
                          isUploading: photoState is AsyncLoading,
                        ),
                        const SizedBox(height: 12),
                        Text(employee.fullName,
                            style: Theme.of(context).textTheme.titleLarge),
                        const SizedBox(height: 4),
                        EmployeeStatusBadge(status: employee.status),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                  // Read-only fields
                  _ReadOnlyField(label: 'Email', value: employee.email),
                  _ReadOnlyField(
                      label: 'Department',
                      value: employee.department ?? '—'),
                  _ReadOnlyField(
                      label: 'Designation',
                      value: employee.designation ?? '—'),
                  _ReadOnlyField(
                      label: 'Join Date', value: employee.joinDate ?? '—'),
                  const Divider(height: 32),
                  Text('Update Contact Info',
                      style: Theme.of(context).textTheme.titleSmall),
                  const SizedBox(height: 12),
                  AppTextField(
                    label: 'Phone',
                    controller: _phoneController,
                    keyboardType: TextInputType.phone,
                    onChanged: (_) => setState(() => _dirty = true),
                  ),
                  const SizedBox(height: 12),
                  AppTextField(
                    label: 'Address',
                    controller: _addressController,
                    onChanged: (_) => setState(() => _dirty = true),
                  ),
                  const SizedBox(height: 24),
                  AppButton(
                    label: 'Save Changes',
                    isLoading: actionsState is AsyncLoading,
                    onPressed: () async {
                      if (!_formKey.currentState!.validate()) return;
                      final updated = await ref
                          .read(employeeActionsProvider.notifier)
                          .updateOwnProfile(
                            employee.id,
                            UpdateEmployeeProfileRequest(
                              phone: _phoneController.text.trim().isEmpty
                                  ? null
                                  : _phoneController.text.trim(),
                              address:
                                  _addressController.text.trim().isEmpty
                                      ? null
                                      : _addressController.text.trim(),
                            ),
                          );
                      if (updated != null && context.mounted) {
                        setState(() => _dirty = false);
                        AppSnackbar.showSuccess(
                            context, 'Profile updated successfully.');
                      }
                    },
                  ),
                  const SizedBox(height: 16),
                  // Change password shortcut
                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton(
                      onPressed: () =>
                          context.go('/change-password'),
                      child: const Text('Change Password'),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _ReadOnlyField extends StatelessWidget {
  final String label;
  final String value;
  const _ReadOnlyField({required this.label, required this.value});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(
          children: [
            SizedBox(
              width: 100,
              child: Text(label,
                  style: TextStyle(color: Theme.of(context).hintColor)),
            ),
            Expanded(child: Text(value)),
          ],
        ),
      );
}
```

---

## Widget Specs

### presentation/widgets/employee_list_tile.dart

`StatelessWidget`. Params: `EmployeeModel employee`, `VoidCallback onTap`, `VoidCallback? onDelete`.

Layout: `ListTile` with leading `CircleAvatar` (shows `CachedNetworkImage` if `employee.photoUrl != null`, else falls back to `Text(employee.initials)`). Title: `employee.fullName`. Subtitle: `Row` of `employee.department ?? ''` and `EmployeeStatusBadge`. Trailing: `PopupMenuButton` with items "View", "Delete" (Delete only shown if `onDelete != null`). `onTap` navigates to detail; delete triggers `onDelete`.

### presentation/widgets/employee_status_badge.dart

`StatelessWidget`. Param: `EmployeeStatus status`.

Uses `AppBadge` with color mapping:
- `active` → `Colors.green`
- `pending` → `Colors.orange`
- `inactive` → `Colors.grey`
- `deleted` → `Colors.red`

Label is `status.name.toUpperCase()`. Do not add `@riverpod` — pure display widget.

### presentation/widgets/employee_filter_bar.dart

`StatelessWidget`. Params: `EmployeeListFilter filter`, `ValueChanged<EmployeeListFilter> onFilterChanged`.

Horizontal scrollable `Row` of `FilterChip` widgets:
- **Status**: chips for All, Active, Pending, Inactive
- **Department**: chips for All, Engineering, HR, Finance, Marketing (or configurable)

When a chip is selected, calls `onFilterChanged` with updated filter. Selected chip uses `selectedColor: AppColors.primary.withOpacity(0.15)`.

### presentation/widgets/employee_form.dart

`StatefulWidget`. Params: `EmployeeModel? initialEmployee` (null for create), `Future<void> Function(CreateEmployeeRequest) onSubmit`, `bool isLoading`.

Fields: `firstName*`, `lastName*`, `email*` (disabled if editing), `phone`, `department`, `designation`, `joinDate` (date picker via `showDatePicker`), `managerId` (via `ManagerSelectorField`).

All required fields validate not-empty. Email validates format. `joinDate` shows `TextFormField` (readOnly) that opens `showDatePicker` on tap. Submit button calls `onSubmit` with assembled `CreateEmployeeRequest`.

Extension on `CreateEmployeeRequest`:
```dart
extension CreateToUpdate on CreateEmployeeRequest {
  UpdateEmployeeRequest toUpdateRequest() => UpdateEmployeeRequest(
    firstName: firstName,
    lastName: lastName,
    phone: phone,
    department: department,
    designation: designation,
    joinDate: joinDate,
    managerId: managerId,
  );
}
```

### presentation/widgets/employee_search_bar.dart

`StatefulWidget`. Param: `ValueChanged<String> onSearch`.

`TextField` with `prefixIcon: Icons.search`, `suffixIcon: Icons.clear` (shown when text non-empty), debounce of 400ms via `Timer` (import `dart:async`). On text change, debounce timer resets; on expire, calls `onSearch`. Clearing resets to empty string and calls `onSearch('')`.

### presentation/widgets/profile_photo_widget.dart

`StatelessWidget`. Params: `String? photoUrl`, `String initials`, `double size = 64`, `VoidCallback? onUpload`, `bool isUploading = false`.

Layout: `Stack` → `CircleAvatar(radius: size/2)` with `CachedNetworkImage` (if `photoUrl != null`) else `Text(initials)` with `AppColors.primary` background. If `onUpload != null`: bottom-right `InkWell` → small circular button `Icon(Icons.camera_alt, size: 16)`. If `isUploading`: overlay `CircularProgressIndicator.adaptive()` covers the avatar.

### presentation/widgets/status_history_timeline.dart

`ConsumerWidget`. Param: `String employeeId`.

Calls `employeeRepositoryProvider.getStatusHistory(employeeId)` inside `FutureBuilder` (or use a small inline `@riverpod` provider). For each `EmployeeStatusHistoryModel`, renders a `TimelineTile`-style row:
- Left: vertical line + dot
- Right: `Column` → `Text('${entry.oldStatus ?? 'Created'} → ${entry.newStatus}')`, sub-text `'by ${entry.changedBy}'`, timestamp via `AppDateUtils.formatDateTime`.

Wrap in a `SizedBox(height: 300)` + `ListView` (shrinkWrap: true, physics: NeverScrollableScrollPhysics inside `SingleChildScrollView` on parent screen).

### presentation/widgets/manager_selector_field.dart

`ConsumerStatefulWidget`. Params: `String? initialManagerId`, `ValueChanged<String?> onChanged`.

Shows a `TextFormField` (readOnly) with `suffixIcon: Icons.arrow_drop_down`. On tap: opens a modal bottom sheet with `EmployeeSearchBar` + `ListView` of active employees fetched via `employeeSearchProvider`. On selection: displays `employee.fullName` in the field, calls `onChanged(employee.id)`. Shows "None" option at top to clear manager.

---

## ⚠️ Platform Note: Profile Photo Upload on Web

`dart:io.File` is not available on Web. The `profile_photo_provider.dart` uses `kIsWeb` to branch:
- **Native** (Android/iOS/macOS/Windows/Linux): `repo.uploadPhoto(id, File(path))`
- **Web**: `repo.uploadPhotoBytes(id, bytes, filename)`

Both paths use `XFile` from `image_picker`. The conditional `dart:io` import at the bottom of `profile_photo_provider.dart` uses a stub to satisfy Dart's static analysis — the stub is never executed because of the `kIsWeb` guard. This is the same pattern as the cookie jar split in Phase 1.

---

## Offline Caching Behaviour (Phase 2)

| Action | Cache Behaviour |
|--------|----------------|
| Open employee list | Show Hive `employees_box` instantly → background fetch → update silently |
| Pull-to-refresh | Clear loading state → fetch → update Hive |
| Create employee | Refresh list (cache updated after) |
| Update/activate/deactivate | In-place list update — no cache invalidation needed |
| Delete employee | Remove from in-memory list state; cache re-synced on next list load |
| Offline + no cache | `AppErrorWidget` with retry |
| Offline + cache exists | Show cached list; offline banner shown by `app.dart` |

Employee detail is NOT cached individually — fetched fresh on each view (acceptable as detail is opened only on demand).

---

## Build & Run Verification Checklist

```bash
# Step 1 — Code generation (MUST do after adding models)
dart run build_runner build --delete-conflicting-outputs
# Expected: Build completed successfully!

# Step 2 — Static analysis
flutter analyze
# Expected: No issues found!

# Step 3 — Android (primary test)
flutter run -d emulator-5554
# Step 4 — Web (validate no dart:io compile errors)
flutter run -d chrome
```

### Manual Test Checklist

| Role | Test | Expected Result |
|------|------|----------------|
| ADMIN | Login → tap Employees tab | Employee list loads (cache or network) |
| ADMIN | Pull down on list | Refreshes from network |
| ADMIN | Tap search bar, type name | Results filtered in real-time |
| ADMIN | Tap status filter chip | List filters by selected status |
| ADMIN | Tap + button → fill form → submit | Employee created, snackbar shown, activation email sent by backend |
| ADMIN | Tap employee → Edit | Form pre-filled with existing data |
| ADMIN | Edit form → save | Employee updated, snackbar shown |
| ADMIN | Detail screen → Activate | Status badge changes to ACTIVE |
| ADMIN | Detail screen → Deactivate | Confirm dialog → status badge changes to INACTIVE |
| ADMIN | Detail screen → Delete | Confirm dialog → returns to list, employee removed |
| ADMIN | Tap camera icon on photo | Image picker opens; photo uploads; avatar updates |
| ADMIN | Status History section | Timeline shows status changes with actor names |
| EMPLOYEE | Login → tap Profile tab | Own profile displayed with read-only fields |
| EMPLOYEE | Edit phone/address → Save | Updates saved, success snackbar |
| EMPLOYEE | Tap camera icon on photo | Photo uploads successfully |
| EMPLOYEE | Tap Change Password | Navigates to change password screen |
| BOTH | Kill app → reopen | Auth session restored; employee data loads from cache |

---

## Phase 2 File Count

| Layer | Count |
|-------|-------|
| Employee Models | 5 |
| Employee Repository | 1 |
| Domain Providers | 5 |
| Admin Screens | 4 |
| Employee Screen | 1 |
| Widgets | 8 |
| Router (modified) | 1 (no new file — extends Phase 1) |
| **Total New Files** | **24** |

> Generated files (`.freezed.dart`, `.g.dart`) are not counted — they are auto-created by build_runner.
> No platform config files need changes — camera + photo library permissions were pre-added in Phase 1 (`AndroidManifest.xml` and `Info.plist` from Master Context).
