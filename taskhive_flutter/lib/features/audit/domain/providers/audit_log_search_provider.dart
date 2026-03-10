import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/audit_repository.dart';
import 'audit_log_list_provider.dart';
import '../../data/models/audit_log_model.dart';

part 'audit_log_search_provider.g.dart';

@riverpod
class AuditLogSearchNotifier extends _$AuditLogSearchNotifier {
  AuditSearchFilter? _currentFilter;

  @override
  FutureOr<AuditLogListState> build() async {
    return const AuditLogListState();
  }

  Future<void> search(AuditSearchFilter filter) async {
    _currentFilter = filter;
    state = const AsyncLoading();

    try {
      final newState = await _fetchFilteredPage(0, const AuditLogListState());
      state = AsyncData(newState);
    } catch (e, st) {
      state = AsyncError(e, st);
    }
  }

  Future<AuditLogListState> _fetchFilteredPage(
      int page, AuditLogListState current) async {
    if (_currentFilter == null) return current;

    // Use a copy of the filter with the requested page
    final filterForPage = AuditSearchFilter(
      fromDate: _currentFilter!.fromDate,
      toDate: _currentFilter!.toDate,
      action: _currentFilter!.action,
      entityType: _currentFilter!.entityType,
      actorEmail: _currentFilter!.actorEmail,
      ipAddress: _currentFilter!.ipAddress,
      page: page,
      size: _currentFilter!.size,
    );

    final response =
        await ref.read(auditRepositoryProvider).searchAuditLogs(filterForPage);

    final List<dynamic> content = response['content'] ?? [];
    final items = content
        .map((e) => AuditLogModel.fromJson(e as Map<String, dynamic>))
        .toList();

    return current.copyWith(
      logs: page == 0 ? items : [...current.logs, ...items],
      totalElements: response['totalElements'],
      totalPages: response['totalPages'],
      currentPage: page,
      isLoadingMore: false,
    );
  }

  Future<void> loadMore() async {
    final current = state.value;
    if (current == null ||
        current.isLoadingMore ||
        _currentFilter == null ||
        current.currentPage + 1 >= current.totalPages) {
      return;
    }

    // Optimistic loading state
    state = AsyncData(current.copyWith(isLoadingMore: true));

    try {
      final newState =
          await _fetchFilteredPage(current.currentPage + 1, current);
      state = AsyncData(newState);
    } catch (e) {
      // Revert loading state but keep data on error
      state = AsyncData(current.copyWith(isLoadingMore: false));
    }
  }

  void clear() {
    _currentFilter = null;
    state = const AsyncData(AuditLogListState());
  }
}
