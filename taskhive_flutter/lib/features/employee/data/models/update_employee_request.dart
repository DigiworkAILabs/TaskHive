import 'package:freezed_annotation/freezed_annotation.dart';

part 'update_employee_request.freezed.dart';
part 'update_employee_request.g.dart';

@freezed
class UpdateEmployeeRequest with _$UpdateEmployeeRequest {
  const factory UpdateEmployeeRequest({
    String? firstName,
    String? lastName,
    String? phone,
    String? address,
    String? department,
    String? designation,
    String? joinDate,
  }) = _UpdateEmployeeRequest;

  factory UpdateEmployeeRequest.fromJson(Map<String, dynamic> json) =>
      _$UpdateEmployeeRequestFromJson(json);
}
