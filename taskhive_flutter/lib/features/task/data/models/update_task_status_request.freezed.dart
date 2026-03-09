// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'update_task_status_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

UpdateTaskStatusRequest _$UpdateTaskStatusRequestFromJson(
    Map<String, dynamic> json) {
  return _UpdateTaskStatusRequest.fromJson(json);
}

/// @nodoc
mixin _$UpdateTaskStatusRequest {
  TaskStatus get status => throw _privateConstructorUsedError;
  String? get comment => throw _privateConstructorUsedError;

  /// Serializes this UpdateTaskStatusRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of UpdateTaskStatusRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $UpdateTaskStatusRequestCopyWith<UpdateTaskStatusRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UpdateTaskStatusRequestCopyWith<$Res> {
  factory $UpdateTaskStatusRequestCopyWith(UpdateTaskStatusRequest value,
          $Res Function(UpdateTaskStatusRequest) then) =
      _$UpdateTaskStatusRequestCopyWithImpl<$Res, UpdateTaskStatusRequest>;
  @useResult
  $Res call({TaskStatus status, String? comment});
}

/// @nodoc
class _$UpdateTaskStatusRequestCopyWithImpl<$Res,
        $Val extends UpdateTaskStatusRequest>
    implements $UpdateTaskStatusRequestCopyWith<$Res> {
  _$UpdateTaskStatusRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of UpdateTaskStatusRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? status = null,
    Object? comment = freezed,
  }) {
    return _then(_value.copyWith(
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus,
      comment: freezed == comment
          ? _value.comment
          : comment // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$UpdateTaskStatusRequestImplCopyWith<$Res>
    implements $UpdateTaskStatusRequestCopyWith<$Res> {
  factory _$$UpdateTaskStatusRequestImplCopyWith(
          _$UpdateTaskStatusRequestImpl value,
          $Res Function(_$UpdateTaskStatusRequestImpl) then) =
      __$$UpdateTaskStatusRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({TaskStatus status, String? comment});
}

/// @nodoc
class __$$UpdateTaskStatusRequestImplCopyWithImpl<$Res>
    extends _$UpdateTaskStatusRequestCopyWithImpl<$Res,
        _$UpdateTaskStatusRequestImpl>
    implements _$$UpdateTaskStatusRequestImplCopyWith<$Res> {
  __$$UpdateTaskStatusRequestImplCopyWithImpl(
      _$UpdateTaskStatusRequestImpl _value,
      $Res Function(_$UpdateTaskStatusRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of UpdateTaskStatusRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? status = null,
    Object? comment = freezed,
  }) {
    return _then(_$UpdateTaskStatusRequestImpl(
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus,
      comment: freezed == comment
          ? _value.comment
          : comment // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$UpdateTaskStatusRequestImpl implements _UpdateTaskStatusRequest {
  const _$UpdateTaskStatusRequestImpl({required this.status, this.comment});

  factory _$UpdateTaskStatusRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$UpdateTaskStatusRequestImplFromJson(json);

  @override
  final TaskStatus status;
  @override
  final String? comment;

  @override
  String toString() {
    return 'UpdateTaskStatusRequest(status: $status, comment: $comment)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UpdateTaskStatusRequestImpl &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.comment, comment) || other.comment == comment));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, status, comment);

  /// Create a copy of UpdateTaskStatusRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$UpdateTaskStatusRequestImplCopyWith<_$UpdateTaskStatusRequestImpl>
      get copyWith => __$$UpdateTaskStatusRequestImplCopyWithImpl<
          _$UpdateTaskStatusRequestImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$UpdateTaskStatusRequestImplToJson(
      this,
    );
  }
}

abstract class _UpdateTaskStatusRequest implements UpdateTaskStatusRequest {
  const factory _UpdateTaskStatusRequest(
      {required final TaskStatus status,
      final String? comment}) = _$UpdateTaskStatusRequestImpl;

  factory _UpdateTaskStatusRequest.fromJson(Map<String, dynamic> json) =
      _$UpdateTaskStatusRequestImpl.fromJson;

  @override
  TaskStatus get status;
  @override
  String? get comment;

  /// Create a copy of UpdateTaskStatusRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$UpdateTaskStatusRequestImplCopyWith<_$UpdateTaskStatusRequestImpl>
      get copyWith => throw _privateConstructorUsedError;
}
