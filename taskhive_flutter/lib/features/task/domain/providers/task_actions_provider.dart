import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/create_task_request.dart';
import '../../data/models/task_model.dart';
import '../../data/models/update_task_request.dart';
import '../../data/models/update_task_status_request.dart';
import '../../data/repositories/task_repository.dart';
import 'my_tasks_provider.dart';
import 'overdue_tasks_provider.dart';
import 'task_detail_provider.dart';
import 'task_list_provider.dart';

part 'task_actions_provider.g.dart';

/// Handles all task mutations: create, update, delete, status change.
/// After each mutation it updates the relevant list providers and invalidates
/// the task detail provider to ensure fresh data.
@riverpod
class TaskActions extends _$TaskActions {
  @override
  Future<void> build() async {}

  Future<TaskModel> createTask(CreateTaskRequest request) async {
    state = const AsyncLoading();
    final repo = ref.read(taskRepositoryProvider);
    final task = await repo.createTask(request);
    await ref.read(taskListNotifierProvider.notifier).refresh();
    ref.invalidate(overdueTasksProvider);
    state = const AsyncData(null);
    return task;
  }

  Future<TaskModel> updateTask(String id, UpdateTaskRequest request) async {
    state = const AsyncLoading();
    final repo = ref.read(taskRepositoryProvider);
    final task = await repo.updateTask(id, request);
    ref.read(taskListNotifierProvider.notifier).updateTask(task);
    ref.invalidate(taskDetailProvider(id));
    state = const AsyncData(null);
    return task;
  }

  Future<void> deleteTask(String id) async {
    state = const AsyncLoading();
    final repo = ref.read(taskRepositoryProvider);
    await repo.deleteTask(id);
    ref.read(taskListNotifierProvider.notifier).removeTask(id);
    ref.invalidate(overdueTasksProvider);
    state = const AsyncData(null);
  }

  Future<TaskModel> updateTaskStatus(
      String id, UpdateTaskStatusRequest request) async {
    state = const AsyncLoading();
    final repo = ref.read(taskRepositoryProvider);
    final task = await repo.updateTaskStatus(id, request);
    // Update both admin and employee list views
    _refreshLists(task);
    ref.invalidate(taskDetailProvider(id));
    ref.invalidate(overdueTasksProvider);
    state = const AsyncValue.data(null);
    return task;
  }

  Future<TaskModel> approveTask(String id) async {
    state = const AsyncLoading();
    final repo = ref.read(taskRepositoryProvider);
    final task = await repo.approveTask(id);
    _refreshLists(task);
    ref.invalidate(taskDetailProvider(id));
    state = const AsyncValue.data(null);
    return task;
  }

  Future<TaskModel> rejectTask(String id, String reason) async {
    state = const AsyncLoading();
    final repo = ref.read(taskRepositoryProvider);
    final task = await repo.rejectTask(id, reason);
    _refreshLists(task);
    ref.invalidate(taskDetailProvider(id));
    state = const AsyncValue.data(null);
    return task;
  }

  void _refreshLists(TaskModel task) {
    ref.read(taskListNotifierProvider.notifier).updateTask(task);
    ref.read(myTasksNotifierProvider.notifier).updateTask(task);
  }
}
