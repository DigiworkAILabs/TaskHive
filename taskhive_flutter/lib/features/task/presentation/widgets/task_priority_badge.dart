import 'package:flutter/material.dart';

import '../../domain/enums/task_priority.dart';

/// Displays a color-coded badge for a TaskPriority value with an icon.
class TaskPriorityBadge extends StatelessWidget {
  final TaskPriority priority;

  const TaskPriorityBadge({super.key, required this.priority});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: priority.color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: priority.color.withValues(alpha: 0.35)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(priority.icon, size: 12, color: priority.color),
          const SizedBox(width: 4),
          Text(
            priority.label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: priority.color,
            ),
          ),
        ],
      ),
    );
  }
}
