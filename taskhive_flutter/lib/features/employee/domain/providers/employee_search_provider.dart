import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/models/employee_model.dart';
import '../../data/repositories/employee_repository.dart';

part 'employee_search_provider.g.dart';

/// Debounced employee search — pass the current query string as the family param.
///
/// Returns an empty list immediately for blank/whitespace queries so that no
/// network call is made until the user has typed something meaningful.
/// The screen is responsible for debouncing the query before passing it here
/// (e.g., using a 350 ms Timer that updates the watched query string).
///
/// Endpoint: GET /employees/search?query=... (Postman test 5 confirms `query` param).
@riverpod
Future<List<EmployeeModel>> employeeSearch(
    EmployeeSearchRef ref, String query) async {
  final repo = ref.watch(employeeRepositoryProvider);

  // If query is empty, return up to 100 employees to populate dropdowns menus initially
  if (query.trim().isEmpty) {
    final data = await repo.getEmployees(size: 100);
    final content = (data['content'] as List<dynamic>)
        .map((e) => EmployeeModel.fromJson(e as Map<String, dynamic>))
        .toList();
    return content;
  }
  return repo.searchEmployees(query.trim());
}
