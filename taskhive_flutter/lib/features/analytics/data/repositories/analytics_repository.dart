import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/network/dio_client.dart';
import '../../../../core/network/api_endpoints.dart';
import '../models/admin_dashboard_response.dart';
import '../models/employee_dashboard_response.dart';
import '../models/task_distribution_response.dart';
import '../models/task_completion_trend_response.dart';
import '../models/employee_performance_response.dart';
import '../models/report_export_request.dart';

part 'analytics_repository.g.dart';

class AnalyticsRepository {
  final Dio _dio;

  AnalyticsRepository(this._dio);

  Future<AdminDashboardResponse> getAdminDashboard() async {
    final response = await _dio.get(ApiEndpoints.adminDashboard);
    return AdminDashboardResponse.fromJson(response.data['data']);
  }

  Future<EmployeeDashboardResponse> getEmployeeDashboard() async {
    final response = await _dio.get(ApiEndpoints.employeeDashboard);
    return EmployeeDashboardResponse.fromJson(response.data['data']);
  }

  Future<List<TaskDistributionResponse>> getTaskDistribution() async {
    final response = await _dio.get(ApiEndpoints.taskDistribution);
    final List list = response.data['data'];
    return list.map((e) => TaskDistributionResponse.fromJson(e)).toList();
  }

  Future<List<TaskDistributionResponse>> getTaskByPriority() async {
    final response = await _dio.get(ApiEndpoints.tasksByPriority);
    final List list = response.data['data'];
    return list.map((e) => TaskDistributionResponse.fromJson(e)).toList();
  }

  Future<List<TaskCompletionTrendResponse>> getCompletionTrend() async {
    final response = await _dio.get(ApiEndpoints.completionTrend);
    final List list = response.data['data'];
    return list.map((e) => TaskCompletionTrendResponse.fromJson(e)).toList();
  }

  Future<List<EmployeePerformanceResponse>> getEmployeePerformance() async {
    final response = await _dio.get(ApiEndpoints.employeePerformance);
    final List list = response.data['data'];
    return list.map((e) => EmployeePerformanceResponse.fromJson(e)).toList();
  }

  Future<Map<String, dynamic>> exportReport(ReportExportRequest request) async {
    final response = await _dio.post(
      ApiEndpoints.reportsExport,
      data: request.toJson(),
    );
    return response.data['data'] as Map<String, dynamic>;
  }

  Future<Response<List<int>>> downloadReport(String id) async {
    return _dio.get<List<int>>(
      ApiEndpoints.reportDownload(id),
      options: Options(responseType: ResponseType.bytes),
    );
  }
}

@riverpod
AnalyticsRepository analyticsRepository(AnalyticsRepositoryRef ref) {
  return AnalyticsRepository(ref.read(dioClientProvider));
}
