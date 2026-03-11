import 'package:flutter/material.dart';

import '../../data/models/create_task_request.dart';

class CompletionTimeEstimate extends StatelessWidget {
  final TaskCompletionTimePrediction? prediction;
  final bool isLoading;
  final String? error;

  const CompletionTimeEstimate({
    super.key,
    required this.prediction,
    required this.isLoading,
    this.error,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return const Padding(
        padding: EdgeInsets.only(top: 8.0),
        child: Row(
          children: [
            SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
            SizedBox(width: 8),
            Text('Fetching AI estimate...'),
          ],
        ),
      );
    }

    if (error != null) {
      return Padding(
        padding: const EdgeInsets.only(top: 8.0),
        child: Text(
          error!,
          style: const TextStyle(color: Colors.red, fontSize: 12),
        ),
      );
    }

    if (prediction == null) return const SizedBox.shrink();

    final range = prediction!.confidenceRange;
    return Padding(
      padding: const EdgeInsets.only(top: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'AI estimate: ${prediction!.estimatedHours.toStringAsFixed(1)} hrs'
            ' (range ${range.low.toStringAsFixed(1)}–${range.high.toStringAsFixed(1)} hrs)',
            style: const TextStyle(fontSize: 12),
          ),
          if (prediction!.reasoning.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(top: 4.0),
              child: Text(
                prediction!.reasoning,
                style: const TextStyle(fontSize: 11, color: Colors.grey),
              ),
            ),
        ],
      ),
    );
  }
}

