// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'employee_list_provider.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

String _$employeeListNotifierHash() =>
    r'3d953d445455566902d2cd104a02e21d63662251';

/// Manages the paginated employee list with:
///   • Cache-then-network (stale-while-revalidate via Hive `employees_box`)
///   • Infinite scroll ([loadMore])
///   • Filter support ([applyFilter])
///   • In-place mutations ([updateEmployee], [removeEmployee])
///
/// Copied from [EmployeeListNotifier].
@ProviderFor(EmployeeListNotifier)
final employeeListNotifierProvider = AutoDisposeAsyncNotifierProvider<
    EmployeeListNotifier, EmployeeListState>.internal(
  EmployeeListNotifier.new,
  name: r'employeeListNotifierProvider',
  debugGetCreateSourceHash: const bool.fromEnvironment('dart.vm.product')
      ? null
      : _$employeeListNotifierHash,
  dependencies: null,
  allTransitiveDependencies: null,
);

typedef _$EmployeeListNotifier = AutoDisposeAsyncNotifier<EmployeeListState>;
// ignore_for_file: type=lint
// ignore_for_file: subtype_of_sealed_class, invalid_use_of_internal_member, invalid_use_of_visible_for_testing_member, deprecated_member_use_from_same_package
