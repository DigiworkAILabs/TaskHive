// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_status_history_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$EmployeeStatusHistoryModelImpl _$$EmployeeStatusHistoryModelImplFromJson(
        Map<String, dynamic> json) =>
    _$EmployeeStatusHistoryModelImpl(
      id: json['id'] as String,
      employeeId: json['employeeId'] as String,
      oldStatus: json['oldStatus'] as String?,
      newStatus: json['newStatus'] as String,
      changedBy: json['changedBy'] as String,
      reason: json['reason'] as String?,
      changedAt: json['changedAt'] as String,
    );

Map<String, dynamic> _$$EmployeeStatusHistoryModelImplToJson(
        _$EmployeeStatusHistoryModelImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'employeeId': instance.employeeId,
      if (instance.oldStatus case final value?) 'oldStatus': value,
      'newStatus': instance.newStatus,
      'changedBy': instance.changedBy,
      if (instance.reason case final value?) 'reason': value,
      'changedAt': instance.changedAt,
    };
