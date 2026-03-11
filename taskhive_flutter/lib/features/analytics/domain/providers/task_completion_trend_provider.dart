import 'dart:convert';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/analytics_repository.dart';
import '../../data/models/task_completion_trend_response.dart';
import '../../../../core/storage/cache_service.dart';

part 'task_completion_trend_provider.g.dart';

@riverpod
class TaskCompletionTrendNotifier extends _$TaskCompletionTrendNotifier {
  @override
  FutureOr<List<TaskCompletionTrendResponse>> build() async {
    final cache = CacheService.analyticsBox.get('task_completion_trend');
    if (cache != null) {
      try {
        final list = (jsonDecode(cache) as List)
            .map((e) => TaskCompletionTrendResponse.fromJson(e))
            .toList();
        state = AsyncData(list);
      } catch (_) {}
    }
    return _fetch();
  }
  
  Future<List<TaskCompletionTrendResponse>> _fetch() async {
    final repo = ref.read(analyticsRepositoryProvider);
    final data = await repo.getCompletionTrend();
    CacheService.analyticsBox.put('task_completion_trend', jsonEncode(data.map((e) => e.toJson()).toList()));
    return data;
  }
  
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetch());
  }
}
