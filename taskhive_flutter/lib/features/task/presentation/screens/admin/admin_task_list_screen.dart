import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../data/models/task_model.dart';
import '../../../domain/enums/task_status.dart';
import '../../../domain/providers/overdue_tasks_provider.dart';
import '../../../domain/providers/task_actions_provider.dart';
import '../../../domain/providers/task_list_provider.dart';
import '../../widgets/task_filter_bar.dart';
import '../../widgets/task_list_tile.dart';
import '../../../../notification/presentation/widgets/notification_badge.dart';

/// Admin task list screen.
/// Shows paginated list with status/priority filter chips, overdue badge, and FAB to create.
class AdminTaskListScreen extends ConsumerStatefulWidget {
  const AdminTaskListScreen({super.key});

  @override
  ConsumerState<AdminTaskListScreen> createState() =>
      _AdminTaskListScreenState();
}

class _AdminTaskListScreenState extends ConsumerState<AdminTaskListScreen> {
  TaskListFilter _filter = const TaskListFilter();

  @override
  Widget build(BuildContext context) {
    final tasksAsync = ref.watch(taskListNotifierProvider);
    final overdueAsync = ref.watch(overdueTasksProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Tasks'),
        actions: [
          // Overdue badge
          overdueAsync.whenData((overdue) {
                if (overdue.isEmpty) return const SizedBox.shrink();
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: Chip(
                    label: Text(
                      '${overdue.length} Overdue',
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                    backgroundColor: Theme.of(context).colorScheme.error,
                    visualDensity: VisualDensity.compact,
                  ),
                );
              }).value ??
              const SizedBox.shrink(),

          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () =>
                ref.read(taskListNotifierProvider.notifier).refresh(),
          ),
          NotificationBadge(
            child: IconButton(
              icon: const Icon(Icons.notifications_outlined),
              tooltip: 'Notifications',
              onPressed: () => context.push('/admin/notifications'),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push(AppRoutes.adminCreateTask),
        icon: const Icon(Icons.add),
        label: const Text('New Task'),
      ),
      body: Column(
        children: [
          // Stat cards
          tasksAsync.when(
            data: (state) => _buildStatCards(state),
            loading: () => const SizedBox.shrink(),
            error: (_, __) => const SizedBox.shrink(),
          ),
          // Filter bar
          TaskFilterBar(
            filter: _filter,
            onFilterChanged: (newFilter) {
              setState(() => _filter = newFilter);
              ref
                  .read(taskListNotifierProvider.notifier)
                  .applyFilter(newFilter);
            },
          ),
          // Task list
          Expanded(
            child: tasksAsync.when(
              data: (state) => _TaskList(state: state, filter: _filter),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text('Error: ${e.toString()}'),
                    const SizedBox(height: 8),
                    ElevatedButton(
                      onPressed: () =>
                          ref.read(taskListNotifierProvider.notifier).refresh(),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatCards(TaskListState state) {
    final tasks = state.tasks;
    final total = tasks.length;
    final inProgress =
        tasks.where((t) => t.status == TaskStatus.inProgress).length;
    final done = tasks.where((t) => t.status == TaskStatus.done).length;
    final overdue = tasks.where((t) => t.isOverdue).length;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        children: [
          _StatCard(
              label: 'Total',
              value: total,
              color: Colors.black87,
              icon: Icons.assignment_outlined),
          const SizedBox(width: 8),
          _StatCard(
              label: 'In Progress',
              value: inProgress,
              color: Colors.blue,
              icon: Icons.schedule),
          const SizedBox(width: 8),
          _StatCard(
              label: 'Done',
              value: done,
              color: Colors.green,
              icon: Icons.check_circle_outline),
          const SizedBox(width: 8),
          _StatCard(
              label: 'Overdue',
              value: overdue,
              color: Colors.red,
              icon: Icons.warning_amber_rounded),
        ],
      ),
    );
  }

  Widget _StatCard(
      {required String label,
      required int value,
      required Color color,
      required IconData icon}) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 4),
        decoration: BoxDecoration(
          color: Colors.grey.shade100,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.grey.shade300),
        ),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(icon, size: 12, color: Colors.grey.shade600),
                const SizedBox(width: 4),
                Flexible(
                    child: Text(label,
                        style: TextStyle(
                            fontSize: 10, color: Colors.grey.shade700),
                        overflow: TextOverflow.ellipsis)),
              ],
            ),
            const SizedBox(height: 8),
            Text('$value',
                style: TextStyle(
                    fontSize: 20, fontWeight: FontWeight.bold, color: color)),
          ],
        ),
      ),
    );
  }
}

class _TaskList extends ConsumerWidget {
  final TaskListState state;
  final TaskListFilter filter;

  const _TaskList({required this.state, required this.filter});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tasks = state.tasks;

    if (tasks.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.task_alt, size: 64, color: Colors.grey.shade300),
            const SizedBox(height: 12),
            const Text('No tasks found',
                style: TextStyle(color: Colors.grey, fontSize: 16)),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: () => ref.read(taskListNotifierProvider.notifier).refresh(),
      child: ListView.builder(
        padding: const EdgeInsets.only(bottom: 80),
        itemCount: tasks.length + (state.hasMore ? 1 : 0),
        itemBuilder: (_, i) {
          if (i == tasks.length) {
            // Load more trigger
            WidgetsBinding.instance.addPostFrameCallback((_) {
              ref.read(taskListNotifierProvider.notifier).loadMore();
            });
            return const Center(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: CircularProgressIndicator(),
              ),
            );
          }
          final task = tasks[i];
          return TaskListTile(
            task: task,
            onTap: () => context.push(AppRoutes.adminTaskDetailPath(task.id)),
            onDelete: () => _confirmDelete(context, ref, task),
          );
        },
      ),
    );
  }

  Future<void> _confirmDelete(
      BuildContext context, WidgetRef ref, TaskModel task) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Delete Task?'),
        content: Text('Are you sure you want to delete "${task.title}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext, true),
            style: FilledButton.styleFrom(
                backgroundColor: Theme.of(context).colorScheme.error),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
    if (confirmed == true && context.mounted) {
      try {
        await ref.read(taskActionsProvider.notifier).deleteTask(task.id);
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Task deleted')),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Delete failed: ${e.toString()}')),
          );
        }
      }
    }
  }
}
