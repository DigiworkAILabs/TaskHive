// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'productivity_score_response.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

ProductivityScoreResponse _$ProductivityScoreResponseFromJson(
    Map<String, dynamic> json) {
  return _ProductivityScoreResponse.fromJson(json);
}

/// @nodoc
mixin _$ProductivityScoreResponse {
  double get score => throw _privateConstructorUsedError;
  String get grade => throw _privateConstructorUsedError;
  Map<String, double> get breakdown => throw _privateConstructorUsedError;
  String get trend => throw _privateConstructorUsedError;
  String get reasoning => throw _privateConstructorUsedError;
  bool get fallbackUsed => throw _privateConstructorUsedError;

  /// Serializes this ProductivityScoreResponse to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ProductivityScoreResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ProductivityScoreResponseCopyWith<ProductivityScoreResponse> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProductivityScoreResponseCopyWith<$Res> {
  factory $ProductivityScoreResponseCopyWith(ProductivityScoreResponse value,
          $Res Function(ProductivityScoreResponse) then) =
      _$ProductivityScoreResponseCopyWithImpl<$Res, ProductivityScoreResponse>;
  @useResult
  $Res call(
      {double score,
      String grade,
      Map<String, double> breakdown,
      String trend,
      String reasoning,
      bool fallbackUsed});
}

/// @nodoc
class _$ProductivityScoreResponseCopyWithImpl<$Res,
        $Val extends ProductivityScoreResponse>
    implements $ProductivityScoreResponseCopyWith<$Res> {
  _$ProductivityScoreResponseCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ProductivityScoreResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? score = null,
    Object? grade = null,
    Object? breakdown = null,
    Object? trend = null,
    Object? reasoning = null,
    Object? fallbackUsed = null,
  }) {
    return _then(_value.copyWith(
      score: null == score
          ? _value.score
          : score // ignore: cast_nullable_to_non_nullable
              as double,
      grade: null == grade
          ? _value.grade
          : grade // ignore: cast_nullable_to_non_nullable
              as String,
      breakdown: null == breakdown
          ? _value.breakdown
          : breakdown // ignore: cast_nullable_to_non_nullable
              as Map<String, double>,
      trend: null == trend
          ? _value.trend
          : trend // ignore: cast_nullable_to_non_nullable
              as String,
      reasoning: null == reasoning
          ? _value.reasoning
          : reasoning // ignore: cast_nullable_to_non_nullable
              as String,
      fallbackUsed: null == fallbackUsed
          ? _value.fallbackUsed
          : fallbackUsed // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ProductivityScoreResponseImplCopyWith<$Res>
    implements $ProductivityScoreResponseCopyWith<$Res> {
  factory _$$ProductivityScoreResponseImplCopyWith(
          _$ProductivityScoreResponseImpl value,
          $Res Function(_$ProductivityScoreResponseImpl) then) =
      __$$ProductivityScoreResponseImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {double score,
      String grade,
      Map<String, double> breakdown,
      String trend,
      String reasoning,
      bool fallbackUsed});
}

/// @nodoc
class __$$ProductivityScoreResponseImplCopyWithImpl<$Res>
    extends _$ProductivityScoreResponseCopyWithImpl<$Res,
        _$ProductivityScoreResponseImpl>
    implements _$$ProductivityScoreResponseImplCopyWith<$Res> {
  __$$ProductivityScoreResponseImplCopyWithImpl(
      _$ProductivityScoreResponseImpl _value,
      $Res Function(_$ProductivityScoreResponseImpl) _then)
      : super(_value, _then);

  /// Create a copy of ProductivityScoreResponse
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? score = null,
    Object? grade = null,
    Object? breakdown = null,
    Object? trend = null,
    Object? reasoning = null,
    Object? fallbackUsed = null,
  }) {
    return _then(_$ProductivityScoreResponseImpl(
      score: null == score
          ? _value.score
          : score // ignore: cast_nullable_to_non_nullable
              as double,
      grade: null == grade
          ? _value.grade
          : grade // ignore: cast_nullable_to_non_nullable
              as String,
      breakdown: null == breakdown
          ? _value._breakdown
          : breakdown // ignore: cast_nullable_to_non_nullable
              as Map<String, double>,
      trend: null == trend
          ? _value.trend
          : trend // ignore: cast_nullable_to_non_nullable
              as String,
      reasoning: null == reasoning
          ? _value.reasoning
          : reasoning // ignore: cast_nullable_to_non_nullable
              as String,
      fallbackUsed: null == fallbackUsed
          ? _value.fallbackUsed
          : fallbackUsed // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ProductivityScoreResponseImpl implements _ProductivityScoreResponse {
  const _$ProductivityScoreResponseImpl(
      {required this.score,
      required this.grade,
      required final Map<String, double> breakdown,
      required this.trend,
      required this.reasoning,
      required this.fallbackUsed})
      : _breakdown = breakdown;

  factory _$ProductivityScoreResponseImpl.fromJson(Map<String, dynamic> json) =>
      _$$ProductivityScoreResponseImplFromJson(json);

  @override
  final double score;
  @override
  final String grade;
  final Map<String, double> _breakdown;
  @override
  Map<String, double> get breakdown {
    if (_breakdown is EqualUnmodifiableMapView) return _breakdown;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableMapView(_breakdown);
  }

  @override
  final String trend;
  @override
  final String reasoning;
  @override
  final bool fallbackUsed;

  @override
  String toString() {
    return 'ProductivityScoreResponse(score: $score, grade: $grade, breakdown: $breakdown, trend: $trend, reasoning: $reasoning, fallbackUsed: $fallbackUsed)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProductivityScoreResponseImpl &&
            (identical(other.score, score) || other.score == score) &&
            (identical(other.grade, grade) || other.grade == grade) &&
            const DeepCollectionEquality()
                .equals(other._breakdown, _breakdown) &&
            (identical(other.trend, trend) || other.trend == trend) &&
            (identical(other.reasoning, reasoning) ||
                other.reasoning == reasoning) &&
            (identical(other.fallbackUsed, fallbackUsed) ||
                other.fallbackUsed == fallbackUsed));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      score,
      grade,
      const DeepCollectionEquality().hash(_breakdown),
      trend,
      reasoning,
      fallbackUsed);

  /// Create a copy of ProductivityScoreResponse
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ProductivityScoreResponseImplCopyWith<_$ProductivityScoreResponseImpl>
      get copyWith => __$$ProductivityScoreResponseImplCopyWithImpl<
          _$ProductivityScoreResponseImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ProductivityScoreResponseImplToJson(
      this,
    );
  }
}

abstract class _ProductivityScoreResponse implements ProductivityScoreResponse {
  const factory _ProductivityScoreResponse(
      {required final double score,
      required final String grade,
      required final Map<String, double> breakdown,
      required final String trend,
      required final String reasoning,
      required final bool fallbackUsed}) = _$ProductivityScoreResponseImpl;

  factory _ProductivityScoreResponse.fromJson(Map<String, dynamic> json) =
      _$ProductivityScoreResponseImpl.fromJson;

  @override
  double get score;
  @override
  String get grade;
  @override
  Map<String, double> get breakdown;
  @override
  String get trend;
  @override
  String get reasoning;
  @override
  bool get fallbackUsed;

  /// Create a copy of ProductivityScoreResponse
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ProductivityScoreResponseImplCopyWith<_$ProductivityScoreResponseImpl>
      get copyWith => throw _privateConstructorUsedError;
}
