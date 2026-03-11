// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'admin_dashboard_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AdminDashboardResponseImpl _$$AdminDashboardResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$AdminDashboardResponseImpl(
      totalTasks: (json['totalTasks'] as num?)?.toInt(),
      activeTasks: (json['activeTasks'] as num?)?.toInt(),
      overdueTasks: (json['overdueTasks'] as num?)?.toInt(),
      completedTasks: (json['completedTasks'] as num?)?.toInt(),
      completionRate: (json['completionRate'] as num?)?.toDouble(),
      totalEmployees: (json['totalEmployees'] as num?)?.toInt(),
      avgCompletionHours: (json['avgCompletionHours'] as num?)?.toDouble(),
      metricDate: json['metricDate'] as String?,
    );

Map<String, dynamic> _$$AdminDashboardResponseImplToJson(
        _$AdminDashboardResponseImpl instance) =>
    <String, dynamic>{
      if (instance.totalTasks case final value?) 'totalTasks': value,
      if (instance.activeTasks case final value?) 'activeTasks': value,
      if (instance.overdueTasks case final value?) 'overdueTasks': value,
      if (instance.completedTasks case final value?) 'completedTasks': value,
      if (instance.completionRate case final value?) 'completionRate': value,
      if (instance.totalEmployees case final value?) 'totalEmployees': value,
      if (instance.avgCompletionHours case final value?)
        'avgCompletionHours': value,
      if (instance.metricDate case final value?) 'metricDate': value,
    };
