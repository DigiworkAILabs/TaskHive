import 'package:flutter/material.dart';

/// Purely presentational — shown when task.isOverdue == true.
/// Small red row with warning icon and "Overdue" label.
class OverdueIndicator extends StatelessWidget {
  const OverdueIndicator({super.key});

  @override
  Widget build(BuildContext context) {
    final errorColor = Theme.of(context).colorScheme.error;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(Icons.warning_amber_rounded, size: 14, color: errorColor),
        const SizedBox(width: 4),
        Text(
          'Overdue',
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w600,
            color: errorColor,
          ),
        ),
      ],
    );
  }
}
