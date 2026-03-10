import 'package:flutter/material.dart';

import '../../data/models/create_task_request.dart';
import '../../domain/enums/task_priority.dart';

class PrioritySuggestionBadge extends StatelessWidget {
  final TaskPriorityPrediction? prediction;
  final bool isLoading;
  final String? error;
  final ValueChanged<TaskPriority> onAccept;
  final VoidCallback onIgnore;

  const PrioritySuggestionBadge({
    super.key,
    required this.prediction,
    required this.isLoading,
    required this.error,
    required this.onAccept,
    required this.onIgnore,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return Padding(
        padding: const EdgeInsets.only(top: 8.0, bottom: 8.0),
        child: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: Colors.orange.withValues(alpha: 0.05),
            border: Border.all(color: Colors.orange.withValues(alpha: 0.15)),
            borderRadius: BorderRadius.circular(8),
          ),
          child: const Row(
            children: [
              SizedBox(
                width: 12,
                height: 12,
                child: CircularProgressIndicator(
                    strokeWidth: 2, color: Colors.orange),
              ),
              SizedBox(width: 8),
              Text('Analysing with AI...',
                  style: TextStyle(fontSize: 12, color: Colors.grey)),
            ],
          ),
        ),
      );
    }

    if (prediction == null) return const SizedBox.shrink();

    final priority = TaskPriority.values.firstWhere(
      (p) =>
          p.name.toUpperCase() == prediction!.predictedPriority.toUpperCase(),
      orElse: () => TaskPriority.medium,
    );

    final pct = (prediction!.confidence * 100).round();

    return Padding(
      padding: const EdgeInsets.only(top: 8.0, bottom: 8.0),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        decoration: BoxDecoration(
          color: priority.color.withValues(alpha: 0.08),
          border: Border.all(color: priority.color.withValues(alpha: 0.25)),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Icon(Icons.auto_awesome, size: 12, color: priority.color),
                    const SizedBox(width: 4),
                    Text(
                      'AI suggests: ',
                      style: TextStyle(
                          fontSize: 12,
                          color: priority.color,
                          fontWeight: FontWeight.w600),
                    ),
                    Text(
                      priority.label,
                      style: TextStyle(
                          fontSize: 12,
                          color: priority.color,
                          fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                GestureDetector(
                  onTap: onIgnore,
                  child: const Icon(Icons.close, size: 14, color: Colors.grey),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: LinearProgressIndicator(
                    value: prediction!.confidence,
                    backgroundColor: Colors.white.withValues(alpha: 0.1),
                    valueColor: AlwaysStoppedAnimation<Color>(priority.color),
                    minHeight: 4,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(width: 8),
                Text('$pct% confidence',
                    style: const TextStyle(fontSize: 10, color: Colors.grey)),
              ],
            ),
            if (prediction!.reasoning.isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(
                prediction!.reasoning,
                style: TextStyle(
                    fontSize: 11, color: Colors.grey[600], height: 1.4),
              ),
            ],
            const SizedBox(height: 8),
            Row(
              children: [
                ElevatedButton(
                  onPressed: () => onAccept(priority),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: priority.color,
                    foregroundColor: Colors.white,
                    minimumSize: const Size(0, 24),
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(6)),
                    elevation: 0,
                  ),
                  child: const Text('✓ Accept',
                      style:
                          TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
                ),
                const SizedBox(width: 6),
                OutlinedButton(
                  onPressed: onIgnore,
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.grey[600],
                    minimumSize: const Size(0, 24),
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
                    side: BorderSide(
                        color: priority.color.withValues(alpha: 0.25)),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(6)),
                  ),
                  child: const Text('Ignore', style: TextStyle(fontSize: 11)),
                ),
                if (prediction!.fallbackUsed)
                  Padding(
                    padding: const EdgeInsets.only(left: 6.0),
                    child: Text('(default)',
                        style:
                            TextStyle(fontSize: 10, color: Colors.grey[700])),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
