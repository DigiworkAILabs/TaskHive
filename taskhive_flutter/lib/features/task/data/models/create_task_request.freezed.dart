// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'create_task_request.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

CreateTaskRequest _$CreateTaskRequestFromJson(Map<String, dynamic> json) {
  return _CreateTaskRequest.fromJson(json);
}

/// @nodoc
mixin _$CreateTaskRequest {
  String get title => throw _privateConstructorUsedError;
  String? get description => throw _privateConstructorUsedError;
  TaskPriority get priority => throw _privateConstructorUsedError;
  String get assignedTo => throw _privateConstructorUsedError;
  String get dueDate => throw _privateConstructorUsedError;
  double? get estimatedHours => throw _privateConstructorUsedError;
  List<String> get tags => throw _privateConstructorUsedError;

  /// Serializes this CreateTaskRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of CreateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $CreateTaskRequestCopyWith<CreateTaskRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $CreateTaskRequestCopyWith<$Res> {
  factory $CreateTaskRequestCopyWith(
          CreateTaskRequest value, $Res Function(CreateTaskRequest) then) =
      _$CreateTaskRequestCopyWithImpl<$Res, CreateTaskRequest>;
  @useResult
  $Res call(
      {String title,
      String? description,
      TaskPriority priority,
      String assignedTo,
      String dueDate,
      double? estimatedHours,
      List<String> tags});
}

/// @nodoc
class _$CreateTaskRequestCopyWithImpl<$Res, $Val extends CreateTaskRequest>
    implements $CreateTaskRequestCopyWith<$Res> {
  _$CreateTaskRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of CreateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
    Object? description = freezed,
    Object? priority = null,
    Object? assignedTo = null,
    Object? dueDate = null,
    Object? estimatedHours = freezed,
    Object? tags = null,
  }) {
    return _then(_value.copyWith(
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      priority: null == priority
          ? _value.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as TaskPriority,
      assignedTo: null == assignedTo
          ? _value.assignedTo
          : assignedTo // ignore: cast_nullable_to_non_nullable
              as String,
      dueDate: null == dueDate
          ? _value.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as String,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
      tags: null == tags
          ? _value.tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$CreateTaskRequestImplCopyWith<$Res>
    implements $CreateTaskRequestCopyWith<$Res> {
  factory _$$CreateTaskRequestImplCopyWith(_$CreateTaskRequestImpl value,
          $Res Function(_$CreateTaskRequestImpl) then) =
      __$$CreateTaskRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String title,
      String? description,
      TaskPriority priority,
      String assignedTo,
      String dueDate,
      double? estimatedHours,
      List<String> tags});
}

/// @nodoc
class __$$CreateTaskRequestImplCopyWithImpl<$Res>
    extends _$CreateTaskRequestCopyWithImpl<$Res, _$CreateTaskRequestImpl>
    implements _$$CreateTaskRequestImplCopyWith<$Res> {
  __$$CreateTaskRequestImplCopyWithImpl(_$CreateTaskRequestImpl _value,
      $Res Function(_$CreateTaskRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of CreateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? title = null,
    Object? description = freezed,
    Object? priority = null,
    Object? assignedTo = null,
    Object? dueDate = null,
    Object? estimatedHours = freezed,
    Object? tags = null,
  }) {
    return _then(_$CreateTaskRequestImpl(
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      priority: null == priority
          ? _value.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as TaskPriority,
      assignedTo: null == assignedTo
          ? _value.assignedTo
          : assignedTo // ignore: cast_nullable_to_non_nullable
              as String,
      dueDate: null == dueDate
          ? _value.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as String,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
      tags: null == tags
          ? _value._tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$CreateTaskRequestImpl implements _CreateTaskRequest {
  const _$CreateTaskRequestImpl(
      {required this.title,
      this.description,
      required this.priority,
      required this.assignedTo,
      required this.dueDate,
      this.estimatedHours,
      final List<String> tags = const []})
      : _tags = tags;

  factory _$CreateTaskRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$CreateTaskRequestImplFromJson(json);

  @override
  final String title;
  @override
  final String? description;
  @override
  final TaskPriority priority;
  @override
  final String assignedTo;
  @override
  final String dueDate;
  @override
  final double? estimatedHours;
  final List<String> _tags;
  @override
  @JsonKey()
  List<String> get tags {
    if (_tags is EqualUnmodifiableListView) return _tags;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_tags);
  }

  @override
  String toString() {
    return 'CreateTaskRequest(title: $title, description: $description, priority: $priority, assignedTo: $assignedTo, dueDate: $dueDate, estimatedHours: $estimatedHours, tags: $tags)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$CreateTaskRequestImpl &&
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
            const DeepCollectionEquality().equals(other._tags, _tags));
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
      const DeepCollectionEquality().hash(_tags));

  /// Create a copy of CreateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$CreateTaskRequestImplCopyWith<_$CreateTaskRequestImpl> get copyWith =>
      __$$CreateTaskRequestImplCopyWithImpl<_$CreateTaskRequestImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$CreateTaskRequestImplToJson(
      this,
    );
  }
}

abstract class _CreateTaskRequest implements CreateTaskRequest {
  const factory _CreateTaskRequest(
      {required final String title,
      final String? description,
      required final TaskPriority priority,
      required final String assignedTo,
      required final String dueDate,
      final double? estimatedHours,
      final List<String> tags}) = _$CreateTaskRequestImpl;

  factory _CreateTaskRequest.fromJson(Map<String, dynamic> json) =
      _$CreateTaskRequestImpl.fromJson;

  @override
  String get title;
  @override
  String? get description;
  @override
  TaskPriority get priority;
  @override
  String get assignedTo;
  @override
  String get dueDate;
  @override
  double? get estimatedHours;
  @override
  List<String> get tags;

  /// Create a copy of CreateTaskRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$CreateTaskRequestImplCopyWith<_$CreateTaskRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

TaskPriorityRequestDto _$TaskPriorityRequestDtoFromJson(
    Map<String, dynamic> json) {
  return _TaskPriorityRequestDto.fromJson(json);
}

/// @nodoc
mixin _$TaskPriorityRequestDto {
  String get taskTitle => throw _privateConstructorUsedError;
  String? get taskDescription => throw _privateConstructorUsedError;
  String? get employeeId => throw _privateConstructorUsedError;
  List<String>? get tags => throw _privateConstructorUsedError;
  double? get estimatedHours => throw _privateConstructorUsedError;

  /// Serializes this TaskPriorityRequestDto to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskPriorityRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskPriorityRequestDtoCopyWith<TaskPriorityRequestDto> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskPriorityRequestDtoCopyWith<$Res> {
  factory $TaskPriorityRequestDtoCopyWith(TaskPriorityRequestDto value,
          $Res Function(TaskPriorityRequestDto) then) =
      _$TaskPriorityRequestDtoCopyWithImpl<$Res, TaskPriorityRequestDto>;
  @useResult
  $Res call(
      {String taskTitle,
      String? taskDescription,
      String? employeeId,
      List<String>? tags,
      double? estimatedHours});
}

/// @nodoc
class _$TaskPriorityRequestDtoCopyWithImpl<$Res,
        $Val extends TaskPriorityRequestDto>
    implements $TaskPriorityRequestDtoCopyWith<$Res> {
  _$TaskPriorityRequestDtoCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskPriorityRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? taskTitle = null,
    Object? taskDescription = freezed,
    Object? employeeId = freezed,
    Object? tags = freezed,
    Object? estimatedHours = freezed,
  }) {
    return _then(_value.copyWith(
      taskTitle: null == taskTitle
          ? _value.taskTitle
          : taskTitle // ignore: cast_nullable_to_non_nullable
              as String,
      taskDescription: freezed == taskDescription
          ? _value.taskDescription
          : taskDescription // ignore: cast_nullable_to_non_nullable
              as String?,
      employeeId: freezed == employeeId
          ? _value.employeeId
          : employeeId // ignore: cast_nullable_to_non_nullable
              as String?,
      tags: freezed == tags
          ? _value.tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>?,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskPriorityRequestDtoImplCopyWith<$Res>
    implements $TaskPriorityRequestDtoCopyWith<$Res> {
  factory _$$TaskPriorityRequestDtoImplCopyWith(
          _$TaskPriorityRequestDtoImpl value,
          $Res Function(_$TaskPriorityRequestDtoImpl) then) =
      __$$TaskPriorityRequestDtoImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String taskTitle,
      String? taskDescription,
      String? employeeId,
      List<String>? tags,
      double? estimatedHours});
}

/// @nodoc
class __$$TaskPriorityRequestDtoImplCopyWithImpl<$Res>
    extends _$TaskPriorityRequestDtoCopyWithImpl<$Res,
        _$TaskPriorityRequestDtoImpl>
    implements _$$TaskPriorityRequestDtoImplCopyWith<$Res> {
  __$$TaskPriorityRequestDtoImplCopyWithImpl(
      _$TaskPriorityRequestDtoImpl _value,
      $Res Function(_$TaskPriorityRequestDtoImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskPriorityRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? taskTitle = null,
    Object? taskDescription = freezed,
    Object? employeeId = freezed,
    Object? tags = freezed,
    Object? estimatedHours = freezed,
  }) {
    return _then(_$TaskPriorityRequestDtoImpl(
      taskTitle: null == taskTitle
          ? _value.taskTitle
          : taskTitle // ignore: cast_nullable_to_non_nullable
              as String,
      taskDescription: freezed == taskDescription
          ? _value.taskDescription
          : taskDescription // ignore: cast_nullable_to_non_nullable
              as String?,
      employeeId: freezed == employeeId
          ? _value.employeeId
          : employeeId // ignore: cast_nullable_to_non_nullable
              as String?,
      tags: freezed == tags
          ? _value._tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>?,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$TaskPriorityRequestDtoImpl implements _TaskPriorityRequestDto {
  const _$TaskPriorityRequestDtoImpl(
      {required this.taskTitle,
      this.taskDescription,
      this.employeeId,
      final List<String>? tags,
      this.estimatedHours})
      : _tags = tags;

  factory _$TaskPriorityRequestDtoImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskPriorityRequestDtoImplFromJson(json);

  @override
  final String taskTitle;
  @override
  final String? taskDescription;
  @override
  final String? employeeId;
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
  final double? estimatedHours;

  @override
  String toString() {
    return 'TaskPriorityRequestDto(taskTitle: $taskTitle, taskDescription: $taskDescription, employeeId: $employeeId, tags: $tags, estimatedHours: $estimatedHours)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskPriorityRequestDtoImpl &&
            (identical(other.taskTitle, taskTitle) ||
                other.taskTitle == taskTitle) &&
            (identical(other.taskDescription, taskDescription) ||
                other.taskDescription == taskDescription) &&
            (identical(other.employeeId, employeeId) ||
                other.employeeId == employeeId) &&
            const DeepCollectionEquality().equals(other._tags, _tags) &&
            (identical(other.estimatedHours, estimatedHours) ||
                other.estimatedHours == estimatedHours));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, taskTitle, taskDescription,
      employeeId, const DeepCollectionEquality().hash(_tags), estimatedHours);

  /// Create a copy of TaskPriorityRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskPriorityRequestDtoImplCopyWith<_$TaskPriorityRequestDtoImpl>
      get copyWith => __$$TaskPriorityRequestDtoImplCopyWithImpl<
          _$TaskPriorityRequestDtoImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskPriorityRequestDtoImplToJson(
      this,
    );
  }
}

abstract class _TaskPriorityRequestDto implements TaskPriorityRequestDto {
  const factory _TaskPriorityRequestDto(
      {required final String taskTitle,
      final String? taskDescription,
      final String? employeeId,
      final List<String>? tags,
      final double? estimatedHours}) = _$TaskPriorityRequestDtoImpl;

  factory _TaskPriorityRequestDto.fromJson(Map<String, dynamic> json) =
      _$TaskPriorityRequestDtoImpl.fromJson;

  @override
  String get taskTitle;
  @override
  String? get taskDescription;
  @override
  String? get employeeId;
  @override
  List<String>? get tags;
  @override
  double? get estimatedHours;

  /// Create a copy of TaskPriorityRequestDto
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskPriorityRequestDtoImplCopyWith<_$TaskPriorityRequestDtoImpl>
      get copyWith => throw _privateConstructorUsedError;
}

TaskPriorityPrediction _$TaskPriorityPredictionFromJson(
    Map<String, dynamic> json) {
  return _TaskPriorityPrediction.fromJson(json);
}

/// @nodoc
mixin _$TaskPriorityPrediction {
  String get predictedPriority => throw _privateConstructorUsedError;
  double get confidence => throw _privateConstructorUsedError;
  String get reasoning => throw _privateConstructorUsedError;
  bool get fallbackUsed => throw _privateConstructorUsedError;

  /// Serializes this TaskPriorityPrediction to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskPriorityPrediction
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskPriorityPredictionCopyWith<TaskPriorityPrediction> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskPriorityPredictionCopyWith<$Res> {
  factory $TaskPriorityPredictionCopyWith(TaskPriorityPrediction value,
          $Res Function(TaskPriorityPrediction) then) =
      _$TaskPriorityPredictionCopyWithImpl<$Res, TaskPriorityPrediction>;
  @useResult
  $Res call(
      {String predictedPriority,
      double confidence,
      String reasoning,
      bool fallbackUsed});
}

/// @nodoc
class _$TaskPriorityPredictionCopyWithImpl<$Res,
        $Val extends TaskPriorityPrediction>
    implements $TaskPriorityPredictionCopyWith<$Res> {
  _$TaskPriorityPredictionCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskPriorityPrediction
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? predictedPriority = null,
    Object? confidence = null,
    Object? reasoning = null,
    Object? fallbackUsed = null,
  }) {
    return _then(_value.copyWith(
      predictedPriority: null == predictedPriority
          ? _value.predictedPriority
          : predictedPriority // ignore: cast_nullable_to_non_nullable
              as String,
      confidence: null == confidence
          ? _value.confidence
          : confidence // ignore: cast_nullable_to_non_nullable
              as double,
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
abstract class _$$TaskPriorityPredictionImplCopyWith<$Res>
    implements $TaskPriorityPredictionCopyWith<$Res> {
  factory _$$TaskPriorityPredictionImplCopyWith(
          _$TaskPriorityPredictionImpl value,
          $Res Function(_$TaskPriorityPredictionImpl) then) =
      __$$TaskPriorityPredictionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String predictedPriority,
      double confidence,
      String reasoning,
      bool fallbackUsed});
}

/// @nodoc
class __$$TaskPriorityPredictionImplCopyWithImpl<$Res>
    extends _$TaskPriorityPredictionCopyWithImpl<$Res,
        _$TaskPriorityPredictionImpl>
    implements _$$TaskPriorityPredictionImplCopyWith<$Res> {
  __$$TaskPriorityPredictionImplCopyWithImpl(
      _$TaskPriorityPredictionImpl _value,
      $Res Function(_$TaskPriorityPredictionImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskPriorityPrediction
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? predictedPriority = null,
    Object? confidence = null,
    Object? reasoning = null,
    Object? fallbackUsed = null,
  }) {
    return _then(_$TaskPriorityPredictionImpl(
      predictedPriority: null == predictedPriority
          ? _value.predictedPriority
          : predictedPriority // ignore: cast_nullable_to_non_nullable
              as String,
      confidence: null == confidence
          ? _value.confidence
          : confidence // ignore: cast_nullable_to_non_nullable
              as double,
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
class _$TaskPriorityPredictionImpl implements _TaskPriorityPrediction {
  const _$TaskPriorityPredictionImpl(
      {required this.predictedPriority,
      required this.confidence,
      required this.reasoning,
      required this.fallbackUsed});

  factory _$TaskPriorityPredictionImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskPriorityPredictionImplFromJson(json);

  @override
  final String predictedPriority;
  @override
  final double confidence;
  @override
  final String reasoning;
  @override
  final bool fallbackUsed;

  @override
  String toString() {
    return 'TaskPriorityPrediction(predictedPriority: $predictedPriority, confidence: $confidence, reasoning: $reasoning, fallbackUsed: $fallbackUsed)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskPriorityPredictionImpl &&
            (identical(other.predictedPriority, predictedPriority) ||
                other.predictedPriority == predictedPriority) &&
            (identical(other.confidence, confidence) ||
                other.confidence == confidence) &&
            (identical(other.reasoning, reasoning) ||
                other.reasoning == reasoning) &&
            (identical(other.fallbackUsed, fallbackUsed) ||
                other.fallbackUsed == fallbackUsed));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType, predictedPriority, confidence, reasoning, fallbackUsed);

  /// Create a copy of TaskPriorityPrediction
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskPriorityPredictionImplCopyWith<_$TaskPriorityPredictionImpl>
      get copyWith => __$$TaskPriorityPredictionImplCopyWithImpl<
          _$TaskPriorityPredictionImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskPriorityPredictionImplToJson(
      this,
    );
  }
}

abstract class _TaskPriorityPrediction implements TaskPriorityPrediction {
  const factory _TaskPriorityPrediction(
      {required final String predictedPriority,
      required final double confidence,
      required final String reasoning,
      required final bool fallbackUsed}) = _$TaskPriorityPredictionImpl;

  factory _TaskPriorityPrediction.fromJson(Map<String, dynamic> json) =
      _$TaskPriorityPredictionImpl.fromJson;

  @override
  String get predictedPriority;
  @override
  double get confidence;
  @override
  String get reasoning;
  @override
  bool get fallbackUsed;

  /// Create a copy of TaskPriorityPrediction
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskPriorityPredictionImplCopyWith<_$TaskPriorityPredictionImpl>
      get copyWith => throw _privateConstructorUsedError;
}
