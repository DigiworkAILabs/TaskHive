// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_task_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$CreateTaskRequestImpl _$$CreateTaskRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$CreateTaskRequestImpl(
      title: json['title'] as String,
      description: json['description'] as String?,
      priority: $enumDecode(_$TaskPriorityEnumMap, json['priority']),
      assignedTo: json['assignedTo'] as String,
      dueDate: json['dueDate'] as String,
      estimatedHours: (json['estimatedHours'] as num?)?.toDouble(),
      tags:
          (json['tags'] as List<dynamic>?)?.map((e) => e as String).toList() ??
              const [],
    );

Map<String, dynamic> _$$CreateTaskRequestImplToJson(
        _$CreateTaskRequestImpl instance) =>
    <String, dynamic>{
      'title': instance.title,
      if (instance.description case final value?) 'description': value,
      'priority': _$TaskPriorityEnumMap[instance.priority]!,
      'assignedTo': instance.assignedTo,
      'dueDate': instance.dueDate,
      if (instance.estimatedHours case final value?) 'estimatedHours': value,
      'tags': instance.tags,
    };

const _$TaskPriorityEnumMap = {
  TaskPriority.low: 'LOW',
  TaskPriority.medium: 'MEDIUM',
  TaskPriority.high: 'HIGH',
  TaskPriority.critical: 'CRITICAL',
};
