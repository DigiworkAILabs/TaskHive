// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_comments_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$taskCommentsHash() => r'1ee02f7036ed2d3118e2d36de3775b9ff60e273e';

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

abstract class _$TaskComments
    extends BuildlessAutoDisposeAsyncNotifier<List<TaskCommentModel>> {
  late final String taskId;

  FutureOr<List<TaskCommentModel>> build(
    String taskId,
  );
}

/// Family AsyncNotifier for comments on a specific task.
/// Comments are appended on add.
///
/// Copied from [TaskComments].
@ProviderFor(TaskComments)
const taskCommentsProvider = TaskCommentsFamily();

/// Family AsyncNotifier for comments on a specific task.
/// Comments are appended on add.
///
/// Copied from [TaskComments].
class TaskCommentsFamily extends Family<AsyncValue<List<TaskCommentModel>>> {
  /// Family AsyncNotifier for comments on a specific task.
  /// Comments are appended on add.
  ///
  /// Copied from [TaskComments].
  const TaskCommentsFamily();

  /// Family AsyncNotifier for comments on a specific task.
  /// Comments are appended on add.
  ///
  /// Copied from [TaskComments].
  TaskCommentsProvider call(
    String taskId,
  ) {
    return TaskCommentsProvider(
      taskId,
    );
  }

  @override
  TaskCommentsProvider getProviderOverride(
    covariant TaskCommentsProvider provider,
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
  String? get name => r'taskCommentsProvider';
}

/// Family AsyncNotifier for comments on a specific task.
/// Comments are appended on add.
///
/// Copied from [TaskComments].
class TaskCommentsProvider extends AutoDisposeAsyncNotifierProviderImpl<
    TaskComments, List<TaskCommentModel>> {
  /// Family AsyncNotifier for comments on a specific task.
  /// Comments are appended on add.
  ///
  /// Copied from [TaskComments].
  TaskCommentsProvider(
    String taskId,
  ) : this._internal(
          () => TaskComments()..taskId = taskId,
          from: taskCommentsProvider,
          name: r'taskCommentsProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$taskCommentsHash,
          dependencies: TaskCommentsFamily._dependencies,
          allTransitiveDependencies:
              TaskCommentsFamily._allTransitiveDependencies,
          taskId: taskId,
        );

  TaskCommentsProvider._internal(
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
  FutureOr<List<TaskCommentModel>> runNotifierBuild(
    covariant TaskComments notifier,
  ) {
    return notifier.build(
      taskId,
    );
  }

  @override
  Override overrideWith(TaskComments Function() create) {
    return ProviderOverride(
      origin: this,
      override: TaskCommentsProvider._internal(
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
  AutoDisposeAsyncNotifierProviderElement<TaskComments, List<TaskCommentModel>>
      createElement() {
    return _TaskCommentsProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is TaskCommentsProvider && other.taskId == taskId;
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
mixin TaskCommentsRef
    on AutoDisposeAsyncNotifierProviderRef<List<TaskCommentModel>> {
  /// The parameter `taskId` of this provider.
  String get taskId;
}

class _TaskCommentsProviderElement
    extends AutoDisposeAsyncNotifierProviderElement<TaskComments,
        List<TaskCommentModel>> with TaskCommentsRef {
  _TaskCommentsProviderElement(super.provider);

  @override
  String get taskId => (origin as TaskCommentsProvider).taskId;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
