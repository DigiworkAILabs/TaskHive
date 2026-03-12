import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../notification/presentation/widgets/notification_badge.dart';
import '../../../../core/widgets/responsive_layout.dart';

class EmployeeShellScreen extends StatelessWidget {
  final StatefulNavigationShell navigationShell;
  const EmployeeShellScreen({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isDesktop = constraints.maxWidth >= AppBreakpoints.desktop;

        if (isDesktop) {
          return Scaffold(
            body: Row(
              children: [
                NavigationRail(
                  selectedIndex: navigationShell.currentIndex,
                  onDestinationSelected: (i) => navigationShell.goBranch(
                    i,
                    initialLocation: i == navigationShell.currentIndex,
                  ),
                  labelType: NavigationRailLabelType.all,
                  destinations: const [
                    NavigationRailDestination(
                      icon: Icon(Icons.dashboard_outlined),
                      selectedIcon: Icon(Icons.dashboard),
                      label: Text('Dashboard'),
                    ),
                    NavigationRailDestination(
                      icon: Icon(Icons.task_alt_outlined),
                      selectedIcon: Icon(Icons.task_alt),
                      label: Text('My Tasks'),
                    ),
                    NavigationRailDestination(
                      icon: NotificationBadge(
                          child: Icon(Icons.notifications_outlined)),
                      selectedIcon: NotificationBadge(
                          child: Icon(Icons.notifications)),
                      label: Text('Notifications'),
                    ),
                    NavigationRailDestination(
                      icon: Icon(Icons.person_outline),
                      selectedIcon: Icon(Icons.person),
                      label: Text('Profile'),
                    ),
                  ],
                ),
                const VerticalDivider(thickness: 1, width: 1),
                Expanded(child: navigationShell),
              ],
            ),
          );
        }

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
                  icon: NotificationBadge(
                      child: Icon(Icons.notifications_outlined)),
                  selectedIcon:
                      NotificationBadge(child: Icon(Icons.notifications)),
                  label: 'Notifications'),
              NavigationDestination(
                  icon: Icon(Icons.person_outline),
                  selectedIcon: Icon(Icons.person),
                  label: 'Profile'),
            ],
          ),
        );
      },
    );
  }
}
