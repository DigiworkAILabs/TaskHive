// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_search_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$employeeSearchHash() => r'006209e8a475885e5db5fcc28a07deef99745da4';

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

/// Debounced employee search — pass the current query string as the family param.
///
/// Returns an empty list immediately for blank/whitespace queries so that no
/// network call is made until the user has typed something meaningful.
/// The screen is responsible for debouncing the query before passing it here
/// (e.g., using a 350 ms Timer that updates the watched query string).
///
/// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
///
/// Copied from [employeeSearch].
@ProviderFor(employeeSearch)
const employeeSearchProvider = EmployeeSearchFamily();

/// Debounced employee search — pass the current query string as the family param.
///
/// Returns an empty list immediately for blank/whitespace queries so that no
/// network call is made until the user has typed something meaningful.
/// The screen is responsible for debouncing the query before passing it here
/// (e.g., using a 350 ms Timer that updates the watched query string).
///
/// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
///
/// Copied from [employeeSearch].
class EmployeeSearchFamily extends Family<AsyncValue<List<EmployeeModel>>> {
  /// Debounced employee search — pass the current query string as the family param.
  ///
  /// Returns an empty list immediately for blank/whitespace queries so that no
  /// network call is made until the user has typed something meaningful.
  /// The screen is responsible for debouncing the query before passing it here
  /// (e.g., using a 350 ms Timer that updates the watched query string).
  ///
  /// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
  ///
  /// Copied from [employeeSearch].
  const EmployeeSearchFamily();

  /// Debounced employee search — pass the current query string as the family param.
  ///
  /// Returns an empty list immediately for blank/whitespace queries so that no
  /// network call is made until the user has typed something meaningful.
  /// The screen is responsible for debouncing the query before passing it here
  /// (e.g., using a 350 ms Timer that updates the watched query string).
  ///
  /// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
  ///
  /// Copied from [employeeSearch].
  EmployeeSearchProvider call(
    String query,
  ) {
    return EmployeeSearchProvider(
      query,
    );
  }

  @override
  EmployeeSearchProvider getProviderOverride(
    covariant EmployeeSearchProvider provider,
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
  String? get name => r'employeeSearchProvider';
}

/// Debounced employee search — pass the current query string as the family param.
///
/// Returns an empty list immediately for blank/whitespace queries so that no
/// network call is made until the user has typed something meaningful.
/// The screen is responsible for debouncing the query before passing it here
/// (e.g., using a 350 ms Timer that updates the watched query string).
///
/// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
///
/// Copied from [employeeSearch].
class EmployeeSearchProvider
    extends AutoDisposeFutureProvider<List<EmployeeModel>> {
  /// Debounced employee search — pass the current query string as the family param.
  ///
  /// Returns an empty list immediately for blank/whitespace queries so that no
  /// network call is made until the user has typed something meaningful.
  /// The screen is responsible for debouncing the query before passing it here
  /// (e.g., using a 350 ms Timer that updates the watched query string).
  ///
  /// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
  ///
  /// Copied from [employeeSearch].
  EmployeeSearchProvider(
    String query,
  ) : this._internal(
          (ref) => employeeSearch(
            ref as EmployeeSearchRef,
            query,
          ),
          from: employeeSearchProvider,
          name: r'employeeSearchProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$employeeSearchHash,
          dependencies: EmployeeSearchFamily._dependencies,
          allTransitiveDependencies:
              EmployeeSearchFamily._allTransitiveDependencies,
          query: query,
        );

  EmployeeSearchProvider._internal(
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
    FutureOr<List<EmployeeModel>> Function(EmployeeSearchRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: EmployeeSearchProvider._internal(
        (ref) => create(ref as EmployeeSearchRef),
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
  AutoDisposeFutureProviderElement<List<EmployeeModel>> createElement() {
    return _EmployeeSearchProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is EmployeeSearchProvider && other.query == query;
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
mixin EmployeeSearchRef on AutoDisposeFutureProviderRef<List<EmployeeModel>> {
  /// The parameter `query` of this provider.
  String get query;
}

class _EmployeeSearchProviderElement
    extends AutoDisposeFutureProviderElement<List<EmployeeModel>>
    with EmployeeSearchRef {
  _EmployeeSearchProviderElement(super.provider);

  @override
  String get query => (origin as EmployeeSearchProvider).query;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
