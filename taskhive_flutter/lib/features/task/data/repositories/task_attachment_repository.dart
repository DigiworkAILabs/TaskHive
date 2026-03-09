import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../models/task_attachment_model.dart';

final taskAttachmentRepositoryProvider =
    Provider<TaskAttachmentRepository>((ref) {
  return TaskAttachmentRepository(dio: ref.watch(dioClientProvider));
});

class TaskAttachmentRepository {
  final Dio _dio;
  TaskAttachmentRepository({required Dio dio}) : _dio = dio;

  /// Fetch all attachments for a task.
  Future<List<TaskAttachmentModel>> getAttachments(String taskId) async {
    try {
      final response = await _dio.get(ApiEndpoints.taskAttachments(taskId));
      final data = response.data['data'];
      final list = (data is List ? data : (data as Map)['content']) as List;
      return list
          .map((e) => TaskAttachmentModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  /// Upload attachment from an XFile. Handles Web vs native automatically.
  /// Uses multipart field `"file"` (confirmed by Postman step 18).
  Future<TaskAttachmentModel> uploadAttachment(
    String taskId,
    XFile file,
  ) async {
    try {
      MultipartFile multipartFile;
      if (kIsWeb) {
        final bytes = await file.readAsBytes();
        multipartFile = MultipartFile.fromBytes(
          bytes,
          filename: file.name,
        );
      } else {
        multipartFile = await MultipartFile.fromFile(
          file.path,
          filename: file.name,
        );
      }

      final formData = FormData.fromMap({'file': multipartFile});
      final response = await _dio.post(
        ApiEndpoints.taskAttachments(taskId),
        data: formData,
        options: Options(contentType: 'multipart/form-data'),
      );
      return TaskAttachmentModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
