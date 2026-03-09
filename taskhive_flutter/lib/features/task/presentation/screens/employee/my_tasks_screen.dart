import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../domain/enums/task_status.dart';
import '../../../domain/providers/my_tasks_provider.dart';
import '../../widgets/task_list_tile.dart';

/// Employee-facing "My Tasks" screen.
/// Shows the logged-in employee's assigned tasks: filterable by status/priority.
/// No create/delete capabilities — employees can only view and update status.
class MyTasksScreen extends ConsumerStatefulWidget {
  const MyTasksScreen({super.key});

  @override
  ConsumerState<MyTasksScreen> createState() => _MyTasksScreenState();
}

class _MyTasksScreenState extends ConsumerState<MyTasksScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  int _selectedIndex = 0;

  // Tab → status filter value (null = All)
  static const _tabStatuses = <TaskStatus?>[
    null,
    TaskStatus.todo,
    TaskStatus.inProgress,
    TaskStatus.inReview,
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _tabController.addListener(() {
      if (_tabController.index != _selectedIndex) {
        _selectedIndex = _tabController.index;
        final status = _tabStatuses[_selectedIndex];
        ref
            .read(myTasksNotifierProvider.notifier)
            .applyFilter(status?.backendValue);
      }
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final tasksAsync = ref.watch(myTasksNotifierProvider);
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Tasks'),
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          tabs: const [
            Tab(text: 'All'),
            Tab(text: 'To Do'),
            Tab(text: 'In Progress'),
            Tab(text: 'In Review'),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () =>
                ref.read(myTasksNotifierProvider.notifier).refresh(),
          ),
        ],
      ),
      body: tasksAsync.when(
        data: (state) {
          if (state.tasks.isEmpty) {
            return Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(Icons.task_alt, size: 64, color: Colors.grey.shade300),
                  const SizedBox(height: 12),
                  const Text('No tasks assigned',
                      style: TextStyle(color: Colors.grey, fontSize: 16)),
                ],
              ),
            );
          }
          return RefreshIndicator(
            onRefresh: () =>
                ref.read(myTasksNotifierProvider.notifier).refresh(),
            child: ListView.builder(
              padding: const EdgeInsets.only(bottom: 16),
              itemCount: state.tasks.length + (state.hasMore ? 1 : 0),
              itemBuilder: (_, i) {
                if (i == state.tasks.length) {
                  WidgetsBinding.instance.addPostFrameCallback((_) {
                    ref.read(myTasksNotifierProvider.notifier).loadMore();
                  });
                  return const Center(
                    child: Padding(
                      padding: EdgeInsets.all(16),
                      child: CircularProgressIndicator(),
                    ),
                  );
                }
                final task = state.tasks[i];
                return TaskListTile(
                  task: task,
                  onTap: () =>
                      context.push(AppRoutes.employeeTaskDetailPath(task.id)),
                );
              },
            ),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Error: ${e.toString()}'),
              const SizedBox(height: 8),
              ElevatedButton(
                onPressed: () =>
                    ref.read(myTasksNotifierProvider.notifier).refresh(),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
