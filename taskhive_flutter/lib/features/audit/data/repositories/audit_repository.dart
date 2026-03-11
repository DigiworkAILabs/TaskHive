import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/network/dio_client.dart';
import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/logger.dart';

part 'audit_repository.g.dart';

class AuditSearchFilter {
  final String? fromDate;
  final String? toDate;
  final String? action;
  final String? entityType;
  final String? actorEmail;
  final String? ipAddress;
  final int page;
  final int size;

  AuditSearchFilter({
    this.fromDate,
    this.toDate,
    this.action,
    this.entityType,
    this.actorEmail,
    this.ipAddress,
    this.page = 0,
    this.size = 20,
  });

  bool get isEmpty =>
      fromDate == null &&
      toDate == null &&
      action == null &&
      entityType == null &&
      actorEmail == null &&
      ipAddress == null;

  Map<String, dynamic> toJson() {
    return {
      if (fromDate != null) 'startDate': '${fromDate}T00:00:00',
      if (toDate != null) 'endDate': '${toDate}T23:59:59',
      if (action != null) 'action': action,
      if (entityType != null) 'entityType': entityType,
      if (actorEmail != null) 'actorEmail': actorEmail,
      if (ipAddress != null) 'ipAddress': ipAddress,
      'page': page,
      'size': size,
    };
  }
}

@riverpod
AuditRepository auditRepository(AuditRepositoryRef ref) {
  final dio = ref.watch(dioClientProvider);
  return AuditRepository(dio);
}

class AuditRepository {
  final Dio _dio;
  AuditRepository(this._dio);

  Future<Map<String, dynamic>> getAuditLogs(
      {int page = 0, int size = 20}) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.auditLogs,
        queryParameters: {'page': page, 'size': size},
      );
      if (response.data['success'] == true) {
        return response.data['data'];
      }
      throw ApiException(
          message: response.data['message'] ?? 'Failed to fetch audit logs');
    } catch (e) {
      appLogger.e('AuditRepository.getAuditLogs: $e');
      rethrow;
    }
  }

  Future<Map<String, dynamic>> searchAuditLogs(AuditSearchFilter filter) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.auditLogsSearch,
        queryParameters: filter.toJson(),
      );
      if (response.data['success'] == true) {
        return response.data['data'];
      }
      throw ApiException(
          message: response.data['message'] ?? 'Failed to search audit logs');
    } catch (e) {
      appLogger.e('AuditRepository.searchAuditLogs: $e');
      rethrow;
    }
  }

  Future<Map<String, dynamic>> getEntityTimeline(
      String entityType, String entityId,
      {int page = 0, int size = 100}) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.auditLogsByEntity(entityType, entityId),
        queryParameters: {'page': page, 'size': size},
      );
      if (response.data['success'] == true) {
        return response.data['data'];
      }
      throw ApiException(
          message:
              response.data['message'] ?? 'Failed to fetch entity timeline');
    } catch (e) {
      appLogger.e('AuditRepository.getEntityTimeline: $e');
      rethrow;
    }
  }

  Future<Map<String, dynamic>> getSecurityEvents(
      {int page = 0, int size = 20}) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.securityEvents,
        queryParameters: {'page': page, 'size': size},
      );
      if (response.data['success'] == true) {
        return response.data['data'];
      }
      throw ApiException(
          message:
              response.data['message'] ?? 'Failed to fetch security events');
    } catch (e) {
      appLogger.e('AuditRepository.getSecurityEvents: $e');
      rethrow;
    }
  }

  Future<List<int>> downloadComplianceReport() async {
    try {
      final response = await _dio.get(
        ApiEndpoints.complianceReport,
        queryParameters: {'reportType': 'USER_ACCESS', 'format': 'csv'},
        options: Options(responseType: ResponseType.bytes),
      );
      return (response.data as List).cast<int>();
    } catch (e) {
      appLogger.e('AuditRepository.downloadComplianceReport: $e');
      rethrow;
    }
  }
}
