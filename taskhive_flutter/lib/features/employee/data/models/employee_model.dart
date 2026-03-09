import 'package:freezed_annotation/freezed_annotation.dart';

part 'employee_model.freezed.dart';
part 'employee_model.g.dart';

enum EmployeeStatus {
  @JsonValue('PENDING')
  pending,
  @JsonValue('ACTIVE')
  active,
  @JsonValue('INACTIVE')
  inactive,
  @JsonValue('DELETED')
  deleted,
}

@freezed
class EmployeeModel with _$EmployeeModel {
  const factory EmployeeModel({
    required String id,
    String? userId,
    required String firstName,
    required String lastName,
    required String email,
    String? phone,
    String? address,
    String? department,
    String? designation,
    String? joinDate,
    String? photoUrl,
    required EmployeeStatus status,
    String? createdAt,
    String? updatedAt,
  }) = _EmployeeModel;

  factory EmployeeModel.fromJson(Map<String, dynamic> json) =>
      _$EmployeeModelFromJson(json);
}

extension EmployeeModelX on EmployeeModel {
  String get fullName => '$firstName $lastName';

  String get initials =>
      '${firstName.isNotEmpty ? firstName[0] : ''}${lastName.isNotEmpty ? lastName[0] : ''}'
          .toUpperCase();

  bool get isActive => status == EmployeeStatus.active;
  bool get isPending => status == EmployeeStatus.pending;
  bool get isInactive => status == EmployeeStatus.inactive;
  bool get isDeleted => status == EmployeeStatus.deleted;

  bool get canActivate =>
      status == EmployeeStatus.pending || status == EmployeeStatus.inactive;

  bool get canDeactivate => status == EmployeeStatus.active;
}
