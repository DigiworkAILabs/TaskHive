// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'security_event_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

SecurityEventModel _$SecurityEventModelFromJson(Map<String, dynamic> json) {
  return _SecurityEventModel.fromJson(json);
}

/// @nodoc
mixin _$SecurityEventModel {
  String get id => throw _privateConstructorUsedError;
  String? get eventType => throw _privateConstructorUsedError;
  String? get userId => throw _privateConstructorUsedError;
  String? get ipAddress => throw _privateConstructorUsedError;
  String? get userAgent => throw _privateConstructorUsedError;
  bool? get success => throw _privateConstructorUsedError;
  @AuditStateConverter()
  Map<String, dynamic>? get details => throw _privateConstructorUsedError;
  @JsonKey(name: 'timestamp')
  String? get createdAt => throw _privateConstructorUsedError;

  /// Serializes this SecurityEventModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of SecurityEventModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $SecurityEventModelCopyWith<SecurityEventModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SecurityEventModelCopyWith<$Res> {
  factory $SecurityEventModelCopyWith(
          SecurityEventModel value, $Res Function(SecurityEventModel) then) =
      _$SecurityEventModelCopyWithImpl<$Res, SecurityEventModel>;
  @useResult
  $Res call(
      {String id,
      String? eventType,
      String? userId,
      String? ipAddress,
      String? userAgent,
      bool? success,
      @AuditStateConverter() Map<String, dynamic>? details,
      @JsonKey(name: 'timestamp') String? createdAt});
}

/// @nodoc
class _$SecurityEventModelCopyWithImpl<$Res, $Val extends SecurityEventModel>
    implements $SecurityEventModelCopyWith<$Res> {
  _$SecurityEventModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of SecurityEventModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? eventType = freezed,
    Object? userId = freezed,
    Object? ipAddress = freezed,
    Object? userAgent = freezed,
    Object? success = freezed,
    Object? details = freezed,
    Object? createdAt = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      eventType: freezed == eventType
          ? _value.eventType
          : eventType // ignore: cast_nullable_to_non_nullable
              as String?,
      userId: freezed == userId
          ? _value.userId
          : userId // ignore: cast_nullable_to_non_nullable
              as String?,
      ipAddress: freezed == ipAddress
          ? _value.ipAddress
          : ipAddress // ignore: cast_nullable_to_non_nullable
              as String?,
      userAgent: freezed == userAgent
          ? _value.userAgent
          : userAgent // ignore: cast_nullable_to_non_nullable
              as String?,
      success: freezed == success
          ? _value.success
          : success // ignore: cast_nullable_to_non_nullable
              as bool?,
      details: freezed == details
          ? _value.details
          : details // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SecurityEventModelImplCopyWith<$Res>
    implements $SecurityEventModelCopyWith<$Res> {
  factory _$$SecurityEventModelImplCopyWith(_$SecurityEventModelImpl value,
          $Res Function(_$SecurityEventModelImpl) then) =
      __$$SecurityEventModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String? eventType,
      String? userId,
      String? ipAddress,
      String? userAgent,
      bool? success,
      @AuditStateConverter() Map<String, dynamic>? details,
      @JsonKey(name: 'timestamp') String? createdAt});
}

/// @nodoc
class __$$SecurityEventModelImplCopyWithImpl<$Res>
    extends _$SecurityEventModelCopyWithImpl<$Res, _$SecurityEventModelImpl>
    implements _$$SecurityEventModelImplCopyWith<$Res> {
  __$$SecurityEventModelImplCopyWithImpl(_$SecurityEventModelImpl _value,
      $Res Function(_$SecurityEventModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of SecurityEventModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? eventType = freezed,
    Object? userId = freezed,
    Object? ipAddress = freezed,
    Object? userAgent = freezed,
    Object? success = freezed,
    Object? details = freezed,
    Object? createdAt = freezed,
  }) {
    return _then(_$SecurityEventModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      eventType: freezed == eventType
          ? _value.eventType
          : eventType // ignore: cast_nullable_to_non_nullable
              as String?,
      userId: freezed == userId
          ? _value.userId
          : userId // ignore: cast_nullable_to_non_nullable
              as String?,
      ipAddress: freezed == ipAddress
          ? _value.ipAddress
          : ipAddress // ignore: cast_nullable_to_non_nullable
              as String?,
      userAgent: freezed == userAgent
          ? _value.userAgent
          : userAgent // ignore: cast_nullable_to_non_nullable
              as String?,
      success: freezed == success
          ? _value.success
          : success // ignore: cast_nullable_to_non_nullable
              as bool?,
      details: freezed == details
          ? _value._details
          : details // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SecurityEventModelImpl implements _SecurityEventModel {
  const _$SecurityEventModelImpl(
      {required this.id,
      this.eventType,
      this.userId,
      this.ipAddress,
      this.userAgent,
      this.success,
      @AuditStateConverter() final Map<String, dynamic>? details,
      @JsonKey(name: 'timestamp') this.createdAt})
      : _details = details;

  factory _$SecurityEventModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$SecurityEventModelImplFromJson(json);

  @override
  final String id;
  @override
  final String? eventType;
  @override
  final String? userId;
  @override
  final String? ipAddress;
  @override
  final String? userAgent;
  @override
  final bool? success;
  final Map<String, dynamic>? _details;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get details {
    final value = _details;
    if (value == null) return null;
    if (_details is EqualUnmodifiableMapView) return _details;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  @override
  @JsonKey(name: 'timestamp')
  final String? createdAt;

  @override
  String toString() {
    return 'SecurityEventModel(id: $id, eventType: $eventType, userId: $userId, ipAddress: $ipAddress, userAgent: $userAgent, success: $success, details: $details, createdAt: $createdAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SecurityEventModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.eventType, eventType) ||
                other.eventType == eventType) &&
            (identical(other.userId, userId) || other.userId == userId) &&
            (identical(other.ipAddress, ipAddress) ||
                other.ipAddress == ipAddress) &&
            (identical(other.userAgent, userAgent) ||
                other.userAgent == userAgent) &&
            (identical(other.success, success) || other.success == success) &&
            const DeepCollectionEquality().equals(other._details, _details) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      eventType,
      userId,
      ipAddress,
      userAgent,
      success,
      const DeepCollectionEquality().hash(_details),
      createdAt);

  /// Create a copy of SecurityEventModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$SecurityEventModelImplCopyWith<_$SecurityEventModelImpl> get copyWith =>
      __$$SecurityEventModelImplCopyWithImpl<_$SecurityEventModelImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SecurityEventModelImplToJson(
      this,
    );
  }
}

abstract class _SecurityEventModel implements SecurityEventModel {
  const factory _SecurityEventModel(
          {required final String id,
          final String? eventType,
          final String? userId,
          final String? ipAddress,
          final String? userAgent,
          final bool? success,
          @AuditStateConverter() final Map<String, dynamic>? details,
          @JsonKey(name: 'timestamp') final String? createdAt}) =
      _$SecurityEventModelImpl;

  factory _SecurityEventModel.fromJson(Map<String, dynamic> json) =
      _$SecurityEventModelImpl.fromJson;

  @override
  String get id;
  @override
  String? get eventType;
  @override
  String? get userId;
  @override
  String? get ipAddress;
  @override
  String? get userAgent;
  @override
  bool? get success;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get details;
  @override
  @JsonKey(name: 'timestamp')
  String? get createdAt;

  /// Create a copy of SecurityEventModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$SecurityEventModelImplCopyWith<_$SecurityEventModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
