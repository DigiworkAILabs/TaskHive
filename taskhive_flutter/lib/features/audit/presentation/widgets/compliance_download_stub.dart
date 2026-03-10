// ignore_for_file: avoid_web_libraries_in_flutter

import 'dart:typed_data';

Future<void> downloadComplianceReport(Uint8List bytes, String filename) async {
  throw UnsupportedError(
      'Cannot save compliance report without dart:html or path_provider');
}
