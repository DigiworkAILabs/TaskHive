import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/notification_repository.dart';
import '../../data/models/notification_preference_model.dart';
import '../../../../core/utils/logger.dart';

part 'notification_preference_provider.g.dart';

@riverpod
class NotificationPreferenceNotifier extends _$NotificationPreferenceNotifier {
  @override
  FutureOr<NotificationPreferenceModel> build() async {
    return ref.read(notificationRepositoryProvider).getPreferences();
  }

  Future<void> updatePreferences(NotificationPreferenceModel newPrefs) async {
    final oldState = state;

    // Optimistic UI update
    state = AsyncData(newPrefs);

    try {
      final result = await ref
          .read(notificationRepositoryProvider)
          .updatePreferences(newPrefs);
      state = AsyncData(result);
    } catch (e, st) {
      appLogger.e('Failed to update preferences: $e');
      // Revert on failure
      state = oldState;
      rethrow;
    }
  }
}
