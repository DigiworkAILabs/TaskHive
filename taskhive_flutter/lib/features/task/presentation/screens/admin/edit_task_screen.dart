import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../domain/providers/task_actions_provider.dart';
import '../../../domain/providers/task_detail_provider.dart';
import '../../widgets/task_form.dart';

/// Screen to edit an existing task. ADMIN only.
/// Pre-fills the form with the current task values loaded from [taskDetailProvider].
class EditTaskScreen extends ConsumerWidget {
  final String taskId;

  const EditTaskScreen({super.key, required this.taskId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final taskAsync = ref.watch(taskDetailProvider(taskId));
    return Scaffold(
      appBar: AppBar(title: const Text('Edit Task')),
      body: taskAsync.when(
        data: (task) => TaskForm(
          initialTitle: task.title,
          initialDescription: task.description,
          initialPriority: task.priority,
          initialAssigneeId: task.assignedTo,
          initialDueDate:
              task.dueDate != null ? DateTime.tryParse(task.dueDate!) : null,
          initialEstimatedHours: task.estimatedHours,
          initialTags: task.tags,
          onUpdateSubmit: (request) async {
            await ref
                .read(taskActionsProvider.notifier)
                .updateTask(task.id, request);
            if (context.mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Task updated successfully!')),
              );
              context.pop();
            }
          },
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Error loading task: ${e.toString()}'),
              const SizedBox(height: 8),
              ElevatedButton(
                onPressed: () => ref.invalidate(taskDetailProvider(taskId)),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
