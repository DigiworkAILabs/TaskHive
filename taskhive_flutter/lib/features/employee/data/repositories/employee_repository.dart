// dart:io is only available on native platforms.
// On Web, the conditional import resolves to the stub which provides a
// structural placeholder for File — the Web code path never calls uploadPhoto().
// This follows Master Context Rule 1: never top-level import dart:io in a file
// that is also compiled for Web.
import 'dart:io' as dart_io
    if (dart.library.html) 'package:taskhive_flutter/core/utils/dart_io_stub.dart';

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

  // ─────────────────────────────────────────────────────────────
  // List & Search
  // ─────────────────────────────────────────────────────────────

  /// Paginated employee list with optional filters.
  ///
  /// Postman test 4 confirms `sortBy` and `sortDir` are valid query params —
  /// these are absent from the Phase 2 spec repo code but added here for
  /// completeness (Conflict 1 resolution).
  Future<Map<String, dynamic>> getEmployees({
    int page = 0,
    int size = 20,
    String? department,
    String? status,
    String? search,
    String? sortBy,
    String? sortDir,
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
          if (search != null && search.isNotEmpty) 'name': search,
          if (sortBy != null && sortBy.isNotEmpty) 'sortBy': sortBy,
          if (sortDir != null && sortDir.isNotEmpty) 'sortDir': sortDir,
        },
      );
      // Returns the raw `data` map; provider parses content + pagination fields.
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Search employees by name, email, department, or designation.
  ///
  /// Query param is `query` (Postman test 5 `query=John`).
  /// The Phase 2 spec uses `q` — this is a spec error (Conflict 2 resolution).
  /// Returns a flat list parsed from the paginated `data.content` array.
  Future<List<EmployeeModel>> searchEmployees(String query) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.employeeSearch,
        queryParameters: {'query': query},
      );
      final list = response.data['data']['content'] as List<dynamic>;
      return list
          .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  // ─────────────────────────────────────────────────────────────
  // Single Employee
  // ─────────────────────────────────────────────────────────────

  /// Fetch a single employee by UUID.
  Future<EmployeeModel> getEmployeeById(String id) async {
    try {
      final response = await _dio.get(ApiEndpoints.employeeById(id));
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  // ─────────────────────────────────────────────────────────────
  // CRUD — Admin only
  // ─────────────────────────────────────────────────────────────

  /// Create a new employee. Backend auto-sends an activation email (FR-EMP-02).
  /// Returns the created employee in PENDING status (confirmed Postman test 1).
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

  /// Update any employee profile field. ADMIN only (FR-EMP-05).
  /// Sends only non-null fields (build.yaml include_if_null: false).
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

  /// Employee updates their own phone and address. EMPLOYEE role only (FR-EMP-06).
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

  /// Soft-delete an employee. ADMIN only.
  /// Backend preserves the record; the employee can no longer login (FR-EMP-07/08).
  Future<void> deleteEmployee(String id) async {
    try {
      await _dio.delete(ApiEndpoints.employeeById(id));
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  // ─────────────────────────────────────────────────────────────
  // Status Transitions — Admin only
  // ─────────────────────────────────────────────────────────────

  /// Activate an employee account. ADMIN only (FR-EMP-09).
  /// Backend returns the updated employee with status == "ACTIVE"
  /// (confirmed Postman test 11).
  Future<EmployeeModel> activateEmployee(String id) async {
    try {
      final response = await _dio.patch(ApiEndpoints.employeeActivate(id));
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Deactivate an employee account. ADMIN only (FR-EMP-09).
  /// Backend returns the updated employee with status == "INACTIVE"
  /// (confirmed Postman test 12).
  Future<EmployeeModel> deactivateEmployee(String id) async {
    try {
      final response = await _dio.patch(ApiEndpoints.employeeDeactivate(id));
      return EmployeeModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  // ─────────────────────────────────────────────────────────────
  // Photo Upload — All roles
  // ─────────────────────────────────────────────────────────────

  /// Upload profile photo from a native dart:io File.
  ///
  /// Only called on native platforms — guarded by `kIsWeb` in
  /// [ProfilePhotoNotifier.pickAndUpload]. On Web, [uploadPhotoBytes] is used.
  ///
  /// FormData key: `file` (Postman test 9 — Conflict 4 resolution).
  /// Response: `data` is a String URL (Postman test 9 asserts data.includes('uploads')
  ///            — Conflict 5 resolution; NOT a { photoUrl: ... } object).
  Future<String> uploadPhoto(String id, dart_io.File file) async {
    try {
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(
          file.path,
          filename: file.path.split('/').last,
        ),
      });
      final response = await _dio.post(
        ApiEndpoints.employeePhoto(id),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return response.data['data'] as String;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Upload profile photo from raw bytes. Web-safe: no dart:io dependency.
  ///
  /// FormData key: `file` (Postman test 9 — Conflict 4 resolution).
  Future<String> uploadPhotoBytes(
      String id, List<int> bytes, String filename) async {
    try {
      final formData = FormData.fromMap({
        'file': MultipartFile.fromBytes(bytes, filename: filename),
      });
      final response = await _dio.post(
        ApiEndpoints.employeePhoto(id),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return response.data['data'] as String;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  // ─────────────────────────────────────────────────────────────
  // Status History
  // ─────────────────────────────────────────────────────────────

  /// Fetch status change history for an employee (FR-EMP-14).
  ///
  /// Note: The specific /employees/{id}/history endpoint is missing from the backend.
  /// We fallback to the Audit logs entity timeline which tracks "STATUS_CHANGE" actions.
  Future<List<EmployeeStatusHistoryModel>> getStatusHistory(
      String employeeId) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.auditLogsByEntity('EMPLOYEE', employeeId),
      );

      // Backend returns PageResponse<AuditLogResponse> in response.data['data']
      final list = (response.data['data']['content'] as List<dynamic>)
          .where((e) => e['action'] == 'STATUS_CHANGE' || e['action'] == 'CREATE')
          .toList();

      return list.map((e) {
        final Map<String, dynamic> audit = e as Map<String, dynamic>;
        return EmployeeStatusHistoryModel(
          id: audit['id'] as String,
          employeeId: employeeId,
          oldStatus: audit['beforeState'],
          newStatus: audit['afterState'] ?? 'UNKNOWN',
          changedBy: audit['actorEmail'] as String,
          changedAt: audit['createdAt'] as String,
          reason: 'Status changed via system audit',
        );
      }).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
