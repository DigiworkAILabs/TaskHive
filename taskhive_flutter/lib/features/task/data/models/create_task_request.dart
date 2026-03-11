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

@freezed
class TaskPriorityRequestDto with _$TaskPriorityRequestDto {
  const factory TaskPriorityRequestDto({
    required String taskTitle,
    String? taskDescription,
    String? employeeId,
    List<String>? tags,
    double? estimatedHours,
  }) = _TaskPriorityRequestDto;

  factory TaskPriorityRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskPriorityRequestDtoFromJson(json);
}

@freezed
class TaskPriorityPrediction with _$TaskPriorityPrediction {
  const factory TaskPriorityPrediction({
    required String predictedPriority,
    required double confidence,
    required String reasoning,
    required bool fallbackUsed,
  }) = _TaskPriorityPrediction;

  factory TaskPriorityPrediction.fromJson(Map<String, dynamic> json) =>
      _$TaskPriorityPredictionFromJson(json);
}

@freezed
class TaskCompletionTimeRequestDto with _$TaskCompletionTimeRequestDto {
  const factory TaskCompletionTimeRequestDto({
    required String taskTitle,
    String? taskDescription,
    required String priority,
    required String employeeId,
    double? estimatedHours,
  }) = _TaskCompletionTimeRequestDto;

  factory TaskCompletionTimeRequestDto.fromJson(Map<String, dynamic> json) =>
      _$TaskCompletionTimeRequestDtoFromJson(json);
}

@freezed
class ConfidenceRange with _$ConfidenceRange {
  const factory ConfidenceRange({
    required double low,
    required double high,
  }) = _ConfidenceRange;

  factory ConfidenceRange.fromJson(Map<String, dynamic> json) =>
      _$ConfidenceRangeFromJson(json);
}

@freezed
class TaskCompletionTimePrediction with _$TaskCompletionTimePrediction {
  const factory TaskCompletionTimePrediction({
    required double estimatedHours,
    required ConfidenceRange confidenceRange,
    required String reasoning,
    required bool fallbackUsed,
  }) = _TaskCompletionTimePrediction;

  factory TaskCompletionTimePrediction.fromJson(Map<String, dynamic> json) =>
      _$TaskCompletionTimePredictionFromJson(json);
}
