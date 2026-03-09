import 'package:flutter/material.dart';

import '../../domain/enums/task_priority.dart';
import '../../domain/enums/task_status.dart';
import '../../domain/providers/task_list_provider.dart';

/// Horizontal scrollable filter bar for the admin task list.
/// Provides status chips and priority chips.
class TaskFilterBar extends StatelessWidget {
  final TaskListFilter filter;
  final ValueChanged<TaskListFilter> onFilterChanged;

  const TaskFilterBar({
    super.key,
    required this.filter,
    required this.onFilterChanged,
  });

  @override
  Widget build(BuildContext context) {
    final hasFilters = filter.hasFilters;
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // Search Field
          SizedBox(
            width: 200,
            height: 40,
            child: TextField(
              decoration: const InputDecoration(
                hintText: 'Search tasks...',
                prefixIcon: Icon(Icons.search, size: 18),
                border: OutlineInputBorder(),
                contentPadding: EdgeInsets.zero,
                isDense: true,
                hintStyle: TextStyle(fontSize: 13),
              ),
              style: const TextStyle(fontSize: 13),
              onSubmitted: (val) {
                onFilterChanged(
                  TaskListFilter(
                    status: filter.status,
                    priority: filter.priority,
                    assignedTo: filter.assignedTo,
                    tags: filter.tags,
                    search: val.trim().isEmpty ? null : val.trim(),
                  ),
                );
              },
            ),
          ),
          const SizedBox(width: 12),
          // Clear all button
          if (hasFilters)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: ActionChip(
                label: const Text('Clear'),
                avatar: const Icon(Icons.clear, size: 16),
                onPressed: () => onFilterChanged(const TaskListFilter()),
              ),
            ),
          // Status chips
          _label(context, 'Status:'),
          _statusChip(context, null, 'All'),
          ...TaskStatus.values.map((s) => _statusChip(context, s, s.label)),
          const SizedBox(width: 12),
          // Priority chips
          _label(context, 'Priority:'),
          _priorityChip(context, null, 'All'),
          ...TaskPriority.values.map((p) => _priorityChip(context, p, p.label)),
        ],
      ),
    );
  }

  Widget _label(BuildContext context, String text) => Padding(
        padding: const EdgeInsets.only(right: 6),
        child: Text(text,
            style: const TextStyle(fontSize: 12, color: Colors.grey)),
      );

  Widget _statusChip(BuildContext context, TaskStatus? status, String label) {
    final selected = filter.status == status?.backendValue;
    return Padding(
      padding: const EdgeInsets.only(right: 6),
      child: FilterChip(
        label: Text(label),
        selected: selected || (status == null && filter.status == null),
        onSelected: (_) => onFilterChanged(
          TaskListFilter(
            status: status?.backendValue,
            priority: filter.priority,
            assignedTo: filter.assignedTo,
            tags: filter.tags,
            search: filter.search,
          ),
        ),
        selectedColor:
            Theme.of(context).colorScheme.primary.withValues(alpha: 0.15),
      ),
    );
  }

  Widget _priorityChip(
      BuildContext context, TaskPriority? priority, String label) {
    final selected = filter.priority == priority?.backendValue;
    return Padding(
      padding: const EdgeInsets.only(right: 6),
      child: FilterChip(
        label: Text(label),
        selected: selected || (priority == null && filter.priority == null),
        onSelected: (_) => onFilterChanged(
          TaskListFilter(
            status: filter.status,
            priority: priority?.backendValue,
            assignedTo: filter.assignedTo,
            tags: filter.tags,
            search: filter.search,
          ),
        ),
        selectedColor:
            Theme.of(context).colorScheme.primary.withValues(alpha: 0.15),
      ),
    );
  }
}
