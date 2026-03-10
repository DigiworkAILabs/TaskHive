// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_preference_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$NotificationPreferenceModelImpl _$$NotificationPreferenceModelImplFromJson(
        Map<String, dynamic> json) =>
    _$NotificationPreferenceModelImpl(
      emailEnabled: json['emailEnabled'] as bool? ?? true,
      inAppEnabled: json['inAppEnabled'] as bool? ?? true,
      taskAssigned: json['taskAssigned'] as bool? ?? true,
      taskOverdue: json['taskOverdue'] as bool? ?? true,
      dailyDigest: json['dailyDigest'] as bool? ?? false,
      digestTime: json['digestTime'] as String?,
    );

Map<String, dynamic> _$$NotificationPreferenceModelImplToJson(
        _$NotificationPreferenceModelImpl instance) =>
    <String, dynamic>{
      'emailEnabled': instance.emailEnabled,
      'inAppEnabled': instance.inAppEnabled,
      'taskAssigned': instance.taskAssigned,
      'taskOverdue': instance.taskOverdue,
      'dailyDigest': instance.dailyDigest,
      if (instance.digestTime case final value?) 'digestTime': value,
    };
