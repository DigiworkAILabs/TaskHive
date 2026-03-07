/// All API endpoint paths for the entire app (all phases).
/// Declared in Phase 1 so imports never need to change in later phases.
class ApiEndpoints {
  ApiEndpoints._();

  // BaseURL is set in DioClient

  // Auth
  static const String login = '/auth/login';
  static const String logout = '/auth/logout';
  static const String refresh = '/auth/refresh';
  static const String forgotPassword = '/auth/forgot-password';
  static const String resetPassword = '/auth/reset-password';
  static const String activateAccount = '/auth/activate-account';
  static const String changePassword = '/auth/change-password';
  static const String me = '/auth/me';

  // Employee (Phase 2)
  static const String employees = '/employees';
  static String employeeById(String id) => '/employees/$id';
  static String employeeActivate(String id) => '/employees/$id/activate';
  static String employeeDeactivate(String id) => '/employees/$id/deactivate';
  static String employeePhoto(String id) => '/employees/$id/photo';
  static String employeeProfile(String id) => '/employees/$id/profile';
  static const String employeeSearch = '/employees/search';

  // Task (Phase 3)
  static const String tasks = '/tasks';
  static String taskById(String id) => '/tasks/$id';
  static String taskStatus(String id) => '/tasks/$id/status';
  static String taskComments(String id) => '/tasks/$id/comments';
  static String taskAttachments(String id) => '/tasks/$id/attachments';
  static String taskHistory(String id) => '/tasks/$id/history';
  static const String myTasks = '/tasks/my-tasks';
  static const String overdueTasks = '/tasks/overdue';
  static const String taskSearch = '/tasks/search';

  // Notification (Phase 4)
  static const String notifications = '/notifications';
  static const String notificationsUnread = '/notifications/unread';
  static const String notificationsUnreadCount = '/notifications/unread-count';
  static String notificationMarkRead(String id) => '/notifications/$id/read';
  static const String notificationsMarkAllRead = '/notifications/mark-all-read';
  static const String notificationPreferences = '/notifications/preferences';

  // Analytics (Phase 6)
  static const String adminDashboard = '/analytics/dashboard/admin';
  static const String employeeDashboard = '/analytics/dashboard/employee';
  static const String taskDistribution = '/analytics/tasks/distribution';
  static const String tasksByPriority = '/analytics/tasks/by-priority';
  static const String completionTrend = '/analytics/tasks/completion-trend';
  static const String employeePerformance = '/analytics/employees/performance';
  static const String reportsExport = '/analytics/reports/export';
  static String reportDownload(String id) => '/analytics/reports/$id/download';

  // Audit (Phase 5)
  static const String auditLogs = '/audit/logs';
  static const String auditLogsSearch = '/audit/logs/search';
  static String auditLogsByEntity(String type, String id) =>
      '/audit/logs/entity/$type/$id';
  static const String securityEvents = '/audit/security-events';
  static const String complianceReport = '/audit/compliance/report';
}
