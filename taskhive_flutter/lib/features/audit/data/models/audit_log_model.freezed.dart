// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'audit_log_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

AuditLogModel _$AuditLogModelFromJson(Map<String, dynamic> json) {
  return _AuditLogModel.fromJson(json);
}

/// @nodoc
mixin _$AuditLogModel {
  String get id => throw _privateConstructorUsedError;
  String? get actorId => throw _privateConstructorUsedError;
  String? get actorEmail => throw _privateConstructorUsedError;
  String? get action => throw _privateConstructorUsedError;
  String? get entityType => throw _privateConstructorUsedError;
  String? get entityId => throw _privateConstructorUsedError;
  @AuditStateConverter()
  Map<String, dynamic>? get beforeState => throw _privateConstructorUsedError;
  @AuditStateConverter()
  Map<String, dynamic>? get afterState => throw _privateConstructorUsedError;
  @AuditStateConverter()
  Map<String, dynamic>? get metadata => throw _privateConstructorUsedError;
  String? get ipAddress => throw _privateConstructorUsedError;
  String? get userAgent => throw _privateConstructorUsedError;
  @JsonKey(name: 'createdAt')
  String? get createdAt => throw _privateConstructorUsedError;

  /// Serializes this AuditLogModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AuditLogModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AuditLogModelCopyWith<AuditLogModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AuditLogModelCopyWith<$Res> {
  factory $AuditLogModelCopyWith(
          AuditLogModel value, $Res Function(AuditLogModel) then) =
      _$AuditLogModelCopyWithImpl<$Res, AuditLogModel>;
  @useResult
  $Res call(
      {String id,
      String? actorId,
      String? actorEmail,
      String? action,
      String? entityType,
      String? entityId,
      @AuditStateConverter() Map<String, dynamic>? beforeState,
      @AuditStateConverter() Map<String, dynamic>? afterState,
      @AuditStateConverter() Map<String, dynamic>? metadata,
      String? ipAddress,
      String? userAgent,
      @JsonKey(name: 'createdAt') String? createdAt});
}

/// @nodoc
class _$AuditLogModelCopyWithImpl<$Res, $Val extends AuditLogModel>
    implements $AuditLogModelCopyWith<$Res> {
  _$AuditLogModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AuditLogModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? actorId = freezed,
    Object? actorEmail = freezed,
    Object? action = freezed,
    Object? entityType = freezed,
    Object? entityId = freezed,
    Object? beforeState = freezed,
    Object? afterState = freezed,
    Object? metadata = freezed,
    Object? ipAddress = freezed,
    Object? userAgent = freezed,
    Object? createdAt = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      actorId: freezed == actorId
          ? _value.actorId
          : actorId // ignore: cast_nullable_to_non_nullable
              as String?,
      actorEmail: freezed == actorEmail
          ? _value.actorEmail
          : actorEmail // ignore: cast_nullable_to_non_nullable
              as String?,
      action: freezed == action
          ? _value.action
          : action // ignore: cast_nullable_to_non_nullable
              as String?,
      entityType: freezed == entityType
          ? _value.entityType
          : entityType // ignore: cast_nullable_to_non_nullable
              as String?,
      entityId: freezed == entityId
          ? _value.entityId
          : entityId // ignore: cast_nullable_to_non_nullable
              as String?,
      beforeState: freezed == beforeState
          ? _value.beforeState
          : beforeState // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      afterState: freezed == afterState
          ? _value.afterState
          : afterState // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      metadata: freezed == metadata
          ? _value.metadata
          : metadata // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      ipAddress: freezed == ipAddress
          ? _value.ipAddress
          : ipAddress // ignore: cast_nullable_to_non_nullable
              as String?,
      userAgent: freezed == userAgent
          ? _value.userAgent
          : userAgent // ignore: cast_nullable_to_non_nullable
              as String?,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AuditLogModelImplCopyWith<$Res>
    implements $AuditLogModelCopyWith<$Res> {
  factory _$$AuditLogModelImplCopyWith(
          _$AuditLogModelImpl value, $Res Function(_$AuditLogModelImpl) then) =
      __$$AuditLogModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String? actorId,
      String? actorEmail,
      String? action,
      String? entityType,
      String? entityId,
      @AuditStateConverter() Map<String, dynamic>? beforeState,
      @AuditStateConverter() Map<String, dynamic>? afterState,
      @AuditStateConverter() Map<String, dynamic>? metadata,
      String? ipAddress,
      String? userAgent,
      @JsonKey(name: 'createdAt') String? createdAt});
}

/// @nodoc
class __$$AuditLogModelImplCopyWithImpl<$Res>
    extends _$AuditLogModelCopyWithImpl<$Res, _$AuditLogModelImpl>
    implements _$$AuditLogModelImplCopyWith<$Res> {
  __$$AuditLogModelImplCopyWithImpl(
      _$AuditLogModelImpl _value, $Res Function(_$AuditLogModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of AuditLogModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? actorId = freezed,
    Object? actorEmail = freezed,
    Object? action = freezed,
    Object? entityType = freezed,
    Object? entityId = freezed,
    Object? beforeState = freezed,
    Object? afterState = freezed,
    Object? metadata = freezed,
    Object? ipAddress = freezed,
    Object? userAgent = freezed,
    Object? createdAt = freezed,
  }) {
    return _then(_$AuditLogModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      actorId: freezed == actorId
          ? _value.actorId
          : actorId // ignore: cast_nullable_to_non_nullable
              as String?,
      actorEmail: freezed == actorEmail
          ? _value.actorEmail
          : actorEmail // ignore: cast_nullable_to_non_nullable
              as String?,
      action: freezed == action
          ? _value.action
          : action // ignore: cast_nullable_to_non_nullable
              as String?,
      entityType: freezed == entityType
          ? _value.entityType
          : entityType // ignore: cast_nullable_to_non_nullable
              as String?,
      entityId: freezed == entityId
          ? _value.entityId
          : entityId // ignore: cast_nullable_to_non_nullable
              as String?,
      beforeState: freezed == beforeState
          ? _value._beforeState
          : beforeState // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      afterState: freezed == afterState
          ? _value._afterState
          : afterState // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      metadata: freezed == metadata
          ? _value._metadata
          : metadata // ignore: cast_nullable_to_non_nullable
              as Map<String, dynamic>?,
      ipAddress: freezed == ipAddress
          ? _value.ipAddress
          : ipAddress // ignore: cast_nullable_to_non_nullable
              as String?,
      userAgent: freezed == userAgent
          ? _value.userAgent
          : userAgent // ignore: cast_nullable_to_non_nullable
              as String?,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AuditLogModelImpl implements _AuditLogModel {
  const _$AuditLogModelImpl(
      {required this.id,
      this.actorId,
      this.actorEmail,
      this.action,
      this.entityType,
      this.entityId,
      @AuditStateConverter() final Map<String, dynamic>? beforeState,
      @AuditStateConverter() final Map<String, dynamic>? afterState,
      @AuditStateConverter() final Map<String, dynamic>? metadata,
      this.ipAddress,
      this.userAgent,
      @JsonKey(name: 'createdAt') this.createdAt})
      : _beforeState = beforeState,
        _afterState = afterState,
        _metadata = metadata;

  factory _$AuditLogModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$AuditLogModelImplFromJson(json);

  @override
  final String id;
  @override
  final String? actorId;
  @override
  final String? actorEmail;
  @override
  final String? action;
  @override
  final String? entityType;
  @override
  final String? entityId;
  final Map<String, dynamic>? _beforeState;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get beforeState {
    final value = _beforeState;
    if (value == null) return null;
    if (_beforeState is EqualUnmodifiableMapView) return _beforeState;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  final Map<String, dynamic>? _afterState;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get afterState {
    final value = _afterState;
    if (value == null) return null;
    if (_afterState is EqualUnmodifiableMapView) return _afterState;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  final Map<String, dynamic>? _metadata;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get metadata {
    final value = _metadata;
    if (value == null) return null;
    if (_metadata is EqualUnmodifiableMapView) return _metadata;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(value);
  }

  @override
  final String? ipAddress;
  @override
  final String? userAgent;
  @override
  @JsonKey(name: 'createdAt')
  final String? createdAt;

  @override
  String toString() {
    return 'AuditLogModel(id: $id, actorId: $actorId, actorEmail: $actorEmail, action: $action, entityType: $entityType, entityId: $entityId, beforeState: $beforeState, afterState: $afterState, metadata: $metadata, ipAddress: $ipAddress, userAgent: $userAgent, createdAt: $createdAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AuditLogModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.actorId, actorId) || other.actorId == actorId) &&
            (identical(other.actorEmail, actorEmail) ||
                other.actorEmail == actorEmail) &&
            (identical(other.action, action) || other.action == action) &&
            (identical(other.entityType, entityType) ||
                other.entityType == entityType) &&
            (identical(other.entityId, entityId) ||
                other.entityId == entityId) &&
            const DeepCollectionEquality()
                .equals(other._beforeState, _beforeState) &&
            const DeepCollectionEquality()
                .equals(other._afterState, _afterState) &&
            const DeepCollectionEquality().equals(other._metadata, _metadata) &&
            (identical(other.ipAddress, ipAddress) ||
                other.ipAddress == ipAddress) &&
            (identical(other.userAgent, userAgent) ||
                other.userAgent == userAgent) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      actorId,
      actorEmail,
      action,
      entityType,
      entityId,
      const DeepCollectionEquality().hash(_beforeState),
      const DeepCollectionEquality().hash(_afterState),
      const DeepCollectionEquality().hash(_metadata),
      ipAddress,
      userAgent,
      createdAt);

  /// Create a copy of AuditLogModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AuditLogModelImplCopyWith<_$AuditLogModelImpl> get copyWith =>
      __$$AuditLogModelImplCopyWithImpl<_$AuditLogModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AuditLogModelImplToJson(
      this,
    );
  }
}

abstract class _AuditLogModel implements AuditLogModel {
  const factory _AuditLogModel(
          {required final String id,
          final String? actorId,
          final String? actorEmail,
          final String? action,
          final String? entityType,
          final String? entityId,
          @AuditStateConverter() final Map<String, dynamic>? beforeState,
          @AuditStateConverter() final Map<String, dynamic>? afterState,
          @AuditStateConverter() final Map<String, dynamic>? metadata,
          final String? ipAddress,
          final String? userAgent,
          @JsonKey(name: 'createdAt') final String? createdAt}) =
      _$AuditLogModelImpl;

  factory _AuditLogModel.fromJson(Map<String, dynamic> json) =
      _$AuditLogModelImpl.fromJson;

  @override
  String get id;
  @override
  String? get actorId;
  @override
  String? get actorEmail;
  @override
  String? get action;
  @override
  String? get entityType;
  @override
  String? get entityId;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get beforeState;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get afterState;
  @override
  @AuditStateConverter()
  Map<String, dynamic>? get metadata;
  @override
  String? get ipAddress;
  @override
  String? get userAgent;
  @override
  @JsonKey(name: 'createdAt')
  String? get createdAt;

  /// Create a copy of AuditLogModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AuditLogModelImplCopyWith<_$AuditLogModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
