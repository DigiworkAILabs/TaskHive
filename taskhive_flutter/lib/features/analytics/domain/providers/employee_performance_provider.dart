import 'dart:convert';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/analytics_repository.dart';
import '../../data/models/employee_performance_response.dart';
import '../../../../core/storage/cache_service.dart';

part 'employee_performance_provider.g.dart';

@riverpod
class EmployeePerformanceNotifier extends _$EmployeePerformanceNotifier {
  @override
  FutureOr<List<EmployeePerformanceResponse>> build() async {
    final cache = CacheService.analyticsBox.get('employee_performance');
    if (cache != null) {
      try {
        final list = (jsonDecode(cache) as List)
            .map((e) => EmployeePerformanceResponse.fromJson(e))
            .toList();
        state = AsyncData(list);
      } catch (_) {}
    }
    return _fetch();
  }
  
  Future<List<EmployeePerformanceResponse>> _fetch() async {
    final repo = ref.read(analyticsRepositoryProvider);
    final data = await repo.getEmployeePerformance();
    CacheService.analyticsBox.put('employee_performance', jsonEncode(data.map((e) => e.toJson()).toList()));
    return data;
  }
  
  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetch());
  }
}
