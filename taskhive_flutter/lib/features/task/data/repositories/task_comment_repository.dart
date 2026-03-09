import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../models/task_comment_model.dart';
import '../models/task_comment_request.dart';

final taskCommentRepositoryProvider = Provider<TaskCommentRepository>((ref) {
  return TaskCommentRepository(dio: ref.watch(dioClientProvider));
});

class TaskCommentRepository {
  final Dio _dio;
  TaskCommentRepository({required Dio dio}) : _dio = dio;

  /// Fetch comments for a task. Paginated (confirmed by Postman step 17).
  Future<List<TaskCommentModel>> getComments(
    String taskId, {
    int page = 0,
    int size = 50,
  }) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.taskComments(taskId),
        queryParameters: {'page': page, 'size': size},
      );
      // Postman shows paginated response with `.content`
      final data = response.data['data'];
      final list = (data is Map ? data['content'] : data) as List<dynamic>;
      return list
          .map((e) => TaskCommentModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Add a comment to a task. Returns the created comment.
  Future<TaskCommentModel> addComment(
    String taskId,
    TaskCommentRequest request,
  ) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.taskComments(taskId),
        data: request.toJson(),
      );
      return TaskCommentModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
