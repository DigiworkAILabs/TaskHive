// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'audit_log_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AuditLogModelImpl _$$AuditLogModelImplFromJson(Map<String, dynamic> json) =>
    _$AuditLogModelImpl(
      id: json['id'] as String,
      actorId: json['actorId'] as String?,
      actorEmail: json['actorEmail'] as String?,
      action: json['action'] as String?,
      entityType: json['entityType'] as String?,
      entityId: json['entityId'] as String?,
      beforeState: const AuditStateConverter().fromJson(json['beforeState']),
      afterState: const AuditStateConverter().fromJson(json['afterState']),
      metadata: const AuditStateConverter().fromJson(json['metadata']),
      ipAddress: json['ipAddress'] as String?,
      userAgent: json['userAgent'] as String?,
      createdAt: json['createdAt'] as String?,
    );

Map<String, dynamic> _$$AuditLogModelImplToJson(_$AuditLogModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      if (instance.actorId case final value?) 'actorId': value,
      if (instance.actorEmail case final value?) 'actorEmail': value,
      if (instance.action case final value?) 'action': value,
      if (instance.entityType case final value?) 'entityType': value,
      if (instance.entityId case final value?) 'entityId': value,
      if (const AuditStateConverter().toJson(instance.beforeState)
          case final value?)
        'beforeState': value,
      if (const AuditStateConverter().toJson(instance.afterState)
          case final value?)
        'afterState': value,
      if (const AuditStateConverter().toJson(instance.metadata)
          case final value?)
        'metadata': value,
      if (instance.ipAddress case final value?) 'ipAddress': value,
      if (instance.userAgent case final value?) 'userAgent': value,
      if (instance.createdAt case final value?) 'createdAt': value,
    };
