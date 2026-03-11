// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_performance_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$EmployeePerformanceResponseImpl _$$EmployeePerformanceResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$EmployeePerformanceResponseImpl(
      employeeId: json['employeeId'] as String?,
      employeeName: json['employeeName'] as String?,
      tasksAssigned: (json['tasksAssigned'] as num?)?.toInt(),
      tasksCompleted: (json['tasksCompleted'] as num?)?.toInt(),
      onTimeRate: (json['onTimeRate'] as num?)?.toDouble(),
      avgCompletionHours: (json['avgCompletionHours'] as num?)?.toDouble(),
    );

Map<String, dynamic> _$$EmployeePerformanceResponseImplToJson(
        _$EmployeePerformanceResponseImpl instance) =>
    <String, dynamic>{
      if (instance.employeeId case final value?) 'employeeId': value,
      if (instance.employeeName case final value?) 'employeeName': value,
      if (instance.tasksAssigned case final value?) 'tasksAssigned': value,
      if (instance.tasksCompleted case final value?) 'tasksCompleted': value,
      if (instance.onTimeRate case final value?) 'onTimeRate': value,
      if (instance.avgCompletionHours case final value?)
        'avgCompletionHours': value,
    };
