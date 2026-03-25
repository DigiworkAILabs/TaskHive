import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_attachment_model.freezed.dart';
part 'task_attachment_model.g.dart';

@freezed
class TaskAttachmentModel with _$TaskAttachmentModel {
  const factory TaskAttachmentModel({
    required String id,
    required String taskId,
    required String uploadedBy,
    String? uploaderName,
    required String fileName,
    required String fileUrl,
    required int fileSize,
    required String mimeType,
    String? attachmentPurpose,
    String? createdAt,
  }) = _TaskAttachmentModel;

  factory TaskAttachmentModel.fromJson(Map<String, dynamic> json) =>
      _$TaskAttachmentModelFromJson(json);
}

extension TaskAttachmentModelX on TaskAttachmentModel {
  /// Human-readable file size (e.g., "204 KB", "1.2 MB")
  String get formattedFileSize {
    if (fileSize < 1024) return '$fileSize B';
    if (fileSize < 1024 * 1024) {
      return '${(fileSize / 1024).toStringAsFixed(1)} KB';
    }
    return '${(fileSize / (1024 * 1024)).toStringAsFixed(1)} MB';
  }
}
