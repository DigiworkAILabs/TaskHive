import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../models/user_model.dart';

final Provider<AuthRepository> authRepositoryProvider =
    Provider<AuthRepository>((ref) {
  return AuthRepository(dio: ref.watch(dioClientProvider));
});

class AuthRepository {
  final Dio _dio;
  AuthRepository({required Dio dio}) : _dio = dio;

  Future<UserModel> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.login,
        data: {'email': email, 'password': password},
      );
      final responseData = response.data['data'] as Map<String, dynamic>;

      // The Spring Boot backend returns {"user": {...}} inside data
      if (responseData.containsKey('user')) {
        return UserModel.fromJson(responseData['user'] as Map<String, dynamic>);
      }
      return UserModel.fromJson(responseData);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> logout() async {
    try {
      await _dio.post(ApiEndpoints.logout);
    } on DioException catch (e) {
      // 401/403 means already logged out — not an error
      if (e.response?.statusCode != 401 && e.response?.statusCode != 403) {
        throw ApiException.fromDioError(e);
      }
    }
  }

  Future<UserModel> getMe() async {
    try {
      final response = await _dio.get(ApiEndpoints.me);
      final responseData = response.data['data'] as Map<String, dynamic>;

      if (responseData.containsKey('user')) {
        return UserModel.fromJson(responseData['user'] as Map<String, dynamic>);
      }
      return UserModel.fromJson(responseData);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> forgotPassword(String email) async {
    try {
      await _dio.post(ApiEndpoints.forgotPassword, data: {'email': email});
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> resetPassword({
    required String token,
    required String newPassword,
  }) async {
    try {
      await _dio.post(ApiEndpoints.resetPassword,
          data: {'token': token, 'newPassword': newPassword});
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> activateAccount({
    required String token,
    required String password,
  }) async {
    try {
      await _dio.post(ApiEndpoints.activateAccount,
          data: {'token': token, 'password': password});
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    try {
      await _dio.post(ApiEndpoints.changePassword, data: {
        'currentPassword': currentPassword,
        'newPassword': newPassword,
      });
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
