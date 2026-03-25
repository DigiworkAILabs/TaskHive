// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_task_status_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$UpdateTaskStatusRequestImpl _$$UpdateTaskStatusRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$UpdateTaskStatusRequestImpl(
      status: $enumDecode(_$TaskStatusEnumMap, json['status']),
      comment: json['comment'] as String?,
      reason: json['reason'] as String?,
    );

Map<String, dynamic> _$$UpdateTaskStatusRequestImplToJson(
        _$UpdateTaskStatusRequestImpl instance) =>
    <String, dynamic>{
      'status': _$TaskStatusEnumMap[instance.status]!,
      if (instance.comment case final value?) 'comment': value,
      if (instance.reason case final value?) 'reason': value,
    };

const _$TaskStatusEnumMap = {
  TaskStatus.todo: 'TODO',
  TaskStatus.inProgress: 'IN_PROGRESS',
  TaskStatus.inReview: 'IN_REVIEW',
  TaskStatus.pendingApproval: 'PENDING_APPROVAL',
  TaskStatus.done: 'DONE',
  TaskStatus.cancelled: 'CANCELLED',
};
