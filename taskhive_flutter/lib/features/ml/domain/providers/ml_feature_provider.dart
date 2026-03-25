import 'package:riverpod_annotation/riverpod_annotation.dart';
import 'package:shared_preferences/shared_preferences.dart';

part 'ml_feature_provider.g.dart';

class MlFeatureState {
  final bool isMlEnabled;
  final bool isGeminiEnabled;

  const MlFeatureState({
    required this.isMlEnabled,
    required this.isGeminiEnabled,
  });
}

const _kMlEnabledKey = 'ml_enabled';
const _kGeminiEnabledKey = 'gemini_enabled';

@riverpod
class MlFeatureToggle extends _$MlFeatureToggle {
  @override
  FutureOr<MlFeatureState> build() async {
    final prefs = await SharedPreferences.getInstance();
    bool isMlEnabled = prefs.getBool(_kMlEnabledKey) ?? false;
    bool isGeminiEnabled = prefs.getBool(_kGeminiEnabledKey) ?? false;

    // Enforce mutual exclusivity on load:
    if (isMlEnabled && isGeminiEnabled) {
      isGeminiEnabled = false;
      await prefs.setBool(_kGeminiEnabledKey, false);
    }

    return MlFeatureState(
      isMlEnabled: isMlEnabled,
      isGeminiEnabled: isGeminiEnabled,
    );
  }

  Future<void> toggleMl() async {
    final current = state.value;
    if (current == null) return;

    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final prefs = await SharedPreferences.getInstance();
      final newValue = !current.isMlEnabled;
      await prefs.setBool(_kMlEnabledKey, newValue);
      
      bool newGeminiValue = current.isGeminiEnabled;
      if (newValue) {
        newGeminiValue = false;
        await prefs.setBool(_kGeminiEnabledKey, false);
      }

      return MlFeatureState(
        isMlEnabled: newValue,
        isGeminiEnabled: newGeminiValue,
      );
    });
  }

  Future<void> toggleGemini() async {
    final current = state.value;
    if (current == null) return;

    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final prefs = await SharedPreferences.getInstance();
      final newValue = !current.isGeminiEnabled;
      await prefs.setBool(_kGeminiEnabledKey, newValue);
      
      bool newMlValue = current.isMlEnabled;
      if (newValue) {
        newMlValue = false;
        await prefs.setBool(_kMlEnabledKey, false);
      }

      return MlFeatureState(
        isMlEnabled: newMlValue,
        isGeminiEnabled: newValue,
      );
    });
  }
}
