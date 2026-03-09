import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../data/models/task_attachment_model.dart';

/// Displays a single file attachment — icon, name, size, uploader, timestamp.
/// Tapping opens the fileUrl in the browser via url_launcher.
class TaskAttachmentTile extends StatelessWidget {
  final TaskAttachmentModel attachment;
  final String baseUrl;

  const TaskAttachmentTile({
    super.key,
    required this.attachment,
    required this.baseUrl,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: _fileIcon(attachment.mimeType),
      title: Text(
        attachment.fileName,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      subtitle: Text(
        '${attachment.formattedFileSize} • ${attachment.uploaderName ?? attachment.uploadedBy}',
        style: const TextStyle(fontSize: 12),
      ),
      trailing: const Icon(Icons.open_in_new, size: 16),
      onTap: () => _openFile(context),
    );
  }

  Widget _fileIcon(String mimeType) {
    IconData icon;
    Color color;
    if (mimeType.contains('pdf')) {
      icon = Icons.picture_as_pdf;
      color = Colors.red;
    } else if (mimeType.contains('image')) {
      icon = Icons.image;
      color = Colors.blue;
    } else if (mimeType.contains('word') || mimeType.contains('docx')) {
      icon = Icons.description;
      color = Colors.indigo;
    } else {
      icon = Icons.attach_file;
      color = Colors.grey;
    }
    return Icon(icon, color: color, size: 28);
  }

  Future<void> _openFile(BuildContext context) async {
    final url = attachment.fileUrl.startsWith('http')
        ? attachment.fileUrl
        : '$baseUrl${attachment.fileUrl}';
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } else {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Cannot open this file')),
        );
      }
    }
  }
}
