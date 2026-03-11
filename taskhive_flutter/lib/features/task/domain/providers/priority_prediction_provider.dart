import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/create_task_request.dart';
import '../../data/repositories/task_repository.dart';

part 'priority_prediction_provider.g.dart';

@riverpod
class PriorityPrediction extends _$PriorityPrediction {
  @override
  AsyncValue<TaskPriorityPrediction?> build() {
    return const AsyncData(null);
  }

  Future<void> predict(TaskPriorityRequestDto request) async {
    if (request.taskTitle.trim().length < 2) return;

    state = const AsyncLoading();

    try {
      final repo = ref.read(taskRepositoryProvider);
      final prediction = await repo.predictTaskPriority(request);
      state = AsyncData(prediction);
    } catch (e, st) {
      // Keep it as a non-breaking error that UI can optionally display
      state = AsyncError('Priority suggestion unavailable', st);
    }
  }

  void reset() {
    state = const AsyncData(null);
  }
}
