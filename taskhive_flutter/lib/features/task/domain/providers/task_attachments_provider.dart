import 'package:image_picker/image_picker.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/task_attachment_model.dart';
import '../../data/repositories/task_attachment_repository.dart';

part 'task_attachments_provider.g.dart';

/// Family AsyncNotifier for attachments on a specific task.
/// Appends the new attachment to the list on successful upload.
@riverpod
class TaskAttachments extends _$TaskAttachments {
  late String _taskId;

  @override
  Future<List<TaskAttachmentModel>> build(String taskId) async {
    _taskId = taskId;
    final repo = ref.read(taskAttachmentRepositoryProvider);
    return repo.getAttachments(taskId);
  }

  Future<void> uploadAttachment(XFile file) async {
    final repo = ref.read(taskAttachmentRepositoryProvider);
    final attachment = await repo.uploadAttachment(_taskId, file);
    final current = state.valueOrNull ?? [];
    state = AsyncData([...current, attachment]);
  }
}
