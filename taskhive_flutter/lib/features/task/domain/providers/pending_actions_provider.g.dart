// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pending_actions_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$pendingActionsHash() => r'f722d3afef1a51a1fe50c32b9ef881a94f4c232f';

/// In-memory queue of offline actions for replay when connectivity restores.
/// The queue is lost on app restart (acceptable for Phase 3).
///
/// Copied from [PendingActions].
@ProviderFor(PendingActions)
final pendingActionsProvider = AutoDisposeNotifierProvider<PendingActions,
    List<PendingActionModel>>.internal(
  PendingActions.new,
  name: r'pendingActionsProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$pendingActionsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$PendingActions = AutoDisposeNotifier<List<PendingActionModel>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
