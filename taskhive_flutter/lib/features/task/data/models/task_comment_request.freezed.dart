// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_comment_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskCommentRequest _$TaskCommentRequestFromJson(Map<String, dynamic> json) {
  return _TaskCommentRequest.fromJson(json);
}

/// @nodoc
mixin _$TaskCommentRequest {
  String get content => throw _privateConstructorUsedError;

  /// Serializes this TaskCommentRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskCommentRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskCommentRequestCopyWith<TaskCommentRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskCommentRequestCopyWith<$Res> {
  factory $TaskCommentRequestCopyWith(
          TaskCommentRequest value, $Res Function(TaskCommentRequest) then) =
      _$TaskCommentRequestCopyWithImpl<$Res, TaskCommentRequest>;
  @useResult
  $Res call({String content});
}

/// @nodoc
class _$TaskCommentRequestCopyWithImpl<$Res, $Val extends TaskCommentRequest>
    implements $TaskCommentRequestCopyWith<$Res> {
  _$TaskCommentRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskCommentRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? content = null,
  }) {
    return _then(_value.copyWith(
      content: null == content
          ? _value.content
          : content // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskCommentRequestImplCopyWith<$Res>
    implements $TaskCommentRequestCopyWith<$Res> {
  factory _$$TaskCommentRequestImplCopyWith(_$TaskCommentRequestImpl value,
          $Res Function(_$TaskCommentRequestImpl) then) =
      __$$TaskCommentRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String content});
}

/// @nodoc
class __$$TaskCommentRequestImplCopyWithImpl<$Res>
    extends _$TaskCommentRequestCopyWithImpl<$Res, _$TaskCommentRequestImpl>
    implements _$$TaskCommentRequestImplCopyWith<$Res> {
  __$$TaskCommentRequestImplCopyWithImpl(_$TaskCommentRequestImpl _value,
      $Res Function(_$TaskCommentRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskCommentRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? content = null,
  }) {
    return _then(_$TaskCommentRequestImpl(
      content: null == content
          ? _value.content
          : content // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$TaskCommentRequestImpl implements _TaskCommentRequest {
  const _$TaskCommentRequestImpl({required this.content});

  factory _$TaskCommentRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskCommentRequestImplFromJson(json);

  @override
  final String content;

  @override
  String toString() {
    return 'TaskCommentRequest(content: $content)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskCommentRequestImpl &&
            (identical(other.content, content) || other.content == content));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, content);

  /// Create a copy of TaskCommentRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskCommentRequestImplCopyWith<_$TaskCommentRequestImpl> get copyWith =>
      __$$TaskCommentRequestImplCopyWithImpl<_$TaskCommentRequestImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskCommentRequestImplToJson(
      this,
    );
  }
}

abstract class _TaskCommentRequest implements TaskCommentRequest {
  const factory _TaskCommentRequest({required final String content}) =
      _$TaskCommentRequestImpl;

  factory _TaskCommentRequest.fromJson(Map<String, dynamic> json) =
      _$TaskCommentRequestImpl.fromJson;

  @override
  String get content;

  /// Create a copy of TaskCommentRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskCommentRequestImplCopyWith<_$TaskCommentRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
