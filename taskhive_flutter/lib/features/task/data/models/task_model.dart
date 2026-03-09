import 'package:freezed_annotation/freezed_annotation.dart';

import '../../domain/enums/task_priority.dart';
import '../../domain/enums/task_status.dart';

part 'task_model.freezed.dart';
part 'task_model.g.dart';

@freezed
class TaskModel with _$TaskModel {
  const factory TaskModel({
    required String id,
    required String title,
    String? description,
    required TaskStatus status,
    required TaskPriority priority,
    String? assignedTo,
    String? assigneeName,
    String? dueDate,
    String? completedAt,
    double? estimatedHours,
    @Default([]) List<String> tags,
    @Default(false) bool isOverdue,
    String? createdBy,
    String? createdAt,
    String? updatedAt,
  }) = _TaskModel;

  factory TaskModel.fromJson(Map<String, dynamic> json) =>
      _$TaskModelFromJson(json);
}

extension TaskModelX on TaskModel {
  bool get isTerminal => status.isTerminal;

  List<TaskStatus> get allowedNextStatuses => status.allowedTransitions;

  bool get isDueToday {
    if (dueDate == null) return false;
    try {
      final due = DateTime.parse(dueDate!);
      final now = DateTime.now();
      return due.year == now.year &&
          due.month == now.month &&
          due.day == now.day;
    } catch (_) {
      return false;
    }
  }
}
