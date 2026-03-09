// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_comment_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskCommentModel _$TaskCommentModelFromJson(Map<String, dynamic> json) {
  return _TaskCommentModel.fromJson(json);
}

/// @nodoc
mixin _$TaskCommentModel {
  String get id => throw _privateConstructorUsedError;
  String get taskId => throw _privateConstructorUsedError;
  String get authorId => throw _privateConstructorUsedError;
  String get authorName => throw _privateConstructorUsedError;
  String get content => throw _privateConstructorUsedError;
  String? get createdAt => throw _privateConstructorUsedError;

  /// Serializes this TaskCommentModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskCommentModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskCommentModelCopyWith<TaskCommentModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskCommentModelCopyWith<$Res> {
  factory $TaskCommentModelCopyWith(
          TaskCommentModel value, $Res Function(TaskCommentModel) then) =
      _$TaskCommentModelCopyWithImpl<$Res, TaskCommentModel>;
  @useResult
  $Res call(
      {String id,
      String taskId,
      String authorId,
      String authorName,
      String content,
      String? createdAt});
}

/// @nodoc
class _$TaskCommentModelCopyWithImpl<$Res, $Val extends TaskCommentModel>
    implements $TaskCommentModelCopyWith<$Res> {
  _$TaskCommentModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskCommentModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? taskId = null,
    Object? authorId = null,
    Object? authorName = null,
    Object? content = null,
    Object? createdAt = freezed,
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
      authorId: null == authorId
          ? _value.authorId
          : authorId // ignore: cast_nullable_to_non_nullable
              as String,
      authorName: null == authorName
          ? _value.authorName
          : authorName // ignore: cast_nullable_to_non_nullable
              as String,
      content: null == content
          ? _value.content
          : content // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskCommentModelImplCopyWith<$Res>
    implements $TaskCommentModelCopyWith<$Res> {
  factory _$$TaskCommentModelImplCopyWith(_$TaskCommentModelImpl value,
          $Res Function(_$TaskCommentModelImpl) then) =
      __$$TaskCommentModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String taskId,
      String authorId,
      String authorName,
      String content,
      String? createdAt});
}

/// @nodoc
class __$$TaskCommentModelImplCopyWithImpl<$Res>
    extends _$TaskCommentModelCopyWithImpl<$Res, _$TaskCommentModelImpl>
    implements _$$TaskCommentModelImplCopyWith<$Res> {
  __$$TaskCommentModelImplCopyWithImpl(_$TaskCommentModelImpl _value,
      $Res Function(_$TaskCommentModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskCommentModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? taskId = null,
    Object? authorId = null,
    Object? authorName = null,
    Object? content = null,
    Object? createdAt = freezed,
  }) {
    return _then(_$TaskCommentModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      taskId: null == taskId
          ? _value.taskId
          : taskId // ignore: cast_nullable_to_non_nullable
              as String,
      authorId: null == authorId
          ? _value.authorId
          : authorId // ignore: cast_nullable_to_non_nullable
              as String,
      authorName: null == authorName
          ? _value.authorName
          : authorName // ignore: cast_nullable_to_non_nullable
              as String,
      content: null == content
          ? _value.content
          : content // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$TaskCommentModelImpl implements _TaskCommentModel {
  const _$TaskCommentModelImpl(
      {required this.id,
      required this.taskId,
      required this.authorId,
      required this.authorName,
      required this.content,
      this.createdAt});

  factory _$TaskCommentModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskCommentModelImplFromJson(json);

  @override
  final String id;
  @override
  final String taskId;
  @override
  final String authorId;
  @override
  final String authorName;
  @override
  final String content;
  @override
  final String? createdAt;

  @override
  String toString() {
    return 'TaskCommentModel(id: $id, taskId: $taskId, authorId: $authorId, authorName: $authorName, content: $content, createdAt: $createdAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskCommentModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.taskId, taskId) || other.taskId == taskId) &&
            (identical(other.authorId, authorId) ||
                other.authorId == authorId) &&
            (identical(other.authorName, authorName) ||
                other.authorName == authorName) &&
            (identical(other.content, content) || other.content == content) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType, id, taskId, authorId, authorName, content, createdAt);

  /// Create a copy of TaskCommentModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskCommentModelImplCopyWith<_$TaskCommentModelImpl> get copyWith =>
      __$$TaskCommentModelImplCopyWithImpl<_$TaskCommentModelImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskCommentModelImplToJson(
      this,
    );
  }
}

abstract class _TaskCommentModel implements TaskCommentModel {
  const factory _TaskCommentModel(
      {required final String id,
      required final String taskId,
      required final String authorId,
      required final String authorName,
      required final String content,
      final String? createdAt}) = _$TaskCommentModelImpl;

  factory _TaskCommentModel.fromJson(Map<String, dynamic> json) =
      _$TaskCommentModelImpl.fromJson;

  @override
  String get id;
  @override
  String get taskId;
  @override
  String get authorId;
  @override
  String get authorName;
  @override
  String get content;
  @override
  String? get createdAt;

  /// Create a copy of TaskCommentModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskCommentModelImplCopyWith<_$TaskCommentModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
