import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/task_status_history_model.dart';
import '../../data/repositories/task_repository.dart';

part 'task_history_provider.g.dart';

/// Family provider that fetches status change history for a specific task.
/// No caching — always fresh. Invalidated when task status changes.
@riverpod
Future<List<TaskStatusHistoryModel>> taskHistory(
    TaskHistoryRef ref, String taskId) async {
  final repo = ref.read(taskRepositoryProvider);
  final raw = await repo.getHistory(taskId);
  return raw.map((e) => TaskStatusHistoryModel.fromJson(e)).toList();
}
