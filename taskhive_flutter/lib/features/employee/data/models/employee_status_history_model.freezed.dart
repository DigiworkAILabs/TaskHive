// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'employee_status_history_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

EmployeeStatusHistoryModel _$EmployeeStatusHistoryModelFromJson(
    Map<String, dynamic> json) {
  return _EmployeeStatusHistoryModel.fromJson(json);
}

/// @nodoc
mixin _$EmployeeStatusHistoryModel {
  String get id => throw _privateConstructorUsedError;
  String get employeeId => throw _privateConstructorUsedError;
  String? get oldStatus => throw _privateConstructorUsedError;
  String get newStatus => throw _privateConstructorUsedError;
  String get changedBy => throw _privateConstructorUsedError;
  String? get reason => throw _privateConstructorUsedError;
  String get changedAt => throw _privateConstructorUsedError;

  /// Serializes this EmployeeStatusHistoryModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of EmployeeStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $EmployeeStatusHistoryModelCopyWith<EmployeeStatusHistoryModel>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $EmployeeStatusHistoryModelCopyWith<$Res> {
  factory $EmployeeStatusHistoryModelCopyWith(EmployeeStatusHistoryModel value,
          $Res Function(EmployeeStatusHistoryModel) then) =
      _$EmployeeStatusHistoryModelCopyWithImpl<$Res,
          EmployeeStatusHistoryModel>;
  @useResult
  $Res call(
      {String id,
      String employeeId,
      String? oldStatus,
      String newStatus,
      String changedBy,
      String? reason,
      String changedAt});
}

/// @nodoc
class _$EmployeeStatusHistoryModelCopyWithImpl<$Res,
        $Val extends EmployeeStatusHistoryModel>
    implements $EmployeeStatusHistoryModelCopyWith<$Res> {
  _$EmployeeStatusHistoryModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of EmployeeStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? employeeId = null,
    Object? oldStatus = freezed,
    Object? newStatus = null,
    Object? changedBy = null,
    Object? reason = freezed,
    Object? changedAt = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      employeeId: null == employeeId
          ? _value.employeeId
          : employeeId // ignore: cast_nullable_to_non_nullable
              as String,
      oldStatus: freezed == oldStatus
          ? _value.oldStatus
          : oldStatus // ignore: cast_nullable_to_non_nullable
              as String?,
      newStatus: null == newStatus
          ? _value.newStatus
          : newStatus // ignore: cast_nullable_to_non_nullable
              as String,
      changedBy: null == changedBy
          ? _value.changedBy
          : changedBy // ignore: cast_nullable_to_non_nullable
              as String,
      reason: freezed == reason
          ? _value.reason
          : reason // ignore: cast_nullable_to_non_nullable
              as String?,
      changedAt: null == changedAt
          ? _value.changedAt
          : changedAt // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$EmployeeStatusHistoryModelImplCopyWith<$Res>
    implements $EmployeeStatusHistoryModelCopyWith<$Res> {
  factory _$$EmployeeStatusHistoryModelImplCopyWith(
          _$EmployeeStatusHistoryModelImpl value,
          $Res Function(_$EmployeeStatusHistoryModelImpl) then) =
      __$$EmployeeStatusHistoryModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String employeeId,
      String? oldStatus,
      String newStatus,
      String changedBy,
      String? reason,
      String changedAt});
}

/// @nodoc
class __$$EmployeeStatusHistoryModelImplCopyWithImpl<$Res>
    extends _$EmployeeStatusHistoryModelCopyWithImpl<$Res,
        _$EmployeeStatusHistoryModelImpl>
    implements _$$EmployeeStatusHistoryModelImplCopyWith<$Res> {
  __$$EmployeeStatusHistoryModelImplCopyWithImpl(
      _$EmployeeStatusHistoryModelImpl _value,
      $Res Function(_$EmployeeStatusHistoryModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of EmployeeStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? employeeId = null,
    Object? oldStatus = freezed,
    Object? newStatus = null,
    Object? changedBy = null,
    Object? reason = freezed,
    Object? changedAt = null,
  }) {
    return _then(_$EmployeeStatusHistoryModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      employeeId: null == employeeId
          ? _value.employeeId
          : employeeId // ignore: cast_nullable_to_non_nullable
              as String,
      oldStatus: freezed == oldStatus
          ? _value.oldStatus
          : oldStatus // ignore: cast_nullable_to_non_nullable
              as String?,
      newStatus: null == newStatus
          ? _value.newStatus
          : newStatus // ignore: cast_nullable_to_non_nullable
              as String,
      changedBy: null == changedBy
          ? _value.changedBy
          : changedBy // ignore: cast_nullable_to_non_nullable
              as String,
      reason: freezed == reason
          ? _value.reason
          : reason // ignore: cast_nullable_to_non_nullable
              as String?,
      changedAt: null == changedAt
          ? _value.changedAt
          : changedAt // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$EmployeeStatusHistoryModelImpl implements _EmployeeStatusHistoryModel {
  const _$EmployeeStatusHistoryModelImpl(
      {required this.id,
      required this.employeeId,
      this.oldStatus,
      required this.newStatus,
      required this.changedBy,
      this.reason,
      required this.changedAt});

  factory _$EmployeeStatusHistoryModelImpl.fromJson(
          Map<String, dynamic> json) =>
      _$$EmployeeStatusHistoryModelImplFromJson(json);

  @override
  final String id;
  @override
  final String employeeId;
  @override
  final String? oldStatus;
  @override
  final String newStatus;
  @override
  final String changedBy;
  @override
  final String? reason;
  @override
  final String changedAt;

  @override
  String toString() {
    return 'EmployeeStatusHistoryModel(id: $id, employeeId: $employeeId, oldStatus: $oldStatus, newStatus: $newStatus, changedBy: $changedBy, reason: $reason, changedAt: $changedAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$EmployeeStatusHistoryModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.employeeId, employeeId) ||
                other.employeeId == employeeId) &&
            (identical(other.oldStatus, oldStatus) ||
                other.oldStatus == oldStatus) &&
            (identical(other.newStatus, newStatus) ||
                other.newStatus == newStatus) &&
            (identical(other.changedBy, changedBy) ||
                other.changedBy == changedBy) &&
            (identical(other.reason, reason) || other.reason == reason) &&
            (identical(other.changedAt, changedAt) ||
                other.changedAt == changedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, employeeId, oldStatus,
      newStatus, changedBy, reason, changedAt);

  /// Create a copy of EmployeeStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$EmployeeStatusHistoryModelImplCopyWith<_$EmployeeStatusHistoryModelImpl>
      get copyWith => __$$EmployeeStatusHistoryModelImplCopyWithImpl<
          _$EmployeeStatusHistoryModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$EmployeeStatusHistoryModelImplToJson(
      this,
    );
  }
}

abstract class _EmployeeStatusHistoryModel
    implements EmployeeStatusHistoryModel {
  const factory _EmployeeStatusHistoryModel(
      {required final String id,
      required final String employeeId,
      final String? oldStatus,
      required final String newStatus,
      required final String changedBy,
      final String? reason,
      required final String changedAt}) = _$EmployeeStatusHistoryModelImpl;

  factory _EmployeeStatusHistoryModel.fromJson(Map<String, dynamic> json) =
      _$EmployeeStatusHistoryModelImpl.fromJson;

  @override
  String get id;
  @override
  String get employeeId;
  @override
  String? get oldStatus;
  @override
  String get newStatus;
  @override
  String get changedBy;
  @override
  String? get reason;
  @override
  String get changedAt;

  /// Create a copy of EmployeeStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$EmployeeStatusHistoryModelImplCopyWith<_$EmployeeStatusHistoryModelImpl>
      get copyWith => throw _privateConstructorUsedError;
}
