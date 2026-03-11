import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../../core/network/dio_client.dart';
import '../../../../core/network/api_endpoints.dart';
import '../models/productivity_score_response.dart';

part 'ml_repository.g.dart';

class MLRepository {
  final Dio _dio;
  MLRepository(this._dio);

  Future<ProductivityScoreResponse> getProductivityScore(String employeeId) async {
    final response = await _dio.get(ApiEndpoints.mlProductivityScore(employeeId));
    return ProductivityScoreResponse.fromJson(response.data['data']);
  }
}

@riverpod
MLRepository mlRepository(MlRepositoryRef ref) {
  return MLRepository(ref.read(dioClientProvider));
}
