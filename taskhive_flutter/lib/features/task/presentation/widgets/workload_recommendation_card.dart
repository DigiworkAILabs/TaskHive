import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../employee/domain/providers/employee_search_provider.dart';
import '../../data/models/create_task_request.dart';

class WorkloadRecommendationCard extends ConsumerWidget {
  final WorkloadRecommendationResponse? prediction;
  final bool isLoading;
  final String? error;
  final ValueChanged<String> onAssign;

  const WorkloadRecommendationCard({
    super.key,
    required this.prediction,
    required this.isLoading,
    this.error,
    required this.onAssign,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (isLoading) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(16.0),
          child: Row(
            children: [
              SizedBox(
                width: 16,
                height: 16,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
              SizedBox(width: 12),
              Text('Analyzing workload balance...'),
            ],
          ),
        ),
      );
    }

    if (error != null) {
      return Card(
        color: Colors.red.shade50,
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Text(
            error!,
            style: const TextStyle(color: Colors.red, fontSize: 13),
          ),
        ),
      );
    }

    if (prediction == null) return const SizedBox.shrink();

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: Colors.grey.shade300),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.psychology, color: Colors.orange, size: 20),
                const SizedBox(width: 8),
                const Text(
                  'AI Recommendation',
                  style: TextStyle(fontWeight: FontWeight.bold),
                ),
                const Spacer(),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Row(
                    children: [
                      Icon(Icons.bar_chart, size: 12, color: Colors.grey.shade600),
                      const SizedBox(width: 4),
                      Text(
                        'Ranked by Workload & Performance',
                        style: TextStyle(fontSize: 10, color: Colors.grey.shade600),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              prediction!.reasoning,
              style: const TextStyle(fontSize: 13),
            ),
            const SizedBox(height: 16),
            ...prediction!.scoreBreakdown.map((score) {
              return _CandidateRow(
                score: score,
                isRecommended: score.employeeId == prediction!.recommendedEmployeeId,
              );
            }),
            const SizedBox(height: 16),
            if (prediction!.recommendedEmployeeId != null)
              SizedBox(
                width: double.infinity,
                child: Consumer(builder: (context, ref, _) {
                  final employeesAsync = ref.watch(employeeSearchProvider(''));
                  final recommendedName = employeesAsync.maybeWhen(
                    data: (list) {
                      final emp = list.firstWhere(
                        (e) => e.id == prediction!.recommendedEmployeeId,
                        orElse: () => list.first,
                      );
                      return '${emp.firstName} ${emp.lastName}';
                    },
                    orElse: () => 'Employee',
                  );

                  return FilledButton.icon(
                    onPressed: () => onAssign(prediction!.recommendedEmployeeId!),
                    icon: const Icon(Icons.person_add_alt_1_outlined, size: 18),
                    label: Text('Assign to $recommendedName'),
                    style: FilledButton.styleFrom(
                      backgroundColor: Colors.black87,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                  );
                }),
              ),
          ],
        ),
      ),
    );
  }
}

class _CandidateRow extends ConsumerWidget {
  final EmployeeScoreBreakdown score;
  final bool isRecommended;

  const _CandidateRow({
    required this.score,
    required this.isRecommended,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final employeesAsync = ref.watch(employeeSearchProvider(''));
    final name = employeesAsync.maybeWhen(
      data: (list) {
        final emp = list.where((e) => e.id == score.employeeId).firstOrNull;
        return emp != null ? '${emp.firstName} ${emp.lastName}' : 'Unknown';
      },
      orElse: () => 'Loading...',
    );

    final color = isRecommended ? Colors.orange : Colors.grey.shade400;

    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                name,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: isRecommended ? FontWeight.bold : FontWeight.normal,
                  color: isRecommended ? Colors.orange.shade800 : Colors.grey.shade700,
                ),
              ),
              Text(
                '${score.score.toInt()}%',
                style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
              ),
            ],
          ),
          const SizedBox(height: 4),
          ClipRRect(
            borderRadius: BorderRadius.circular(2),
            child: LinearProgressIndicator(
              value: score.score / 100,
              backgroundColor: Colors.grey.shade200,
              valueColor: AlwaysStoppedAnimation<Color>(color),
              minHeight: 6,
            ),
          ),
        ],
      ),
    );
  }
}
