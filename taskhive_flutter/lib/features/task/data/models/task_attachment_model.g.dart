// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_attachment_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$TaskAttachmentModelImpl _$$TaskAttachmentModelImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskAttachmentModelImpl(
      id: json['id'] as String,
      taskId: json['taskId'] as String,
      uploadedBy: json['uploadedBy'] as String,
      uploaderName: json['uploaderName'] as String?,
      fileName: json['fileName'] as String,
      fileUrl: json['fileUrl'] as String,
      fileSize: (json['fileSize'] as num).toInt(),
      mimeType: json['mimeType'] as String,
      createdAt: json['createdAt'] as String?,
    );

Map<String, dynamic> _$$TaskAttachmentModelImplToJson(
        _$TaskAttachmentModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'taskId': instance.taskId,
      'uploadedBy': instance.uploadedBy,
      if (instance.uploaderName case final value?) 'uploaderName': value,
      'fileName': instance.fileName,
      'fileUrl': instance.fileUrl,
      'fileSize': instance.fileSize,
      'mimeType': instance.mimeType,
      if (instance.createdAt case final value?) 'createdAt': value,
    };
