import 'package:freezed_annotation/freezed_annotation.dart';

import '../../domain/enums/task_priority.dart';

part 'update_task_request.freezed.dart';
part 'update_task_request.g.dart';

/// Partial update request for task fields (ADMIN only).
/// All fields are nullable — only provided fields are sent to backend.
/// build.yaml `include_if_null: false` handles omitting nulls automatically.
@freezed
class UpdateTaskRequest with _$UpdateTaskRequest {
  const factory UpdateTaskRequest({
    String? title,
    String? description,
    TaskPriority? priority,
    String? assignedTo,
    String? dueDate,
    double? estimatedHours,
    List<String>? tags,
    bool? proofRequired,
    bool? approvalRequired,
  }) = _UpdateTaskRequest;

  factory UpdateTaskRequest.fromJson(Map<String, dynamic> json) =>
      _$UpdateTaskRequestFromJson(json);
}
