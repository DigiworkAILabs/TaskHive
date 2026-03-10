// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'security_event_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SecurityEventModelImpl _$$SecurityEventModelImplFromJson(
        Map<String, dynamic> json) =>
    _$SecurityEventModelImpl(
      id: json['id'] as String,
      eventType: json['eventType'] as String?,
      userId: json['userId'] as String?,
      ipAddress: json['ipAddress'] as String?,
      userAgent: json['userAgent'] as String?,
      success: json['success'] as bool?,
      details: const AuditStateConverter().fromJson(json['details']),
      createdAt: json['timestamp'] as String?,
    );

Map<String, dynamic> _$$SecurityEventModelImplToJson(
        _$SecurityEventModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      if (instance.eventType case final value?) 'eventType': value,
      if (instance.userId case final value?) 'userId': value,
      if (instance.ipAddress case final value?) 'ipAddress': value,
      if (instance.userAgent case final value?) 'userAgent': value,
      if (instance.success case final value?) 'success': value,
      if (const AuditStateConverter().toJson(instance.details)
          case final value?)
        'details': value,
      if (instance.createdAt case final value?) 'timestamp': value,
    };
