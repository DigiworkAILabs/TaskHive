import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/notification_repository.dart';
import '../../../../core/storage/cache_service.dart';

part 'unread_count_provider.g.dart';

@riverpod
class UnreadCount extends _$UnreadCount {
  @override
  FutureOr<int> build() async {
    // Return cached value immediately if available
    final cachedCount = CacheService.analyticsBox.get('unread_count') as int?;
    if (cachedCount != null) {
      // Background refresh
      _fetchAndCache();
      return cachedCount;
    }

    return _fetchAndCache();
  }

  Future<int> _fetchAndCache() async {
    try {
      final count =
          await ref.read(notificationRepositoryProvider).getUnreadCount();
      await CacheService.analyticsBox.put('unread_count', count);
      state = AsyncData(count);
      return count;
    } catch (e, st) {
      if (state.hasValue) {
        return state.value!;
      }
      state = AsyncError(e, st);
      rethrow;
    }
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    await _fetchAndCache();
  }

  void increment() {
    final current = state.valueOrNull ?? 0;
    final newCount = current + 1;
    CacheService.analyticsBox.put('unread_count', newCount);
    state = AsyncData(newCount);
  }

  void decrement() {
    final current = state.valueOrNull ?? 0;
    if (current > 0) {
      final newCount = current - 1;
      CacheService.analyticsBox.put('unread_count', newCount);
      state = AsyncData(newCount);
    }
  }

  void reset() {
    CacheService.analyticsBox.put('unread_count', 0);
    state = const AsyncData(0);
  }

  void setCount(int count) {
    CacheService.analyticsBox.put('unread_count', count);
    state = AsyncData(count);
  }
}
