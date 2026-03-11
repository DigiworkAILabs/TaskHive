import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_confirm_dialog.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_detail_provider.dart';
import '../../../domain/providers/profile_photo_provider.dart';
import '../../../data/models/employee_model.dart';
import '../../widgets/employee_status_badge.dart';
import '../../widgets/profile_photo_widget.dart';
import '../../widgets/status_history_timeline.dart';
import '../../../../ml/domain/providers/productivity_score_provider.dart';
import '../../../../ml/presentation/widgets/productivity_score_card.dart';

class EmployeeDetailScreen extends ConsumerWidget {
  final String employeeId;
  const EmployeeDetailScreen({super.key, required this.employeeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(employeeDetailProvider(employeeId));
    final actionsState = ref.watch(employeeActionsProvider);
    final photoState = ref.watch(profilePhotoNotifierProvider);
    final scoreAsync = ref.watch(productivityScoreProvider(employeeId));

    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: const Text('Employee Detail'),
        actions: [
          detailAsync.whenOrNull(
                data: (employee) => IconButton(
                  icon: const Icon(Icons.edit_outlined),
                  tooltip: 'Edit',
                  onPressed: () =>
                      context.go(AppRoutes.adminEditEmployeePath(employeeId)),
                ),
              ) ??
              const SizedBox.shrink(),
        ],
      ),
      body: detailAsync.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(message: e.toString()),
        data: (employee) => SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Profile photo + name header
              Center(
                child: Column(
                  children: [
                    ProfilePhotoWidget(
                      photoUrl: employee.photoUrl,
                      initials: employee.initials,
                      size: 96,
                      onUpload: () => ref
                          .read(profilePhotoNotifierProvider.notifier)
                          .pickAndUpload(employeeId),
                      isUploading: photoState is AsyncLoading,
                    ),
                    const SizedBox(height: 12),
                    Text(employee.fullName,
                        style: Theme.of(context).textTheme.titleLarge),
                    const SizedBox(height: 4),
                    EmployeeStatusBadge(status: employee.status),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              // Info rows
              _InfoRow(label: 'Email', value: employee.email),
              _InfoRow(label: 'Phone', value: employee.phone ?? '—'),
              _InfoRow(label: 'Address', value: employee.address ?? '—'),
              _InfoRow(label: 'Department', value: employee.department ?? '—'),
              _InfoRow(
                  label: 'Designation', value: employee.designation ?? '—'),
              _InfoRow(label: 'Join Date', value: employee.joinDate ?? '—'),
              const SizedBox(height: 32),

              // AI Productivity Section
              Row(
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
              ),
              const SizedBox(height: 16),
              scoreAsync.when(
                data: (score) => ProductivityScoreCard(scoreData: score),
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
                      const Icon(Icons.error_outline,
                          color: Colors.red, size: 20),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'Unable to load productivity score: ${e.toString()}',
                          style:
                              const TextStyle(color: Colors.red, fontSize: 13),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 32),
              // Action buttons
              if (!employee.isDeleted) ...[
                Row(
                  children: [
                    if (employee.canActivate)
                      Expanded(
                        child: FilledButton.icon(
                          icon: const Icon(Icons.check_circle_outline),
                          label: const Text('Activate'),
                          onPressed: () async {
                            final ok = await ref
                                .read(employeeActionsProvider.notifier)
                                .activateEmployee(employeeId);
                            if (ok != null && context.mounted) {
                              AppSnackbar.showSuccess(
                                  context, 'Employee activated.');
                            }
                          },
                        ),
                      ),
                    if (employee.canDeactivate)
                      Expanded(
                        child: OutlinedButton.icon(
                          icon: const Icon(Icons.block_outlined),
                          label: const Text('Deactivate'),
                          style: OutlinedButton.styleFrom(
                              foregroundColor:
                                  Theme.of(context).colorScheme.error),
                          onPressed: () async {
                            final confirmed = await AppConfirmDialog.show(
                              context,
                              title: 'Deactivate Employee',
                              message: 'Deactivate ${employee.fullName}?',
                              confirmLabel: 'Deactivate',
                            );
                            if (confirmed && context.mounted) {
                              await ref
                                  .read(employeeActionsProvider.notifier)
                                  .deactivateEmployee(employeeId);
                              if (context.mounted) {
                                AppSnackbar.showSuccess(
                                    context, 'Employee deactivated.');
                              }
                            }
                          },
                        ),
                      ),
                  ]
                      .map((w) => Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 4),
                            child: w,
                          ))
                      .toList(),
                ),
                const SizedBox(height: 8),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.delete_outline),
                    label: const Text('Delete Employee'),
                    style: OutlinedButton.styleFrom(
                        foregroundColor: Theme.of(context).colorScheme.error),
                    onPressed: () async {
                      final confirmed = await AppConfirmDialog.show(
                        context,
                        title: 'Delete Employee',
                        message:
                            'Delete ${employee.fullName}? Historical data will be preserved.',
                        confirmLabel: 'Delete',
                      );
                      if (confirmed && context.mounted) {
                        final ok = await ref
                            .read(employeeActionsProvider.notifier)
                            .deleteEmployee(employeeId);
                        if (ok && context.mounted) {
                          AppSnackbar.showSuccess(context, 'Employee deleted.');
                          context.go(AppRoutes.adminEmployees);
                        }
                      }
                    },
                  ),
                ),
              ],
              const SizedBox(height: 24),
              // Status history
              Text('Status History',
                  style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              StatusHistoryTimeline(employeeId: employeeId),
              if (actionsState is AsyncLoading)
                const Padding(
                  padding: EdgeInsets.all(16),
                  child: Center(child: CircularProgressIndicator.adaptive()),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;
  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 6),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 110,
              child: Text(label,
                  style: Theme.of(context)
                      .textTheme
                      .bodyMedium
                      ?.copyWith(color: Theme.of(context).hintColor)),
            ),
            Expanded(child: Text(value)),
          ],
        ),
      );
}
