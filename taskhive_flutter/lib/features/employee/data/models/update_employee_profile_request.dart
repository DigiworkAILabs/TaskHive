import 'package:freezed_annotation/freezed_annotation.dart';

part 'update_employee_profile_request.freezed.dart';
part 'update_employee_profile_request.g.dart';

@freezed
class UpdateEmployeeProfileRequest with _$UpdateEmployeeProfileRequest {
  const factory UpdateEmployeeProfileRequest({
    String? phone,
    String? address,
  }) = _UpdateEmployeeProfileRequest;

  factory UpdateEmployeeProfileRequest.fromJson(Map<String, dynamic> json) =>
      _$UpdateEmployeeProfileRequestFromJson(json);
}
