import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/models/productivity_score_response.dart';
import '../../data/repositories/ml_repository.dart';

part 'productivity_score_provider.g.dart';

@riverpod
Future<ProductivityScoreResponse> productivityScore(
    ProductivityScoreRef ref, String employeeId) async {
  final repo = ref.watch(mlRepositoryProvider);
  return repo.getProductivityScore(employeeId);
}
