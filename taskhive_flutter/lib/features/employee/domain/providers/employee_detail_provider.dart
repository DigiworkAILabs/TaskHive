import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/employee_model.dart';
import '../../data/repositories/employee_repository.dart';

part 'employee_detail_provider.g.dart';

/// Fetches and caches a single employee by UUID.
///
/// This is a family provider — each unique [id] gets its own cached instance.
/// Invalidated by [EmployeeActions] after any mutation (update, status change)
/// so the detail screen always reflects the latest server state.
@riverpod
Future<EmployeeModel> employeeDetail(EmployeeDetailRef ref, String id) async {
  final repo = ref.watch(employeeRepositoryProvider);
  return repo.getEmployeeById(id);
}
