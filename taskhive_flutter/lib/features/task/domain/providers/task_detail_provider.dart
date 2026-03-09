import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/task_model.dart';
import '../../data/repositories/task_repository.dart';

part 'task_detail_provider.g.dart';

/// Fetch a single task by ID. Family provider — one instance per task ID.
/// Invalidated after status changes, edits, or delete.
@riverpod
Future<TaskModel> taskDetail(TaskDetailRef ref, String id) async {
  final repo = ref.read(taskRepositoryProvider);
  return repo.getTaskById(id);
}
