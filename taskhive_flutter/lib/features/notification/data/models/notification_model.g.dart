// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$NotificationModelImpl _$$NotificationModelImplFromJson(
        Map<String, dynamic> json) =>
    _$NotificationModelImpl(
      id: json['id'] as String,
      userId: json['userId'] as String?,
      type: json['type'] as String?,
      title: json['title'] as String?,
      message: json['message'] as String?,
      isRead: _readStatusValue(json, 'isRead') as bool? ?? false,
      readAt: json['readAt'] as String?,
      entityType: json['entityType'] as String?,
      entityId: json['entityId'] as String?,
      createdAt: json['createdAt'] as String?,
    );

Map<String, dynamic> _$$NotificationModelImplToJson(
        _$NotificationModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      if (instance.userId case final value?) 'userId': value,
      if (instance.type case final value?) 'type': value,
      if (instance.title case final value?) 'title': value,
      if (instance.message case final value?) 'message': value,
      'isRead': instance.isRead,
      if (instance.readAt case final value?) 'readAt': value,
      if (instance.entityType case final value?) 'entityType': value,
      if (instance.entityId case final value?) 'entityId': value,
      if (instance.createdAt case final value?) 'createdAt': value,
    };
