// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_dashboard_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$EmployeeDashboardResponseImpl _$$EmployeeDashboardResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$EmployeeDashboardResponseImpl(
      totalTasks: (json['totalTasks'] as num?)?.toInt(),
      todoTasks: (json['todoTasks'] as num?)?.toInt(),
      inProgressTasks: (json['inProgressTasks'] as num?)?.toInt(),
      inReviewTasks: (json['inReviewTasks'] as num?)?.toInt(),
      completedTasks: (json['completedTasks'] as num?)?.toInt(),
      onTimeCompletionRate: (json['onTimeCompletionRate'] as num?)?.toDouble(),
      avgCompletionHours: (json['avgCompletionHours'] as num?)?.toDouble(),
    );

Map<String, dynamic> _$$EmployeeDashboardResponseImplToJson(
        _$EmployeeDashboardResponseImpl instance) =>
    <String, dynamic>{
      if (instance.totalTasks case final value?) 'totalTasks': value,
      if (instance.todoTasks case final value?) 'todoTasks': value,
      if (instance.inProgressTasks case final value?) 'inProgressTasks': value,
      if (instance.inReviewTasks case final value?) 'inReviewTasks': value,
      if (instance.completedTasks case final value?) 'completedTasks': value,
      if (instance.onTimeCompletionRate case final value?)
        'onTimeCompletionRate': value,
      if (instance.avgCompletionHours case final value?)
        'avgCompletionHours': value,
    };
