import 'package:freezed_annotation/freezed_annotation.dart';

part 'employee_status_history_model.freezed.dart';
part 'employee_status_history_model.g.dart';

@freezed
class EmployeeStatusHistoryModel with _$EmployeeStatusHistoryModel {
  const factory EmployeeStatusHistoryModel({
    required String id,
    required String employeeId,
    String? oldStatus,
    required String newStatus,
    required String changedBy,
    String? reason,
    required String changedAt,
  }) = _EmployeeStatusHistoryModel;

  factory EmployeeStatusHistoryModel.fromJson(Map<String, dynamic> json) =>
      _$EmployeeStatusHistoryModelFromJson(json);
}
