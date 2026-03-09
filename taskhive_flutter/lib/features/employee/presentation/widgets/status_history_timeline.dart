import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../core/utils/date_utils.dart';
import '../../data/models/employee_status_history_model.dart';
import '../../data/repositories/employee_repository.dart';

final statusHistoryProvider =
    FutureProvider.family<List<EmployeeStatusHistoryModel>, String>(
        (ref, id) async {
  final repo = ref.watch(employeeRepositoryProvider);
  return repo.getStatusHistory(id);
});

class StatusHistoryTimeline extends ConsumerWidget {
  final String employeeId;

  const StatusHistoryTimeline({super.key, required this.employeeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final historyAsync = ref.watch(statusHistoryProvider(employeeId));

    return SizedBox(
      height: 300,
      child: historyAsync.when(
        loading: () =>
            const Center(child: CircularProgressIndicator.adaptive()),
        error: (err, stack) => Center(
          child: Text(
            'Cannot load history: $err',
            style: TextStyle(color: Theme.of(context).colorScheme.error),
          ),
        ),
        data: (history) {
          if (history.isEmpty) {
            return const Center(
              child: Text('No status history available.'),
            );
          }
          return ListView.builder(
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            itemCount: history.length,
            itemBuilder: (context, index) {
              final entry = history[index];
              final isLast = index == history.length - 1;

              return Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Timeline column
                  Column(
                    children: [
                      Container(
                        width: 12,
                        height: 12,
                        decoration: BoxDecoration(
                          color: Theme.of(context).colorScheme.primary,
                          shape: BoxShape.circle,
                        ),
                      ),
                      if (!isLast)
                        Container(
                          width: 2,
                          height: 50, // adjust based on expected child height
                          color: Theme.of(context).dividerColor,
                        ),
                    ],
                  ),
                  const SizedBox(width: 16),
                  // Content column
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.only(bottom: 24),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '${entry.oldStatus ?? 'Created'} → ${entry.newStatus}',
                            style: const TextStyle(fontWeight: FontWeight.w600),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            'by ${entry.changedBy}',
                            style: TextStyle(
                              fontSize: 12,
                              color: Theme.of(context).hintColor,
                            ),
                          ),
                          const SizedBox(height: 2),
                          Text(
                            AppDateUtils.formatDateTime(
                                DateTime.parse(entry.changedAt)),
                            style: TextStyle(
                              fontSize: 12,
                              color: Theme.of(context).hintColor,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              );
            },
          );
        },
      ),
    );
  }
}
