// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_task_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$UpdateTaskRequestImpl _$$UpdateTaskRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$UpdateTaskRequestImpl(
      title: json['title'] as String?,
      description: json['description'] as String?,
      priority: $enumDecodeNullable(_$TaskPriorityEnumMap, json['priority']),
      assignedTo: json['assignedTo'] as String?,
      dueDate: json['dueDate'] as String?,
      estimatedHours: (json['estimatedHours'] as num?)?.toDouble(),
      tags: (json['tags'] as List<dynamic>?)?.map((e) => e as String).toList(),
      proofRequired: json['proofRequired'] as bool?,
      approvalRequired: json['approvalRequired'] as bool?,
    );

Map<String, dynamic> _$$UpdateTaskRequestImplToJson(
        _$UpdateTaskRequestImpl instance) =>
    <String, dynamic>{
      if (instance.title case final value?) 'title': value,
      if (instance.description case final value?) 'description': value,
      if (_$TaskPriorityEnumMap[instance.priority] case final value?)
        'priority': value,
      if (instance.assignedTo case final value?) 'assignedTo': value,
      if (instance.dueDate case final value?) 'dueDate': value,
      if (instance.estimatedHours case final value?) 'estimatedHours': value,
      if (instance.tags case final value?) 'tags': value,
      if (instance.proofRequired case final value?) 'proofRequired': value,
      if (instance.approvalRequired case final value?)
        'approvalRequired': value,
    };

const _$TaskPriorityEnumMap = {
  TaskPriority.low: 'LOW',
  TaskPriority.medium: 'MEDIUM',
  TaskPriority.high: 'HIGH',
  TaskPriority.critical: 'CRITICAL',
};
