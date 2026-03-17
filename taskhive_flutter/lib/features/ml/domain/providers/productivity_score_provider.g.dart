// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'productivity_score_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$productivityScoreHash() => r'aa714d5f9cb52ec217351ae2225e78ba82eb1f33';

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

/// See also [productivityScore].
@ProviderFor(productivityScore)
const productivityScoreProvider = ProductivityScoreFamily();

/// See also [productivityScore].
class ProductivityScoreFamily
    extends Family<AsyncValue<ProductivityScoreResponse?>> {
  /// See also [productivityScore].
  const ProductivityScoreFamily();

  /// See also [productivityScore].
  ProductivityScoreProvider call(
    String employeeId,
  ) {
    return ProductivityScoreProvider(
      employeeId,
    );
  }

  @override
  ProductivityScoreProvider getProviderOverride(
    covariant ProductivityScoreProvider provider,
  ) {
    return call(
      provider.employeeId,
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
  String? get name => r'productivityScoreProvider';
}

/// See also [productivityScore].
class ProductivityScoreProvider
    extends AutoDisposeFutureProvider<ProductivityScoreResponse?> {
  /// See also [productivityScore].
  ProductivityScoreProvider(
    String employeeId,
  ) : this._internal(
          (ref) => productivityScore(
            ref as ProductivityScoreRef,
            employeeId,
          ),
          from: productivityScoreProvider,
          name: r'productivityScoreProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$productivityScoreHash,
          dependencies: ProductivityScoreFamily._dependencies,
          allTransitiveDependencies:
              ProductivityScoreFamily._allTransitiveDependencies,
          employeeId: employeeId,
        );

  ProductivityScoreProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.employeeId,
  }) : super.internal();

  final String employeeId;

  @override
  Override overrideWith(
    FutureOr<ProductivityScoreResponse?> Function(ProductivityScoreRef provider)
        create,
  ) {
    return ProviderOverride(
      origin: this,
      override: ProductivityScoreProvider._internal(
        (ref) => create(ref as ProductivityScoreRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        employeeId: employeeId,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<ProductivityScoreResponse?> createElement() {
    return _ProductivityScoreProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is ProductivityScoreProvider && other.employeeId == employeeId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, employeeId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin ProductivityScoreRef
    on AutoDisposeFutureProviderRef<ProductivityScoreResponse?> {
  /// The parameter `employeeId` of this provider.
  String get employeeId;
}

class _ProductivityScoreProviderElement
    extends AutoDisposeFutureProviderElement<ProductivityScoreResponse?>
    with ProductivityScoreRef {
  _ProductivityScoreProviderElement(super.provider);

  @override
  String get employeeId => (origin as ProductivityScoreProvider).employeeId;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
