import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../data/repositories/audit_repository.dart';
import '../../../../core/widgets/app_snackbar.dart';
import 'compliance_download.dart';

class ComplianceReportButton extends ConsumerStatefulWidget {
  const ComplianceReportButton({super.key});

  @override
  ConsumerState<ComplianceReportButton> createState() =>
      _ComplianceReportButtonState();
}

class _ComplianceReportButtonState
    extends ConsumerState<ComplianceReportButton> {
  bool _isDownloading = false;

  Future<void> _downloadReport() async {
    setState(() => _isDownloading = true);

    try {
      final bytes =
          await ref.read(auditRepositoryProvider).downloadComplianceReport();
      final uint8List = Uint8List.fromList(bytes);
      final dateStr = DateFormat('yyyyMMdd_HHmm').format(DateTime.now());
      final filename = 'compliance_report_$dateStr.pdf';

      if (context.mounted) {
        await downloadComplianceReport(uint8List, filename);
        if (context.mounted) {
          // ignore: use_build_context_synchronously
          AppSnackbar.showSuccess(context, 'Compliance report downloaded.');
        }
      }
    } catch (e) {
      if (context.mounted) {
        // ignore: use_build_context_synchronously
        AppSnackbar.showError(context, 'Failed to download report: $e');
      }
    } finally {
      if (mounted) {
        setState(() => _isDownloading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isDownloading) {
      return const Padding(
        padding: EdgeInsets.symmetric(horizontal: 16.0),
        child: Center(
          child: SizedBox(
            width: 20,
            height: 20,
            child: CircularProgressIndicator(strokeWidth: 2),
          ),
        ),
      );
    }

    return IconButton(
      icon: const Icon(Icons.download),
      tooltip: 'Download Compliance Report',
      onPressed: _downloadReport,
    );
  }
}
