import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/audit_repository.dart';
import '../../data/models/audit_log_model.dart';
import '../../../../core/utils/logger.dart';

part 'audit_log_list_provider.g.dart';

class AuditLogListState {
  final List<AuditLogModel> logs;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final bool isLoadingMore;

  const AuditLogListState({
    this.logs = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.currentPage = 0,
    this.isLoadingMore = false,
  });

  AuditLogListState copyWith({
    List<AuditLogModel>? logs,
    int? totalElements,
    int? totalPages,
    int? currentPage,
    bool? isLoadingMore,
  }) {
    return AuditLogListState(
      logs: logs ?? this.logs,
      totalElements: totalElements ?? this.totalElements,
      totalPages: totalPages ?? this.totalPages,
      currentPage: currentPage ?? this.currentPage,
      isLoadingMore: isLoadingMore ?? this.isLoadingMore,
    );
  }
}

@riverpod
class AuditLogListNotifier extends _$AuditLogListNotifier {
  static const int _pageSize = 20;

  @override
  FutureOr<AuditLogListState> build() async {
    return _fetchPage(0, const AuditLogListState());
  }

  Future<AuditLogListState> _fetchPage(
      int page, AuditLogListState current) async {
    final response = await ref
        .read(auditRepositoryProvider)
        .getAuditLogs(page: page, size: _pageSize);

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

  Future<void> refresh() async {
    state = const AsyncLoading();
    state =
        await AsyncValue.guard(() => _fetchPage(0, const AuditLogListState()));
  }

  Future<void> loadMore() async {
    final current = state.value;
    if (current == null ||
        current.isLoadingMore ||
        current.currentPage + 1 >= current.totalPages) {
      return;
    }

    // Optimistic loading state
    state = AsyncData(current.copyWith(isLoadingMore: true));

    try {
      final newState = await _fetchPage(current.currentPage + 1, current);
      state = AsyncData(newState);
    } catch (e) {
      appLogger.e('Failed to load more audit logs: $e');
      // Revert loading state but keep data on error
      state = AsyncData(current.copyWith(isLoadingMore: false));
    }
  }

  // Not used directly when searching, but useful if we wanted to clear filters and revert
  void clearFilter() => refresh();
}
