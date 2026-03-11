// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_completion_trend_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskCompletionTrendResponse _$TaskCompletionTrendResponseFromJson(
    Map<String, dynamic> json) {
  return _TaskCompletionTrendResponse.fromJson(json);
}

/// @nodoc
mixin _$TaskCompletionTrendResponse {
  String? get date => throw _privateConstructorUsedError;
  int? get count => throw _privateConstructorUsedError;

  /// Serializes this TaskCompletionTrendResponse to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskCompletionTrendResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskCompletionTrendResponseCopyWith<TaskCompletionTrendResponse>
      get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskCompletionTrendResponseCopyWith<$Res> {
  factory $TaskCompletionTrendResponseCopyWith(
          TaskCompletionTrendResponse value,
          $Res Function(TaskCompletionTrendResponse) then) =
      _$TaskCompletionTrendResponseCopyWithImpl<$Res,
          TaskCompletionTrendResponse>;
  @useResult
  $Res call({String? date, int? count});
}

/// @nodoc
class _$TaskCompletionTrendResponseCopyWithImpl<$Res,
        $Val extends TaskCompletionTrendResponse>
    implements $TaskCompletionTrendResponseCopyWith<$Res> {
  _$TaskCompletionTrendResponseCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskCompletionTrendResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? date = freezed,
    Object? count = freezed,
  }) {
    return _then(_value.copyWith(
      date: freezed == date
          ? _value.date
          : date // ignore: cast_nullable_to_non_nullable
              as String?,
      count: freezed == count
          ? _value.count
          : count // ignore: cast_nullable_to_non_nullable
              as int?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskCompletionTrendResponseImplCopyWith<$Res>
    implements $TaskCompletionTrendResponseCopyWith<$Res> {
  factory _$$TaskCompletionTrendResponseImplCopyWith(
          _$TaskCompletionTrendResponseImpl value,
          $Res Function(_$TaskCompletionTrendResponseImpl) then) =
      __$$TaskCompletionTrendResponseImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String? date, int? count});
}

/// @nodoc
class __$$TaskCompletionTrendResponseImplCopyWithImpl<$Res>
    extends _$TaskCompletionTrendResponseCopyWithImpl<$Res,
        _$TaskCompletionTrendResponseImpl>
    implements _$$TaskCompletionTrendResponseImplCopyWith<$Res> {
  __$$TaskCompletionTrendResponseImplCopyWithImpl(
      _$TaskCompletionTrendResponseImpl _value,
      $Res Function(_$TaskCompletionTrendResponseImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskCompletionTrendResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? date = freezed,
    Object? count = freezed,
  }) {
    return _then(_$TaskCompletionTrendResponseImpl(
      date: freezed == date
          ? _value.date
          : date // ignore: cast_nullable_to_non_nullable
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
class _$TaskCompletionTrendResponseImpl
    implements _TaskCompletionTrendResponse {
  const _$TaskCompletionTrendResponseImpl({this.date, this.count});

  factory _$TaskCompletionTrendResponseImpl.fromJson(
          Map<String, dynamic> json) =>
      _$$TaskCompletionTrendResponseImplFromJson(json);

  @override
  final String? date;
  @override
  final int? count;

  @override
  String toString() {
    return 'TaskCompletionTrendResponse(date: $date, count: $count)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskCompletionTrendResponseImpl &&
            (identical(other.date, date) || other.date == date) &&
            (identical(other.count, count) || other.count == count));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, date, count);

  /// Create a copy of TaskCompletionTrendResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskCompletionTrendResponseImplCopyWith<_$TaskCompletionTrendResponseImpl>
      get copyWith => __$$TaskCompletionTrendResponseImplCopyWithImpl<
          _$TaskCompletionTrendResponseImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskCompletionTrendResponseImplToJson(
      this,
    );
  }
}

abstract class _TaskCompletionTrendResponse
    implements TaskCompletionTrendResponse {
  const factory _TaskCompletionTrendResponse(
      {final String? date,
      final int? count}) = _$TaskCompletionTrendResponseImpl;

  factory _TaskCompletionTrendResponse.fromJson(Map<String, dynamic> json) =
      _$TaskCompletionTrendResponseImpl.fromJson;

  @override
  String? get date;
  @override
  int? get count;

  /// Create a copy of TaskCompletionTrendResponse
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskCompletionTrendResponseImplCopyWith<_$TaskCompletionTrendResponseImpl>
      get copyWith => throw _privateConstructorUsedError;
}
