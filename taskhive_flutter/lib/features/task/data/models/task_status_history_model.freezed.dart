// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_status_history_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskStatusHistoryModel _$TaskStatusHistoryModelFromJson(
    Map<String, dynamic> json) {
  return _TaskStatusHistoryModel.fromJson(json);
}

/// @nodoc
mixin _$TaskStatusHistoryModel {
  String get id => throw _privateConstructorUsedError;
  String get taskId => throw _privateConstructorUsedError;
  String? get oldStatus => throw _privateConstructorUsedError;
  String get newStatus => throw _privateConstructorUsedError;
  String get changedByName => throw _privateConstructorUsedError;
  String? get comment => throw _privateConstructorUsedError;
  String get changedAt => throw _privateConstructorUsedError;

  /// Serializes this TaskStatusHistoryModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskStatusHistoryModelCopyWith<TaskStatusHistoryModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskStatusHistoryModelCopyWith<$Res> {
  factory $TaskStatusHistoryModelCopyWith(TaskStatusHistoryModel value,
          $Res Function(TaskStatusHistoryModel) then) =
      _$TaskStatusHistoryModelCopyWithImpl<$Res, TaskStatusHistoryModel>;
  @useResult
  $Res call(
      {String id,
      String taskId,
      String? oldStatus,
      String newStatus,
      String changedByName,
      String? comment,
      String changedAt});
}

/// @nodoc
class _$TaskStatusHistoryModelCopyWithImpl<$Res,
        $Val extends TaskStatusHistoryModel>
    implements $TaskStatusHistoryModelCopyWith<$Res> {
  _$TaskStatusHistoryModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? taskId = null,
    Object? oldStatus = freezed,
    Object? newStatus = null,
    Object? changedByName = null,
    Object? comment = freezed,
    Object? changedAt = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      taskId: null == taskId
          ? _value.taskId
          : taskId // ignore: cast_nullable_to_non_nullable
              as String,
      oldStatus: freezed == oldStatus
          ? _value.oldStatus
          : oldStatus // ignore: cast_nullable_to_non_nullable
              as String?,
      newStatus: null == newStatus
          ? _value.newStatus
          : newStatus // ignore: cast_nullable_to_non_nullable
              as String,
      changedByName: null == changedByName
          ? _value.changedByName
          : changedByName // ignore: cast_nullable_to_non_nullable
              as String,
      comment: freezed == comment
          ? _value.comment
          : comment // ignore: cast_nullable_to_non_nullable
              as String?,
      changedAt: null == changedAt
          ? _value.changedAt
          : changedAt // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskStatusHistoryModelImplCopyWith<$Res>
    implements $TaskStatusHistoryModelCopyWith<$Res> {
  factory _$$TaskStatusHistoryModelImplCopyWith(
          _$TaskStatusHistoryModelImpl value,
          $Res Function(_$TaskStatusHistoryModelImpl) then) =
      __$$TaskStatusHistoryModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String taskId,
      String? oldStatus,
      String newStatus,
      String changedByName,
      String? comment,
      String changedAt});
}

/// @nodoc
class __$$TaskStatusHistoryModelImplCopyWithImpl<$Res>
    extends _$TaskStatusHistoryModelCopyWithImpl<$Res,
        _$TaskStatusHistoryModelImpl>
    implements _$$TaskStatusHistoryModelImplCopyWith<$Res> {
  __$$TaskStatusHistoryModelImplCopyWithImpl(
      _$TaskStatusHistoryModelImpl _value,
      $Res Function(_$TaskStatusHistoryModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? taskId = null,
    Object? oldStatus = freezed,
    Object? newStatus = null,
    Object? changedByName = null,
    Object? comment = freezed,
    Object? changedAt = null,
  }) {
    return _then(_$TaskStatusHistoryModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      taskId: null == taskId
          ? _value.taskId
          : taskId // ignore: cast_nullable_to_non_nullable
              as String,
      oldStatus: freezed == oldStatus
          ? _value.oldStatus
          : oldStatus // ignore: cast_nullable_to_non_nullable
              as String?,
      newStatus: null == newStatus
          ? _value.newStatus
          : newStatus // ignore: cast_nullable_to_non_nullable
              as String,
      changedByName: null == changedByName
          ? _value.changedByName
          : changedByName // ignore: cast_nullable_to_non_nullable
              as String,
      comment: freezed == comment
          ? _value.comment
          : comment // ignore: cast_nullable_to_non_nullable
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
class _$TaskStatusHistoryModelImpl implements _TaskStatusHistoryModel {
  const _$TaskStatusHistoryModelImpl(
      {required this.id,
      required this.taskId,
      this.oldStatus,
      required this.newStatus,
      required this.changedByName,
      this.comment,
      required this.changedAt});

  factory _$TaskStatusHistoryModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskStatusHistoryModelImplFromJson(json);

  @override
  final String id;
  @override
  final String taskId;
  @override
  final String? oldStatus;
  @override
  final String newStatus;
  @override
  final String changedByName;
  @override
  final String? comment;
  @override
  final String changedAt;

  @override
  String toString() {
    return 'TaskStatusHistoryModel(id: $id, taskId: $taskId, oldStatus: $oldStatus, newStatus: $newStatus, changedByName: $changedByName, comment: $comment, changedAt: $changedAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskStatusHistoryModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.taskId, taskId) || other.taskId == taskId) &&
            (identical(other.oldStatus, oldStatus) ||
                other.oldStatus == oldStatus) &&
            (identical(other.newStatus, newStatus) ||
                other.newStatus == newStatus) &&
            (identical(other.changedByName, changedByName) ||
                other.changedByName == changedByName) &&
            (identical(other.comment, comment) || other.comment == comment) &&
            (identical(other.changedAt, changedAt) ||
                other.changedAt == changedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, taskId, oldStatus, newStatus,
      changedByName, comment, changedAt);

  /// Create a copy of TaskStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskStatusHistoryModelImplCopyWith<_$TaskStatusHistoryModelImpl>
      get copyWith => __$$TaskStatusHistoryModelImplCopyWithImpl<
          _$TaskStatusHistoryModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskStatusHistoryModelImplToJson(
      this,
    );
  }
}

abstract class _TaskStatusHistoryModel implements TaskStatusHistoryModel {
  const factory _TaskStatusHistoryModel(
      {required final String id,
      required final String taskId,
      final String? oldStatus,
      required final String newStatus,
      required final String changedByName,
      final String? comment,
      required final String changedAt}) = _$TaskStatusHistoryModelImpl;

  factory _TaskStatusHistoryModel.fromJson(Map<String, dynamic> json) =
      _$TaskStatusHistoryModelImpl.fromJson;

  @override
  String get id;
  @override
  String get taskId;
  @override
  String? get oldStatus;
  @override
  String get newStatus;
  @override
  String get changedByName;
  @override
  String? get comment;
  @override
  String get changedAt;

  /// Create a copy of TaskStatusHistoryModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskStatusHistoryModelImplCopyWith<_$TaskStatusHistoryModelImpl>
      get copyWith => throw _privateConstructorUsedError;
}
