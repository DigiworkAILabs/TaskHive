import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_status_history_model.freezed.dart';
part 'task_status_history_model.g.dart';

/// Represents a single entry in the task status change history.
/// Note: field name is `changedByName` (not `changedBy`) — confirmed by Postman.
@freezed
class TaskStatusHistoryModel with _$TaskStatusHistoryModel {
  const factory TaskStatusHistoryModel({
    required String id,
    required String taskId,
    String? oldStatus,
    required String newStatus,
    required String changedByName,
    String? comment,
    required String changedAt,
  }) = _TaskStatusHistoryModel;

  factory TaskStatusHistoryModel.fromJson(Map<String, dynamic> json) =>
      _$TaskStatusHistoryModelFromJson(json);
}
