import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

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

/// Employee-facing task detail screen.
/// Shows full task info + StatusTransitionSelector (limited to allowed transitions for employee)
/// + comments (employee can only add comments) + attachments + status history.
class EmployeeTaskDetailScreen extends ConsumerWidget {
  final String taskId;

  const EmployeeTaskDetailScreen({super.key, required this.taskId});

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

            // Badges
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
            if (task.tags.isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 16),
                child: Wrap(
                  spacing: 6,
                  children: task.tags
                      .map((t) => Chip(
                            label:
                                Text(t, style: const TextStyle(fontSize: 12)),
                            visualDensity: VisualDensity.compact,
                          ))
                      .toList(),
                ),
              ),

            const Divider(height: 24),

            // Employee can change status using allowed transitions (same selector, same rules)
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
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: Text('Move to "${newStatus.label}"?'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Are you sure you want to change the status?'),
            const SizedBox(height: 12),
            TextField(
              controller: commentController,
              decoration: const InputDecoration(
                hintText: 'Add a comment (optional)...',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(_, false),
              child: const Text('Cancel')),
          FilledButton(
              onPressed: () => Navigator.pop(_, true),
              child: const Text('Confirm')),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await ref.read(taskActionsProvider.notifier).updateTaskStatus(
            id,
            UpdateTaskStatusRequest(
              status: newStatus,
              comment: commentController.text.isEmpty
                  ? null
                  : commentController.text,
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
