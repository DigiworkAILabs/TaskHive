import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../notification/presentation/widgets/notification_badge.dart';

class EmployeeShellScreen extends StatelessWidget {
  final StatefulNavigationShell navigationShell;
  const EmployeeShellScreen({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: navigationShell.currentIndex,
        onDestinationSelected: (i) => navigationShell.goBranch(
          i,
          initialLocation: i == navigationShell.currentIndex,
        ),
        destinations: const [
          NavigationDestination(
              icon: Icon(Icons.dashboard_outlined),
              selectedIcon: Icon(Icons.dashboard),
              label: 'Dashboard'),
          NavigationDestination(
              icon: Icon(Icons.task_alt_outlined),
              selectedIcon: Icon(Icons.task_alt),
              label: 'My Tasks'),
          NavigationDestination(
              icon:
                  NotificationBadge(child: Icon(Icons.notifications_outlined)),
              selectedIcon: NotificationBadge(child: Icon(Icons.notifications)),
              label: 'Notifications'),
          NavigationDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person),
              label: 'Profile'),
        ],
      ),
    );
  }
}
