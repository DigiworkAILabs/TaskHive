// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_attachments_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$taskAttachmentsHash() => r'35ae7e9eaf399b0b0b381a88c34a8b8ccb2cda71';

/// Copied from Dart SDK
class _SystemHash {
  _SystemHash._();

  static int combine(int hash, int value) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + value);
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x0007ffff & hash) << 10));
    return hash ^ (hash >> 6);
  }

  static int finish(int hash) {
    // ignore: parameter_assignments
    hash = 0x1fffffff & (hash + ((0x03ffffff & hash) << 3));
    // ignore: parameter_assignments
    hash = hash ^ (hash >> 11);
    return 0x1fffffff & (hash + ((0x00003fff & hash) << 15));
  }
}

abstract class _$TaskAttachments
    extends BuildlessAutoDisposeAsyncNotifier<List<TaskAttachmentModel>> {
  late final String taskId;

  FutureOr<List<TaskAttachmentModel>> build(
    String taskId,
  );
}

/// Family AsyncNotifier for attachments on a specific task.
/// Appends the new attachment to the list on successful upload.
///
/// Copied from [TaskAttachments].
@ProviderFor(TaskAttachments)
const taskAttachmentsProvider = TaskAttachmentsFamily();

/// Family AsyncNotifier for attachments on a specific task.
/// Appends the new attachment to the list on successful upload.
///
/// Copied from [TaskAttachments].
class TaskAttachmentsFamily
    extends Family<AsyncValue<List<TaskAttachmentModel>>> {
  /// Family AsyncNotifier for attachments on a specific task.
  /// Appends the new attachment to the list on successful upload.
  ///
  /// Copied from [TaskAttachments].
  const TaskAttachmentsFamily();

  /// Family AsyncNotifier for attachments on a specific task.
  /// Appends the new attachment to the list on successful upload.
  ///
  /// Copied from [TaskAttachments].
  TaskAttachmentsProvider call(
    String taskId,
  ) {
    return TaskAttachmentsProvider(
      taskId,
    );
  }

  @override
  TaskAttachmentsProvider getProviderOverride(
    covariant TaskAttachmentsProvider provider,
  ) {
    return call(
      provider.taskId,
    );
  }

  static const Iterable<ProviderOrFamily>? _dependencies = null;

  @override
  Iterable<ProviderOrFamily>? get dependencies => _dependencies;

  static const Iterable<ProviderOrFamily>? _allTransitiveDependencies = null;

  @override
  Iterable<ProviderOrFamily>? get allTransitiveDependencies =>
      _allTransitiveDependencies;

  @override
  String? get name => r'taskAttachmentsProvider';
}

/// Family AsyncNotifier for attachments on a specific task.
/// Appends the new attachment to the list on successful upload.
///
/// Copied from [TaskAttachments].
class TaskAttachmentsProvider extends AutoDisposeAsyncNotifierProviderImpl<
    TaskAttachments, List<TaskAttachmentModel>> {
  /// Family AsyncNotifier for attachments on a specific task.
  /// Appends the new attachment to the list on successful upload.
  ///
  /// Copied from [TaskAttachments].
  TaskAttachmentsProvider(
    String taskId,
  ) : this._internal(
          () => TaskAttachments()..taskId = taskId,
          from: taskAttachmentsProvider,
          name: r'taskAttachmentsProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$taskAttachmentsHash,
          dependencies: TaskAttachmentsFamily._dependencies,
          allTransitiveDependencies:
              TaskAttachmentsFamily._allTransitiveDependencies,
          taskId: taskId,
        );

  TaskAttachmentsProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.taskId,
  }) : super.internal();

  final String taskId;

  @override
  FutureOr<List<TaskAttachmentModel>> runNotifierBuild(
    covariant TaskAttachments notifier,
  ) {
    return notifier.build(
      taskId,
    );
  }

  @override
  Override overrideWith(TaskAttachments Function() create) {
    return ProviderOverride(
      origin: this,
      override: TaskAttachmentsProvider._internal(
        () => create()..taskId = taskId,
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        taskId: taskId,
      ),
    );
  }

  @override
  AutoDisposeAsyncNotifierProviderElement<TaskAttachments,
      List<TaskAttachmentModel>> createElement() {
    return _TaskAttachmentsProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is TaskAttachmentsProvider && other.taskId == taskId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, taskId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin TaskAttachmentsRef
    on AutoDisposeAsyncNotifierProviderRef<List<TaskAttachmentModel>> {
  /// The parameter `taskId` of this provider.
  String get taskId;
}

class _TaskAttachmentsProviderElement
    extends AutoDisposeAsyncNotifierProviderElement<TaskAttachments,
        List<TaskAttachmentModel>> with TaskAttachmentsRef {
  _TaskAttachmentsProviderElement(super.provider);

  @override
  String get taskId => (origin as TaskAttachmentsProvider).taskId;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
