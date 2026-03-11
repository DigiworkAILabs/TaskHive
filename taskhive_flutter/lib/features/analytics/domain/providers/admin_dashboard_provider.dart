import 'dart:convert';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/analytics_repository.dart';
import '../../data/models/admin_dashboard_response.dart';
import '../../../../core/storage/cache_service.dart';

part 'admin_dashboard_provider.g.dart';

@riverpod
class AdminDashboardNotifier extends _$AdminDashboardNotifier {
  @override
  FutureOr<AdminDashboardResponse?> build() async {
    final cache = CacheService.analyticsBox.get('admin_dashboard');
    if (cache != null) {
      try {
        final decoded = jsonDecode(cache);
        state = AsyncData(AdminDashboardResponse.fromJson(decoded));
      } catch (_) {}
    }
    return _fetch();
  }

  Future<AdminDashboardResponse> _fetch() async {
    final repository = ref.read(analyticsRepositoryProvider);
    final data = await repository.getAdminDashboard();
    CacheService.analyticsBox.put('admin_dashboard', jsonEncode(data.toJson()));
    return data;
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() => _fetch());
  }
}
