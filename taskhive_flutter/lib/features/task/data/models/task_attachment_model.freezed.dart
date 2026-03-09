// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'task_attachment_model.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

TaskAttachmentModel _$TaskAttachmentModelFromJson(Map<String, dynamic> json) {
  return _TaskAttachmentModel.fromJson(json);
}

/// @nodoc
mixin _$TaskAttachmentModel {
  String get id => throw _privateConstructorUsedError;
  String get taskId => throw _privateConstructorUsedError;
  String get uploadedBy => throw _privateConstructorUsedError;
  String? get uploaderName => throw _privateConstructorUsedError;
  String get fileName => throw _privateConstructorUsedError;
  String get fileUrl => throw _privateConstructorUsedError;
  int get fileSize => throw _privateConstructorUsedError;
  String get mimeType => throw _privateConstructorUsedError;
  String? get createdAt => throw _privateConstructorUsedError;

  /// Serializes this TaskAttachmentModel to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TaskAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TaskAttachmentModelCopyWith<TaskAttachmentModel> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TaskAttachmentModelCopyWith<$Res> {
  factory $TaskAttachmentModelCopyWith(
          TaskAttachmentModel value, $Res Function(TaskAttachmentModel) then) =
      _$TaskAttachmentModelCopyWithImpl<$Res, TaskAttachmentModel>;
  @useResult
  $Res call(
      {String id,
      String taskId,
      String uploadedBy,
      String? uploaderName,
      String fileName,
      String fileUrl,
      int fileSize,
      String mimeType,
      String? createdAt});
}

/// @nodoc
class _$TaskAttachmentModelCopyWithImpl<$Res, $Val extends TaskAttachmentModel>
    implements $TaskAttachmentModelCopyWith<$Res> {
  _$TaskAttachmentModelCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TaskAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? taskId = null,
    Object? uploadedBy = null,
    Object? uploaderName = freezed,
    Object? fileName = null,
    Object? fileUrl = null,
    Object? fileSize = null,
    Object? mimeType = null,
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
      uploadedBy: null == uploadedBy
          ? _value.uploadedBy
          : uploadedBy // ignore: cast_nullable_to_non_nullable
              as String,
      uploaderName: freezed == uploaderName
          ? _value.uploaderName
          : uploaderName // ignore: cast_nullable_to_non_nullable
              as String?,
      fileName: null == fileName
          ? _value.fileName
          : fileName // ignore: cast_nullable_to_non_nullable
              as String,
      fileUrl: null == fileUrl
          ? _value.fileUrl
          : fileUrl // ignore: cast_nullable_to_non_nullable
              as String,
      fileSize: null == fileSize
          ? _value.fileSize
          : fileSize // ignore: cast_nullable_to_non_nullable
              as int,
      mimeType: null == mimeType
          ? _value.mimeType
          : mimeType // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: freezed == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TaskAttachmentModelImplCopyWith<$Res>
    implements $TaskAttachmentModelCopyWith<$Res> {
  factory _$$TaskAttachmentModelImplCopyWith(_$TaskAttachmentModelImpl value,
          $Res Function(_$TaskAttachmentModelImpl) then) =
      __$$TaskAttachmentModelImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String taskId,
      String uploadedBy,
      String? uploaderName,
      String fileName,
      String fileUrl,
      int fileSize,
      String mimeType,
      String? createdAt});
}

/// @nodoc
class __$$TaskAttachmentModelImplCopyWithImpl<$Res>
    extends _$TaskAttachmentModelCopyWithImpl<$Res, _$TaskAttachmentModelImpl>
    implements _$$TaskAttachmentModelImplCopyWith<$Res> {
  __$$TaskAttachmentModelImplCopyWithImpl(_$TaskAttachmentModelImpl _value,
      $Res Function(_$TaskAttachmentModelImpl) _then)
      : super(_value, _then);

  /// Create a copy of TaskAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? taskId = null,
    Object? uploadedBy = null,
    Object? uploaderName = freezed,
    Object? fileName = null,
    Object? fileUrl = null,
    Object? fileSize = null,
    Object? mimeType = null,
    Object? createdAt = freezed,
  }) {
    return _then(_$TaskAttachmentModelImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      taskId: null == taskId
          ? _value.taskId
          : taskId // ignore: cast_nullable_to_non_nullable
              as String,
      uploadedBy: null == uploadedBy
          ? _value.uploadedBy
          : uploadedBy // ignore: cast_nullable_to_non_nullable
              as String,
      uploaderName: freezed == uploaderName
          ? _value.uploaderName
          : uploaderName // ignore: cast_nullable_to_non_nullable
              as String?,
      fileName: null == fileName
          ? _value.fileName
          : fileName // ignore: cast_nullable_to_non_nullable
              as String,
      fileUrl: null == fileUrl
          ? _value.fileUrl
          : fileUrl // ignore: cast_nullable_to_non_nullable
              as String,
      fileSize: null == fileSize
          ? _value.fileSize
          : fileSize // ignore: cast_nullable_to_non_nullable
              as int,
      mimeType: null == mimeType
          ? _value.mimeType
          : mimeType // ignore: cast_nullable_to_non_nullable
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
class _$TaskAttachmentModelImpl implements _TaskAttachmentModel {
  const _$TaskAttachmentModelImpl(
      {required this.id,
      required this.taskId,
      required this.uploadedBy,
      this.uploaderName,
      required this.fileName,
      required this.fileUrl,
      required this.fileSize,
      required this.mimeType,
      this.createdAt});

  factory _$TaskAttachmentModelImpl.fromJson(Map<String, dynamic> json) =>
      _$$TaskAttachmentModelImplFromJson(json);

  @override
  final String id;
  @override
  final String taskId;
  @override
  final String uploadedBy;
  @override
  final String? uploaderName;
  @override
  final String fileName;
  @override
  final String fileUrl;
  @override
  final int fileSize;
  @override
  final String mimeType;
  @override
  final String? createdAt;

  @override
  String toString() {
    return 'TaskAttachmentModel(id: $id, taskId: $taskId, uploadedBy: $uploadedBy, uploaderName: $uploaderName, fileName: $fileName, fileUrl: $fileUrl, fileSize: $fileSize, mimeType: $mimeType, createdAt: $createdAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TaskAttachmentModelImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.taskId, taskId) || other.taskId == taskId) &&
            (identical(other.uploadedBy, uploadedBy) ||
                other.uploadedBy == uploadedBy) &&
            (identical(other.uploaderName, uploaderName) ||
                other.uploaderName == uploaderName) &&
            (identical(other.fileName, fileName) ||
                other.fileName == fileName) &&
            (identical(other.fileUrl, fileUrl) || other.fileUrl == fileUrl) &&
            (identical(other.fileSize, fileSize) ||
                other.fileSize == fileSize) &&
            (identical(other.mimeType, mimeType) ||
                other.mimeType == mimeType) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, taskId, uploadedBy,
      uploaderName, fileName, fileUrl, fileSize, mimeType, createdAt);

  /// Create a copy of TaskAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TaskAttachmentModelImplCopyWith<_$TaskAttachmentModelImpl> get copyWith =>
      __$$TaskAttachmentModelImplCopyWithImpl<_$TaskAttachmentModelImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TaskAttachmentModelImplToJson(
      this,
    );
  }
}

abstract class _TaskAttachmentModel implements TaskAttachmentModel {
  const factory _TaskAttachmentModel(
      {required final String id,
      required final String taskId,
      required final String uploadedBy,
      final String? uploaderName,
      required final String fileName,
      required final String fileUrl,
      required final int fileSize,
      required final String mimeType,
      final String? createdAt}) = _$TaskAttachmentModelImpl;

  factory _TaskAttachmentModel.fromJson(Map<String, dynamic> json) =
      _$TaskAttachmentModelImpl.fromJson;

  @override
  String get id;
  @override
  String get taskId;
  @override
  String get uploadedBy;
  @override
  String? get uploaderName;
  @override
  String get fileName;
  @override
  String get fileUrl;
  @override
  int get fileSize;
  @override
  String get mimeType;
  @override
  String? get createdAt;

  /// Create a copy of TaskAttachmentModel
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TaskAttachmentModelImplCopyWith<_$TaskAttachmentModelImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
