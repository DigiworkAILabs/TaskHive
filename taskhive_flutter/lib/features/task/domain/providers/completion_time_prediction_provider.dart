import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/create_task_request.dart';
import '../../data/repositories/task_repository.dart';

part 'completion_time_prediction_provider.g.dart';

@riverpod
class CompletionTimePredictionNotifier extends _$CompletionTimePredictionNotifier {
  @override
  AsyncValue<TaskCompletionTimePrediction?> build() {
    return const AsyncData(null);
  }

  Future<void> predict(TaskCompletionTimeRequestDto request) async {
    state = const AsyncLoading();
    try {
      final repo = ref.read(taskRepositoryProvider);
      final result = await repo.predictCompletionTime(request);
      state = AsyncData(result);
    } catch (e, st) {
      state = AsyncError('Completion time estimate unavailable', st);
    }
  }

  void clear() {
    state = const AsyncData(null);
  }
}

