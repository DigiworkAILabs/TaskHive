// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'create_employee_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

CreateEmployeeRequest _$CreateEmployeeRequestFromJson(
    Map<String, dynamic> json) {
  return _CreateEmployeeRequest.fromJson(json);
}

/// @nodoc
mixin _$CreateEmployeeRequest {
  String get firstName => throw _privateConstructorUsedError;
  String get lastName => throw _privateConstructorUsedError;
  String get email => throw _privateConstructorUsedError;
  String? get phone => throw _privateConstructorUsedError;
  String? get department => throw _privateConstructorUsedError;
  String? get designation => throw _privateConstructorUsedError;
  String? get joinDate => throw _privateConstructorUsedError;

  /// Serializes this CreateEmployeeRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of CreateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $CreateEmployeeRequestCopyWith<CreateEmployeeRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CreateEmployeeRequestCopyWith<$Res> {
  factory $CreateEmployeeRequestCopyWith(CreateEmployeeRequest value,
          $Res Function(CreateEmployeeRequest) then) =
      _$CreateEmployeeRequestCopyWithImpl<$Res, CreateEmployeeRequest>;
  @useResult
  $Res call(
      {String firstName,
      String lastName,
      String email,
      String? phone,
      String? department,
      String? designation,
      String? joinDate});
}

/// @nodoc
class _$CreateEmployeeRequestCopyWithImpl<$Res,
        $Val extends CreateEmployeeRequest>
    implements $CreateEmployeeRequestCopyWith<$Res> {
  _$CreateEmployeeRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of CreateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? firstName = null,
    Object? lastName = null,
    Object? email = null,
    Object? phone = freezed,
    Object? department = freezed,
    Object? designation = freezed,
    Object? joinDate = freezed,
  }) {
    return _then(_value.copyWith(
      firstName: null == firstName
          ? _value.firstName
          : firstName // ignore: cast_nullable_to_non_nullable
              as String,
      lastName: null == lastName
          ? _value.lastName
          : lastName // ignore: cast_nullable_to_non_nullable
              as String,
      email: null == email
          ? _value.email
          : email // ignore: cast_nullable_to_non_nullable
              as String,
      phone: freezed == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String?,
      department: freezed == department
          ? _value.department
          : department // ignore: cast_nullable_to_non_nullable
              as String?,
      designation: freezed == designation
          ? _value.designation
          : designation // ignore: cast_nullable_to_non_nullable
              as String?,
      joinDate: freezed == joinDate
          ? _value.joinDate
          : joinDate // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$CreateEmployeeRequestImplCopyWith<$Res>
    implements $CreateEmployeeRequestCopyWith<$Res> {
  factory _$$CreateEmployeeRequestImplCopyWith(
          _$CreateEmployeeRequestImpl value,
          $Res Function(_$CreateEmployeeRequestImpl) then) =
      __$$CreateEmployeeRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String firstName,
      String lastName,
      String email,
      String? phone,
      String? department,
      String? designation,
      String? joinDate});
}

/// @nodoc
class __$$CreateEmployeeRequestImplCopyWithImpl<$Res>
    extends _$CreateEmployeeRequestCopyWithImpl<$Res,
        _$CreateEmployeeRequestImpl>
    implements _$$CreateEmployeeRequestImplCopyWith<$Res> {
  __$$CreateEmployeeRequestImplCopyWithImpl(_$CreateEmployeeRequestImpl _value,
      $Res Function(_$CreateEmployeeRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of CreateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? firstName = null,
    Object? lastName = null,
    Object? email = null,
    Object? phone = freezed,
    Object? department = freezed,
    Object? designation = freezed,
    Object? joinDate = freezed,
  }) {
    return _then(_$CreateEmployeeRequestImpl(
      firstName: null == firstName
          ? _value.firstName
          : firstName // ignore: cast_nullable_to_non_nullable
              as String,
      lastName: null == lastName
          ? _value.lastName
          : lastName // ignore: cast_nullable_to_non_nullable
              as String,
      email: null == email
          ? _value.email
          : email // ignore: cast_nullable_to_non_nullable
              as String,
      phone: freezed == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String?,
      department: freezed == department
          ? _value.department
          : department // ignore: cast_nullable_to_non_nullable
              as String?,
      designation: freezed == designation
          ? _value.designation
          : designation // ignore: cast_nullable_to_non_nullable
              as String?,
      joinDate: freezed == joinDate
          ? _value.joinDate
          : joinDate // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$CreateEmployeeRequestImpl implements _CreateEmployeeRequest {
  const _$CreateEmployeeRequestImpl(
      {required this.firstName,
      required this.lastName,
      required this.email,
      this.phone,
      this.department,
      this.designation,
      this.joinDate});

  factory _$CreateEmployeeRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$CreateEmployeeRequestImplFromJson(json);

  @override
  final String firstName;
  @override
  final String lastName;
  @override
  final String email;
  @override
  final String? phone;
  @override
  final String? department;
  @override
  final String? designation;
  @override
  final String? joinDate;

  @override
  String toString() {
    return 'CreateEmployeeRequest(firstName: $firstName, lastName: $lastName, email: $email, phone: $phone, department: $department, designation: $designation, joinDate: $joinDate)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CreateEmployeeRequestImpl &&
            (identical(other.firstName, firstName) ||
                other.firstName == firstName) &&
            (identical(other.lastName, lastName) ||
                other.lastName == lastName) &&
            (identical(other.email, email) || other.email == email) &&
            (identical(other.phone, phone) || other.phone == phone) &&
            (identical(other.department, department) ||
                other.department == department) &&
            (identical(other.designation, designation) ||
                other.designation == designation) &&
            (identical(other.joinDate, joinDate) ||
                other.joinDate == joinDate));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, firstName, lastName, email,
      phone, department, designation, joinDate);

  /// Create a copy of CreateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$CreateEmployeeRequestImplCopyWith<_$CreateEmployeeRequestImpl>
      get copyWith => __$$CreateEmployeeRequestImplCopyWithImpl<
          _$CreateEmployeeRequestImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$CreateEmployeeRequestImplToJson(
      this,
    );
  }
}

abstract class _CreateEmployeeRequest implements CreateEmployeeRequest {
  const factory _CreateEmployeeRequest(
      {required final String firstName,
      required final String lastName,
      required final String email,
      final String? phone,
      final String? department,
      final String? designation,
      final String? joinDate}) = _$CreateEmployeeRequestImpl;

  factory _CreateEmployeeRequest.fromJson(Map<String, dynamic> json) =
      _$CreateEmployeeRequestImpl.fromJson;

  @override
  String get firstName;
  @override
  String get lastName;
  @override
  String get email;
  @override
  String? get phone;
  @override
  String? get department;
  @override
  String? get designation;
  @override
  String? get joinDate;

  /// Create a copy of CreateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$CreateEmployeeRequestImplCopyWith<_$CreateEmployeeRequestImpl>
      get copyWith => throw _privateConstructorUsedError;
}
