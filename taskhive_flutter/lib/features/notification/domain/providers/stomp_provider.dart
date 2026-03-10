import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/network/stomp_client.dart';
import '../../../auth/domain/providers/current_user_provider.dart';
import '../../data/models/notification_model.dart';
import 'notification_list_provider.dart';
import 'unread_count_provider.dart';

class StompNotifier extends Notifier<StompClientWrapper> {
  @override
  StompClientWrapper build() {
    final user = ref.watch(currentUserProvider);

    if (user == null) {
      return StompClientWrapper.empty(ref: ref);
    }

    final wrapper = StompClientWrapper(userId: user.id, ref: ref);

    // We do not connect immediately in build to avoid side-effects during provider initialization if possible,
    // but the spec says "On connection" and "watches currentUserProvider".
    // We'll connect it here since it's the STOMP lifecycle manager.
    wrapper.connect();

    ref.onDispose(() {
      wrapper.disconnect();
    });

    return wrapper;
  }

  void handleNewNotification(NotificationModel notification) {
    ref.read(unreadCountProvider.notifier).increment();
    ref
        .read(notificationListNotifierProvider.notifier)
        .prependNew(notification);
  }
}

final stompProvider = NotifierProvider<StompNotifier, StompClientWrapper>(() {
  return StompNotifier();
});
