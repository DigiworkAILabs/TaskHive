/// All route path constants for the entire app (all 6 phases).
/// Declared in Phase 1 — future phases only add new entries here.
class AppRoutes {
  AppRoutes._();

  // System
  static const String splash = '/splash';

  // Auth (public)
  static const String login = '/login';
  static const String forgotPassword = '/forgot-password';
  static const String resetPassword = '/reset-password';
  static const String activateAccount = '/activate-account';

  // Authenticated (both roles)
  static const String changePassword = '/change-password';

  // Admin shell branches
  static const String adminDashboard = '/admin/dashboard';
  static const String adminEmployees = '/admin/employees';
  static const String adminCreateEmployee = '/admin/employees/create';
  static const String adminEmployeeDetail = '/admin/employees/:id';
  static const String adminEditEmployee = '/admin/employees/:id/edit';
  static const String adminTasks = '/admin/tasks';
  static const String adminCreateTask = '/admin/tasks/create';
  static const String adminTaskDetail = '/admin/tasks/:id';
  static const String adminEditTask = '/admin/tasks/:id/edit';
  static const String adminAnalytics = '/admin/analytics';
  static const String adminAudit = '/admin/audit';
  static const String adminAuditEntityTimeline =
      '/admin/audit/entity/:type/:id';

  // Employee shell branches
  static const String employeeDashboard = '/employee/dashboard';
  static const String employeeTasks = '/employee/tasks';
  static const String employeeTaskDetail = '/employee/tasks/:id';
  static const String employeeNotifications = '/employee/notifications';
  static const String employeeNotificationPreferences =
      '/employee/notifications/preferences';
  static const String employeeProfile = '/employee/profile';

  // Helper: build parameterized paths at runtime
  static String adminEmployeeDetailPath(String id) => '/admin/employees/$id';
  static String adminEditEmployeePath(String id) => '/admin/employees/$id/edit';
  static String adminTaskDetailPath(String id) => '/admin/tasks/$id';
  static String adminEditTaskPath(String id) => '/admin/tasks/$id/edit';
  static String employeeTaskDetailPath(String id) => '/employee/tasks/$id';
  static String adminAuditEntityPath(String type, String id) =>
      '/admin/audit/entity/$type/$id';
}
