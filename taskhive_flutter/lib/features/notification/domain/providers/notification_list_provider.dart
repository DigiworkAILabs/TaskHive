import 'dart:convert';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/notification_repository.dart';
import '../../data/models/notification_model.dart';
import 'unread_count_provider.dart';
import '../../../../core/storage/cache_service.dart';

part 'notification_list_provider.g.dart';

@riverpod
class NotificationListNotifier extends _$NotificationListNotifier {
  bool _hasMore = true;
  int _page = 0;
  static const int _pageSize = 20;

  @override
  FutureOr<List<NotificationModel>> build() async {
    // 1. Load from cache first
    final cachedData =
        CacheService.notificationsBox.get('notifications_page_0');
    if (cachedData != null) {
      try {
        final list = (jsonDecode(cachedData) as List)
            .map((e) => NotificationModel.fromJson(e as Map<String, dynamic>))
            .toList();

        // 2. Fetch network in background
        _fetchPage(0).then((netList) {
          state = AsyncData(netList);
        }).catchError((_) {
          // Ignore background network error if we have cache
        });

        return list;
      } catch (_) {
        // Fallback to network on cache parse error
      }
    }

    // 3. Normal network fetch
    return _fetchPage(0);
  }

  Future<List<NotificationModel>> _fetchPage(int page) async {
    final response = await ref
        .read(notificationRepositoryProvider)
        .getNotifications(page: page, size: _pageSize);

    final List<dynamic> content = response['content'] ??
        (response['notifications'] as Map<String, dynamic>?)?['content'] ??
        [];
    final items = content
        .map((e) => NotificationModel.fromJson(e as Map<String, dynamic>))
        .toList();

    // Sync unread count if available in the response
    final unreadCount = response['unreadCount'] as num?;
    if (unreadCount != null) {
      ref.read(unreadCountProvider.notifier).setCount(unreadCount.toInt());
    }

    if (items.length < _pageSize) {
      _hasMore = false;
    }

    _page = page;

    // Cache first page
    if (page == 0) {
      final jsonList = items.map((e) => e.toJson()).toList();
      CacheService.notificationsBox
          .put('notifications_page_0', jsonEncode(jsonList));
    }

    return items;
  }

  Future<void> loadMore() async {
    if (!_hasMore || state.isLoading || state.hasError) return;

    final currentList = state.value ?? [];
    state = const AsyncLoading();

    try {
      final nextPage = _page + 1;
      final newItems = await _fetchPage(nextPage);
      state = AsyncData([...currentList, ...newItems]);
    } catch (e, st) {
      state = AsyncError(e, st);
      // Restore previous state so we don't lose the list on error
      state = AsyncData(currentList);
    }
  }

  Future<void> refresh() async {
    _hasMore = true;
    _page = 0;
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetchPage(0));
  }

  void prependNew(NotificationModel notification) {
    if (state.hasValue) {
      final currentList = state.value!;

      // Don't duplicate if already present
      if (currentList.any((n) => n.id == notification.id)) return;

      final updatedList = [notification, ...currentList];
      state = AsyncData(updatedList);

      // Update cache for the first page
      final jsonList =
          updatedList.take(_pageSize).map((e) => e.toJson()).toList();
      CacheService.notificationsBox
          .put('notifications_page_0', jsonEncode(jsonList));
    }
  }

  void markRead(String id) {
    if (state.hasValue) {
      final updatedList = state.value!.map((n) {
        if (n.id == id && !n.isRead) {
          return n.copyWith(
              isRead: true, readAt: DateTime.now().toIso8601String());
        }
        return n;
      }).toList();

      state = AsyncData(updatedList);

      // Update cache
      final jsonList =
          updatedList.take(_pageSize).map((e) => e.toJson()).toList();
      CacheService.notificationsBox
          .put('notifications_page_0', jsonEncode(jsonList));
    }
  }

  void markAllRead() {
    if (state.hasValue) {
      final nowStr = DateTime.now().toIso8601String();
      final updatedList = state.value!.map((n) {
        return n.copyWith(isRead: true, readAt: n.readAt ?? nowStr);
      }).toList();

      state = AsyncData(updatedList);

      // Update cache
      final jsonList =
          updatedList.take(_pageSize).map((e) => e.toJson()).toList();
      CacheService.notificationsBox
          .put('notifications_page_0', jsonEncode(jsonList));
    }
  }
}
