// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'update_employee_profile_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

UpdateEmployeeProfileRequest _$UpdateEmployeeProfileRequestFromJson(
    Map<String, dynamic> json) {
  return _UpdateEmployeeProfileRequest.fromJson(json);
}

/// @nodoc
mixin _$UpdateEmployeeProfileRequest {
  String? get phone => throw _privateConstructorUsedError;
  String? get address => throw _privateConstructorUsedError;

  /// Serializes this UpdateEmployeeProfileRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of UpdateEmployeeProfileRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $UpdateEmployeeProfileRequestCopyWith<UpdateEmployeeProfileRequest>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UpdateEmployeeProfileRequestCopyWith<$Res> {
  factory $UpdateEmployeeProfileRequestCopyWith(
          UpdateEmployeeProfileRequest value,
          $Res Function(UpdateEmployeeProfileRequest) then) =
      _$UpdateEmployeeProfileRequestCopyWithImpl<$Res,
          UpdateEmployeeProfileRequest>;
  @useResult
  $Res call({String? phone, String? address});
}

/// @nodoc
class _$UpdateEmployeeProfileRequestCopyWithImpl<$Res,
        $Val extends UpdateEmployeeProfileRequest>
    implements $UpdateEmployeeProfileRequestCopyWith<$Res> {
  _$UpdateEmployeeProfileRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of UpdateEmployeeProfileRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? phone = freezed,
    Object? address = freezed,
  }) {
    return _then(_value.copyWith(
      phone: freezed == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String?,
      address: freezed == address
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$UpdateEmployeeProfileRequestImplCopyWith<$Res>
    implements $UpdateEmployeeProfileRequestCopyWith<$Res> {
  factory _$$UpdateEmployeeProfileRequestImplCopyWith(
          _$UpdateEmployeeProfileRequestImpl value,
          $Res Function(_$UpdateEmployeeProfileRequestImpl) then) =
      __$$UpdateEmployeeProfileRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String? phone, String? address});
}

/// @nodoc
class __$$UpdateEmployeeProfileRequestImplCopyWithImpl<$Res>
    extends _$UpdateEmployeeProfileRequestCopyWithImpl<$Res,
        _$UpdateEmployeeProfileRequestImpl>
    implements _$$UpdateEmployeeProfileRequestImplCopyWith<$Res> {
  __$$UpdateEmployeeProfileRequestImplCopyWithImpl(
      _$UpdateEmployeeProfileRequestImpl _value,
      $Res Function(_$UpdateEmployeeProfileRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of UpdateEmployeeProfileRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? phone = freezed,
    Object? address = freezed,
  }) {
    return _then(_$UpdateEmployeeProfileRequestImpl(
      phone: freezed == phone
          ? _value.phone
          : phone // ignore: cast_nullable_to_non_nullable
              as String?,
      address: freezed == address
          ? _value.address
          : address // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$UpdateEmployeeProfileRequestImpl
    implements _UpdateEmployeeProfileRequest {
  const _$UpdateEmployeeProfileRequestImpl({this.phone, this.address});

  factory _$UpdateEmployeeProfileRequestImpl.fromJson(
          Map<String, dynamic> json) =>
      _$$UpdateEmployeeProfileRequestImplFromJson(json);

  @override
  final String? phone;
  @override
  final String? address;

  @override
  String toString() {
    return 'UpdateEmployeeProfileRequest(phone: $phone, address: $address)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UpdateEmployeeProfileRequestImpl &&
            (identical(other.phone, phone) || other.phone == phone) &&
            (identical(other.address, address) || other.address == address));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, phone, address);

  /// Create a copy of UpdateEmployeeProfileRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$UpdateEmployeeProfileRequestImplCopyWith<
          _$UpdateEmployeeProfileRequestImpl>
      get copyWith => __$$UpdateEmployeeProfileRequestImplCopyWithImpl<
          _$UpdateEmployeeProfileRequestImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$UpdateEmployeeProfileRequestImplToJson(
      this,
    );
  }
}

abstract class _UpdateEmployeeProfileRequest
    implements UpdateEmployeeProfileRequest {
  const factory _UpdateEmployeeProfileRequest(
      {final String? phone,
      final String? address}) = _$UpdateEmployeeProfileRequestImpl;

  factory _UpdateEmployeeProfileRequest.fromJson(Map<String, dynamic> json) =
      _$UpdateEmployeeProfileRequestImpl.fromJson;

  @override
  String? get phone;
  @override
  String? get address;

  /// Create a copy of UpdateEmployeeProfileRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$UpdateEmployeeProfileRequestImplCopyWith<
          _$UpdateEmployeeProfileRequestImpl>
      get copyWith => throw _privateConstructorUsedError;
}
