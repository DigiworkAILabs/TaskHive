// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$TaskModelImpl _$$TaskModelImplFromJson(Map<String, dynamic> json) =>
    _$TaskModelImpl(
      id: json['id'] as String,
      title: json['title'] as String,
      description: json['description'] as String?,
      status: $enumDecode(_$TaskStatusEnumMap, json['status']),
      priority: $enumDecode(_$TaskPriorityEnumMap, json['priority']),
      assignedTo: json['assignedTo'] as String?,
      assigneeName: json['assigneeName'] as String?,
      dueDate: json['dueDate'] as String?,
      completedAt: json['completedAt'] as String?,
      estimatedHours: (json['estimatedHours'] as num?)?.toDouble(),
      tags:
          (json['tags'] as List<dynamic>?)?.map((e) => e as String).toList() ??
              const [],
      isOverdue: json['isOverdue'] as bool? ?? false,
      createdBy: json['createdBy'] as String?,
      createdAt: json['createdAt'] as String?,
      updatedAt: json['updatedAt'] as String?,
    );

Map<String, dynamic> _$$TaskModelImplToJson(_$TaskModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      if (instance.description case final value?) 'description': value,
      'status': _$TaskStatusEnumMap[instance.status]!,
      'priority': _$TaskPriorityEnumMap[instance.priority]!,
      if (instance.assignedTo case final value?) 'assignedTo': value,
      if (instance.assigneeName case final value?) 'assigneeName': value,
      if (instance.dueDate case final value?) 'dueDate': value,
      if (instance.completedAt case final value?) 'completedAt': value,
      if (instance.estimatedHours case final value?) 'estimatedHours': value,
      'tags': instance.tags,
      'isOverdue': instance.isOverdue,
      if (instance.createdBy case final value?) 'createdBy': value,
      if (instance.createdAt case final value?) 'createdAt': value,
      if (instance.updatedAt case final value?) 'updatedAt': value,
    };

const _$TaskStatusEnumMap = {
  TaskStatus.todo: 'TODO',
  TaskStatus.inProgress: 'IN_PROGRESS',
  TaskStatus.inReview: 'IN_REVIEW',
  TaskStatus.done: 'DONE',
  TaskStatus.cancelled: 'CANCELLED',
};

const _$TaskPriorityEnumMap = {
  TaskPriority.low: 'LOW',
  TaskPriority.medium: 'MEDIUM',
  TaskPriority.high: 'HIGH',
  TaskPriority.critical: 'CRITICAL',
};
