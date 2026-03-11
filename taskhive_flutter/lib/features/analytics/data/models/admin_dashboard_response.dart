import 'package:freezed_annotation/freezed_annotation.dart';

part 'admin_dashboard_response.freezed.dart';
part 'admin_dashboard_response.g.dart';

@freezed
class AdminDashboardResponse with _$AdminDashboardResponse {
  const factory AdminDashboardResponse({
    int? totalTasks,
    int? activeTasks,
    int? overdueTasks,
    int? completedTasks,
    double? completionRate,
    int? totalEmployees,
    double? avgCompletionHours,
    String? metricDate,
  }) = _AdminDashboardResponse;

  factory AdminDashboardResponse.fromJson(Map<String, dynamic> json) =>
      _$AdminDashboardResponseFromJson(json);
}
