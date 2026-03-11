import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../models/create_task_request.dart';
import '../models/task_model.dart';
import '../models/update_task_request.dart';
import '../models/update_task_status_request.dart';

final taskRepositoryProvider = Provider<TaskRepository>((ref) {
  return TaskRepository(dio: ref.watch(dioClientProvider));
});

class TaskRepository {
  final Dio _dio;
  TaskRepository({required Dio dio}) : _dio = dio;

  /// Fetch paginated task list with optional filters.
  /// Query params confirmed against Postman collection.
  Future<Map<String, dynamic>> getTasks({
    int page = 0,
    int size = 20,
    String? status,
    String? priority,
    String? assignedTo,
    List<String>? tags,
    String? search,
    String? sortBy,
    String? sortDir,
  }) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.tasks,
        queryParameters: {
          'page': page,
          'size': size,
          if (status != null && status.isNotEmpty) 'status': status,
          if (priority != null && priority.isNotEmpty) 'priority': priority,
          if (assignedTo != null && assignedTo.isNotEmpty)
            'assignedTo': assignedTo,
          if (tags != null && tags.isNotEmpty) 'tags': tags.join(','),
          if (search != null && search.isNotEmpty) 'search': search,
          if (sortBy != null && sortBy.isNotEmpty) 'sortBy': sortBy,
          if (sortDir != null && sortDir.isNotEmpty) 'sortDir': sortDir,
        },
      );
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Fetch single task by ID.
  Future<TaskModel> getTaskById(String id) async {
    try {
      final response = await _dio.get(ApiEndpoints.taskById(id));
      return TaskModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Create a new task. ADMIN only.
  Future<TaskModel> createTask(CreateTaskRequest request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.tasks,
        data: request.toJson(),
      );
      return TaskModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Update task fields. ADMIN only.
  Future<TaskModel> updateTask(String id, UpdateTaskRequest request) async {
    try {
      final json = request.toJson()
        ..removeWhere((_, v) => v == null); // omit nulls for partial update
      final response = await _dio.put(ApiEndpoints.taskById(id), data: json);
      return TaskModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Soft delete task. ADMIN only.
  Future<void> deleteTask(String id) async {
    try {
      await _dio.delete(ApiEndpoints.taskById(id));
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Update task status. ADMIN (any task) or EMPLOYEE (own tasks only).
  /// Request field: `status` (confirmed by Postman — not `newStatus`).
  Future<TaskModel> updateTaskStatus(
      String id, UpdateTaskStatusRequest request) async {
    try {
      final response = await _dio.patch(
        ApiEndpoints.taskStatus(id),
        data: request.toJson(),
      );
      return TaskModel.fromJson(response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Fetch employee's own assigned tasks (paginated).
  Future<Map<String, dynamic>> getMyTasks({
    int page = 0,
    int size = 20,
    String? status,
    String? sortBy,
    String? sortDir,
  }) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.myTasks,
        queryParameters: {
          'page': page,
          'size': size,
          if (status != null && status.isNotEmpty) 'status': status,
          if (sortBy != null && sortBy.isNotEmpty) 'sortBy': sortBy,
          if (sortDir != null && sortDir.isNotEmpty) 'sortDir': sortDir,
        },
      );
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Fetch overdue tasks (ADMIN only). Paginated — Postman uses page+size.
  Future<Map<String, dynamic>> getOverdueTasks({
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.overdueTasks,
        queryParameters: {'page': page, 'size': size},
      );
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Full-text search tasks by title/description.
  /// Uses `query` param (confirmed by Postman — not `q`).
  /// Returns paginated data map.
  Future<Map<String, dynamic>> searchTasks(
    String query, {
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.taskSearch,
        queryParameters: {'query': query, 'page': page, 'size': size},
      );
      return response.data['data'] as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Upload attachment from file path (native platforms only).
  Future<String> uploadAttachmentFile(String taskId, File file) async {
    try {
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromFile(
          file.path,
          filename: file.path.split('/').last,
        ),
      });
      final response = await _dio.post(
        ApiEndpoints.taskAttachments(taskId),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return response.data['data']['fileUrl'] as String;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Upload attachment from bytes (Web platform).
  Future<String> uploadAttachmentBytes(
      String taskId, List<int> bytes, String filename) async {
    try {
      final formData = FormData.fromMap({
        'file': MultipartFile.fromBytes(bytes, filename: filename),
      });
      final response = await _dio.post(
        ApiEndpoints.taskAttachments(taskId),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return response.data['data']['fileUrl'] as String;
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Fetch status change history for a task.
  Future<List<Map<String, dynamic>>> getHistory(String taskId) async {
    try {
      final response = await _dio.get(ApiEndpoints.taskHistory(taskId));
      final data = response.data['data'];
      final list = data is List ? data : (data as Map)['content'] as List;
      return list.map((e) => Map<String, dynamic>.from(e as Map)).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Predict task priority using ML backend.
  Future<TaskPriorityPrediction> predictTaskPriority(
      TaskPriorityRequestDto request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.mlPredictTaskPriority,
        data: request.toJson(),
      );
      return TaskPriorityPrediction.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Predict completion time (estimated hours) using ML backend.
  Future<TaskCompletionTimePrediction> predictCompletionTime(
      TaskCompletionTimeRequestDto request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.mlPredictCompletionTime,
        data: request.toJson(),
      );
      return TaskCompletionTimePrediction.fromJson(
        response.data['data'] as Map<String, dynamic>,
      );
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Recommend the best employee for a task based on current workload.
  Future<WorkloadRecommendationResponse> recommendWorkloadBalance(
      WorkloadRecommendationRequest request) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.mlRecommendWorkload,
        data: request.toJson(),
      );
      return WorkloadRecommendationResponse.fromJson(
        response.data['data'] as Map<String, dynamic>,
      );
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
