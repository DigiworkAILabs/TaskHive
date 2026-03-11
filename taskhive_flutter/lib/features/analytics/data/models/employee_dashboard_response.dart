import 'package:freezed_annotation/freezed_annotation.dart';

part 'employee_dashboard_response.freezed.dart';
part 'employee_dashboard_response.g.dart';

@freezed
class EmployeeDashboardResponse with _$EmployeeDashboardResponse {
  const factory EmployeeDashboardResponse({
    int? totalTasks,
    int? todoTasks,
    int? inProgressTasks,
    int? inReviewTasks,
    int? completedTasks,
    double? onTimeCompletionRate,
    double? avgCompletionHours,
  }) = _EmployeeDashboardResponse;

  factory EmployeeDashboardResponse.fromJson(Map<String, dynamic> json) =>
      _$EmployeeDashboardResponseFromJson(json);
}
