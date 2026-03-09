import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/create_employee_request.dart';
import '../../data/models/employee_model.dart';
import '../../data/models/update_employee_profile_request.dart';
import '../../data/models/update_employee_request.dart';
import '../../data/repositories/employee_repository.dart';
import 'employee_detail_provider.dart';
import 'employee_list_provider.dart';

part 'employee_actions_provider.g.dart';

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
@riverpod
class EmployeeActions extends _$EmployeeActions {
  @override
  AsyncValue<void> build() => const AsyncData(null);

  // ── Create ─────────────────────────────────────────────────────────────────

  /// Creates a new employee and refreshes the list.
  /// Returns the created [EmployeeModel] on success, or null on error.
  Future<EmployeeModel?> createEmployee(CreateEmployeeRequest request) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.createEmployee(request);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: AsyncError.new,
      loading: AsyncLoading.new,
    );
    if (result is AsyncData) {
      // Full refresh so the new employee appears at the correct sorted position.
      ref.read(employeeListNotifierProvider.notifier).refresh();
      return result.value;
    }
    return null;
  }

  // ── Update (Admin) ─────────────────────────────────────────────────────────

  /// Updates any employee field (ADMIN). Updates the list in-place and
  /// invalidates the detail cache.
  Future<EmployeeModel?> updateEmployee(
      String id, UpdateEmployeeRequest request) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.updateEmployee(id, request);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: AsyncError.new,
      loading: AsyncLoading.new,
    );
    if (result is AsyncData) {
      ref
          .read(employeeListNotifierProvider.notifier)
          .updateEmployee(result.value!);
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }

  // ── Update Own Profile (Employee) ──────────────────────────────────────────

  /// Employee updates their own phone/address (FR-EMP-06). Only invalidates
  /// the detail cache — list does not need refreshing.
  Future<EmployeeModel?> updateOwnProfile(
      String id, UpdateEmployeeProfileRequest request) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.updateOwnProfile(id, request);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: AsyncError.new,
      loading: AsyncLoading.new,
    );
    if (result is AsyncData) {
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  /// Soft-deletes an employee and removes them from the in-memory list.
  /// Returns true on success, false on error.
  Future<bool> deleteEmployee(String id) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      await repo.deleteEmployee(id);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: AsyncError.new,
      loading: AsyncLoading.new,
    );
    if (result is AsyncData) {
      ref.read(employeeListNotifierProvider.notifier).removeEmployee(id);
    }
    return result is AsyncData;
  }

  // ── Status Transitions ─────────────────────────────────────────────────────

  /// Activates an employee account (ADMIN, FR-EMP-09).
  /// Updates the list badge and invalidates the detail cache.
  Future<EmployeeModel?> activateEmployee(String id) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.activateEmployee(id);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: AsyncError.new,
      loading: AsyncLoading.new,
    );
    if (result is AsyncData) {
      ref
          .read(employeeListNotifierProvider.notifier)
          .updateEmployee(result.value!);
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }

  /// Deactivates an employee account (ADMIN, FR-EMP-09).
  /// Updates the list badge and invalidates the detail cache.
  Future<EmployeeModel?> deactivateEmployee(String id) async {
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final repo = ref.read(employeeRepositoryProvider);
      return repo.deactivateEmployee(id);
    });
    state = result.when(
      data: (_) => const AsyncData(null),
      error: AsyncError.new,
      loading: AsyncLoading.new,
    );
    if (result is AsyncData) {
      ref
          .read(employeeListNotifierProvider.notifier)
          .updateEmployee(result.value!);
      ref.invalidate(employeeDetailProvider(id));
    }
    return result.valueOrNull;
  }
}
