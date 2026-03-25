import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/widgets/responsive_layout.dart';
import '../../../auth/domain/providers/auth_provider.dart';
import '../../../ml/presentation/widgets/ml_feature_toggle.dart';

class AdminShellScreen extends ConsumerWidget {
  final StatefulNavigationShell navigationShell;
  const AdminShellScreen({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sharedAppBar = AppBar(
      title: const Text('TaskHive Admin'),
      actions: const [
        Padding(
          padding: EdgeInsets.symmetric(horizontal: 16.0),
          child: MlFeatureToggleWidget(),
        ),
      ],
    );

    return LayoutBuilder(
      builder: (context, constraints) {
        final isDesktop = constraints.maxWidth >= AppBreakpoints.desktop;

        if (isDesktop) {
          return Scaffold(
            appBar: sharedAppBar,
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
                        icon: Icon(Icons.people_outline),
                        selectedIcon: Icon(Icons.people),
                        label: Text('Employees'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.task_alt_outlined),
                        selectedIcon: Icon(Icons.task_alt),
                        label: Text('Tasks'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.bar_chart_outlined),
                        selectedIcon: Icon(Icons.bar_chart),
                        label: Text('Analytics'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.security_outlined),
                        selectedIcon: Icon(Icons.security),
                        label: Text('Audit'),
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
          appBar: sharedAppBar,
          drawer: Drawer(
            child: Column(
              children: [
                const DrawerHeader(
                  decoration: BoxDecoration(color: Colors.orange),
                  child: Center(
                    child: Text(
                      'System Settings',
                      style: TextStyle(color: Colors.white, fontSize: 20),
                    ),
                  ),
                ),
                Expanded(
                  child: ListView(
                    padding: const EdgeInsets.all(16.0),
                    children: const [], // Toggles moved to AppBar
                  ),
                ),
                const Divider(),
                ListTile(
                  leading: const Icon(Icons.logout, color: Colors.red),
                  title:
                      const Text('Logout', style: TextStyle(color: Colors.red)),
                  onTap: () {
                    ref.read(authStateNotifierProvider.notifier).logout();
                  },
                ),
              ],
            ),
          ),
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
                  icon: Icon(Icons.people_outline),
                  selectedIcon: Icon(Icons.people),
                  label: 'Employees'),
              NavigationDestination(
                  icon: Icon(Icons.task_alt_outlined),
                  selectedIcon: Icon(Icons.task_alt),
                  label: 'Tasks'),
              NavigationDestination(
                  icon: Icon(Icons.bar_chart_outlined),
                  selectedIcon: Icon(Icons.bar_chart),
                  label: 'Analytics'),
              NavigationDestination(
                  icon: Icon(Icons.security_outlined),
                  selectedIcon: Icon(Icons.security),
                  label: 'Audit'),
            ],
          ),
        );
      },
    );
  }
}
