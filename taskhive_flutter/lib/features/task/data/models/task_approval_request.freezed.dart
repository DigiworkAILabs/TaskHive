// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_approval_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskApprovalRequest _$TaskApprovalRequestFromJson(Map<String, dynamic> json) {
  return _TaskApprovalRequest.fromJson(json);
}

/// @nodoc
mixin _$TaskApprovalRequest {
  String get reason => throw _privateConstructorUsedError;

  /// Serializes this TaskApprovalRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskApprovalRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskApprovalRequestCopyWith<TaskApprovalRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskApprovalRequestCopyWith<$Res> {
  factory $TaskApprovalRequestCopyWith(
          TaskApprovalRequest value, $Res Function(TaskApprovalRequest) then) =
      _$TaskApprovalRequestCopyWithImpl<$Res, TaskApprovalRequest>;
  @useResult
  $Res call({String reason});
}

/// @nodoc
class _$TaskApprovalRequestCopyWithImpl<$Res, $Val extends TaskApprovalRequest>
    implements $TaskApprovalRequestCopyWith<$Res> {
  _$TaskApprovalRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskApprovalRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? reason = null,
  }) {
    return _then(_value.copyWith(
      reason: null == reason
          ? _value.reason
          : reason // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskApprovalRequestImplCopyWith<$Res>
    implements $TaskApprovalRequestCopyWith<$Res> {
  factory _$$TaskApprovalRequestImplCopyWith(_$TaskApprovalRequestImpl value,
          $Res Function(_$TaskApprovalRequestImpl) then) =
      __$$TaskApprovalRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String reason});
}

/// @nodoc
class __$$TaskApprovalRequestImplCopyWithImpl<$Res>
    extends _$TaskApprovalRequestCopyWithImpl<$Res, _$TaskApprovalRequestImpl>
    implements _$$TaskApprovalRequestImplCopyWith<$Res> {
  __$$TaskApprovalRequestImplCopyWithImpl(_$TaskApprovalRequestImpl _value,
      $Res Function(_$TaskApprovalRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskApprovalRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? reason = null,
  }) {
    return _then(_$TaskApprovalRequestImpl(
      reason: null == reason
          ? _value.reason
          : reason // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$TaskApprovalRequestImpl implements _TaskApprovalRequest {
  const _$TaskApprovalRequestImpl({required this.reason});

  factory _$TaskApprovalRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskApprovalRequestImplFromJson(json);

  @override
  final String reason;

  @override
  String toString() {
    return 'TaskApprovalRequest(reason: $reason)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskApprovalRequestImpl &&
            (identical(other.reason, reason) || other.reason == reason));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, reason);

  /// Create a copy of TaskApprovalRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskApprovalRequestImplCopyWith<_$TaskApprovalRequestImpl> get copyWith =>
      __$$TaskApprovalRequestImplCopyWithImpl<_$TaskApprovalRequestImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskApprovalRequestImplToJson(
      this,
    );
  }
}

abstract class _TaskApprovalRequest implements TaskApprovalRequest {
  const factory _TaskApprovalRequest({required final String reason}) =
      _$TaskApprovalRequestImpl;

  factory _TaskApprovalRequest.fromJson(Map<String, dynamic> json) =
      _$TaskApprovalRequestImpl.fromJson;

  @override
  String get reason;

  /// Create a copy of TaskApprovalRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskApprovalRequestImplCopyWith<_$TaskApprovalRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
