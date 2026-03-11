import 'dart:typed_data';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/analytics_repository.dart';
import '../../data/models/report_export_request.dart';
import '../../../audit/presentation/widgets/compliance_download.dart';

part 'report_export_provider.g.dart';

@riverpod
class ReportExportNotifier extends _$ReportExportNotifier {
  @override
  FutureOr<void> build() {}

  Future<void> exportAndDownload(String reportType) async {
    state = const AsyncLoading();
    try {
      final repo = ref.read(analyticsRepositoryProvider);
      
      // 1. Export
      final request = ReportExportRequest(reportType: reportType);
      final exportResponse = await repo.exportReport(request);
      final String reportId = exportResponse['reportId'];
      
      // 2. Download bytes
      final downloadResponse = await repo.downloadReport(reportId);
      
      // 3. Extract filename
      String fileName = 'report_$reportId.csv';
      final contentDisposition = downloadResponse.headers.value('content-disposition');
      if (contentDisposition != null && contentDisposition.contains('filename=')) {
        final match = RegExp(r'filename="([^"]+)"').firstMatch(contentDisposition);
        if (match != null) {
          fileName = match.group(1) ?? fileName;
        }
      }
      
      // 4. Save file
      final bytes = Uint8List.fromList(downloadResponse.data ?? []);
      await downloadComplianceReport(bytes, fileName);
      
      state = const AsyncData(null);
    } catch (e, st) {
      state = AsyncError(e, st);
      rethrow;
    }
  }
}
