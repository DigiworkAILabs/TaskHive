// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_search_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$taskSearchHash() => r'2b3adcfebdbf222cae5827b4cf37345bb34373c1';

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

/// Family provider for full-text task search.
/// Returns an empty list for blank queries (saves unnecessary network call).
///
/// Copied from [taskSearch].
@ProviderFor(taskSearch)
const taskSearchProvider = TaskSearchFamily();

/// Family provider for full-text task search.
/// Returns an empty list for blank queries (saves unnecessary network call).
///
/// Copied from [taskSearch].
class TaskSearchFamily extends Family<AsyncValue<List<TaskModel>>> {
  /// Family provider for full-text task search.
  /// Returns an empty list for blank queries (saves unnecessary network call).
  ///
  /// Copied from [taskSearch].
  const TaskSearchFamily();

  /// Family provider for full-text task search.
  /// Returns an empty list for blank queries (saves unnecessary network call).
  ///
  /// Copied from [taskSearch].
  TaskSearchProvider call(
    String query,
  ) {
    return TaskSearchProvider(
      query,
    );
  }

  @override
  TaskSearchProvider getProviderOverride(
    covariant TaskSearchProvider provider,
  ) {
    return call(
      provider.query,
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
  String? get name => r'taskSearchProvider';
}

/// Family provider for full-text task search.
/// Returns an empty list for blank queries (saves unnecessary network call).
///
/// Copied from [taskSearch].
class TaskSearchProvider extends AutoDisposeFutureProvider<List<TaskModel>> {
  /// Family provider for full-text task search.
  /// Returns an empty list for blank queries (saves unnecessary network call).
  ///
  /// Copied from [taskSearch].
  TaskSearchProvider(
    String query,
  ) : this._internal(
          (ref) => taskSearch(
            ref as TaskSearchRef,
            query,
          ),
          from: taskSearchProvider,
          name: r'taskSearchProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$taskSearchHash,
          dependencies: TaskSearchFamily._dependencies,
          allTransitiveDependencies:
              TaskSearchFamily._allTransitiveDependencies,
          query: query,
        );

  TaskSearchProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.query,
  }) : super.internal();

  final String query;

  @override
  Override overrideWith(
    FutureOr<List<TaskModel>> Function(TaskSearchRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: TaskSearchProvider._internal(
        (ref) => create(ref as TaskSearchRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        query: query,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<TaskModel>> createElement() {
    return _TaskSearchProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is TaskSearchProvider && other.query == query;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, query.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin TaskSearchRef on AutoDisposeFutureProviderRef<List<TaskModel>> {
  /// The parameter `query` of this provider.
  String get query;
}

class _TaskSearchProviderElement
    extends AutoDisposeFutureProviderElement<List<TaskModel>>
    with TaskSearchRef {
  _TaskSearchProviderElement(super.provider);

  @override
  String get query => (origin as TaskSearchProvider).query;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
