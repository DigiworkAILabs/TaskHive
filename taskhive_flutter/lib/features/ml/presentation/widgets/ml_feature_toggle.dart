import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/providers/ml_feature_provider.dart';

class MlFeatureToggleWidget extends ConsumerWidget {
  const MlFeatureToggleWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stateAsync = ref.watch(mlFeatureToggleProvider);

    return stateAsync.when(
      data: (mlState) {
        return Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Local ML Toggle
            _ToggleItem(
              isEnabled: mlState.isMlEnabled,
              onChanged: (_) =>
                  ref.read(mlFeatureToggleProvider.notifier).toggleMl(),
              icon: Icons.psychology_outlined,
              activeColor: Colors.orange,
              tooltip: mlState.isMlEnabled
                  ? 'Local ML Service: ON'
                  : 'Local ML Service: OFF',
            ),
            const SizedBox(width: 8),
            // Gemini AI Toggle
            _ToggleItem(
              isEnabled: mlState.isGeminiEnabled,
              onChanged: (_) =>
                  ref.read(mlFeatureToggleProvider.notifier).toggleGemini(),
              icon: Icons.auto_awesome_outlined,
              activeColor: Colors.purple,
              tooltip: mlState.isGeminiEnabled
                  ? 'Gemini AI Suggest: ON'
                  : 'Gemini AI Suggest: OFF',
            ),
          ],
        );
      },
      loading: () => const Center(
        child: SizedBox(
          width: 20,
          height: 20,
          child: CircularProgressIndicator(strokeWidth: 2),
        ),
      ),
      error: (_, __) => const SizedBox.shrink(),
    );
  }
}

class _ToggleItem extends StatelessWidget {
  final bool isEnabled;
  final ValueChanged<bool> onChanged;
  final IconData icon;
  final Color activeColor;
  final String tooltip;

  const _ToggleItem({
    required this.isEnabled,
    required this.onChanged,
    required this.icon,
    required this.activeColor,
    required this.tooltip,
  });

  @override
  Widget build(BuildContext context) {
    return Tooltip(
      message: tooltip,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 20, color: isEnabled ? activeColor : Colors.grey),
          Transform.scale(
            scale: 0.8,
            child: Switch(
              value: isEnabled,
              onChanged: onChanged,
              activeColor: activeColor,
              materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
          ),
        ],
      ),
    );
  }
}
