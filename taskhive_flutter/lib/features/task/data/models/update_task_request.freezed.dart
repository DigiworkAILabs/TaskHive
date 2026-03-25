// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'update_task_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

UpdateTaskRequest _$UpdateTaskRequestFromJson(Map<String, dynamic> json) {
  return _UpdateTaskRequest.fromJson(json);
}

/// @nodoc
mixin _$UpdateTaskRequest {
  String? get title => throw _privateConstructorUsedError;
  String? get description => throw _privateConstructorUsedError;
  TaskPriority? get priority => throw _privateConstructorUsedError;
  String? get assignedTo => throw _privateConstructorUsedError;
  String? get dueDate => throw _privateConstructorUsedError;
  double? get estimatedHours => throw _privateConstructorUsedError;
  List<String>? get tags => throw _privateConstructorUsedError;
  bool? get proofRequired => throw _privateConstructorUsedError;
  bool? get approvalRequired => throw _privateConstructorUsedError;

  /// Serializes this UpdateTaskRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of UpdateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $UpdateTaskRequestCopyWith<UpdateTaskRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $UpdateTaskRequestCopyWith<$Res> {
  factory $UpdateTaskRequestCopyWith(
          UpdateTaskRequest value, $Res Function(UpdateTaskRequest) then) =
      _$UpdateTaskRequestCopyWithImpl<$Res, UpdateTaskRequest>;
  @useResult
  $Res call(
      {String? title,
      String? description,
      TaskPriority? priority,
      String? assignedTo,
      String? dueDate,
      double? estimatedHours,
      List<String>? tags,
      bool? proofRequired,
      bool? approvalRequired});
}

/// @nodoc
class _$UpdateTaskRequestCopyWithImpl<$Res, $Val extends UpdateTaskRequest>
    implements $UpdateTaskRequestCopyWith<$Res> {
  _$UpdateTaskRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of UpdateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = freezed,
    Object? description = freezed,
    Object? priority = freezed,
    Object? assignedTo = freezed,
    Object? dueDate = freezed,
    Object? estimatedHours = freezed,
    Object? tags = freezed,
    Object? proofRequired = freezed,
    Object? approvalRequired = freezed,
  }) {
    return _then(_value.copyWith(
      title: freezed == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String?,
      description: freezed == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      priority: freezed == priority
          ? _value.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as TaskPriority?,
      assignedTo: freezed == assignedTo
          ? _value.assignedTo
          : assignedTo // ignore: cast_nullable_to_non_nullable
              as String?,
      dueDate: freezed == dueDate
          ? _value.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as String?,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
      tags: freezed == tags
          ? _value.tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>?,
      proofRequired: freezed == proofRequired
          ? _value.proofRequired
          : proofRequired // ignore: cast_nullable_to_non_nullable
              as bool?,
      approvalRequired: freezed == approvalRequired
          ? _value.approvalRequired
          : approvalRequired // ignore: cast_nullable_to_non_nullable
              as bool?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$UpdateTaskRequestImplCopyWith<$Res>
    implements $UpdateTaskRequestCopyWith<$Res> {
  factory _$$UpdateTaskRequestImplCopyWith(_$UpdateTaskRequestImpl value,
          $Res Function(_$UpdateTaskRequestImpl) then) =
      __$$UpdateTaskRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String? title,
      String? description,
      TaskPriority? priority,
      String? assignedTo,
      String? dueDate,
      double? estimatedHours,
      List<String>? tags,
      bool? proofRequired,
      bool? approvalRequired});
}

/// @nodoc
class __$$UpdateTaskRequestImplCopyWithImpl<$Res>
    extends _$UpdateTaskRequestCopyWithImpl<$Res, _$UpdateTaskRequestImpl>
    implements _$$UpdateTaskRequestImplCopyWith<$Res> {
  __$$UpdateTaskRequestImplCopyWithImpl(_$UpdateTaskRequestImpl _value,
      $Res Function(_$UpdateTaskRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of UpdateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = freezed,
    Object? description = freezed,
    Object? priority = freezed,
    Object? assignedTo = freezed,
    Object? dueDate = freezed,
    Object? estimatedHours = freezed,
    Object? tags = freezed,
    Object? proofRequired = freezed,
    Object? approvalRequired = freezed,
  }) {
    return _then(_$UpdateTaskRequestImpl(
      title: freezed == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String?,
      description: freezed == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      priority: freezed == priority
          ? _value.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as TaskPriority?,
      assignedTo: freezed == assignedTo
          ? _value.assignedTo
          : assignedTo // ignore: cast_nullable_to_non_nullable
              as String?,
      dueDate: freezed == dueDate
          ? _value.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as String?,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
      tags: freezed == tags
          ? _value._tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>?,
      proofRequired: freezed == proofRequired
          ? _value.proofRequired
          : proofRequired // ignore: cast_nullable_to_non_nullable
              as bool?,
      approvalRequired: freezed == approvalRequired
          ? _value.approvalRequired
          : approvalRequired // ignore: cast_nullable_to_non_nullable
              as bool?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$UpdateTaskRequestImpl implements _UpdateTaskRequest {
  const _$UpdateTaskRequestImpl(
      {this.title,
      this.description,
      this.priority,
      this.assignedTo,
      this.dueDate,
      this.estimatedHours,
      final List<String>? tags,
      this.proofRequired,
      this.approvalRequired})
      : _tags = tags;

  factory _$UpdateTaskRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$UpdateTaskRequestImplFromJson(json);

  @override
  final String? title;
  @override
  final String? description;
  @override
  final TaskPriority? priority;
  @override
  final String? assignedTo;
  @override
  final String? dueDate;
  @override
  final double? estimatedHours;
  final List<String>? _tags;
  @override
  List<String>? get tags {
    final value = _tags;
    if (value == null) return null;
    if (_tags is EqualUnmodifiableListView) return _tags;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(value);
  }

  @override
  final bool? proofRequired;
  @override
  final bool? approvalRequired;

  @override
  String toString() {
    return 'UpdateTaskRequest(title: $title, description: $description, priority: $priority, assignedTo: $assignedTo, dueDate: $dueDate, estimatedHours: $estimatedHours, tags: $tags, proofRequired: $proofRequired, approvalRequired: $approvalRequired)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$UpdateTaskRequestImpl &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.priority, priority) ||
                other.priority == priority) &&
            (identical(other.assignedTo, assignedTo) ||
                other.assignedTo == assignedTo) &&
            (identical(other.dueDate, dueDate) || other.dueDate == dueDate) &&
            (identical(other.estimatedHours, estimatedHours) ||
                other.estimatedHours == estimatedHours) &&
            const DeepCollectionEquality().equals(other._tags, _tags) &&
            (identical(other.proofRequired, proofRequired) ||
                other.proofRequired == proofRequired) &&
            (identical(other.approvalRequired, approvalRequired) ||
                other.approvalRequired == approvalRequired));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      title,
      description,
      priority,
      assignedTo,
      dueDate,
      estimatedHours,
      const DeepCollectionEquality().hash(_tags),
      proofRequired,
      approvalRequired);

  /// Create a copy of UpdateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$UpdateTaskRequestImplCopyWith<_$UpdateTaskRequestImpl> get copyWith =>
      __$$UpdateTaskRequestImplCopyWithImpl<_$UpdateTaskRequestImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$UpdateTaskRequestImplToJson(
      this,
    );
  }
}

abstract class _UpdateTaskRequest implements UpdateTaskRequest {
  const factory _UpdateTaskRequest(
      {final String? title,
      final String? description,
      final TaskPriority? priority,
      final String? assignedTo,
      final String? dueDate,
      final double? estimatedHours,
      final List<String>? tags,
      final bool? proofRequired,
      final bool? approvalRequired}) = _$UpdateTaskRequestImpl;

  factory _UpdateTaskRequest.fromJson(Map<String, dynamic> json) =
      _$UpdateTaskRequestImpl.fromJson;

  @override
  String? get title;
  @override
  String? get description;
  @override
  TaskPriority? get priority;
  @override
  String? get assignedTo;
  @override
  String? get dueDate;
  @override
  double? get estimatedHours;
  @override
  List<String>? get tags;
  @override
  bool? get proofRequired;
  @override
  bool? get approvalRequired;

  /// Create a copy of UpdateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$UpdateTaskRequestImplCopyWith<_$UpdateTaskRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
