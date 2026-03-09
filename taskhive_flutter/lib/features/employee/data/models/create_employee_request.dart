import 'package:freezed_annotation/freezed_annotation.dart';

part 'create_employee_request.freezed.dart';
part 'create_employee_request.g.dart';

@freezed
class CreateEmployeeRequest with _$CreateEmployeeRequest {
  const factory CreateEmployeeRequest({
    required String firstName,
    required String lastName,
    required String email,
    String? phone,
    String? department,
    String? designation,
    String? joinDate,
  }) = _CreateEmployeeRequest;

  factory CreateEmployeeRequest.fromJson(Map<String, dynamic> json) =>
      _$CreateEmployeeRequestFromJson(json);
}
