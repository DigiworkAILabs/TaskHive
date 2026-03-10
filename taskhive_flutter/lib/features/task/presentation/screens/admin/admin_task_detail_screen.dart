import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../data/models/update_task_status_request.dart';
import '../../../domain/enums/task_status.dart';
import '../../../domain/providers/task_actions_provider.dart';
import '../../../domain/providers/task_detail_provider.dart';
import '../../widgets/status_transition_selector.dart';
import '../../widgets/task_attachments_section.dart';
import '../../widgets/task_comments_section.dart';
import '../../widgets/task_priority_badge.dart';
import '../../widgets/task_status_badge.dart';
import '../../widgets/task_status_history_timeline.dart';

/// Admin task detail screen.
/// Shows full task info + status transition (any task) + comments + attachments + history.
class AdminTaskDetailScreen extends ConsumerWidget {
  final String taskId;

  const AdminTaskDetailScreen({super.key, required this.taskId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final taskAsync = ref.watch(taskDetailProvider(taskId));
    return Scaffold(
      appBar: AppBar(
        title: taskAsync.when(
          data: (t) => Text(t.title, overflow: TextOverflow.ellipsis),
          loading: () => const Text('Task Detail'),
          error: (_, __) => const Text('Task Detail'),
        ),
        actions: [
          taskAsync.whenData((task) {
                return IconButton(
                  icon: const Icon(Icons.edit_outlined),
                  tooltip: 'Edit task',
                  onPressed: () =>
                      context.push(AppRoutes.adminEditTaskPath(task.id)),
                );
              }).value ??
              const SizedBox.shrink(),
        ],
      ),
      body: taskAsync.when(
        data: (task) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Title + overdue
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Text(
                    task.title,
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),

            // Status + Priority badges
            Wrap(
              spacing: 8,
              runSpacing: 4,
              children: [
                TaskStatusBadge(status: task.status),
                TaskPriorityBadge(priority: task.priority),
                if (task.isOverdue)
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.red.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Text('⚠ Overdue',
                        style: TextStyle(color: Colors.red, fontSize: 12)),
                  ),
              ],
            ),
            const SizedBox(height: 12),

            // Description
            if (task.description?.isNotEmpty == true) ...[
              const Text('Description',
                  style: TextStyle(fontWeight: FontWeight.w600)),
              const SizedBox(height: 4),
              Text(task.description!),
              const SizedBox(height: 12),
            ],

            // Metadata row
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              childAspectRatio: 2.2,
              mainAxisSpacing: 16,
              crossAxisSpacing: 16,
              children: [
                _MetaItem(
                    icon: Icons.person_outline,
                    label: 'Assigned To',
                    value: task.assigneeName ?? task.assignedTo ?? '—'),
                _MetaItem(
                    icon: Icons.calendar_today_outlined,
                    label: 'Due Date',
                    value: task.dueDate?.split('T').first ?? '—',
                    valueColor: task.isOverdue ? Colors.red : null),
                _MetaItem(
                    icon: Icons.timer_outlined,
                    label: 'Est. Hours',
                    value: task.estimatedHours != null
                        ? '${task.estimatedHours}h'
                        : '—'),
                _MetaItem(
                    icon: Icons.calendar_today_outlined,
                    label: 'Created',
                    value: task.createdAt?.split('T').first ?? '—'),
              ],
            ),
            const Divider(height: 28),

            // Status transition (admin can transition any task)
            if (!task.status.isTerminal)
              StatusTransitionSelector(
                currentStatus: task.status,
                onChanged: (newStatus) =>
                    _changeStatus(context, ref, task.id, newStatus),
              ),
            const SizedBox(height: 16),

            const Divider(height: 24),
            TaskCommentsSection(taskId: taskId),
            const Divider(height: 24),
            TaskAttachmentsSection(taskId: taskId),
            const Divider(height: 24),
            TaskStatusHistoryTimeline(taskId: taskId),
            const SizedBox(height: 40),
          ],
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: ${e.toString()}')),
      ),
    );
  }

  Future<void> _changeStatus(
    BuildContext context,
    WidgetRef ref,
    String id,
    TaskStatus newStatus,
  ) async {
    final commentController = TextEditingController();
    final comment = await showDialog<String>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: Text('Change to "${newStatus.label}"?'),
        content: TextField(
          controller: commentController,
          decoration: const InputDecoration(
            hintText: 'Optional comment...',
            border: OutlineInputBorder(),
          ),
          maxLines: 3,
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel')),
          FilledButton(
              onPressed: () =>
                  Navigator.pop(dialogContext, commentController.text),
              child: const Text('Confirm')),
        ],
      ),
    );
    if (comment == null) return;
    try {
      await ref.read(taskActionsProvider.notifier).updateTaskStatus(
            id,
            UpdateTaskStatusRequest(
              status: newStatus,
              comment: comment.isEmpty ? null : comment,
            ),
          );
      ref.invalidate(taskDetailProvider(id));
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed: ${e.toString()}')),
        );
      }
    }
  }
}

class _MetaItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color? valueColor;

  const _MetaItem(
      {required this.icon,
      required this.label,
      required this.value,
      this.valueColor});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.grey.shade100,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Row(
            children: [
              Icon(icon, size: 14, color: Colors.grey.shade600),
              const SizedBox(width: 6),
              Flexible(
                child: Text(label.toUpperCase(),
                    style: TextStyle(
                        color: Colors.grey.shade600,
                        fontSize: 10,
                        letterSpacing: 0.4),
                    overflow: TextOverflow.ellipsis),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text(value,
              style: TextStyle(
                  color: valueColor ?? Colors.black87,
                  fontSize: 14,
                  fontWeight: FontWeight.w600),
              overflow: TextOverflow.ellipsis),
        ],
      ),
    );
  }
}
