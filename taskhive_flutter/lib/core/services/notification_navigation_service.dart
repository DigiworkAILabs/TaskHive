import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../features/auth/domain/providers/auth_provider.dart';
import '../router/app_router.dart';
import '../router/app_routes.dart';

final notificationNavigationServiceProvider =
    Provider<NotificationNavigationService>((ref) {
  final router = ref.watch(appRouterProvider);
  final authState = ref.watch(authStateNotifierProvider);
  return NotificationNavigationService(
      router, authState.user?.role ?? 'EMPLOYEE');
});

class NotificationNavigationService {
  final GoRouter _router;
  final String _userRole;

  NotificationNavigationService(this._router, this._userRole);

  void navigateFromNotification(Map<String, dynamic> data) {
    final entityType = data['entityType'];
    final entityId = data['entityId'];

    if (entityId == null) {
      if (_userRole == 'ADMIN') {
        _router.go(AppRoutes.adminNotifications);
      } else {
        _router.go(AppRoutes.employeeNotifications);
      }
      return;
    }

    // Use `go` instead of `push` because the target routes live inside
    // StatefulShellRoute branches. Using `push` from outside the shell
    // creates a duplicate Navigator GlobalKey error.
    switch (entityType) {
      case 'TASK':
        if (_userRole == 'ADMIN') {
          _router.go('/admin/tasks/$entityId');
        } else {
          _router.go('/employee/tasks/$entityId');
        }
        break;
      case 'EMPLOYEE':
        if (_userRole == 'ADMIN') {
          _router.go('/admin/employees/$entityId');
        }
        break;
      case 'SYSTEM':
      default:
        if (_userRole == 'ADMIN') {
          _router.go(AppRoutes.adminNotifications);
        } else {
          _router.go(AppRoutes.employeeNotifications);
        }
        break;
    }
  }
}
