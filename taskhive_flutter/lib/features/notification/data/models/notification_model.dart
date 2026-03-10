import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:flutter/material.dart';

part 'notification_model.freezed.dart';
part 'notification_model.g.dart';

@freezed
class NotificationModel with _$NotificationModel {
  const factory NotificationModel({
    required String id,
    String? userId,
    String? type,
    String? title,
    String? message,
    @JsonKey(name: 'isRead', readValue: _readStatusValue)
    @Default(false)
    bool isRead,
    String? readAt,
    String? entityType,
    String? entityId,
    String? createdAt,
  }) = _NotificationModel;

  factory NotificationModel.fromJson(Map<String, dynamic> json) =>
      _$NotificationModelFromJson(json);
}

Object? _readStatusValue(Map json, String key) =>
    json['isRead'] ?? json['read'];

extension NotificationModelX on NotificationModel {
  bool get isUnread => !isRead;

  IconData get icon {
    switch (type) {
      case 'TASK_ASSIGNED':
        return Icons.assignment_ind;
      case 'TASK_OVERDUE':
        return Icons.warning_amber;
      case 'TASK_STATUS_CHANGED':
        return Icons.sync;
      case 'TASK_COMMENT_ADDED':
        return Icons.comment;
      default:
        return Icons.notifications;
    }
  }
}
