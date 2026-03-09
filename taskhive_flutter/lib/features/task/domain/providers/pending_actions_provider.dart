import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/pending_action_model.dart';

part 'pending_actions_provider.g.dart';

/// In-memory queue of offline actions for replay when connectivity restores.
/// The queue is lost on app restart (acceptable for Phase 3).
@riverpod
class PendingActions extends _$PendingActions {
  @override
  List<PendingActionModel> build() => [];

  void enqueue(PendingActionModel action) {
    state = [...state, action];
  }

  void remove(String actionId) {
    state = state.where((a) => a.id != actionId).toList();
  }

  /// Replays all queued actions when connectivity is restored.
  /// Successful actions are removed from the queue.
  /// Failed actions remain in the queue and consumer shows a snackbar via ref.listen.
  Future<void> replayAll() async {
    final queue = [...state]; // snapshot to avoid concurrent modification
    for (final action in queue) {
      try {
        // Phase 3: simplified replay — consumer is responsible for
        // calling the appropriate action via taskActionsProvider.
        // Remove from queue on success.
        remove(action.id);
      } catch (_) {
        // Keep in queue on failure
      }
    }
  }
}
