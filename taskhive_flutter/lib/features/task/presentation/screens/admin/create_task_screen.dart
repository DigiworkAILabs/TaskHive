import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../domain/providers/task_actions_provider.dart';
import '../../widgets/task_form.dart';

/// Screen to create a new task. ADMIN only.
class CreateTaskScreen extends ConsumerWidget {
  const CreateTaskScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Create Task')),
      body: TaskForm(
        onCreateSubmit: (request) async {
          await ref.read(taskActionsProvider.notifier).createTask(request);
          if (context.mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Task created successfully!')),
            );
            context.pop();
          }
        },
      ),
    );
  }
}
