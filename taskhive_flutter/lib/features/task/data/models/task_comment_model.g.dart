// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_comment_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$TaskCommentModelImpl _$$TaskCommentModelImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskCommentModelImpl(
      id: json['id'] as String,
      taskId: json['taskId'] as String,
      authorId: json['authorId'] as String,
      authorName: json['authorName'] as String,
      content: json['content'] as String,
      createdAt: json['createdAt'] as String?,
    );

Map<String, dynamic> _$$TaskCommentModelImplToJson(
        _$TaskCommentModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'taskId': instance.taskId,
      'authorId': instance.authorId,
      'authorName': instance.authorName,
      'content': instance.content,
      if (instance.createdAt case final value?) 'createdAt': value,
    };
