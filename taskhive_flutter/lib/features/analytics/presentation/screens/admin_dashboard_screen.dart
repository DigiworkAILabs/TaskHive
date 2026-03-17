import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../../../core/widgets/responsive_layout.dart';
import '../../../audit/domain/providers/audit_log_list_provider.dart';
import '../../../auth/domain/providers/auth_provider.dart';
import '../../domain/providers/admin_dashboard_provider.dart';
import '../../domain/providers/employee_performance_provider.dart';
import '../../domain/providers/task_distribution_provider.dart';
import '../widgets/admin_kpi_row.dart';
import '../widgets/task_priority_donut_chart.dart';
import '../widgets/task_status_pie_chart.dart';
import '../../../ml/presentation/widgets/ml_feature_toggle.dart';

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

class AdminDashboardScreen extends ConsumerStatefulWidget {
  const AdminDashboardScreen({super.key});

  @override
  ConsumerState<AdminDashboardScreen> createState() =>
      _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends ConsumerState<AdminDashboardScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(auditLogListNotifierProvider.notifier).refresh();
    });
  }

  Future<void> _onRefresh() async {
    await Future.wait([
      ref.read(adminDashboardNotifierProvider.notifier).refresh(),
      ref.read(taskDistributionNotifierProvider.notifier).refresh(),
      ref.read(employeePerformanceNotifierProvider.notifier).refresh(),
      ref.read(auditLogListNotifierProvider.notifier).refresh(),
    ]);
  }

  @override
  Widget build(BuildContext context) {
    final userState = ref.watch(authStateNotifierProvider);
    final user = userState.user;
    final firstName = user?.firstName ?? 'Admin';
    final formattedDate = DateFormat('EEEE, MMM d').format(DateTime.now());

    final dashboardState = ref.watch(adminDashboardNotifierProvider);
    final distributionState = ref.watch(taskDistributionNotifierProvider);
    final performanceState = ref.watch(employeePerformanceNotifierProvider);
    final auditLogsState = ref.watch(auditLogListNotifierProvider);

    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
      body: RefreshIndicator(
        onRefresh: _onRefresh,
        child: CustomScrollView(
          slivers: [
            SliverAppBar(
              floating: true,
              snap: true,
              title: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Good morning, $firstName'),
                  Text(
                    formattedDate,
                    style: const TextStyle(
                        fontSize: 12, fontWeight: FontWeight.normal),
                  ),
                ],
              ),
              actions: [
                const MlFeatureToggleWidget(),
                IconButton(
                  icon: const Icon(Icons.logout),
                  tooltip: 'Logout',
                  onPressed: () =>
                      ref.read(authStateNotifierProvider.notifier).logout(),
                ),
                IconButton(
                  icon: const Icon(Icons.refresh),
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
                      performanceState,
                      auditLogsState,
                    ),
                  ),
                  tablet: Column(
                    children: _buildMobileLayout(
                      dashboardState,
                      distributionState,
                      performanceState,
                      auditLogsState,
                    ),
                  ),
                  desktop: _buildDesktopLayout(
                    dashboardState,
                    distributionState,
                    performanceState,
                    auditLogsState,
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
    AsyncValue dashboardState,
    AsyncValue distributionState,
    AsyncValue performanceState,
    AsyncValue auditLogsState,
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

      // Section 3: Overdue Alert Banner
      _buildOverdueAlert(dashboardState),

      const SizedBox(height: 24),

      // Section 2: Quick Glance Charts
      const _SectionHeader(
          title: 'Task Distribution', subtitle: 'At a glance overview'),
      const SizedBox(height: 16),
      TaskStatusPieChart(
        dataState: distributionState.whenData((v) => v.byStatus),
        height: 180,
      ),
      const SizedBox(height: 16),
      TaskPriorityDonutChart(
        dataState: distributionState.whenData((v) => v.byPriority),
        height: 180,
      ),

      const SizedBox(height: 24),

      // Section 5: Top Performers
      const _SectionHeader(
          title: 'Top Performers', subtitle: 'By tasks completed'),
      const SizedBox(height: 16),
      _buildTopPerformers(performanceState),

      const SizedBox(height: 24),

      // Section 4: Recent Activity
      const _SectionHeader(
          title: 'Recent Activity', subtitle: 'Last 5 system actions'),
      const SizedBox(height: 16),
      _buildRecentActivity(auditLogsState),
      const SizedBox(height: 32),
    ];
  }

  Widget _buildDesktopLayout(
    AsyncValue dashboardState,
    AsyncValue distributionState,
    AsyncValue performanceState,
    AsyncValue auditLogsState,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // LEFT COLUMN (40%)
            Expanded(
              flex: 4,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionHeader(title: 'Overview'),
                  const SizedBox(height: 16),
                  dashboardState.when(
                    loading: () => const AppLoading(),
                    error: (e, _) => AppErrorWidget(message: e.toString()),
                    data: (data) => data == null
                        ? const AppEmptyState(message: 'No data')
                        : AdminKpiRow(data: data),
                  ),
                  _buildOverdueAlert(dashboardState, isDesktop: true),
                  const SizedBox(height: 24),
                  const _SectionHeader(
                      title: 'Top Performers', subtitle: 'By tasks completed'),
                  const SizedBox(height: 16),
                  _buildTopPerformers(performanceState),
                ],
              ),
            ),
            const SizedBox(width: 32),
            // RIGHT COLUMN (60%)
            Expanded(
              flex: 6,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionHeader(title: 'Analytics Insights'),
                  const SizedBox(height: 16),
                  TaskStatusPieChart(
                    dataState: distributionState.whenData((v) => v.byStatus),
                    height: 220,
                  ),
                  const SizedBox(height: 24),
                  TaskPriorityDonutChart(
                    dataState: distributionState.whenData((v) => v.byPriority),
                    height: 220,
                  ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 32),
        const _SectionHeader(
            title: 'Recent Activity', subtitle: 'Last 5 system actions'),
        const SizedBox(height: 16),
        _buildRecentActivity(auditLogsState),
        const SizedBox(height: 32),
      ],
    );
  }

  Widget _buildOverdueAlert(AsyncValue dashboardState, {bool isDesktop = false}) {
    return dashboardState.maybeWhen(
      data: (data) {
        final overdue = data?.overdueTasks ?? 0;
        if (overdue > 0) {
          return Padding(
            padding: const EdgeInsets.only(top: 24.0),
            child: Card(
              color: Colors.red.shade50,
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
                side: BorderSide(color: Colors.red.shade200),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    Icon(Icons.warning_amber_rounded, color: Colors.red.shade700),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Text(
                        '⚠️ $overdue tasks are overdue',
                        style: TextStyle(
                            color: Colors.red.shade900,
                            fontWeight: FontWeight.bold),
                      ),
                    ),
                    AppButton(
                      label: 'View Tasks',
                      onPressed: () => context.go('/admin/tasks'),
                      isOutlined: true,
                      fullWidth: false,
                    ),
                  ],
                ),
              ),
            ),
          );
        }
        return const SizedBox.shrink();
      },
      orElse: () => const SizedBox.shrink(),
    );
  }

  Widget _buildTopPerformers(AsyncValue performanceState) {
    return performanceState.when(
      loading: () => const AppLoading(),
      error: (e, _) => AppErrorWidget(message: e.toString()),
      data: (listData) {
        if (listData.isEmpty) {
          return const AppEmptyState(message: 'No performers data');
        }
        final sorted = List.of(listData as List)
          ..sort((a, b) =>
              (b.tasksCompleted ?? 0).compareTo(a.tasksCompleted ?? 0));
        final top3 = sorted.take(3).toList();

        return Row(
          children: top3.map((emp) {
            return Expanded(
              child: Card(
                elevation: 2,
                margin: const EdgeInsets.only(right: 8.0),
                child: Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Column(
                    children: [
                      CircleAvatar(
                        child: Text(
                            (emp.employeeName?.isNotEmpty == true)
                                ? emp.employeeName![0].toUpperCase()
                                : '?',
                            style: const TextStyle(fontWeight: FontWeight.bold)),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        emp.employeeName ?? 'Unknown',
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                      Text('${emp.tasksCompleted} completed',
                          style:
                              const TextStyle(fontSize: 12, color: Colors.grey)),
                    ],
                  ),
                ),
              ),
            );
          }).toList(),
        );
      },
    );
  }

  Widget _buildRecentActivity(AsyncValue auditLogsState) {
    return auditLogsState.when(
      loading: () => const AppLoading(),
      error: (e, _) => AppErrorWidget(message: e.toString()),
      data: (state) {
        final logs = (state as dynamic).logs;
        if (logs.isEmpty) {
          return const AppEmptyState(message: 'No recent activity');
        }
        final recent5 = logs.take(5).toList();
        return Card(
          elevation: 2,
          child: ListView.separated(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: recent5.length,
            separatorBuilder: (context, index) => const Divider(height: 1),
            itemBuilder: (context, index) {
              final log = recent5[index];
              return ListTile(
                leading: const CircleAvatar(
                  radius: 16,
                  child: Icon(Icons.history, size: 16),
                ),
                title: Text(log.action ?? 'Unknown'),
                subtitle: Text(
                    'By ${log.actorEmail ?? 'Unknown'} • ${DateFormat.yMd().add_jm().format(DateTime.tryParse(log.createdAt ?? '') ?? DateTime.now())}'),
              );
            },
          ),
        );
      },
    );
  }
}
