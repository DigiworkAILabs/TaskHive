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
      proofRequired: json['proofRequired'] as bool? ?? false,
      approvalRequired: json['approvalRequired'] as bool? ?? false,
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
      'proofRequired': instance.proofRequired,
      'approvalRequired': instance.approvalRequired,
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

_$TaskCompletionTimeRequestDtoImpl _$$TaskCompletionTimeRequestDtoImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskCompletionTimeRequestDtoImpl(
      taskTitle: json['taskTitle'] as String,
      taskDescription: json['taskDescription'] as String?,
      priority: json['priority'] as String,
      employeeId: json['employeeId'] as String,
      estimatedHours: (json['estimatedHours'] as num?)?.toDouble(),
    );

Map<String, dynamic> _$$TaskCompletionTimeRequestDtoImplToJson(
        _$TaskCompletionTimeRequestDtoImpl instance) =>
    <String, dynamic>{
      'taskTitle': instance.taskTitle,
      if (instance.taskDescription case final value?) 'taskDescription': value,
      'priority': instance.priority,
      'employeeId': instance.employeeId,
      if (instance.estimatedHours case final value?) 'estimatedHours': value,
    };

_$ConfidenceRangeImpl _$$ConfidenceRangeImplFromJson(
        Map<String, dynamic> json) =>
    _$ConfidenceRangeImpl(
      low: (json['low'] as num).toDouble(),
      high: (json['high'] as num).toDouble(),
    );

Map<String, dynamic> _$$ConfidenceRangeImplToJson(
        _$ConfidenceRangeImpl instance) =>
    <String, dynamic>{
      'low': instance.low,
      'high': instance.high,
    };

_$TaskCompletionTimePredictionImpl _$$TaskCompletionTimePredictionImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskCompletionTimePredictionImpl(
      estimatedHours: (json['estimatedHours'] as num).toDouble(),
      confidenceRange: ConfidenceRange.fromJson(
          json['confidenceRange'] as Map<String, dynamic>),
      reasoning: json['reasoning'] as String,
      fallbackUsed: json['fallbackUsed'] as bool,
    );

Map<String, dynamic> _$$TaskCompletionTimePredictionImplToJson(
        _$TaskCompletionTimePredictionImpl instance) =>
    <String, dynamic>{
      'estimatedHours': instance.estimatedHours,
      'confidenceRange': instance.confidenceRange.toJson(),
      'reasoning': instance.reasoning,
      'fallbackUsed': instance.fallbackUsed,
    };

_$WorkloadRecommendationRequestImpl
    _$$WorkloadRecommendationRequestImplFromJson(Map<String, dynamic> json) =>
        _$WorkloadRecommendationRequestImpl(
          taskTitle: json['taskTitle'] as String,
          taskPriority: json['taskPriority'] as String,
          taskEstimatedHours: (json['taskEstimatedHours'] as num?)?.toDouble(),
          candidateEmployeeIds: (json['candidateEmployeeIds'] as List<dynamic>)
              .map((e) => e as String)
              .toList(),
        );

Map<String, dynamic> _$$WorkloadRecommendationRequestImplToJson(
        _$WorkloadRecommendationRequestImpl instance) =>
    <String, dynamic>{
      'taskTitle': instance.taskTitle,
      'taskPriority': instance.taskPriority,
      if (instance.taskEstimatedHours case final value?)
        'taskEstimatedHours': value,
      'candidateEmployeeIds': instance.candidateEmployeeIds,
    };

_$WorkloadRecommendationResponseImpl
    _$$WorkloadRecommendationResponseImplFromJson(Map<String, dynamic> json) =>
        _$WorkloadRecommendationResponseImpl(
          recommendedEmployeeId: json['recommendedEmployeeId'] as String?,
          scoreBreakdown: (json['scoreBreakdown'] as List<dynamic>?)
                  ?.map((e) => EmployeeScoreBreakdown.fromJson(
                      e as Map<String, dynamic>))
                  .toList() ??
              const [],
          reasoning: json['reasoning'] as String,
          fallbackUsed: json['fallbackUsed'] as bool,
        );

Map<String, dynamic> _$$WorkloadRecommendationResponseImplToJson(
        _$WorkloadRecommendationResponseImpl instance) =>
    <String, dynamic>{
      if (instance.recommendedEmployeeId case final value?)
        'recommendedEmployeeId': value,
      'scoreBreakdown': instance.scoreBreakdown.map((e) => e.toJson()).toList(),
      'reasoning': instance.reasoning,
      'fallbackUsed': instance.fallbackUsed,
    };

_$EmployeeScoreBreakdownImpl _$$EmployeeScoreBreakdownImplFromJson(
        Map<String, dynamic> json) =>
    _$EmployeeScoreBreakdownImpl(
      employeeId: json['employeeId'] as String,
      score: (json['score'] as num).toDouble(),
    );

Map<String, dynamic> _$$EmployeeScoreBreakdownImplToJson(
        _$EmployeeScoreBreakdownImpl instance) =>
    <String, dynamic>{
      'employeeId': instance.employeeId,
      'score': instance.score,
    };
