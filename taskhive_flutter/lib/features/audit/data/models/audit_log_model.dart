import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:flutter/material.dart';

import 'audit_converters.dart';

part 'audit_log_model.freezed.dart';
part 'audit_log_model.g.dart';

@freezed
class AuditLogModel with _$AuditLogModel {
  const factory AuditLogModel({
    required String id,
    String? actorId,
    String? actorEmail,
    String? action,
    String? entityType,
    String? entityId,
    @AuditStateConverter() Map<String, dynamic>? beforeState,
    @AuditStateConverter() Map<String, dynamic>? afterState,
    @AuditStateConverter() Map<String, dynamic>? metadata,
    String? ipAddress,
    String? userAgent,
    @JsonKey(name: 'createdAt') String? createdAt,
  }) = _AuditLogModel;

  factory AuditLogModel.fromJson(Map<String, dynamic> json) =>
      _$AuditLogModelFromJson(json);
}

extension AuditLogModelX on AuditLogModel {
  bool get hasStateDiff => beforeState != null && afterState != null;
  bool get isCreation => beforeState == null && afterState != null;
  bool get isDeletion => beforeState != null && afterState == null;

  IconData get actionIcon {
    final act = action ?? '';
    if (act.startsWith('EMPLOYEE_')) return Icons.person;
    if (act.startsWith('TASK_')) return Icons.assignment;
    if (act.startsWith('LOGIN_')) return Icons.lock;
    return Icons.history;
  }
}
