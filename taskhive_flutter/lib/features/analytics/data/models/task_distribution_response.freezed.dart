// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_distribution_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskDistributionResponse _$TaskDistributionResponseFromJson(
    Map<String, dynamic> json) {
  return _TaskDistributionResponse.fromJson(json);
}

/// @nodoc
mixin _$TaskDistributionResponse {
  String? get status => throw _privateConstructorUsedError;
  int? get count => throw _privateConstructorUsedError;

  /// Serializes this TaskDistributionResponse to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskDistributionResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskDistributionResponseCopyWith<TaskDistributionResponse> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskDistributionResponseCopyWith<$Res> {
  factory $TaskDistributionResponseCopyWith(TaskDistributionResponse value,
          $Res Function(TaskDistributionResponse) then) =
      _$TaskDistributionResponseCopyWithImpl<$Res, TaskDistributionResponse>;
  @useResult
  $Res call({String? status, int? count});
}

/// @nodoc
class _$TaskDistributionResponseCopyWithImpl<$Res,
        $Val extends TaskDistributionResponse>
    implements $TaskDistributionResponseCopyWith<$Res> {
  _$TaskDistributionResponseCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskDistributionResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? status = freezed,
    Object? count = freezed,
  }) {
    return _then(_value.copyWith(
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      count: freezed == count
          ? _value.count
          : count // ignore: cast_nullable_to_non_nullable
              as int?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskDistributionResponseImplCopyWith<$Res>
    implements $TaskDistributionResponseCopyWith<$Res> {
  factory _$$TaskDistributionResponseImplCopyWith(
          _$TaskDistributionResponseImpl value,
          $Res Function(_$TaskDistributionResponseImpl) then) =
      __$$TaskDistributionResponseImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String? status, int? count});
}

/// @nodoc
class __$$TaskDistributionResponseImplCopyWithImpl<$Res>
    extends _$TaskDistributionResponseCopyWithImpl<$Res,
        _$TaskDistributionResponseImpl>
    implements _$$TaskDistributionResponseImplCopyWith<$Res> {
  __$$TaskDistributionResponseImplCopyWithImpl(
      _$TaskDistributionResponseImpl _value,
      $Res Function(_$TaskDistributionResponseImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskDistributionResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? status = freezed,
    Object? count = freezed,
  }) {
    return _then(_$TaskDistributionResponseImpl(
      status: freezed == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String?,
      count: freezed == count
          ? _value.count
          : count // ignore: cast_nullable_to_non_nullable
              as int?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$TaskDistributionResponseImpl implements _TaskDistributionResponse {
  const _$TaskDistributionResponseImpl({this.status, this.count});

  factory _$TaskDistributionResponseImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskDistributionResponseImplFromJson(json);

  @override
  final String? status;
  @override
  final int? count;

  @override
  String toString() {
    return 'TaskDistributionResponse(status: $status, count: $count)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskDistributionResponseImpl &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.count, count) || other.count == count));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, status, count);

  /// Create a copy of TaskDistributionResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskDistributionResponseImplCopyWith<_$TaskDistributionResponseImpl>
      get copyWith => __$$TaskDistributionResponseImplCopyWithImpl<
          _$TaskDistributionResponseImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskDistributionResponseImplToJson(
      this,
    );
  }
}

abstract class _TaskDistributionResponse implements TaskDistributionResponse {
  const factory _TaskDistributionResponse(
      {final String? status,
      final int? count}) = _$TaskDistributionResponseImpl;

  factory _TaskDistributionResponse.fromJson(Map<String, dynamic> json) =
      _$TaskDistributionResponseImpl.fromJson;

  @override
  String? get status;
  @override
  int? get count;

  /// Create a copy of TaskDistributionResponse
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskDistributionResponseImplCopyWith<_$TaskDistributionResponseImpl>
      get copyWith => throw _privateConstructorUsedError;
}
