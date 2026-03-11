// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_completion_trend_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$TaskCompletionTrendResponseImpl _$$TaskCompletionTrendResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskCompletionTrendResponseImpl(
      date: json['date'] as String?,
      count: (json['count'] as num?)?.toInt(),
    );

Map<String, dynamic> _$$TaskCompletionTrendResponseImplToJson(
        _$TaskCompletionTrendResponseImpl instance) =>
    <String, dynamic>{
      if (instance.date case final value?) 'date': value,
      if (instance.count case final value?) 'count': value,
    };
