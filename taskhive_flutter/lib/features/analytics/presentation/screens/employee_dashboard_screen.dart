import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';


import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../../../core/widgets/stat_card.dart';
import '../../../auth/domain/providers/auth_provider.dart';
import '../../domain/providers/employee_dashboard_provider.dart';
import '../../data/models/task_distribution_response.dart';
import '../widgets/task_status_pie_chart.dart';

class _SectionHeader extends StatelessWidget {
  final String title;
  final String? subtitle;

  const _SectionHeader({required this.title, this.subtitle});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title,
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                )),
        if (subtitle != null) ...[
          const SizedBox(height: 4),
          Text(subtitle!,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Colors.grey,
                  )),
        ],
      ],
    );
  }
}

class EmployeeDashboardScreen extends ConsumerStatefulWidget {
  const EmployeeDashboardScreen({super.key});

  @override
  ConsumerState<EmployeeDashboardScreen> createState() => _EmployeeDashboardScreenState();
}

class _EmployeeDashboardScreenState extends ConsumerState<EmployeeDashboardScreen> {
  Future<void> _onRefresh() async {
    await ref.read(employeeDashboardNotifierProvider.notifier).refresh();
  }

  @override
  Widget build(BuildContext context) {
    final userState = ref.watch(authStateNotifierProvider);
    final user = userState.user;
    final firstName = user?.firstName ?? 'Employee';

    final dashboardState = ref.watch(employeeDashboardNotifierProvider);

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
      body: RefreshIndicator(
        onRefresh: _onRefresh,
        child: CustomScrollView(
          slivers: [
            SliverAppBar(
              floating: true,
              snap: true,
              title: Text('My Dashboard, $firstName'),
              actions: [
                IconButton(
                  icon: const Icon(Icons.logout),
                  tooltip: 'Logout',
                  onPressed: () => ref.read(authStateNotifierProvider.notifier).logout(),
                ),
                IconButton(
                  icon: const Icon(Icons.refresh),
                  tooltip: 'Refresh',
                  onPressed: _onRefresh,
                )
              ],
            ),
            SliverPadding(
              padding: const EdgeInsets.all(16.0),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  // Section 1: KPI Summary Row
                  dashboardState.when(
                    loading: () => const AppLoading(),
                    error: (e, _) => AppErrorWidget(message: e.toString()),
                    data: (data) {
                      if (data == null) return const AppEmptyState(message: 'No data');
                      
                      // Calculate overdue dynamically or conditionally based on active logic?
                      // Wait, EmployeeDashboardResponse doesn't explicitly return `overdueTasks`.
                      // The Next.js code did not have it. BUT the Phase 6 spec says:
                      // "My Overdue (red)"
                      // Let's check employee_dashboard_response.dart: We only have:
                      // totalTasks, todoTasks, inProgressTasks, inReviewTasks, completedTasks, onTimeCompletionRate, avgCompletionHours.
                      // Wait! In Phase 6 spec "Section 1 - Personal KPI Row: My Total Tasks | My Active | My Completed | My Overdue (red) | On-Time Rate | Avg Hours"
                      // Since Next.js frontend didn't have overdrive, we will adapt based on available data.
                      
                      final active = (data.todoTasks ?? 0) + (data.inProgressTasks ?? 0) + (data.inReviewTasks ?? 0);

                      return SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            StatCard(
                              title: 'Total Tasks',
                              value: (data.totalTasks ?? 0).toString(),
                              icon: Icons.list_alt,
                              color: const Color(0xFF6366F1), // Indigo
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Active',
                              value: active.toString(),
                              icon: Icons.play_arrow,
                              color: const Color(0xFF3B82F6), // Blue
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Completed',
                              value: (data.completedTasks ?? 0).toString(),
                              icon: Icons.check_circle_outline,
                              color: const Color(0xFF22C55E), // Green
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'On-Time Rate',
                              value: '${(data.onTimeCompletionRate ?? 0).toStringAsFixed(1)}%',
                              icon: Icons.trending_up,
                              color: const Color(0xFFF97316), // Orange
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Avg Hours',
                              value: '${(data.avgCompletionHours ?? 0).toStringAsFixed(1)} h',
                              icon: Icons.access_time,
                              color: const Color(0xFFA855F7), // Purple
                            ),
                          ],
                        ),
                      );
                    },
                  ),

                  // Section 3: Overdue Alert Banner
                  // As EmployeeDashboardData doesn't return overdueTasks per Step 3 contract,
                  // we'll hide it. Phase 6 spec said 'if myOverdueTasks > 0'. Since we don't have it, it's 0.

                  const SizedBox(height: 24),

                  // Section 2: Task Status Pie
                  const _SectionHeader(title: 'My Task Status', subtitle: 'Detailed breakdown'),
                  const SizedBox(height: 16),
                  dashboardState.when(
                    loading: () => const AppLoading(),
                    error: (e, _) => AppErrorWidget(message: e.toString()),
                    data: (data) {
                      if (data == null) return const SizedBox.shrink();
                      
                      // Convert dashboard payload to List<TaskDistributionResponse> to reuse widget
                      final mockDistribution = [
                        TaskDistributionResponse(status: 'TODO', count: data.todoTasks ?? 0),
                        TaskDistributionResponse(status: 'IN_PROGRESS', count: data.inProgressTasks ?? 0),
                        TaskDistributionResponse(status: 'IN_REVIEW', count: data.inReviewTasks ?? 0),
                        TaskDistributionResponse(status: 'DONE', count: data.completedTasks ?? 0),
                      ].where((e) => e.count != null && e.count! > 0).toList(); // only show >0
                      
                      return TaskStatusPieChart(
                        dataState: AsyncData(mockDistribution),
                        height: 180,
                      );
                    },
                  ),

                  const SizedBox(height: 24),

                  const SizedBox(height: 24),
                  
                  const SizedBox(height: 24),
                  AppButton(
                    label: 'View My Tasks',
                    onPressed: () => context.go('/employee/tasks'),
                  ),
                  const SizedBox(height: 32),
                ]),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
