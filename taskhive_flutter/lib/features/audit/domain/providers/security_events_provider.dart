import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/audit_repository.dart';
import '../../data/models/security_event_model.dart';
import '../../../../core/utils/logger.dart';

part 'security_events_provider.g.dart';

class SecurityEventsState {
  final List<SecurityEventModel> events;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final bool isLoadingMore;

  const SecurityEventsState({
    this.events = const [],
    this.totalElements = 0,
    this.totalPages = 0,
    this.currentPage = 0,
    this.isLoadingMore = false,
  });

  SecurityEventsState copyWith({
    List<SecurityEventModel>? events,
    int? totalElements,
    int? totalPages,
    int? currentPage,
    bool? isLoadingMore,
  }) {
    return SecurityEventsState(
      events: events ?? this.events,
      totalElements: totalElements ?? this.totalElements,
      totalPages: totalPages ?? this.totalPages,
      currentPage: currentPage ?? this.currentPage,
      isLoadingMore: isLoadingMore ?? this.isLoadingMore,
    );
  }
}

@riverpod
class SecurityEventsNotifier extends _$SecurityEventsNotifier {
  static const int _pageSize = 20;

  @override
  FutureOr<SecurityEventsState> build() async {
    return _fetchPage(0, const SecurityEventsState());
  }

  Future<SecurityEventsState> _fetchPage(
      int page, SecurityEventsState current) async {
    final response = await ref
        .read(auditRepositoryProvider)
        .getSecurityEvents(page: page, size: _pageSize);

    final List<dynamic> content = response['content'] ?? [];
    final items = content
        .map((e) => SecurityEventModel.fromJson(e as Map<String, dynamic>))
        .toList();

    return current.copyWith(
      events: page == 0 ? items : [...current.events, ...items],
      totalElements: response['totalElements'],
      totalPages: response['totalPages'],
      currentPage: page,
      isLoadingMore: false,
    );
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(
        () => _fetchPage(0, const SecurityEventsState()));
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
      appLogger.e('Failed to load more security events: $e');
      // Revert loading state but keep data on error
      state = AsyncData(current.copyWith(isLoadingMore: false));
    }
  }
}
