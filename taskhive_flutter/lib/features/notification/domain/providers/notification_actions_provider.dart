import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/notification_repository.dart';
import 'notification_list_provider.dart';
import 'unread_count_provider.dart';
import '../../../../core/connectivity/connectivity_provider.dart';
import '../../../../core/utils/logger.dart';

part 'notification_actions_provider.g.dart';

@riverpod
class NotificationActions extends _$NotificationActions {
  @override
  FutureOr<void> build() {}

  Future<void> markAsRead(String id) async {
    final isOnline = ref.read(connectivityProvider).valueOrNull ?? true;
    if (!isOnline) {
      appLogger.w('Offline — skipping markAsRead');
      return;
    }

    state = const AsyncLoading();
    try {
      await ref.read(notificationRepositoryProvider).markAsRead(id);

      // Update local state
      ref.read(notificationListNotifierProvider.notifier).markRead(id);
      ref.read(unreadCountProvider.notifier).decrement();

      state = const AsyncData(null);
    } catch (e, st) {
      state = AsyncError(e, st);
      appLogger.e('Failed to mark notification as read: $e');
    }
  }

  Future<void> markAllRead() async {
    final isOnline = ref.read(connectivityProvider).valueOrNull ?? true;
    if (!isOnline) {
      appLogger.w('Offline — skipping markAllRead');
      return;
    }

    state = const AsyncLoading();
    try {
      await ref.read(notificationRepositoryProvider).markAllRead();

      // Update local state
      ref.read(notificationListNotifierProvider.notifier).markAllRead();
      ref.read(unreadCountProvider.notifier).reset();

      state = const AsyncData(null);
    } catch (e, st) {
      state = AsyncError(e, st);
      appLogger.e('Failed to mark all notifications as read: $e');
    }
  }
}
