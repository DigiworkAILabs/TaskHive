import 'dart:convert';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/storage/cache_service.dart';
import '../../../task/data/repositories/task_repository.dart';
import '../../../task/data/models/task_model.dart';
import '../../../task/domain/enums/task_status.dart';
import '../../data/models/employee_dashboard_response.dart';

part 'employee_dashboard_provider.g.dart';

@riverpod
class EmployeeDashboardNotifier extends _$EmployeeDashboardNotifier {
  @override
  FutureOr<EmployeeDashboardResponse?> build() async {
    final cache = CacheService.analyticsBox.get('employee_dashboard');
    if (cache != null) {
      try {
        final decoded = jsonDecode(cache);
        state = AsyncData(EmployeeDashboardResponse.fromJson(decoded));
      } catch (_) {}
    }
    return _fetch();
  }

  Future<EmployeeDashboardResponse> _fetch() async {
    final taskRepo = ref.read(taskRepositoryProvider);
    
    // Fetch all my tasks (large size to match Next.js logic)
    final data = await taskRepo.getMyTasks(size: 100);
    final List content = data['content'] ?? [];
    final tasks = content.map((e) => TaskModel.fromJson(e)).toList();

    // Calculate stats locally
    int todo = 0;
    int inProgress = 0;
    int inReview = 0;
    int completed = 0;

    for (final task in tasks) {
      switch (task.status) {
        case TaskStatus.todo:
          todo++;
          break;
        case TaskStatus.inProgress:
          inProgress++;
          break;
        case TaskStatus.inReview:
          inReview++;
          break;
        case TaskStatus.done:
          completed++;
          break;
        default:
          break;
      }
    }

    final dashboard = EmployeeDashboardResponse(
      totalTasks: tasks.length,
      todoTasks: todo,
      inProgressTasks: inProgress,
      inReviewTasks: inReview,
      completedTasks: completed,
      onTimeCompletionRate: 0.0,
      avgCompletionHours: 0.0,
    );

    CacheService.analyticsBox.put('employee_dashboard', jsonEncode(dashboard.toJson()));
    return dashboard;
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetch());
  }
}
