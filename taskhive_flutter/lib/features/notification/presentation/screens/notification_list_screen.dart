import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/providers/notification_list_provider.dart';
import '../../domain/providers/notification_actions_provider.dart';
import '../../domain/providers/unread_count_provider.dart';
import '../widgets/notification_list_tile.dart';
import '../../../../core/services/notification_navigation_service.dart';

class NotificationListScreen extends ConsumerStatefulWidget {
  const NotificationListScreen({Key? key}) : super(key: key);

  @override
  ConsumerState<NotificationListScreen> createState() =>
      _NotificationListScreenState();
}

class _NotificationListScreenState
    extends ConsumerState<NotificationListScreen> {
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      ref.read(notificationListNotifierProvider.notifier).loadMore();
    }
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final notificationsAsync = ref.watch(notificationListNotifierProvider);
    final unreadCount = ref.watch(unreadCountProvider).valueOrNull ?? 0;

    // Listen to actions for errors
    ref.listen(notificationActionsProvider, (_, curr) {
      if (curr is AsyncError) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Failed to update notification status')));
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          if (unreadCount > 0)
            TextButton(
              onPressed: () {
                ref.read(notificationActionsProvider.notifier).markAllRead();
              },
              child: const Text('Mark all read'),
            ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () =>
            ref.read(notificationListNotifierProvider.notifier).refresh(),
        child: notificationsAsync.when(
          data: (notifications) {
            if (notifications.isEmpty) {
              return Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.notifications_none,
                        size: 64, color: Colors.grey.shade400),
                    const SizedBox(height: 16),
                    Text('No notifications yet',
                        style: Theme.of(context).textTheme.titleMedium),
                  ],
                ),
              );
            }

            return ListView.separated(
              controller: _scrollController,
              itemCount:
                  notifications.length + (notificationsAsync.isLoading ? 1 : 0),
              separatorBuilder: (_, __) => const Divider(height: 1),
              itemBuilder: (context, index) {
                if (index == notifications.length) {
                  return const Padding(
                    padding: EdgeInsets.all(16.0),
                    child: Center(child: CircularProgressIndicator()),
                  );
                }

                final notification = notifications[index];
                return NotificationListTile(
                  notification: notification,
                  onTap: () {
                    // 1. Mark as read
                    ref
                        .read(notificationActionsProvider.notifier)
                        .markAsRead(notification.id);

                    // 2. Navigate
                    if (notification.entityId != null) {
                      final navService =
                          ref.read(notificationNavigationServiceProvider);
                      navService.navigateFromNotification({
                        'entityType': notification.entityType,
                        'entityId': notification.entityId,
                      });
                    }
                  },
                );
              },
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (err, st) => Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text('Error loading notifications'),
                ElevatedButton(
                  onPressed: () => ref
                      .read(notificationListNotifierProvider.notifier)
                      .refresh(),
                  child: const Text('Retry'),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
