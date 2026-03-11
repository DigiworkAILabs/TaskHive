// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_distribution_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$TaskDistributionResponseImpl _$$TaskDistributionResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$TaskDistributionResponseImpl(
      status: json['status'] as String?,
      count: (json['count'] as num?)?.toInt(),
    );

Map<String, dynamic> _$$TaskDistributionResponseImplToJson(
        _$TaskDistributionResponseImpl instance) =>
    <String, dynamic>{
      if (instance.status case final value?) 'status': value,
      if (instance.count case final value?) 'count': value,
    };
