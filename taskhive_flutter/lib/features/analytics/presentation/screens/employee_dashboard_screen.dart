import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../../../core/widgets/responsive_layout.dart';
import '../../../auth/domain/providers/auth_provider.dart';
import '../../domain/providers/employee_dashboard_provider.dart';
import '../../data/models/task_distribution_response.dart';
import '../widgets/employee_kpi_row.dart';
import '../widgets/task_status_pie_chart.dart';
import '../../../ml/domain/providers/productivity_score_provider.dart';
import '../../../ml/presentation/widgets/productivity_score_card.dart';
import '../../../ml/domain/providers/ml_feature_provider.dart';

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
  ConsumerState<EmployeeDashboardScreen> createState() =>
      _EmployeeDashboardScreenState();
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
                  onPressed: () =>
                      ref.read(authStateNotifierProvider.notifier).logout(),
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
                    children: _buildMobileLayout(context, dashboardState, user),
                  ),
                  tablet: Column(
                    children: _buildMobileLayout(context, dashboardState, user),
                  ),
                  desktop: _buildDesktopLayout(context, dashboardState, user),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildMobileLayout(
      BuildContext context, AsyncValue dashboardState, dynamic user) {
    return [
      // Section 1: KPI Summary Row
      dashboardState.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(message: e.toString()),
        data: (data) => data == null
            ? const AppEmptyState(message: 'No data')
            : EmployeeKpiRow(data: data),
      ),

      const SizedBox(height: 24),

      // Section 2: Task Status Pie
      const _SectionHeader(
          title: 'My Task Status', subtitle: 'Detailed breakdown'),
      const SizedBox(height: 16),
      _buildTaskStatusPie(dashboardState),

      const SizedBox(height: 24),

      // AI Productivity Section
      _buildAiPerformanceHeader(context),
      const SizedBox(height: 16),
      if (user != null) _buildProductivityScore(user),

      const SizedBox(height: 32),
      AppButton(
        label: 'View My Tasks',
        onPressed: () => context.go('/employee/tasks'),
      ),
      const SizedBox(height: 32),
    ];
  }

  Widget _buildDesktopLayout(
      BuildContext context, AsyncValue dashboardState, dynamic user) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // LEFT COLUMN (40%)
        Expanded(
          flex: 4,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const _SectionHeader(title: 'My Overview'),
              const SizedBox(height: 16),
              dashboardState.when(
                loading: () => const AppLoading(),
                error: (e, _) => AppErrorWidget(message: e.toString()),
                data: (data) => data == null
                    ? const AppEmptyState(message: 'No data')
                    : EmployeeKpiRow(data: data),
              ),
              const SizedBox(height: 32),
              _buildAiPerformanceHeader(context),
              const SizedBox(height: 16),
              if (user != null) _buildProductivityScore(user),
              const SizedBox(height: 32),
              AppButton(
                label: 'View My Tasks',
                onPressed: () => context.go('/employee/tasks'),
              ),
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
              const _SectionHeader(
                  title: 'Performance Charts', subtitle: 'Visual status summary'),
              const SizedBox(height: 16),
              _buildTaskStatusPie(dashboardState, height: 350),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildAiPerformanceHeader(BuildContext context) {
    return Row(
      children: [
        const Icon(Icons.psychology, size: 20, color: Colors.indigo),
        const SizedBox(width: 8),
        Text(
          'AI PERFORMANCE INSIGHTS',
          style: Theme.of(context).textTheme.labelSmall?.copyWith(
                fontWeight: FontWeight.bold,
                letterSpacing: 1.1,
                color: Colors.grey.shade600,
              ),
        ),
      ],
    );
  }

  Widget _buildTaskStatusPie(AsyncValue dashboardState, {double height = 180}) {
    return dashboardState.when(
      loading: () => const AppLoading(),
      error: (e, _) => AppErrorWidget(message: e.toString()),
      data: (data) {
        if (data == null) return const SizedBox.shrink();
        final mockDistribution = [
          TaskDistributionResponse(status: 'TODO', count: data.todoTasks ?? 0),
          TaskDistributionResponse(
              status: 'IN_PROGRESS', count: data.inProgressTasks ?? 0),
          TaskDistributionResponse(
              status: 'IN_REVIEW', count: data.inReviewTasks ?? 0),
          TaskDistributionResponse(status: 'DONE', count: data.completedTasks ?? 0),
        ].where((e) => e.count != null && e.count! > 0).toList();

        return TaskStatusPieChart(
          dataState: AsyncData(mockDistribution),
          height: height,
        );
      },
    );
  }

  Widget _buildProductivityScore(dynamic user) {
    return Consumer(builder: (context, ref, child) {
      final isMlEnabledAsync = ref.watch(mlFeatureToggleProvider);
      
      return isMlEnabledAsync.when(
        data: (isMlEnabled) {
          if (!isMlEnabled) {
            return Container(
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                color: Theme.of(context).cardColor,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: Theme.of(context).dividerColor.withOpacity(0.1)),
              ),
              child: Center(
                child: Column(
                  children: [
                    Icon(Icons.auto_awesome, size: 32, color: Colors.grey.shade400),
                    const SizedBox(height: 12),
                    Text(
                      'AI Insights Disabled',
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Turn on ML Insights in the AppBar to see productivity predictions.',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.grey),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
            );
          }

          return ref.watch(productivityScoreProvider(user.id)).when(
                data: (score) => ProductivityScoreCard(scoreData: score!),
                loading: () => const Center(
                  child: Padding(
                    padding: EdgeInsets.all(32),
                    child: CircularProgressIndicator(),
                  ),
                ),
                error: (e, _) => Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.red.withOpacity(0.05),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: Colors.red.withOpacity(0.1)),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.error_outline, color: Colors.red, size: 20),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'Unable to load productivity score: ${e.toString()}',
                          style: const TextStyle(color: Colors.red, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
              );
        },
        loading: () => const AppLoading(),
        error: (_, __) => const SizedBox.shrink(),
      );
    });
  }
}
