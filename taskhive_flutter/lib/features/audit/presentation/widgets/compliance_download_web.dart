// ignore_for_file: avoid_web_libraries_in_flutter, deprecated_member_use

import 'dart:html' as html;
import 'dart:typed_data';

import '../../../../core/utils/logger.dart';

Future<void> downloadComplianceReport(Uint8List bytes, String filename) async {
  try {
    final blob = html.Blob([bytes]);
    final url = html.Url.createObjectUrlFromBlob(blob);
    html.AnchorElement(href: url)
      ..setAttribute('download', filename)
      ..click();
    html.Url.revokeObjectUrl(url);
    appLogger.i('Compliance report download triggered on web for $filename');
  } catch (e) {
    appLogger.e('Error saving compliance report on web: $e');
    rethrow;
  }
}
