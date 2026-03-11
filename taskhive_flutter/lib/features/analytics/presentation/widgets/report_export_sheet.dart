import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_snackbar.dart';
import '../../domain/providers/report_export_provider.dart';

class ReportExportSheet extends ConsumerStatefulWidget {
  const ReportExportSheet({super.key});

  @override
  ConsumerState<ReportExportSheet> createState() => _ReportExportSheetState();
}

class _ReportExportSheetState extends ConsumerState<ReportExportSheet> {
  String _reportType = 'TASK_SUMMARY';

  void _export() async {
    try {
      await ref
          .read(reportExportNotifierProvider.notifier)
          .exportAndDownload(_reportType);
      if (mounted) {
        context.pop();
        AppSnackbar.showSuccess(context, 'Report exported successfully');
      }
    } catch (e) {
      if (mounted) {
        AppSnackbar.showError(context, e.toString());
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(reportExportNotifierProvider);
    final isLoading = state.isLoading;

    return Padding(
      padding: EdgeInsets.only(
        left: 24,
        right: 24,
        top: 24,
        bottom: MediaQuery.of(context).viewInsets.bottom + 24,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              Text(
                'Export Report',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const Spacer(),
              IconButton(
                icon: const Icon(Icons.close),
                onPressed: () => context.pop(),
              )
            ],
          ),
          const SizedBox(height: 24),
          const Text('Report Type',
              style: TextStyle(fontWeight: FontWeight.w500)),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            initialValue: _reportType,
            decoration: InputDecoration(
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              contentPadding:
                  const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            ),
            items: const [
              DropdownMenuItem(
                  value: 'TASK_SUMMARY', child: Text('Task Summary')),
              DropdownMenuItem(
                  value: 'EMPLOYEE_PERFORMANCE',
                  child: Text('Employee Performance')),
            ],
            onChanged: (val) {
              if (val != null) {
                setState(() => _reportType = val);
              }
            },
          ),
          const SizedBox(height: 32),
          AppButton(
            label: 'Export CSV',
            onPressed: _export,
            isLoading: isLoading,
          ),
        ],
      ),
    );
  }
}
