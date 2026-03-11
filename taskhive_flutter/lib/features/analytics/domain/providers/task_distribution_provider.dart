import 'dart:convert';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/analytics_repository.dart';
import '../../data/models/task_distribution_response.dart';
import '../../../../core/storage/cache_service.dart';

part 'task_distribution_provider.g.dart';

class TaskDistributionState {
  final List<TaskDistributionResponse> byStatus;
  final List<TaskDistributionResponse> byPriority;

  TaskDistributionState({
    this.byStatus = const [],
    this.byPriority = const [],
  });
}

@riverpod
class TaskDistributionNotifier extends _$TaskDistributionNotifier {
  @override
  FutureOr<TaskDistributionState> build() async {
    final cacheStatus = CacheService.analyticsBox.get('task_distribution_status');
    final cachePriority = CacheService.analyticsBox.get('task_distribution_priority');
    
    if (cacheStatus != null && cachePriority != null) {
      try {
        final statusList = (jsonDecode(cacheStatus) as List)
            .map((e) => TaskDistributionResponse.fromJson(e))
            .toList();
        final priorityList = (jsonDecode(cachePriority) as List)
            .map((e) => TaskDistributionResponse.fromJson(e))
            .toList();
            
        state = AsyncData(TaskDistributionState(
          byStatus: statusList,
          byPriority: priorityList,
        ));
      } catch (_) {}
    }
    
    return _fetch();
  }
  
  Future<TaskDistributionState> _fetch() async {
    final repo = ref.read(analyticsRepositoryProvider);
    
    // Admin dashboard loads task distribution elements together
    final results = await Future.wait([
      repo.getTaskDistribution(),
      repo.getTaskByPriority(),
    ]);
    
    final byStatus = results[0];
    final byPriority = results[1];
    
    CacheService.analyticsBox.put('task_distribution_status', jsonEncode(byStatus.map((e) => e.toJson()).toList()));
    CacheService.analyticsBox.put('task_distribution_priority', jsonEncode(byPriority.map((e) => e.toJson()).toList()));
    
    return TaskDistributionState(
      byStatus: byStatus,
      byPriority: byPriority,
    );
  }
  
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetch());
  }
}
