// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_timeline_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$entityTimelineHash() => r'fac11d115fedc908b8e5be1876bc3a326097d3a8';

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

/// See also [entityTimeline].
@ProviderFor(entityTimeline)
const entityTimelineProvider = EntityTimelineFamily();

/// See also [entityTimeline].
class EntityTimelineFamily extends Family<AsyncValue<List<AuditLogModel>>> {
  /// See also [entityTimeline].
  const EntityTimelineFamily();

  /// See also [entityTimeline].
  EntityTimelineProvider call(
    String entityType,
    String entityId,
  ) {
    return EntityTimelineProvider(
      entityType,
      entityId,
    );
  }

  @override
  EntityTimelineProvider getProviderOverride(
    covariant EntityTimelineProvider provider,
  ) {
    return call(
      provider.entityType,
      provider.entityId,
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
  String? get name => r'entityTimelineProvider';
}

/// See also [entityTimeline].
class EntityTimelineProvider
    extends AutoDisposeFutureProvider<List<AuditLogModel>> {
  /// See also [entityTimeline].
  EntityTimelineProvider(
    String entityType,
    String entityId,
  ) : this._internal(
          (ref) => entityTimeline(
            ref as EntityTimelineRef,
            entityType,
            entityId,
          ),
          from: entityTimelineProvider,
          name: r'entityTimelineProvider',
          debugGetCreateSourceHash:
              const bool.fromEnvironment('dart.vm.product')
                  ? null
                  : _$entityTimelineHash,
          dependencies: EntityTimelineFamily._dependencies,
          allTransitiveDependencies:
              EntityTimelineFamily._allTransitiveDependencies,
          entityType: entityType,
          entityId: entityId,
        );

  EntityTimelineProvider._internal(
    super._createNotifier, {
    required super.name,
    required super.dependencies,
    required super.allTransitiveDependencies,
    required super.debugGetCreateSourceHash,
    required super.from,
    required this.entityType,
    required this.entityId,
  }) : super.internal();

  final String entityType;
  final String entityId;

  @override
  Override overrideWith(
    FutureOr<List<AuditLogModel>> Function(EntityTimelineRef provider) create,
  ) {
    return ProviderOverride(
      origin: this,
      override: EntityTimelineProvider._internal(
        (ref) => create(ref as EntityTimelineRef),
        from: from,
        name: null,
        dependencies: null,
        allTransitiveDependencies: null,
        debugGetCreateSourceHash: null,
        entityType: entityType,
        entityId: entityId,
      ),
    );
  }

  @override
  AutoDisposeFutureProviderElement<List<AuditLogModel>> createElement() {
    return _EntityTimelineProviderElement(this);
  }

  @override
  bool operator ==(Object other) {
    return other is EntityTimelineProvider &&
        other.entityType == entityType &&
        other.entityId == entityId;
  }

  @override
  int get hashCode {
    var hash = _SystemHash.combine(0, runtimeType.hashCode);
    hash = _SystemHash.combine(hash, entityType.hashCode);
    hash = _SystemHash.combine(hash, entityId.hashCode);

    return _SystemHash.finish(hash);
  }
}

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
mixin EntityTimelineRef on AutoDisposeFutureProviderRef<List<AuditLogModel>> {
  /// The parameter `entityType` of this provider.
  String get entityType;

  /// The parameter `entityId` of this provider.
  String get entityId;
}

class _EntityTimelineProviderElement
    extends AutoDisposeFutureProviderElement<List<AuditLogModel>>
    with EntityTimelineRef {
  _EntityTimelineProviderElement(super.provider);

  @override
  String get entityType => (origin as EntityTimelineProvider).entityType;
  @override
  String get entityId => (origin as EntityTimelineProvider).entityId;
}
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
