import 'package:freezed_annotation/freezed_annotation.dart';

part 'employee_performance_response.freezed.dart';
part 'employee_performance_response.g.dart';

@freezed
class EmployeePerformanceResponse with _$EmployeePerformanceResponse {
  const factory EmployeePerformanceResponse({
    String? employeeId,
    String? employeeName,
    int? tasksAssigned,
    int? tasksCompleted,
    double? onTimeRate,
    double? avgCompletionHours,
  }) = _EmployeePerformanceResponse;

  factory EmployeePerformanceResponse.fromJson(Map<String, dynamic> json) =>
      _$EmployeePerformanceResponseFromJson(json);
}
