// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_history_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$taskHistoryHash() => r'445062bff05fbaefd0abfe97a99b70eb1c3b5ef1';

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

/// Family provider that fetches status change history for a specific task.
/// No caching — always fresh. Invalidated when task status changes.
///
/// Copied from [taskHistory].
@ProviderFor(taskHistory)
const taskHistoryProvider = TaskHistoryFamily();

/// Family provider that fetches status change history for a specific task.
/// No caching — always fresh. Invalidated when task status changes.
///
/// Copied from [taskHistory].
class TaskHistoryFamily
    extends Family<AsyncValue<List<TaskStatusHistoryModel>>> {
  /// Family provider that fetches status change history for a specific task.
  /// No caching — always fresh. Invalidated when task status changes.
  ///
  /// Copied from [taskHistory].
  const TaskHistoryFamily();

  /// Family provider that fetches status change history for a specific task.
  /// No caching — always fresh. Invalidated when task status changes.
  ///
  /// Copied from [taskHistory].
  TaskHistoryProvider call(
    String taskId,
  ) {
    return TaskHistoryProvider(
      taskId,
    );
  }

  @override
  TaskHistoryProvider getProviderOverride(
    covariant TaskHistoryProvider provider,
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
  String? get name => r'taskHistoryProvider';
}

/// Family provider that fetches status change history for a specific task.
/// No caching — always fresh. Invalidated when task status changes.
///
/// Copied from [taskHistory].
class TaskHistoryProvider
    extends AutoDisposeFutureProvider<List<TaskStatusHistoryModel>> {
  /// Family provider that fetches status change history for a specific task.
  /// No caching — always fresh. Invalidated when task status changes.
  ///
  /// Copied from [taskHistory].
  TaskHistoryProvider(
    String taskId,
  ) : this._internal(
          (ref) => taskHistory(
            ref as TaskHistoryRef,
            taskId,
          ),
          from: taskHistoryProvider,
          name: r'taskHistoryProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$taskHistoryHash,
          dependencies: TaskHistoryFamily._dependencies,
          allTransitiveDependencies:
              TaskHistoryFamily._allTransitiveDependencies,
          taskId: taskId,
        );

  TaskHistoryProvider._internal(
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
  Override overrideWith(
    FutureOr<List<TaskStatusHistoryModel>> Function(TaskHistoryRef provider)
        create,
  ) {
    return ProviderOverride(
      origin: this,
      override: TaskHistoryProvider._internal(
        (ref) => create(ref as TaskHistoryRef),
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
  AutoDisposeFutureProviderElement<List<TaskStatusHistoryModel>>
      createElement() {
    return _TaskHistoryProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is TaskHistoryProvider && other.taskId == taskId;
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
mixin TaskHistoryRef
    on AutoDisposeFutureProviderRef<List<TaskStatusHistoryModel>> {
  /// The parameter `taskId` of this provider.
  String get taskId;
}

class _TaskHistoryProviderElement
    extends AutoDisposeFutureProviderElement<List<TaskStatusHistoryModel>>
    with TaskHistoryRef {
  _TaskHistoryProviderElement(super.provider);

  @override
  String get taskId => (origin as TaskHistoryProvider).taskId;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
