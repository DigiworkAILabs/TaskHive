// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'update_employee_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

UpdateEmployeeRequest _$UpdateEmployeeRequestFromJson(
    Map<String, dynamic> json) {
  return _UpdateEmployeeRequest.fromJson(json);
}

/// @nodoc
mixin _$UpdateEmployeeRequest {
  String? get firstName => throw _privateConstructorUsedError;
  String? get lastName => throw _privateConstructorUsedError;
  String? get phone => throw _privateConstructorUsedError;
  String? get address => throw _privateConstructorUsedError;
  String? get department => throw _privateConstructorUsedError;
  String? get designation => throw _privateConstructorUsedError;
  String? get joinDate => throw _privateConstructorUsedError;

  /// Serializes this UpdateEmployeeRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of UpdateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $UpdateEmployeeRequestCopyWith<UpdateEmployeeRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UpdateEmployeeRequestCopyWith<$Res> {
  factory $UpdateEmployeeRequestCopyWith(UpdateEmployeeRequest value,
          $Res Function(UpdateEmployeeRequest) then) =
      _$UpdateEmployeeRequestCopyWithImpl<$Res, UpdateEmployeeRequest>;
  @useResult
  $Res call(
      {String? firstName,
      String? lastName,
      String? phone,
      String? address,
      String? department,
      String? designation,
      String? joinDate});
}

/// @nodoc
class _$UpdateEmployeeRequestCopyWithImpl<$Res,
        $Val extends UpdateEmployeeRequest>
    implements $UpdateEmployeeRequestCopyWith<$Res> {
  _$UpdateEmployeeRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of UpdateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? firstName = freezed,
    Object? lastName = freezed,
    Object? phone = freezed,
    Object? address = freezed,
    Object? department = freezed,
    Object? designation = freezed,
    Object? joinDate = freezed,
  }) {
    return _then(_value.copyWith(
      firstName: freezed == firstName
          ? _value.firstName
          : firstName // ignore: cast_nullable_to_non_nullable
              as String?,
      lastName: freezed == lastName
          ? _value.lastName
          : lastName // ignore: cast_nullable_to_non_nullable
              as String?,
      phone: freezed == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String?,
      address: freezed == address
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
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
abstract class _$$UpdateEmployeeRequestImplCopyWith<$Res>
    implements $UpdateEmployeeRequestCopyWith<$Res> {
  factory _$$UpdateEmployeeRequestImplCopyWith(
          _$UpdateEmployeeRequestImpl value,
          $Res Function(_$UpdateEmployeeRequestImpl) then) =
      __$$UpdateEmployeeRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? firstName,
      String? lastName,
      String? phone,
      String? address,
      String? department,
      String? designation,
      String? joinDate});
}

/// @nodoc
class __$$UpdateEmployeeRequestImplCopyWithImpl<$Res>
    extends _$UpdateEmployeeRequestCopyWithImpl<$Res,
        _$UpdateEmployeeRequestImpl>
    implements _$$UpdateEmployeeRequestImplCopyWith<$Res> {
  __$$UpdateEmployeeRequestImplCopyWithImpl(_$UpdateEmployeeRequestImpl _value,
      $Res Function(_$UpdateEmployeeRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of UpdateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? firstName = freezed,
    Object? lastName = freezed,
    Object? phone = freezed,
    Object? address = freezed,
    Object? department = freezed,
    Object? designation = freezed,
    Object? joinDate = freezed,
  }) {
    return _then(_$UpdateEmployeeRequestImpl(
      firstName: freezed == firstName
          ? _value.firstName
          : firstName // ignore: cast_nullable_to_non_nullable
              as String?,
      lastName: freezed == lastName
          ? _value.lastName
          : lastName // ignore: cast_nullable_to_non_nullable
              as String?,
      phone: freezed == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String?,
      address: freezed == address
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
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
class _$UpdateEmployeeRequestImpl implements _UpdateEmployeeRequest {
  const _$UpdateEmployeeRequestImpl(
      {this.firstName,
      this.lastName,
      this.phone,
      this.address,
      this.department,
      this.designation,
      this.joinDate});

  factory _$UpdateEmployeeRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$UpdateEmployeeRequestImplFromJson(json);

  @override
  final String? firstName;
  @override
  final String? lastName;
  @override
  final String? phone;
  @override
  final String? address;
  @override
  final String? department;
  @override
  final String? designation;
  @override
  final String? joinDate;

  @override
  String toString() {
    return 'UpdateEmployeeRequest(firstName: $firstName, lastName: $lastName, phone: $phone, address: $address, department: $department, designation: $designation, joinDate: $joinDate)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UpdateEmployeeRequestImpl &&
            (identical(other.firstName, firstName) ||
                other.firstName == firstName) &&
            (identical(other.lastName, lastName) ||
                other.lastName == lastName) &&
            (identical(other.phone, phone) || other.phone == phone) &&
            (identical(other.address, address) || other.address == address) &&
            (identical(other.department, department) ||
                other.department == department) &&
            (identical(other.designation, designation) ||
                other.designation == designation) &&
            (identical(other.joinDate, joinDate) ||
                other.joinDate == joinDate));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, firstName, lastName, phone,
      address, department, designation, joinDate);

  /// Create a copy of UpdateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$UpdateEmployeeRequestImplCopyWith<_$UpdateEmployeeRequestImpl>
      get copyWith => __$$UpdateEmployeeRequestImplCopyWithImpl<
          _$UpdateEmployeeRequestImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$UpdateEmployeeRequestImplToJson(
      this,
    );
  }
}

abstract class _UpdateEmployeeRequest implements UpdateEmployeeRequest {
  const factory _UpdateEmployeeRequest(
      {final String? firstName,
      final String? lastName,
      final String? phone,
      final String? address,
      final String? department,
      final String? designation,
      final String? joinDate}) = _$UpdateEmployeeRequestImpl;

  factory _UpdateEmployeeRequest.fromJson(Map<String, dynamic> json) =
      _$UpdateEmployeeRequestImpl.fromJson;

  @override
  String? get firstName;
  @override
  String? get lastName;
  @override
  String? get phone;
  @override
  String? get address;
  @override
  String? get department;
  @override
  String? get designation;
  @override
  String? get joinDate;

  /// Create a copy of UpdateEmployeeRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$UpdateEmployeeRequestImplCopyWith<_$UpdateEmployeeRequestImpl>
      get copyWith => throw _privateConstructorUsedError;
}
