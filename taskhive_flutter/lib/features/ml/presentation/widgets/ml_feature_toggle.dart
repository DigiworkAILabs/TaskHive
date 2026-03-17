import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/providers/ml_feature_provider.dart';

class MlFeatureToggleWidget extends ConsumerWidget {
  const MlFeatureToggleWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isEnabledAsync = ref.watch(mlFeatureToggleProvider);

    return isEnabledAsync.when(
      data: (isEnabled) {
        return Tooltip(
          message: isEnabled ? 'Disable ML Insights' : 'Enable ML Insights',
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.auto_awesome,
                size: 18,
                color: isEnabled ? Theme.of(context).primaryColor : Colors.grey.shade400,
              ),
              const SizedBox(width: 4),
              Switch(
                value: isEnabled,
                onChanged: (value) {
                  ref.read(mlFeatureToggleProvider.notifier).setEnabled(value);
                },
                activeColor: Theme.of(context).primaryColor,
              ),
            ],
          ),
        );
      },
      loading: () => const Padding(
        padding: EdgeInsets.symmetric(horizontal: 16.0),
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
