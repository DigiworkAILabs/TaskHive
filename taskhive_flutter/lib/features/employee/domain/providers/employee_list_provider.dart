import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../../core/storage/cache_service.dart';
import '../../data/models/employee_model.dart';
import '../../data/repositories/employee_repository.dart';

part 'employee_list_provider.g.dart';

// ─────────────────────────────────────────────────────────────────────────────
// Filter state
// ─────────────────────────────────────────────────────────────────────────────

/// Immutable filter applied to the employee list.
///
/// Includes `sortBy` and `sortDir` which are present in the Postman collection
/// (test 4) but absent from the Phase 2 spec — added per Conflict 1 resolution.
class EmployeeListFilter {
  final String? department;
  final String? status;
  final String? search;
  final String? sortBy;
  final String? sortDir;

  const EmployeeListFilter({
    this.department,
    this.status,
    this.search,
    this.sortBy = 'firstName',
    this.sortDir = 'asc',
  });

  EmployeeListFilter copyWith({
    String? department,
    String? status,
    String? search,
    String? sortBy,
    String? sortDir,
  }) =>
      EmployeeListFilter(
        department: department ?? this.department,
        status: status ?? this.status,
        search: search ?? this.search,
        sortBy: sortBy ?? this.sortBy,
        sortDir: sortDir ?? this.sortDir,
      );
}

// ─────────────────────────────────────────────────────────────────────────────
// List state
// ─────────────────────────────────────────────────────────────────────────────

/// Full paginated employee list state held by [EmployeeListNotifier].
class EmployeeListState {
  final List<EmployeeModel> employees;
  final int totalElements;
  final int totalPages;
  final int currentPage;

  /// True while the next page is loading for infinite scroll.
  final bool isLoadingMore;

  /// True when the current data was populated from the Hive cache — the UI
  /// may show an offline/stale indicator until the background refresh completes.
  final bool fromCache;

  const EmployeeListState({
    this.employees = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.currentPage = 0,
    this.isLoadingMore = false,
    this.fromCache = false,
  });

  /// True when more pages exist beyond the currently loaded page.
  bool get hasMore => currentPage < totalPages - 1;

  EmployeeListState copyWith({
    List<EmployeeModel>? employees,
    int? totalElements,
    int? totalPages,
    int? currentPage,
    bool? isLoadingMore,
    bool? fromCache,
  }) =>
      EmployeeListState(
        employees: employees ?? this.employees,
        totalElements: totalElements ?? this.totalElements,
        totalPages: totalPages ?? this.totalPages,
        currentPage: currentPage ?? this.currentPage,
        isLoadingMore: isLoadingMore ?? this.isLoadingMore,
        fromCache: fromCache ?? this.fromCache,
      );
}

// ─────────────────────────────────────────────────────────────────────────────
// Notifier
// ─────────────────────────────────────────────────────────────────────────────

/// Manages the paginated employee list with:
///   • Cache-then-network (stale-while-revalidate via Hive `employees_box`)
///   • Infinite scroll ([loadMore])
///   • Filter support ([applyFilter])
///   • In-place mutations ([updateEmployee], [removeEmployee])
@riverpod
class EmployeeListNotifier extends _$EmployeeListNotifier {
  final _cache = CacheService();
  EmployeeListFilter _filter = const EmployeeListFilter();

  @override
  Future<EmployeeListState> build() async {
    // ── Cache-then-network ────────────────────────────────────────────────────
    // Step 1: If Hive has cached employees, emit them instantly so the list
    // appears before any network round-trip ("stale-while-revalidate").
    final cached = _cache.getCachedEmployees();
    if (cached != null && cached.isNotEmpty) {
      // Schedule a silent background refresh — any errors are swallowed so that
      // stale cache is always shown rather than an error screen on reconnect.
      Future.microtask(_refresh);
      return EmployeeListState(
        employees: cached.map((json) => EmployeeModel.fromJson(json)).toList(),
        fromCache: true,
      );
    }
    // Step 2: No cache — fetch from network synchronously.
    return _fetchPage(0);
  }

  // ── Internal helpers ────────────────────────────────────────────────────────

  Future<EmployeeListState> _fetchPage(int page) async {
    final repo = ref.read(employeeRepositoryProvider);
    final data = await repo.getEmployees(
      page: page,
      size: 20,
      department: _filter.department,
      status: _filter.status,
      search: _filter.search,
      sortBy: _filter.sortBy,
      sortDir: _filter.sortDir,
    );

    final content = (data['content'] as List<dynamic>)
        .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
        .toList();

    // Persist the first page to Hive so it is available on next cold start.
    if (page == 0) {
      await _cache.cacheEmployees(content.map((e) => e.toJson()).toList());
    }

    return EmployeeListState(
      employees: content,
      totalElements: data['totalElements'] as int? ?? 0,
      totalPages: data['totalPages'] as int? ?? 1,
      currentPage: data['number'] as int? ?? 0,
    );
  }

  /// Background refresh called after the cache state is emitted.
  /// Silently updates state; never replaces a cache state with an error state.
  Future<void> _refresh() async {
    try {
      final fresh = await _fetchPage(0);
      state = AsyncData(fresh);
    } catch (_) {
      // Swallow — keep showing cached data; the app-level connectivity banner
      // (from connectivityProvider in Phase 1) already informs the user.
    }
  }

  // ── Public API ──────────────────────────────────────────────────────────────

  /// Pull-to-refresh — resets to page 0 and shows a loading indicator.
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  /// Appends the next page to the current list (infinite scroll).
  /// No-ops if already at the last page or a load is in progress.
  Future<void> loadMore() async {
    final current = state.valueOrNull;
    if (current == null || !current.hasMore || current.isLoadingMore) return;

    state = AsyncData(current.copyWith(isLoadingMore: true));
    try {
      final repo = ref.read(employeeRepositoryProvider);
      final data = await repo.getEmployees(
        page: current.currentPage + 1,
        size: 20,
        department: _filter.department,
        status: _filter.status,
        search: _filter.search,
        sortBy: _filter.sortBy,
        sortDir: _filter.sortDir,
      );
      final more = (data['content'] as List<dynamic>)
          .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
          .toList();

      state = AsyncData(current.copyWith(
        employees: [...current.employees, ...more],
        currentPage: data['number'] as int? ?? current.currentPage + 1,
        totalPages: data['totalPages'] as int? ?? current.totalPages,
        totalElements: data['totalElements'] as int? ?? current.totalElements,
        isLoadingMore: false,
      ));
    } catch (_) {
      state = AsyncData(current.copyWith(isLoadingMore: false));
    }
  }

  /// Apply new filter criteria and reload from page 0.
  Future<void> applyFilter(EmployeeListFilter filter) async {
    _filter = filter;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  /// Remove a single employee from the in-memory list after a successful delete.
  /// Avoids a full re-fetch.
  void removeEmployee(String id) {
    final current = state.valueOrNull;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      employees: current.employees.where((e) => e.id != id).toList(),
      totalElements: current.totalElements - 1,
    ));
  }

  /// Replace a single employee in the in-memory list after an update/status change.
  /// Avoids a full re-fetch while keeping the list badge/status current.
  void updateEmployee(EmployeeModel updated) {
    final current = state.valueOrNull;
    if (current == null) return;
    state = AsyncData(current.copyWith(
      employees: current.employees
          .map((e) => e.id == updated.id ? updated : e)
          .toList(),
    ));
  }
}
