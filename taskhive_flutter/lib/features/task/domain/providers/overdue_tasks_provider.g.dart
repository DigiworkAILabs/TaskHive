// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'overdue_tasks_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$overdueTasksHash() => r'85fb63d50b018be7744f669a23224f882481e4fa';

/// Fetches overdue tasks (ADMIN only). No caching — always fresh.
/// Paginated (confirmed by Postman step 21).
///
/// Copied from [overdueTasks].
@ProviderFor(overdueTasks)
final overdueTasksProvider =
    AutoDisposeFutureProvider<List<TaskModel>>.internal(
  overdueTasks,
  name: r'overdueTasksProvider',
  debugGetCreateSourceHash:
      const bool.fromEnvironment('dart.vm.product') ? null : _$overdueTasksHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

@Deprecated('Will be removed in 3.0. Use Ref instead')
// ignore: unused_element
typedef OverdueTasksRef = AutoDisposeFutureProviderRef<List<TaskModel>>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
