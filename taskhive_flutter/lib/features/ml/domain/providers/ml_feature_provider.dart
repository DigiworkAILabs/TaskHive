import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:shared_preferences/shared_preferences.dart';

part 'ml_feature_provider.g.dart';

const _kMlFeatureEnabledKey = 'ml_feature_enabled';

@riverpod
class MlFeatureToggle extends _$MlFeatureToggle {
  @override
  FutureOr<bool> build() async {
    final prefs = await SharedPreferences.getInstance();
    // Default to false if not set
    return prefs.getBool(_kMlFeatureEnabledKey) ?? false;
  }

  Future<void> toggle() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final prefs = await SharedPreferences.getInstance();
      final currentValue = prefs.getBool(_kMlFeatureEnabledKey) ?? false;
      final newValue = !currentValue;
      await prefs.setBool(_kMlFeatureEnabledKey, newValue);
      return newValue;
    });
  }

  Future<void> setEnabled(bool enabled) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(_kMlFeatureEnabledKey, enabled);
      return enabled;
    });
  }
}
