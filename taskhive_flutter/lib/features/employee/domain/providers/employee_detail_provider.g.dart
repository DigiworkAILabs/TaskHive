// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_detail_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$employeeDetailHash() => r'8cf7991f7bb7b7b70ea38baed27eba27e9dbe1b5';

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

/// Fetches and caches a single employee by UUID.
///
/// This is a family provider — each unique [id] gets its own cached instance.
/// Invalidated by [EmployeeActions] after any mutation (update, status change)
/// so the detail screen always reflects the latest server state.
///
/// Copied from [employeeDetail].
@ProviderFor(employeeDetail)
const employeeDetailProvider = EmployeeDetailFamily();

/// Fetches and caches a single employee by UUID.
///
/// This is a family provider — each unique [id] gets its own cached instance.
/// Invalidated by [EmployeeActions] after any mutation (update, status change)
/// so the detail screen always reflects the latest server state.
///
/// Copied from [employeeDetail].
class EmployeeDetailFamily extends Family<AsyncValue<EmployeeModel>> {
  /// Fetches and caches a single employee by UUID.
  ///
  /// This is a family provider — each unique [id] gets its own cached instance.
  /// Invalidated by [EmployeeActions] after any mutation (update, status change)
  /// so the detail screen always reflects the latest server state.
  ///
  /// Copied from [employeeDetail].
  const EmployeeDetailFamily();

  /// Fetches and caches a single employee by UUID.
  ///
  /// This is a family provider — each unique [id] gets its own cached instance.
  /// Invalidated by [EmployeeActions] after any mutation (update, status change)
  /// so the detail screen always reflects the latest server state.
  ///
  /// Copied from [employeeDetail].
  EmployeeDetailProvider call(
    String id,
  ) {
    return EmployeeDetailProvider(
      id,
    );
  }

  @override
  EmployeeDetailProvider getProviderOverride(
    covariant EmployeeDetailProvider provider,
  ) {
    return call(
      provider.id,
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
  String? get name => r'employeeDetailProvider';
}

/// Fetches and caches a single employee by UUID.
///
/// This is a family provider — each unique [id] gets its own cached instance.
/// Invalidated by [EmployeeActions] after any mutation (update, status change)
/// so the detail screen always reflects the latest server state.
///
/// Copied from [employeeDetail].
class EmployeeDetailProvider extends AutoDisposeFutureProvider<EmployeeModel> {
  /// Fetches and caches a single employee by UUID.
  ///
  /// This is a family provider — each unique [id] gets its own cached instance.
  /// Invalidated by [EmployeeActions] after any mutation (update, status change)
  /// so the detail screen always reflects the latest server state.
  ///
  /// Copied from [employeeDetail].
  EmployeeDetailProvider(
    String id,
  ) : this._internal(
          (ref) => employeeDetail(
            ref as EmployeeDetailRef,
            id,
          ),
          from: employeeDetailProvider,
          name: r'employeeDetailProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$employeeDetailHash,
          dependencies: EmployeeDetailFamily._dependencies,
          allTransitiveDependencies:
              EmployeeDetailFamily._allTransitiveDependencies,
          id: id,
        );

  EmployeeDetailProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.id,
  }) : super.internal();

  final String id;

  @override
  Override overrideWith(
    FutureOr<EmployeeModel> Function(EmployeeDetailRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: EmployeeDetailProvider._internal(
        (ref) => create(ref as EmployeeDetailRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        id: id,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<EmployeeModel> createElement() {
    return _EmployeeDetailProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is EmployeeDetailProvider && other.id == id;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, id.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin EmployeeDetailRef on AutoDisposeFutureProviderRef<EmployeeModel> {
  /// The parameter `id` of this provider.
  String get id;
}

class _EmployeeDetailProviderElement
    extends AutoDisposeFutureProviderElement<EmployeeModel>
    with EmployeeDetailRef {
  _EmployeeDetailProviderElement(super.provider);

  @override
  String get id => (origin as EmployeeDetailProvider).id;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
