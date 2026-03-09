import 'package:freezed_annotation/freezed_annotation.dart';

import '../../domain/enums/task_status.dart';

part 'update_task_status_request.freezed.dart';
part 'update_task_status_request.g.dart';

/// Request body for PATCH /tasks/{id}/status
/// Note: field name is `status` (not `newStatus`) — confirmed by Postman collection.
@freezed
class UpdateTaskStatusRequest with _$UpdateTaskStatusRequest {
  const factory UpdateTaskStatusRequest({
    required TaskStatus status,
    String? comment,
  }) = _UpdateTaskStatusRequest;

  factory UpdateTaskStatusRequest.fromJson(Map<String, dynamic> json) =>
      _$UpdateTaskStatusRequestFromJson(json);
}
