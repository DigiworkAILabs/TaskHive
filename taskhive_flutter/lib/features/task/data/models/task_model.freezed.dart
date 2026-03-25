// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskModel _$TaskModelFromJson(Map<String, dynamic> json) {
  return _TaskModel.fromJson(json);
}

/// @nodoc
mixin _$TaskModel {
  String get id => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String? get description => throw _privateConstructorUsedError;
  TaskStatus get status => throw _privateConstructorUsedError;
  TaskPriority get priority => throw _privateConstructorUsedError;
  String? get assignedTo => throw _privateConstructorUsedError;
  String? get assigneeName => throw _privateConstructorUsedError;
  String? get dueDate => throw _privateConstructorUsedError;
  String? get completedAt => throw _privateConstructorUsedError;
  double? get estimatedHours => throw _privateConstructorUsedError;
  List<String> get tags => throw _privateConstructorUsedError;
  bool get isOverdue => throw _privateConstructorUsedError;
  String? get createdBy => throw _privateConstructorUsedError;
  String? get createdAt => throw _privateConstructorUsedError;
  String? get updatedAt =>
      throw _privateConstructorUsedError; // Phase 1 v2.5 fields
  bool? get isLate => throw _privateConstructorUsedError;
  int? get lateByMinutes => throw _privateConstructorUsedError;
  String? get submittedAt => throw _privateConstructorUsedError;
  bool? get proofRequired => throw _privateConstructorUsedError;
  bool? get approvalRequired => throw _privateConstructorUsedError;

  /// Serializes this TaskModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskModelCopyWith<TaskModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskModelCopyWith<$Res> {
  factory $TaskModelCopyWith(TaskModel value, $Res Function(TaskModel) then) =
      _$TaskModelCopyWithImpl<$Res, TaskModel>;
  @useResult
  $Res call(
      {String id,
      String title,
      String? description,
      TaskStatus status,
      TaskPriority priority,
      String? assignedTo,
      String? assigneeName,
      String? dueDate,
      String? completedAt,
      double? estimatedHours,
      List<String> tags,
      bool isOverdue,
      String? createdBy,
      String? createdAt,
      String? updatedAt,
      bool? isLate,
      int? lateByMinutes,
      String? submittedAt,
      bool? proofRequired,
      bool? approvalRequired});
}

/// @nodoc
class _$TaskModelCopyWithImpl<$Res, $Val extends TaskModel>
    implements $TaskModelCopyWith<$Res> {
  _$TaskModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? description = freezed,
    Object? status = null,
    Object? priority = null,
    Object? assignedTo = freezed,
    Object? assigneeName = freezed,
    Object? dueDate = freezed,
    Object? completedAt = freezed,
    Object? estimatedHours = freezed,
    Object? tags = null,
    Object? isOverdue = null,
    Object? createdBy = freezed,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
    Object? isLate = freezed,
    Object? lateByMinutes = freezed,
    Object? submittedAt = freezed,
    Object? proofRequired = freezed,
    Object? approvalRequired = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus,
      priority: null == priority
          ? _value.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as TaskPriority,
      assignedTo: freezed == assignedTo
          ? _value.assignedTo
          : assignedTo // ignore: cast_nullable_to_non_nullable
              as String?,
      assigneeName: freezed == assigneeName
          ? _value.assigneeName
          : assigneeName // ignore: cast_nullable_to_non_nullable
              as String?,
      dueDate: freezed == dueDate
          ? _value.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as String?,
      completedAt: freezed == completedAt
          ? _value.completedAt
          : completedAt // ignore: cast_nullable_to_non_nullable
              as String?,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
      tags: null == tags
          ? _value.tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>,
      isOverdue: null == isOverdue
          ? _value.isOverdue
          : isOverdue // ignore: cast_nullable_to_non_nullable
              as bool,
      createdBy: freezed == createdBy
          ? _value.createdBy
          : createdBy // ignore: cast_nullable_to_non_nullable
              as String?,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
      updatedAt: freezed == updatedAt
          ? _value.updatedAt
          : updatedAt // ignore: cast_nullable_to_non_nullable
              as String?,
      isLate: freezed == isLate
          ? _value.isLate
          : isLate // ignore: cast_nullable_to_non_nullable
              as bool?,
      lateByMinutes: freezed == lateByMinutes
          ? _value.lateByMinutes
          : lateByMinutes // ignore: cast_nullable_to_non_nullable
              as int?,
      submittedAt: freezed == submittedAt
          ? _value.submittedAt
          : submittedAt // ignore: cast_nullable_to_non_nullable
              as String?,
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
abstract class _$$TaskModelImplCopyWith<$Res>
    implements $TaskModelCopyWith<$Res> {
  factory _$$TaskModelImplCopyWith(
          _$TaskModelImpl value, $Res Function(_$TaskModelImpl) then) =
      __$$TaskModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String title,
      String? description,
      TaskStatus status,
      TaskPriority priority,
      String? assignedTo,
      String? assigneeName,
      String? dueDate,
      String? completedAt,
      double? estimatedHours,
      List<String> tags,
      bool isOverdue,
      String? createdBy,
      String? createdAt,
      String? updatedAt,
      bool? isLate,
      int? lateByMinutes,
      String? submittedAt,
      bool? proofRequired,
      bool? approvalRequired});
}

/// @nodoc
class __$$TaskModelImplCopyWithImpl<$Res>
    extends _$TaskModelCopyWithImpl<$Res, _$TaskModelImpl>
    implements _$$TaskModelImplCopyWith<$Res> {
  __$$TaskModelImplCopyWithImpl(
      _$TaskModelImpl _value, $Res Function(_$TaskModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? description = freezed,
    Object? status = null,
    Object? priority = null,
    Object? assignedTo = freezed,
    Object? assigneeName = freezed,
    Object? dueDate = freezed,
    Object? completedAt = freezed,
    Object? estimatedHours = freezed,
    Object? tags = null,
    Object? isOverdue = null,
    Object? createdBy = freezed,
    Object? createdAt = freezed,
    Object? updatedAt = freezed,
    Object? isLate = freezed,
    Object? lateByMinutes = freezed,
    Object? submittedAt = freezed,
    Object? proofRequired = freezed,
    Object? approvalRequired = freezed,
  }) {
    return _then(_$TaskModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      description: freezed == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String?,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as TaskStatus,
      priority: null == priority
          ? _value.priority
          : priority // ignore: cast_nullable_to_non_nullable
              as TaskPriority,
      assignedTo: freezed == assignedTo
          ? _value.assignedTo
          : assignedTo // ignore: cast_nullable_to_non_nullable
              as String?,
      assigneeName: freezed == assigneeName
          ? _value.assigneeName
          : assigneeName // ignore: cast_nullable_to_non_nullable
              as String?,
      dueDate: freezed == dueDate
          ? _value.dueDate
          : dueDate // ignore: cast_nullable_to_non_nullable
              as String?,
      completedAt: freezed == completedAt
          ? _value.completedAt
          : completedAt // ignore: cast_nullable_to_non_nullable
              as String?,
      estimatedHours: freezed == estimatedHours
          ? _value.estimatedHours
          : estimatedHours // ignore: cast_nullable_to_non_nullable
              as double?,
      tags: null == tags
          ? _value._tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>,
      isOverdue: null == isOverdue
          ? _value.isOverdue
          : isOverdue // ignore: cast_nullable_to_non_nullable
              as bool,
      createdBy: freezed == createdBy
          ? _value.createdBy
          : createdBy // ignore: cast_nullable_to_non_nullable
              as String?,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
      updatedAt: freezed == updatedAt
          ? _value.updatedAt
          : updatedAt // ignore: cast_nullable_to_non_nullable
              as String?,
      isLate: freezed == isLate
          ? _value.isLate
          : isLate // ignore: cast_nullable_to_non_nullable
              as bool?,
      lateByMinutes: freezed == lateByMinutes
          ? _value.lateByMinutes
          : lateByMinutes // ignore: cast_nullable_to_non_nullable
              as int?,
      submittedAt: freezed == submittedAt
          ? _value.submittedAt
          : submittedAt // ignore: cast_nullable_to_non_nullable
              as String?,
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
class _$TaskModelImpl implements _TaskModel {
  const _$TaskModelImpl(
      {required this.id,
      required this.title,
      this.description,
      required this.status,
      required this.priority,
      this.assignedTo,
      this.assigneeName,
      this.dueDate,
      this.completedAt,
      this.estimatedHours,
      final List<String> tags = const [],
      this.isOverdue = false,
      this.createdBy,
      this.createdAt,
      this.updatedAt,
      this.isLate,
      this.lateByMinutes,
      this.submittedAt,
      this.proofRequired,
      this.approvalRequired})
      : _tags = tags;

  factory _$TaskModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskModelImplFromJson(json);

  @override
  final String id;
  @override
  final String title;
  @override
  final String? description;
  @override
  final TaskStatus status;
  @override
  final TaskPriority priority;
  @override
  final String? assignedTo;
  @override
  final String? assigneeName;
  @override
  final String? dueDate;
  @override
  final String? completedAt;
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
  @JsonKey()
  final bool isOverdue;
  @override
  final String? createdBy;
  @override
  final String? createdAt;
  @override
  final String? updatedAt;
// Phase 1 v2.5 fields
  @override
  final bool? isLate;
  @override
  final int? lateByMinutes;
  @override
  final String? submittedAt;
  @override
  final bool? proofRequired;
  @override
  final bool? approvalRequired;

  @override
  String toString() {
    return 'TaskModel(id: $id, title: $title, description: $description, status: $status, priority: $priority, assignedTo: $assignedTo, assigneeName: $assigneeName, dueDate: $dueDate, completedAt: $completedAt, estimatedHours: $estimatedHours, tags: $tags, isOverdue: $isOverdue, createdBy: $createdBy, createdAt: $createdAt, updatedAt: $updatedAt, isLate: $isLate, lateByMinutes: $lateByMinutes, submittedAt: $submittedAt, proofRequired: $proofRequired, approvalRequired: $approvalRequired)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.priority, priority) ||
                other.priority == priority) &&
            (identical(other.assignedTo, assignedTo) ||
                other.assignedTo == assignedTo) &&
            (identical(other.assigneeName, assigneeName) ||
                other.assigneeName == assigneeName) &&
            (identical(other.dueDate, dueDate) || other.dueDate == dueDate) &&
            (identical(other.completedAt, completedAt) ||
                other.completedAt == completedAt) &&
            (identical(other.estimatedHours, estimatedHours) ||
                other.estimatedHours == estimatedHours) &&
            const DeepCollectionEquality().equals(other._tags, _tags) &&
            (identical(other.isOverdue, isOverdue) ||
                other.isOverdue == isOverdue) &&
            (identical(other.createdBy, createdBy) ||
                other.createdBy == createdBy) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.updatedAt, updatedAt) ||
                other.updatedAt == updatedAt) &&
            (identical(other.isLate, isLate) || other.isLate == isLate) &&
            (identical(other.lateByMinutes, lateByMinutes) ||
                other.lateByMinutes == lateByMinutes) &&
            (identical(other.submittedAt, submittedAt) ||
                other.submittedAt == submittedAt) &&
            (identical(other.proofRequired, proofRequired) ||
                other.proofRequired == proofRequired) &&
            (identical(other.approvalRequired, approvalRequired) ||
                other.approvalRequired == approvalRequired));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hashAll([
        runtimeType,
        id,
        title,
        description,
        status,
        priority,
        assignedTo,
        assigneeName,
        dueDate,
        completedAt,
        estimatedHours,
        const DeepCollectionEquality().hash(_tags),
        isOverdue,
        createdBy,
        createdAt,
        updatedAt,
        isLate,
        lateByMinutes,
        submittedAt,
        proofRequired,
        approvalRequired
      ]);

  /// Create a copy of TaskModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskModelImplCopyWith<_$TaskModelImpl> get copyWith =>
      __$$TaskModelImplCopyWithImpl<_$TaskModelImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskModelImplToJson(
      this,
    );
  }
}

abstract class _TaskModel implements TaskModel {
  const factory _TaskModel(
      {required final String id,
      required final String title,
      final String? description,
      required final TaskStatus status,
      required final TaskPriority priority,
      final String? assignedTo,
      final String? assigneeName,
      final String? dueDate,
      final String? completedAt,
      final double? estimatedHours,
      final List<String> tags,
      final bool isOverdue,
      final String? createdBy,
      final String? createdAt,
      final String? updatedAt,
      final bool? isLate,
      final int? lateByMinutes,
      final String? submittedAt,
      final bool? proofRequired,
      final bool? approvalRequired}) = _$TaskModelImpl;

  factory _TaskModel.fromJson(Map<String, dynamic> json) =
      _$TaskModelImpl.fromJson;

  @override
  String get id;
  @override
  String get title;
  @override
  String? get description;
  @override
  TaskStatus get status;
  @override
  TaskPriority get priority;
  @override
  String? get assignedTo;
  @override
  String? get assigneeName;
  @override
  String? get dueDate;
  @override
  String? get completedAt;
  @override
  double? get estimatedHours;
  @override
  List<String> get tags;
  @override
  bool get isOverdue;
  @override
  String? get createdBy;
  @override
  String? get createdAt;
  @override
  String? get updatedAt; // Phase 1 v2.5 fields
  @override
  bool? get isLate;
  @override
  int? get lateByMinutes;
  @override
  String? get submittedAt;
  @override
  bool? get proofRequired;
  @override
  bool? get approvalRequired;

  /// Create a copy of TaskModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskModelImplCopyWith<_$TaskModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
