import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/task_model.dart';
import '../../data/repositories/task_repository.dart';

part 'task_search_provider.g.dart';

/// Family provider for full-text task search.
/// Returns an empty list for blank queries (saves unnecessary network call).
@riverpod
Future<List<TaskModel>> taskSearch(TaskSearchRef ref, String query) async {
  if (query.trim().isEmpty) return [];
  final repo = ref.read(taskRepositoryProvider);
  final data = await repo.searchTasks(query);
  final list = data['content'] as List<dynamic>;
  return list
      .map((e) => TaskModel.fromJson(e as Map<String, dynamic>))
      .toList();
}
