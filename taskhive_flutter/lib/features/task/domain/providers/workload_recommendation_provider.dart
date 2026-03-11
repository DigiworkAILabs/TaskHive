import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/create_task_request.dart';
import '../../data/repositories/task_repository.dart';

part 'workload_recommendation_provider.g.dart';

@riverpod
class WorkloadRecommendationNotifier extends _$WorkloadRecommendationNotifier {
  @override
  AsyncValue<WorkloadRecommendationResponse?> build() {
    return const AsyncData(null);
  }

  Future<void> recommend(WorkloadRecommendationRequest request) async {
    state = const AsyncLoading();
    try {
      final repo = ref.read(taskRepositoryProvider);
      final result = await repo.recommendWorkloadBalance(request);
      state = AsyncData(result);
    } catch (e, st) {
      state = AsyncError('Workload recommendation unavailable', st);
    }
  }

  void reset() {
    state = const AsyncData(null);
  }
}
