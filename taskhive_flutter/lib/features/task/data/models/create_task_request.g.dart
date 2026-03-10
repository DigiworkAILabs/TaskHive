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

_$TaskPriorityRequestDtoImpl _$$TaskPriorityRequestDtoImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskPriorityRequestDtoImpl(
      taskTitle: json['taskTitle'] as String,
      taskDescription: json['taskDescription'] as String?,
      employeeId: json['employeeId'] as String?,
      tags: (json['tags'] as List<dynamic>?)?.map((e) => e as String).toList(),
      estimatedHours: (json['estimatedHours'] as num?)?.toDouble(),
    );

Map<String, dynamic> _$$TaskPriorityRequestDtoImplToJson(
        _$TaskPriorityRequestDtoImpl instance) =>
    <String, dynamic>{
      'taskTitle': instance.taskTitle,
      if (instance.taskDescription case final value?) 'taskDescription': value,
      if (instance.employeeId case final value?) 'employeeId': value,
      if (instance.tags case final value?) 'tags': value,
      if (instance.estimatedHours case final value?) 'estimatedHours': value,
    };

_$TaskPriorityPredictionImpl _$$TaskPriorityPredictionImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskPriorityPredictionImpl(
      predictedPriority: json['predictedPriority'] as String,
      confidence: (json['confidence'] as num).toDouble(),
      reasoning: json['reasoning'] as String,
      fallbackUsed: json['fallbackUsed'] as bool,
    );

Map<String, dynamic> _$$TaskPriorityPredictionImplToJson(
        _$TaskPriorityPredictionImpl instance) =>
    <String, dynamic>{
      'predictedPriority': instance.predictedPriority,
      'confidence': instance.confidence,
      'reasoning': instance.reasoning,
      'fallbackUsed': instance.fallbackUsed,
    };
