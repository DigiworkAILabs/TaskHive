import 'package:hive_flutter/hive_flutter.dart';

/// Hive-backed cache service. All boxes are opened in main.dart before runApp.
/// Phase 1 opens all boxes; each phase uses its own box.
class CacheService {
  static const _employeesBox = 'employees_box';
  static const _tasksBox = 'tasks_box';
  static const _myTasksBox = 'my_tasks_box';
  static const _notificationsBox = 'notifications_box';
  static const _analyticsBox = 'analytics_box';

  /// Raw box access for providers that need granular key-value caching
  static Box get notificationsBox => Hive.box(_notificationsBox);
  static Box get analyticsBox => Hive.box(_analyticsBox);

  // Employees (Phase 2)
  Future<void> cacheEmployees(List<Map<String, dynamic>> data) =>
      Hive.box(_employeesBox).put('list', data);

  List<Map<String, dynamic>>? getCachedEmployees() {
    final raw = Hive.box(_employeesBox).get('list');
    return _castList(raw);
  }

  // Tasks (Phase 3)
  Future<void> cacheTasks(List<Map<String, dynamic>> data) =>
      Hive.box(_tasksBox).put('list', data);

  List<Map<String, dynamic>>? getCachedTasks() =>
      _castList(Hive.box(_tasksBox).get('list'));

  Future<void> cacheMyTasks(List<Map<String, dynamic>> data) =>
      Hive.box(_myTasksBox).put('list', data);

  List<Map<String, dynamic>>? getCachedMyTasks() =>
      _castList(Hive.box(_myTasksBox).get('list'));

  // Notifications (Phase 4)
  Future<void> cacheNotifications(List<Map<String, dynamic>> data) =>
      Hive.box(_notificationsBox).put('list', data);

  List<Map<String, dynamic>>? getCachedNotifications() =>
      _castList(Hive.box(_notificationsBox).get('list'));

  // Analytics (Phase 6)
  Future<void> cacheAnalytics(Map<String, dynamic> data) =>
      Hive.box(_analyticsBox).put('dashboard', data);

  Map<String, dynamic>? getCachedAnalytics() {
    final raw = Hive.box(_analyticsBox).get('dashboard');
    if (raw == null) return null;
    return Map<String, dynamic>.from(raw as Map);
  }

  /// Called on logout — clears ALL cached data so no stale data persists between sessions
  Future<void> clearAll() => Future.wait([
        Hive.box(_employeesBox).clear(),
        Hive.box(_tasksBox).clear(),
        Hive.box(_myTasksBox).clear(),
        Hive.box(_notificationsBox).clear(),
        Hive.box(_analyticsBox).clear(),
      ]);

  List<Map<String, dynamic>>? _castList(dynamic raw) {
    if (raw == null) return null;
    return (raw as List)
        .map((e) => Map<String, dynamic>.from(e as Map))
        .toList();
  }
}
