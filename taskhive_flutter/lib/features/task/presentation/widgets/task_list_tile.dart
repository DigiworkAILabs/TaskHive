import 'package:flutter/material.dart';

import '../../data/models/task_model.dart';
import 'overdue_indicator.dart';
import 'task_priority_badge.dart';
import 'task_status_badge.dart';

/// A list tile for a task. Shows title, status/priority badges, assignee, due date.
/// Trailing popup menu has "View" and optionally "Delete" (admin only, via onDelete).
class TaskListTile extends StatelessWidget {
  final TaskModel task;
  final VoidCallback onTap;
  final VoidCallback? onDelete;

  const TaskListTile({
    super.key,
    required this.task,
    required this.onTap,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      task.title,
                      style: const TextStyle(
                        fontWeight: FontWeight.w600,
                        fontSize: 15,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  if (onDelete != null)
                    PopupMenuButton<String>(
                      icon: const Icon(Icons.more_vert),
                      onSelected: (v) {
                        if (v == 'delete') onDelete!();
                        if (v == 'view') onTap();
                      },
                      itemBuilder: (_) => [
                        const PopupMenuItem(value: 'view', child: Text('View')),
                        const PopupMenuItem(
                          value: 'delete',
                          child: Text('Delete',
                              style: TextStyle(color: Colors.red)),
                        ),
                      ],
                    ),
                ],
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 6,
                runSpacing: 4,
                children: [
                  TaskStatusBadge(status: task.status),
                  TaskPriorityBadge(priority: task.priority),
                  if (task.isOverdue) const OverdueIndicator(),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  const Icon(Icons.person_outline,
                      size: 14, color: Colors.grey),
                  const SizedBox(width: 4),
                  Text(
                    task.assigneeName ?? task.assignedTo ?? 'Unassigned',
                    style: TextStyle(color: Colors.grey.shade600, fontSize: 12),
                  ),
                  if (task.dueDate != null) ...[
                    const SizedBox(width: 12),
                    const Icon(Icons.calendar_today_outlined,
                        size: 14, color: Colors.grey),
                    const SizedBox(width: 4),
                    Text(
                      task.dueDate!.length > 10
                          ? task.dueDate!.substring(0, 10)
                          : task.dueDate!,
                      style: const TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                  ],
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
