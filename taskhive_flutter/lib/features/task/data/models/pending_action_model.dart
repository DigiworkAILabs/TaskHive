/// Represents an offline action queued for replay when connectivity restores.
/// This is an in-memory only model — never serialized to Hive or disk.
/// Queue is lost on app restart (acceptable for Phase 3).
class PendingActionModel {
  final String id;
  final String taskId;

  /// Action type: 'status_change' | 'comment'
  final String type;

  /// Payload varies by type:
  /// - status_change: {'status': 'IN_PROGRESS', 'comment': '...'}
  /// - comment: {'content': '...'}
  final Map<String, dynamic> payload;

  final DateTime createdAt;

  PendingActionModel({
    required this.taskId,
    required this.type,
    required this.payload,
  })  : id = DateTime.now().microsecondsSinceEpoch.toString(),
        createdAt = DateTime.now();
}
