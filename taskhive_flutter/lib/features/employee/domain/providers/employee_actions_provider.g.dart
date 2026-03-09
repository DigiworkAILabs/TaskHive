// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_actions_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$employeeActionsHash() => r'5f74f881c1b4c2424084a8f43be0a32c4b4bc8b8';

/// Handles all employee mutation operations:
/// create, update, delete, activate, deactivate, updateOwnProfile.
///
/// State is `AsyncValue<void>` — the UI watches this to show loading spinners
/// and snackbar errors/success messages.
///
/// After each successful mutation the provider keeps both the list and detail
/// providers in sync without a full re-fetch:
///
/// | Mutation         | List                              | Detail                           |
/// |------------------|-----------------------------------|----------------------------------|
/// | create           | refresh() — new entry paginated   | not needed (user navigates away) |
/// | update           | updateEmployee(updated) in-place  | invalidate(employeeDetailProvider)|
/// | activate         | updateEmployee(updated) in-place  | invalidate(employeeDetailProvider)|
/// | deactivate       | updateEmployee(updated) in-place  | invalidate(employeeDetailProvider)|
/// | delete           | removeEmployee(id) in-place       | not needed (screen pops)         |
/// | updateOwnProfile | no change (list fields unchanged) | invalidate(employeeDetailProvider)|
///
/// Copied from [EmployeeActions].
@ProviderFor(EmployeeActions)
final employeeActionsProvider =
    AutoDisposeNotifierProvider<EmployeeActions, AsyncValue<void>>.internal(
  EmployeeActions.new,
  name: r'employeeActionsProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$employeeActionsHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$EmployeeActions = AutoDisposeNotifier<AsyncValue<void>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
