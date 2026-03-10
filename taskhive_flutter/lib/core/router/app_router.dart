import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/domain/providers/auth_provider.dart';
import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/auth/presentation/screens/forgot_password_screen.dart';
import '../../features/auth/presentation/screens/reset_password_screen.dart';
import '../../features/auth/presentation/screens/activate_account_screen.dart';
import '../../features/auth/presentation/screens/change_password_screen.dart';
import '../../features/admin/presentation/screens/admin_shell_screen.dart';
import '../../features/employee/presentation/screens/employee_shell_screen.dart';

import '../../features/employee/presentation/screens/admin/employee_list_screen.dart';
import '../../features/employee/presentation/screens/admin/employee_detail_screen.dart';
import '../../features/employee/presentation/screens/admin/create_employee_screen.dart';
import '../../features/employee/presentation/screens/admin/edit_employee_screen.dart';
import '../../features/employee/presentation/screens/employee/my_profile_screen.dart';

import '../../features/task/presentation/screens/admin/admin_task_list_screen.dart';
import '../../features/task/presentation/screens/admin/admin_task_detail_screen.dart';
import '../../features/task/presentation/screens/admin/create_task_screen.dart';
import '../../features/task/presentation/screens/admin/edit_task_screen.dart';
import '../../features/task/presentation/screens/employee/my_tasks_screen.dart';
import '../../features/task/presentation/screens/employee/employee_task_detail_screen.dart';

import '../../features/notification/presentation/screens/notification_list_screen.dart';
import '../../features/notification/presentation/screens/notification_preferences_screen.dart';
import '../../features/notification/presentation/widgets/notification_badge.dart';
import '../../features/audit/presentation/screens/audit_screen.dart';
import '../../features/audit/presentation/screens/entity_timeline_screen.dart';

import 'app_routes.dart';

// ── Placeholder screens (replaced phase by phase) ──────────────────────────

class _PlaceholderScreen extends StatelessWidget {
  final String title;
  final bool showNotificationBell;
  const _PlaceholderScreen({
    required this.title,
    this.showNotificationBell = false,
  });

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: Text(title),
          actions: [
            if (showNotificationBell)
              NotificationBadge(
                child: IconButton(
                  icon: const Icon(Icons.notifications_outlined),
                  tooltip: 'Notifications',
                  onPressed: () => context.push('/admin/notifications'),
                ),
              ),
          ],
        ),
        body: Center(
          child: Text('$title\n(Coming in a future phase)',
              textAlign: TextAlign.center),
        ),
      );
}

class _SplashScreen extends StatelessWidget {
  const _SplashScreen();
  @override
  Widget build(BuildContext context) => const Scaffold(
        body: Center(child: CircularProgressIndicator.adaptive()),
      );
}

// ── Router Provider ─────────────────────────────────────────────────────────

final appRouterProvider = Provider<GoRouter>((ref) {
  // ValueNotifier bridges Riverpod state changes to GoRouter's refreshListenable
  final authNotifier = ValueNotifier<int>(0);
  ref.listen<AuthState>(authStateNotifierProvider, (_, __) {
    authNotifier.value++;
  });

  return GoRouter(
    initialLocation: AppRoutes.splash,
    refreshListenable: authNotifier,
    redirect: (context, state) {
      // IMPORTANT: Use ref.read — never ref.watch inside redirect
      final authState = ref.read(authStateNotifierProvider);
      final path = state.uri.path;

      final publicRoutes = [
        AppRoutes.login,
        AppRoutes.forgotPassword,
        AppRoutes.resetPassword,
        AppRoutes.activateAccount,
      ];
      final isPublic = publicRoutes.any((r) => path.startsWith(r));
      final isDeeplink = path.startsWith(AppRoutes.resetPassword) ||
          path.startsWith(AppRoutes.activateAccount);

      // While session restore is in progress → show splash (no redirect)
      if (authState.isLoading) {
        return path == AppRoutes.splash ? null : AppRoutes.splash;
      }

      final authenticated = authState.isAuthenticated;
      final role = authState.user?.role; // 'ADMIN' or 'EMPLOYEE'

      // Not authenticated and on a protected route → login
      if (!authenticated && !isPublic) return AppRoutes.login;

      // Authenticated and on a plain public route → their dashboard
      if (authenticated && isPublic && !isDeeplink) {
        return role == 'ADMIN'
            ? AppRoutes.adminDashboard
            : AppRoutes.employeeDashboard;
      }

      // Wrong role for shell route
      if (authenticated && path.startsWith('/admin') && role != 'ADMIN') {
        return AppRoutes.employeeDashboard;
      }
      if (authenticated && path.startsWith('/employee') && role != 'EMPLOYEE') {
        return AppRoutes.adminDashboard;
      }

      // Splash but loading is done → route to correct destination
      if (path == AppRoutes.splash && !authState.isLoading) {
        if (!authenticated) return AppRoutes.login;
        return role == 'ADMIN'
            ? AppRoutes.adminDashboard
            : AppRoutes.employeeDashboard;
      }

      return null; // No redirect needed
    },
    routes: [
      // Splash
      GoRoute(
        path: AppRoutes.splash,
        builder: (_, __) => const _SplashScreen(),
      ),

      // Auth — public
      GoRoute(path: AppRoutes.login, builder: (_, __) => const LoginScreen()),
      GoRoute(
          path: AppRoutes.forgotPassword,
          builder: (_, __) => const ForgotPasswordScreen()),
      GoRoute(
        path: AppRoutes.resetPassword,
        builder: (_, state) => ResetPasswordScreen(
            token: state.uri.queryParameters['token'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.activateAccount,
        builder: (_, state) => ActivateAccountScreen(
            token: state.uri.queryParameters['token'] ?? ''),
      ),

      // Authenticated — both roles
      GoRoute(
          path: AppRoutes.changePassword,
          builder: (_, __) => const ChangePasswordScreen()),

      // ── Admin Shell (StatefulShellRoute — go_router v14) ──────────────
      StatefulShellRoute.indexedStack(
        builder: (_, __, shell) => AdminShellScreen(navigationShell: shell),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminDashboard,
              builder: (_, __) => const _PlaceholderScreen(
                  title: 'Admin Dashboard', showNotificationBell: true),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminEmployees,
              builder: (_, __) => const EmployeeListScreen(),
              routes: [
                GoRoute(
                  path: 'create',
                  builder: (_, __) => const CreateEmployeeScreen(),
                ),
                GoRoute(
                  path: ':id',
                  builder: (_, state) => EmployeeDetailScreen(
                    employeeId: state.pathParameters['id']!,
                  ),
                ),
                GoRoute(
                  path: ':id/edit',
                  builder: (_, state) => EditEmployeeScreen(
                    employeeId: state.pathParameters['id']!,
                  ),
                ),
              ],
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminTasks,
              builder: (_, __) => const AdminTaskListScreen(),
              routes: [
                GoRoute(
                  path: 'create',
                  builder: (_, __) => const CreateTaskScreen(),
                ),
                GoRoute(
                  path: ':id',
                  builder: (_, state) => AdminTaskDetailScreen(
                    taskId: state.pathParameters['id']!,
                  ),
                ),
                GoRoute(
                  path: ':id/edit',
                  builder: (_, state) => EditTaskScreen(
                    taskId: state.pathParameters['id']!,
                  ),
                ),
              ],
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminAnalytics,
              builder: (_, __) => const _PlaceholderScreen(
                  title: 'Analytics', showNotificationBell: true),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminAudit,
              builder: (_, __) => const AuditScreen(),
              routes: [
                GoRoute(
                  path: 'entity/:type/:id',
                  builder: (_, state) => EntityTimelineScreen(
                    entityType: state.pathParameters['type']!,
                    entityId: state.pathParameters['id']!,
                  ),
                ),
              ],
            ),
          ]),
        ],
      ),

      // ── Employee Shell ─────────────────────────────────────────────────
      StatefulShellRoute.indexedStack(
        builder: (_, __, shell) => EmployeeShellScreen(navigationShell: shell),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeDashboard,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'My Dashboard'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeTasks,
              builder: (_, __) => const MyTasksScreen(),
              routes: [
                GoRoute(
                  path: ':id',
                  builder: (_, state) => EmployeeTaskDetailScreen(
                    taskId: state.pathParameters['id']!,
                  ),
                ),
              ],
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeNotifications,
              builder: (_, __) => const NotificationListScreen(),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeProfile,
              builder: (_, __) => const MyProfileScreen(),
            ),
          ]),
        ],
      ),

      // ── Push Routes (Outside Shell) ─────────────────────────────────────────────────
      GoRoute(
        path: AppRoutes.adminNotifications,
        builder: (_, __) => const NotificationListScreen(),
      ),
      GoRoute(
        path: AppRoutes.employeeNotificationPreferences,
        builder: (_, __) => const NotificationPreferencesScreen(),
      ),
    ],
  );
});
