// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'task_actions_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$taskActionsHash() => r'b3291a5f0afbdb2d37f5e0a80818a0e2952f509d';

/// Handles all task mutations: create, update, delete, status change.
/// After each mutation it updates the relevant list providers and invalidates
/// the task detail provider to ensure fresh data.
///
/// Copied from [TaskActions].
@ProviderFor(TaskActions)
final taskActionsProvider =
    AutoDisposeAsyncNotifierProvider<TaskActions, void>.internal(
  TaskActions.new,
  name: r'taskActionsProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$taskActionsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$TaskActions = AutoDisposeAsyncNotifier<void>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
