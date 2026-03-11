import 'dart:io';
import 'dart:typed_data';

import 'package:path_provider/path_provider.dart';

import '../../../../core/utils/logger.dart';

Future<void> downloadComplianceReport(Uint8List bytes, String filename) async {
  try {
    Directory? downloadsDirectory;
    if (Platform.isAndroid) {
      // Fetch /storage/emulated/0/Download
      downloadsDirectory = Directory('/storage/emulated/0/Download');
      if (!await downloadsDirectory.exists()) {
        downloadsDirectory = await getExternalStorageDirectory();
      }
    } else if (Platform.isIOS) {
      downloadsDirectory = await getApplicationDocumentsDirectory();
    } else {
      downloadsDirectory = await getDownloadsDirectory();
    }

    if (downloadsDirectory != null) {
      // Ensure unique filename
      var file = File('${downloadsDirectory.path}/$filename');
      int counter = 1;
      final nameWithoutExt = filename.split('.').first;
      final ext = filename.split('.').last;

      while (await file.exists()) {
        file =
            File('${downloadsDirectory.path}/${nameWithoutExt}_$counter.$ext');
        counter++;
      }

      await file.writeAsBytes(bytes);
      appLogger.i('Compliance report saved to: ${file.path}');
    } else {
      throw Exception('Could not find downloads directory');
    }
  } catch (e) {
    appLogger.e('Error saving compliance report natively: $e');
    rethrow;
  }
}
