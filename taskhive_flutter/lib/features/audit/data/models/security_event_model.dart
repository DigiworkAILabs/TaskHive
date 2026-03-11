import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:flutter/material.dart';

import 'audit_converters.dart';

part 'security_event_model.freezed.dart';
part 'security_event_model.g.dart';

@freezed
class SecurityEventModel with _$SecurityEventModel {
  const factory SecurityEventModel({
    required String id,
    String? eventType,
    String? userId,
    String? ipAddress,
    String? userAgent,
    bool? success,
    @AuditStateConverter() Map<String, dynamic>? details,
    @JsonKey(name: 'timestamp') String? createdAt,
  }) = _SecurityEventModel;

  factory SecurityEventModel.fromJson(Map<String, dynamic> json) =>
      _$SecurityEventModelFromJson(json);
}

extension SecurityEventModelX on SecurityEventModel {
  Color get severityColor {
    switch (eventType ?? '') {
      case 'LOGIN_FAILED':
        return Colors.orange;
      case 'ACCOUNT_LOCKED':
        return Colors.red;
      case 'UNAUTHORIZED_ACCESS':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  IconData get severityIcon {
    switch (eventType ?? '') {
      case 'LOGIN_FAILED':
        return Icons.login;
      case 'ACCOUNT_LOCKED':
        return Icons.lock_clock;
      case 'UNAUTHORIZED_ACCESS':
        return Icons.gpp_bad;
      default:
        return Icons.warning;
    }
  }
}
