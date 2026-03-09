import 'package:freezed_annotation/freezed_annotation.dart';

import '../../domain/enums/task_priority.dart';

part 'create_task_request.freezed.dart';
part 'create_task_request.g.dart';

@freezed
class CreateTaskRequest with _$CreateTaskRequest {
  const factory CreateTaskRequest({
    required String title,
    String? description,
    required TaskPriority priority,
    required String assignedTo,
    required String dueDate,
    double? estimatedHours,
    @Default([]) List<String> tags,
  }) = _CreateTaskRequest;

  factory CreateTaskRequest.fromJson(Map<String, dynamic> json) =>
      _$CreateTaskRequestFromJson(json);
}
