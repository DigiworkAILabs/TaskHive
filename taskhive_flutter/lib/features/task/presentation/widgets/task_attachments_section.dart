import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../domain/providers/task_attachments_provider.dart';
import 'task_attachment_tile.dart';

/// ConsumerStatefulWidget — shows list of attachments + upload button.
/// Handles Web/native branching via the repository layer.
class TaskAttachmentsSection extends ConsumerStatefulWidget {
  final String taskId;

  const TaskAttachmentsSection({super.key, required this.taskId});

  @override
  ConsumerState<TaskAttachmentsSection> createState() =>
      _TaskAttachmentsSectionState();
}

class _TaskAttachmentsSectionState
    extends ConsumerState<TaskAttachmentsSection> {
  bool _uploading = false;

  Future<void> _upload() async {
    setState(() => _uploading = true);
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['jpg', 'jpeg', 'png', 'pdf', 'docx'],
        withData: true,
      );
      if (result == null || result.files.isEmpty) return;
      final file = result.files.first;
      final xFile = XFile.fromData(
        file.bytes!,
        name: file.name,
      );
      await ref
          .read(taskAttachmentsProvider(widget.taskId).notifier)
          .uploadAttachment(xFile);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('File uploaded successfully')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Upload failed: ${e.toString()}')),
        );
      }
    } finally {
      if (mounted) setState(() => _uploading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final attachmentsAsync = ref.watch(taskAttachmentsProvider(widget.taskId));
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Text('Attachments',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const Spacer(),
            _uploading
                ? const SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(strokeWidth: 2))
                : TextButton.icon(
                    onPressed: _upload,
                    icon: const Icon(Icons.upload_file, size: 16),
                    label: const Text('Upload'),
                  ),
          ],
        ),
        attachmentsAsync.when(
          data: (list) => list.isEmpty
              ? const Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
                  child: Text('No attachments.',
                      style: TextStyle(color: Colors.grey)),
                )
              : Column(
                  children: list
                      .map((a) => TaskAttachmentTile(
                            attachment: a,
                            baseUrl: dotenv.get('BASE_URL', fallback: ''),
                          ))
                      .toList(),
                ),
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Text('Error loading attachments: ${e.toString()}'),
        ),
      ],
    );
  }
}
