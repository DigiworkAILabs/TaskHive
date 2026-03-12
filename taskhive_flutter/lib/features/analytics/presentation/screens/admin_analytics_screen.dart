import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../auth/domain/providers/auth_provider.dart';

import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../../../core/widgets/responsive_layout.dart';
import '../../domain/providers/admin_dashboard_provider.dart';
import '../../domain/providers/employee_performance_provider.dart';
import '../../domain/providers/task_completion_trend_provider.dart';
import '../../domain/providers/task_distribution_provider.dart';
import '../../data/models/admin_dashboard_response.dart';
import '../../data/models/employee_performance_response.dart';
import '../../data/models/task_completion_trend_response.dart';
import '../widgets/admin_kpi_row.dart';
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
  ConsumerState<AdminAnalyticsScreen> createState() =>
      _AdminAnalyticsScreenState();
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
                  onPressed: () =>
                      ref.read(authStateNotifierProvider.notifier).logout(),
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
              sliver: SliverToBoxAdapter(
                child: ResponsiveLayout(
                  mobile: Column(
                    children: _buildMobileLayout(
                      dashboardState,
                      distributionState,
                      trendState,
                      performanceState,
                    ),
                  ),
                  tablet: Column(
                    children: _buildMobileLayout(
                      dashboardState,
                      distributionState,
                      trendState,
                      performanceState,
                    ),
                  ),
                  desktop: _buildDesktopLayout(
                    dashboardState,
                    distributionState,
                    trendState,
                    performanceState,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildMobileLayout(
    AsyncValue<AdminDashboardResponse?> dashboardState,
    AsyncValue<TaskDistributionState> distributionState,
    AsyncValue<List<TaskCompletionTrendResponse>> trendState,
    AsyncValue<List<EmployeePerformanceResponse>> performanceState,
  ) {
    return [
      // Section 1: KPI Summary Row
      dashboardState.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(message: e.toString()),
        data: (data) => data == null
            ? const AppEmptyState(message: 'No data')
            : AdminKpiRow(data: data),
      ),

      const SizedBox(height: 24),

      // Section 2: Task Distribution
      const _SectionHeader(title: 'Task Distribution'),
      const SizedBox(height: 16),
      TaskStatusPieChart(
        dataState: distributionState.whenData((v) => v.byStatus),
        height: 250,
      ),
      const SizedBox(height: 16),
      TaskPriorityDonutChart(
        dataState: distributionState.whenData((v) => v.byPriority),
        height: 250,
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
    ];
  }

  Widget _buildDesktopLayout(
    AsyncValue<AdminDashboardResponse?> dashboardState,
    AsyncValue<TaskDistributionState> distributionState,
    AsyncValue<List<TaskCompletionTrendResponse>> trendState,
    AsyncValue<List<EmployeePerformanceResponse>> performanceState,
  ) {
    return Column(
      children: [
        // Full width KPI Row across top
        dashboardState.when(
          loading: () => const AppLoading(),
          error: (e, _) => AppErrorWidget(message: e.toString()),
          data: (data) => data == null
              ? const AppEmptyState(message: 'No data')
              : AdminKpiRow(data: data),
        ),
        const SizedBox(height: 32),
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // LEFT COLUMN (50%)
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionHeader(title: 'Status Distribution'),
                  const SizedBox(height: 16),
                  TaskStatusPieChart(
                    dataState: distributionState.whenData((v) => v.byStatus),
                    height: 300,
                  ),
                  const SizedBox(height: 32),
                  const _SectionHeader(
                      title: 'Completion Trend',
                      subtitle: 'Tasks completed over 30 days'),
                  const SizedBox(height: 16),
                  CompletionTrendLineChart(dataState: trendState, height: 300),
                ],
              ),
            ),
            const SizedBox(width: 32),
            // RIGHT COLUMN (50%)
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionHeader(title: 'Priority Distribution'),
                  const SizedBox(height: 16),
                  TaskPriorityDonutChart(
                    dataState: distributionState.whenData((v) => v.byPriority),
                    height: 300,
                  ),
                  const SizedBox(height: 32),
                  const _SectionHeader(
                      title: 'Employee Performance',
                      subtitle: 'Individual completions'),
                  const SizedBox(height: 16),
                  EmployeePerformanceBarChart(
                      dataState: performanceState, height: 220),
                  const SizedBox(height: 16),
                  EmployeePerformanceTable(dataState: performanceState),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 32),
      ],
    );
  }
}
