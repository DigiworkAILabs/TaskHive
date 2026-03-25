import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../../core/storage/cache_service.dart';
import '../../data/models/task_model.dart';
import '../../data/repositories/task_repository.dart';

part 'task_list_provider.g.dart';

/// Filter state for the admin task list.
class TaskListFilter {
  final String? status;
  final String? priority;
  final String? assignedTo;
  final List<String>? tags;
  final String? search;
  final String? sortBy;
  final String? sortDir;

  const TaskListFilter({
    this.status,
    this.priority,
    this.assignedTo,
    this.tags,
    this.search,
    this.sortBy,
    this.sortDir,
  });

  TaskListFilter copyWith({
    String? status,
    String? priority,
    String? assignedTo,
    List<String>? tags,
    String? search,
    String? sortBy,
    String? sortDir,
  }) =>
      TaskListFilter(
        status: status ?? this.status,
        priority: priority ?? this.priority,
        assignedTo: assignedTo ?? this.assignedTo,
        tags: tags ?? this.tags,
        search: search ?? this.search,
        sortBy: sortBy ?? this.sortBy,
        sortDir: sortDir ?? this.sortDir,
      );

  bool get hasFilters =>
      status != null ||
      priority != null ||
      assignedTo != null ||
      (tags != null && tags!.isNotEmpty) ||
      (search != null && search!.isNotEmpty) ||
      sortBy != null ||
      sortDir != null;
}

/// Paginated task list state.
class TaskListState {
  final List<TaskModel> tasks;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final bool isLoadingMore;
  final bool fromCache;

  const TaskListState({
    this.tasks = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.currentPage = 0,
    this.isLoadingMore = false,
    this.fromCache = false,
  });

  bool get hasMore => currentPage < totalPages - 1;

  TaskListState copyWith({
    List<TaskModel>? tasks,
    int? totalElements,
    int? totalPages,
    int? currentPage,
    bool? isLoadingMore,
    bool? fromCache,
  }) =>
      TaskListState(
        tasks: tasks ?? this.tasks,
        totalElements: totalElements ?? this.totalElements,
        totalPages: totalPages ?? this.totalPages,
        currentPage: currentPage ?? this.currentPage,
        isLoadingMore: isLoadingMore ?? this.isLoadingMore,
        fromCache: fromCache ?? this.fromCache,
      );
}

@riverpod
class TaskListNotifier extends _$TaskListNotifier {
  final _cacheService = CacheService();
  TaskListFilter _filter = const TaskListFilter();

  @override
  Future<TaskListState> build() async {
    final cached = _cacheService.getCachedTasks();
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
    final isSearching = _filter.search != null && _filter.search!.isNotEmpty;

    final data = isSearching
        ? await repo.searchTasks(
            _filter.search!,
            page: page,
            size: 20,
          )
        : await repo.getTasks(
            page: page,
            size: 20,
            status: _filter.status,
            priority: _filter.priority,
            assignedTo: _filter.assignedTo,
            tags: _filter.tags,
            sortBy: _filter.sortBy,
            sortDir: _filter.sortDir,
          );

    final content = (data['content'] as List<dynamic>)
        .map((e) => TaskModel.fromJson(e as Map<String, dynamic>))
        .toList();

    if (page == 0) {
      await _cacheService.cacheTasks(content.map((e) => e.toJson()).toList());
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
      final isSearching = _filter.search != null && _filter.search!.isNotEmpty;

      final data = isSearching
          ? await repo.searchTasks(
              _filter.search!,
              page: current.currentPage + 1,
              size: 20,
            )
          : await repo.getTasks(
              page: current.currentPage + 1,
              size: 20,
              status: _filter.status,
              priority: _filter.priority,
              assignedTo: _filter.assignedTo,
              tags: _filter.tags,
              sortBy: _filter.sortBy,
              sortDir: _filter.sortDir,
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

  Future<void> applyFilter(TaskListFilter filter) async {
    _filter = filter;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  void removeTask(String id) {
    final current = state.valueOrNull;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      tasks: current.tasks.where((t) => t.id != id).toList(),
      totalElements: current.totalElements - 1,
    ));
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
