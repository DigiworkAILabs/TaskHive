import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/task_comment_model.dart';
import '../../data/models/task_comment_request.dart';
import '../../data/repositories/task_comment_repository.dart';

part 'task_comments_provider.g.dart';

/// Family AsyncNotifier for comments on a specific task.
/// Comments are appended on add.
@riverpod
class TaskComments extends _$TaskComments {
  late String _taskId;

  @override
  Future<List<TaskCommentModel>> build(String taskId) async {
    _taskId = taskId;
    final repo = ref.read(taskCommentRepositoryProvider);
    return repo.getComments(taskId);
  }

  Future<void> addComment(String content) async {
    final repo = ref.read(taskCommentRepositoryProvider);
    final comment = await repo.addComment(
      _taskId,
      TaskCommentRequest(content: content),
    );
    final current = state.valueOrNull ?? [];
    state = AsyncData([...current, comment]);
  }
}
