import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/task_model.dart';
import '../../data/repositories/task_repository.dart';

part 'overdue_tasks_provider.g.dart';

/// Fetches overdue tasks (ADMIN only). No caching — always fresh.
/// Paginated (confirmed by Postman step 21).
@riverpod
Future<List<TaskModel>> overdueTasks(OverdueTasksRef ref) async {
  final repo = ref.read(taskRepositoryProvider);
  final data = await repo.getOverdueTasks(page: 0, size: 50);
  final content = data['content'] as List<dynamic>;
  return content
      .map((e) => TaskModel.fromJson(e as Map<String, dynamic>))
      .toList();
}
