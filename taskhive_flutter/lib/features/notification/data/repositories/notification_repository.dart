import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/dio_client.dart';
import '../models/notification_preference_model.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/utils/logger.dart';

final notificationRepositoryProvider = Provider<NotificationRepository>((ref) {
  final dio = ref.watch(dioClientProvider);
  return NotificationRepository(dio);
});

class NotificationRepository {
  final Dio _dio;

  NotificationRepository(this._dio);

  Future<Map<String, dynamic>> getNotifications(
      {int page = 0, int size = 10}) async {
    try {
      final response = await _dio.get(
        ApiEndpoints.notifications,
        queryParameters: {'page': page, 'size': size},
      );
      if (response.data['success'] == true) {
        return response.data['data'];
      }
      throw ApiException(
          message: response.data['message'] ?? 'Failed to fetch notifications');
    } catch (e) {
      appLogger.e('NotificationRepository.getNotifications: $e');
      rethrow;
    }
  }

  Future<int> getUnreadCount() async {
    try {
      final response = await _dio.get(ApiEndpoints.notificationsUnreadCount);
      appLogger.d(
          'NotificationRepository.getUnreadCount response: ${response.data}');
      if (response.data['success'] == true) {
        return (response.data['data'] as num).toInt();
      }
      throw ApiException(
          message: response.data['message'] ?? 'Failed to fetch unread count');
    } catch (e) {
      appLogger.e('NotificationRepository.getUnreadCount error: $e');
      rethrow;
    }
  }

  Future<void> markAsRead(String id) async {
    try {
      final response = await _dio.patch(ApiEndpoints.notificationMarkRead(id));
      if (response.data['success'] != true) {
        throw ApiException(
            message: response.data['message'] ??
                'Failed to mark notification as read');
      }
    } catch (e) {
      appLogger.e('NotificationRepository.markAsRead: $e');
      rethrow;
    }
  }

  Future<void> markAllRead() async {
    try {
      final response = await _dio.patch(ApiEndpoints.notificationsMarkAllRead);
      if (response.data['success'] != true) {
        throw ApiException(
            message: response.data['message'] ??
                'Failed to mark all notifications as read');
      }
    } catch (e) {
      appLogger.e('NotificationRepository.markAllRead: $e');
      rethrow;
    }
  }

  Future<NotificationPreferenceModel> getPreferences() async {
    try {
      final response = await _dio.get(ApiEndpoints.notificationPreferences);
      if (response.data['success'] == true) {
        return NotificationPreferenceModel.fromJson(response.data['data']);
      }
      throw ApiException(
          message: response.data['message'] ?? 'Failed to fetch preferences');
    } catch (e) {
      appLogger.e('NotificationRepository.getPreferences: $e');
      rethrow;
    }
  }

  Future<NotificationPreferenceModel> updatePreferences(
      NotificationPreferenceModel prefs) async {
    try {
      final response = await _dio.put(
        ApiEndpoints.notificationPreferences,
        data: prefs.toJson(),
      );
      if (response.data['success'] == true) {
        return NotificationPreferenceModel.fromJson(response.data['data']);
      }
      throw ApiException(
          message: response.data['message'] ?? 'Failed to update preferences');
    } catch (e) {
      appLogger.e('NotificationRepository.updatePreferences: $e');
      rethrow;
    }
  }
}
