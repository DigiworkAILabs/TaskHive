// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_status_history_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$TaskStatusHistoryModelImpl _$$TaskStatusHistoryModelImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskStatusHistoryModelImpl(
      id: json['id'] as String,
      taskId: json['taskId'] as String,
      oldStatus: json['oldStatus'] as String?,
      newStatus: json['newStatus'] as String,
      changedByName: json['changedByName'] as String,
      comment: json['comment'] as String?,
      changedAt: json['changedAt'] as String,
    );

Map<String, dynamic> _$$TaskStatusHistoryModelImplToJson(
        _$TaskStatusHistoryModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'taskId': instance.taskId,
      if (instance.oldStatus case final value?) 'oldStatus': value,
      'newStatus': instance.newStatus,
      'changedByName': instance.changedByName,
      if (instance.comment case final value?) 'comment': value,
      'changedAt': instance.changedAt,
    };
