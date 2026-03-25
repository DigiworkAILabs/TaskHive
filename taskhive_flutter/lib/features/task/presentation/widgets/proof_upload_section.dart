import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../data/models/task_attachment_model.dart';
import '../../domain/providers/task_attachments_provider.dart';

class ProofUploadSection extends ConsumerStatefulWidget {
  final String taskId;
  final List<TaskAttachmentModel> attachments;
  final bool isAdmin;
  final bool taskDone;

  const ProofUploadSection({
    super.key,
    required this.taskId,
    required this.attachments,
    this.isAdmin = false,
    this.taskDone = false,
  });

  @override
  ConsumerState<ProofUploadSection> createState() => _ProofUploadSectionState();
}

class _ProofUploadSectionState extends ConsumerState<ProofUploadSection> {
  bool _isUploading = false;

  Future<void> _handleUpload() async {
    final picker = ImagePicker();
    final file = await picker.pickImage(source: ImageSource.gallery);
    if (file == null) return;

    setState(() => _isUploading = true);
    try {
      await ref
          .read(taskAttachmentsProvider(widget.taskId).notifier)
          .uploadAttachment(file, purpose: 'PROOF');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Proof uploaded successfully!')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Upload failed: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isUploading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final proofAttachments = widget.attachments
        .where((a) => a.attachmentPurpose == 'PROOF')
        .toList();
    final rejectedProofs = widget.attachments
        .where((a) => a.attachmentPurpose == 'REJECTED_PROOF')
        .toList();

    final hasProof = proofAttachments.isNotEmpty;
    final sectionColor = hasProof ? Colors.green : Colors.orange;
    final sectionBg = sectionColor.withValues(alpha: 0.05);
    final sectionBorder = sectionColor.withValues(alpha: 0.2);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (rejectedProofs.isNotEmpty) ...[
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.red.withValues(alpha: 0.05),
              border: Border.all(color: Colors.red.withValues(alpha: 0.2)),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Previously rejected proofs:',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: Colors.red,
                  ),
                ),
                const SizedBox(height: 8),
                ...rejectedProofs.map((p) => Padding(
                      padding: const EdgeInsets.only(bottom: 4),
                      child: Row(
                        children: [
                          const Icon(Icons.file_present,
                              size: 14, color: Colors.red),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              p.fileName,
                              style: const TextStyle(
                                fontSize: 12,
                                color: Colors.grey,
                                decoration: TextDecoration.lineThrough,
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ),
                    )),
                const Padding(
                  padding: EdgeInsets.only(top: 8),
                  child: Text(
                    'Please upload a new proof to resubmit.',
                    style: TextStyle(fontSize: 11, color: Colors.red),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
        ],
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: sectionBg,
            border: Border.all(color: sectionBorder),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      Icon(
                        hasProof ? Icons.verified_user : Icons.warning_amber,
                        size: 16,
                        color: sectionColor,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        widget.isAdmin
                            ? (hasProof ? 'Proof Submitted' : 'No Proof')
                            : (hasProof ? 'Proof Uploaded ✓' : 'Proof Required'),
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.bold,
                          color: sectionColor,
                        ),
                      ),
                    ],
                  ),
                  if (widget.isAdmin)
                    const Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(Icons.lock, size: 12, color: Colors.grey),
                        SizedBox(width: 4),
                        Text('View only',
                            style: TextStyle(fontSize: 11, color: Colors.grey)),
                      ],
                    ),
                ],
              ),
              if (proofAttachments.isNotEmpty) ...[
                const SizedBox(height: 12),
                ...proofAttachments.map((att) => _ProofTile(attachment: att)),
              ] else if (!widget.isAdmin) ...[
                const SizedBox(height: 12),
                const Text(
                  'Upload a screenshot or document as proof of completion.',
                  style: TextStyle(fontSize: 12, color: Colors.grey),
                ),
              ],
              if (!widget.isAdmin && !widget.taskDone) ...[
                const SizedBox(height: 12),
                OutlinedButton.icon(
                  onPressed: _isUploading ? null : _handleUpload,
                  icon: _isUploading
                      ? const SizedBox(
                          width: 14,
                          height: 14,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.upload, size: 14),
                  label: Text(_isUploading
                      ? 'Uploading...'
                      : (hasProof ? 'Upload Another' : 'Upload Proof')),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: sectionColor,
                    side: BorderSide(color: sectionColor.withValues(alpha: 0.4)),
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    textStyle: const TextStyle(
                        fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}

class _ProofTile extends StatelessWidget {
  final TaskAttachmentModel attachment;

  const _ProofTile({required this.attachment});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 6),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.green.withValues(alpha: 0.05),
        border: Border.all(color: Colors.green.withValues(alpha: 0.1)),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          const Icon(Icons.file_present, size: 14, color: Colors.green),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  attachment.fileName,
                  style: const TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w500,
                      color: Color(0xFFDEE4E7)),
                  overflow: TextOverflow.ellipsis,
                ),
                Text(
                  attachment.formattedFileSize,
                  style: const TextStyle(fontSize: 11, color: Colors.grey),
                ),
              ],
            ),
          ),
          IconButton(
            icon: const Icon(Icons.open_in_new, size: 14, color: Colors.grey),
            onPressed: () => launchUrl(Uri.parse(attachment.fileUrl)),
          ),
        ],
      ),
    );
  }
}
