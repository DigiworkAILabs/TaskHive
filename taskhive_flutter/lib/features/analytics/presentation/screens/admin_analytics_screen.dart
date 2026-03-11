import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../auth/domain/providers/auth_provider.dart';


import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../../../core/widgets/stat_card.dart';
import '../../domain/providers/admin_dashboard_provider.dart';
import '../../domain/providers/employee_performance_provider.dart';
import '../../domain/providers/task_completion_trend_provider.dart';
import '../../domain/providers/task_distribution_provider.dart';
import '../widgets/completion_trend_line_chart.dart';
import '../widgets/employee_performance_bar_chart.dart';
import '../widgets/employee_performance_table.dart';
import '../widgets/report_export_sheet.dart';
import '../widgets/task_priority_donut_chart.dart';
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

class AdminAnalyticsScreen extends ConsumerStatefulWidget {
  const AdminAnalyticsScreen({super.key});

  @override
  ConsumerState<AdminAnalyticsScreen> createState() => _AdminAnalyticsScreenState();
}

class _AdminAnalyticsScreenState extends ConsumerState<AdminAnalyticsScreen> {
  Future<void> _onRefresh() async {
    await Future.wait([
      ref.read(adminDashboardNotifierProvider.notifier).refresh(),
      ref.read(taskDistributionNotifierProvider.notifier).refresh(),
      ref.read(taskCompletionTrendNotifierProvider.notifier).refresh(),
      ref.read(employeePerformanceNotifierProvider.notifier).refresh(),
    ]);
  }

  void _showExportSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) => const ReportExportSheet(),
    );
  }

  @override
  Widget build(BuildContext context) {
    final dashboardState = ref.watch(adminDashboardNotifierProvider);
    final distributionState = ref.watch(taskDistributionNotifierProvider);
    final trendState = ref.watch(taskCompletionTrendNotifierProvider);
    final performanceState = ref.watch(employeePerformanceNotifierProvider);

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
      body: RefreshIndicator(
        onRefresh: _onRefresh,
        child: CustomScrollView(
          slivers: [
            SliverAppBar(
              floating: true,
              snap: true,
              title: const Text('Analytics'),
              actions: [
                IconButton(
                  icon: const Icon(Icons.logout),
                  tooltip: 'Logout',
                  onPressed: () => ref.read(authStateNotifierProvider.notifier).logout(),
                ),
                IconButton(
                  icon: const Icon(Icons.file_download_outlined),
                  tooltip: 'Export Report',
                  onPressed: _showExportSheet,
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
                  // Section 1: Full KPI Row
                  dashboardState.when(
                    loading: () => const AppLoading(),
                    error: (e, _) => AppErrorWidget(message: e.toString()),
                    data: (data) {
                      if (data == null) return const AppEmptyState(message: 'No data');
                      return SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            StatCard(
                              title: 'Total Tasks',
                              value: (data.totalTasks ?? 0).toString(),
                              icon: Icons.list_alt,
                              color: const Color(0xFF6366F1),
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Active',
                              value: (data.activeTasks ?? 0).toString(),
                              icon: Icons.play_arrow,
                              color: const Color(0xFF3B82F6),
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Overdue',
                              value: (data.overdueTasks ?? 0).toString(),
                              icon: Icons.warning_amber_rounded,
                              color: const Color(0xFFEF4444),
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Completed',
                              value: (data.completedTasks ?? 0).toString(),
                              icon: Icons.check_circle_outline,
                              color: const Color(0xFF22C55E),
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Completion Rate',
                              value: '${(data.completionRate ?? 0).toStringAsFixed(1)}%',
                              icon: Icons.trending_up,
                              color: const Color(0xFFF97316),
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Total Employees',
                              value: (data.totalEmployees ?? 0).toString(),
                              icon: Icons.people_outline,
                              color: const Color(0xFF06B6D4),
                            ),
                            const SizedBox(width: 12),
                            StatCard(
                              title: 'Avg Completion',
                              value: '${(data.avgCompletionHours ?? 0).toStringAsFixed(1)} h',
                              icon: Icons.access_time,
                              color: const Color(0xFFA855F7), // Purple
                            ),
                          ],
                        ),
                      );
                    },
                  ),

                  const SizedBox(height: 24),

                  // Section 2: Task Distribution
                  const _SectionHeader(title: 'Task Distribution'),
                  const SizedBox(height: 16),
                  LayoutBuilder(
                    builder: (context, constraints) {
                      if (constraints.maxWidth > 500) {
                        return Row(
                          children: [
                            Expanded(
                              child: TaskStatusPieChart(
                                dataState: distributionState.whenData((v) => v.byStatus),
                                height: 300,
                              ),
                            ),
                            const SizedBox(width: 16),
                            Expanded(
                              child: TaskPriorityDonutChart(
                                dataState: distributionState.whenData((v) => v.byPriority),
                                height: 300,
                              ),
                            ),
                          ],
                        );
                      }
                      return Column(
                        children: [
                          TaskStatusPieChart(
                            dataState: distributionState.whenData((v) => v.byStatus),
                            height: 250, // Slightly larger for full analytics screen
                          ),
                          const SizedBox(height: 16),
                          TaskPriorityDonutChart(
                            dataState: distributionState.whenData((v) => v.byPriority),
                            height: 250,
                          ),
                        ],
                      );
                    },
                  ),

                  const SizedBox(height: 24),

                  // Section 3: 30-Day Completion Trend
                  const _SectionHeader(
                      title: 'Completion Trend', subtitle: 'Tasks completed over 30 days'),
                  const SizedBox(height: 16),
                  CompletionTrendLineChart(dataState: trendState, height: 250),

                  const SizedBox(height: 24),

                  // Section 4: Employee Performance
                  const _SectionHeader(
                      title: 'Employee Performance',
                      subtitle: 'Individual completions and metrics'),
                  const SizedBox(height: 16),
                  EmployeePerformanceBarChart(dataState: performanceState, height: 220),
                  const SizedBox(height: 16),
                  EmployeePerformanceTable(dataState: performanceState),

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
