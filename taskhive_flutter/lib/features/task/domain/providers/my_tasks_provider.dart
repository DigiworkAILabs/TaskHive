import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../../core/storage/cache_service.dart';
import '../../data/models/task_model.dart';
import '../../data/repositories/task_repository.dart';
import 'task_list_provider.dart' show TaskListState;

part 'my_tasks_provider.g.dart';

@riverpod
class MyTasksNotifier extends _$MyTasksNotifier {
  final _cacheService = CacheService();
  String? _statusFilter;
  String? _sortBy;
  String? _sortDir;

  @override
  Future<TaskListState> build() async {
    final cached = _cacheService.getCachedMyTasks();
    if (cached != null && cached.isNotEmpty) {
      Future.microtask(_backgroundRefresh);
      return TaskListState(
        tasks: cached.map((e) => TaskModel.fromJson(e)).toList(),
        fromCache: true,
      );
    }
    return _fetchPage(0);
  }

  Future<TaskListState> _fetchPage(int page) async {
    final repo = ref.read(taskRepositoryProvider);
    final data = await repo.getMyTasks(
      page: page,
      size: 20,
      status: _statusFilter,
      sortBy: _sortBy,
      sortDir: _sortDir,
    );
    final content = (data['content'] as List<dynamic>)
        .map((e) => TaskModel.fromJson(e as Map<String, dynamic>))
        .toList();

    if (page == 0) {
      await _cacheService.cacheMyTasks(content.map((e) => e.toJson()).toList());
    }

    return TaskListState(
      tasks: content,
      totalElements: data['totalElements'] as int? ?? 0,
      totalPages: data['totalPages'] as int? ?? 1,
      currentPage: data['number'] as int? ?? 0,
    );
  }

  Future<void> _backgroundRefresh() async {
    try {
      final fresh = await _fetchPage(0);
      state = AsyncData(fresh);
    } catch (_) {}
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  Future<void> loadMore() async {
    final current = state.valueOrNull;
    if (current == null || !current.hasMore || current.isLoadingMore) return;
    state = AsyncData(current.copyWith(isLoadingMore: true));
    try {
      final repo = ref.read(taskRepositoryProvider);
      final data = await repo.getMyTasks(
        page: current.currentPage + 1,
        size: 20,
        status: _statusFilter,
        sortBy: _sortBy,
        sortDir: _sortDir,
      );
      final more = (data['content'] as List<dynamic>)
          .map((e) => TaskModel.fromJson(e as Map<String, dynamic>))
          .toList();
      state = AsyncData(current.copyWith(
        tasks: [...current.tasks, ...more],
        currentPage: data['number'] as int? ?? current.currentPage + 1,
        totalPages: data['totalPages'] as int? ?? current.totalPages,
        isLoadingMore: false,
      ));
    } catch (_) {
      state = AsyncData(current.copyWith(isLoadingMore: false));
    }
  }

  Future<void> applySort(String? sortBy, String? sortDir) async {
    _sortBy = sortBy;
    _sortDir = sortDir;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  Future<void> applyFilter(String? status) async {
    _statusFilter = status;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  void updateTask(TaskModel updated) {
    final current = state.valueOrNull;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      tasks:
          current.tasks.map((t) => t.id == updated.id ? updated : t).toList(),
    ));
  }
}
