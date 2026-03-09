import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/providers/task_history_provider.dart';

/// ConsumerWidget — shows a vertical timeline of task status changes.
/// Each row shows: oldStatus → newStatus, actor name, optional comment, timestamp.
class TaskStatusHistoryTimeline extends ConsumerWidget {
  final String taskId;

  const TaskStatusHistoryTimeline({super.key, required this.taskId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final historyAsync = ref.watch(taskHistoryProvider(taskId));
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Status History',
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        historyAsync.when(
          data: (history) {
            if (history.isEmpty) {
              return const Text('No history yet.',
                  style: TextStyle(color: Colors.grey));
            }
            return Column(
              children: history.asMap().entries.map((entry) {
                final i = entry.key;
                final h = entry.value;
                final isLast = i == history.length - 1;
                return IntrinsicHeight(
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Timeline column
                      SizedBox(
                        width: 24,
                        child: Column(
                          children: [
                            Container(
                              width: 10,
                              height: 10,
                              margin: const EdgeInsets.only(top: 4),
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                color: Theme.of(context).colorScheme.primary,
                              ),
                            ),
                            if (!isLast)
                              Expanded(
                                child: Container(
                                  width: 2,
                                  color: Colors.grey.shade300,
                                ),
                              ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 10),
                      // Content
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.only(bottom: 16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              // Status transition
                              Row(
                                children: [
                                  if (h.oldStatus != null) ...[
                                    _statusChip(h.oldStatus!),
                                    const Padding(
                                      padding:
                                          EdgeInsets.symmetric(horizontal: 4),
                                      child:
                                          Icon(Icons.arrow_forward, size: 14),
                                    ),
                                  ],
                                  _statusChip(h.newStatus),
                                ],
                              ),
                              const SizedBox(height: 4),
                              // Actor + timestamp
                              Text(
                                '${h.changedByName} • ${_formatDate(h.changedAt)}',
                                style: const TextStyle(
                                    fontSize: 12, color: Colors.grey),
                              ),
                              // Optional comment
                              if (h.comment != null && h.comment!.isNotEmpty)
                                Padding(
                                  padding: const EdgeInsets.only(top: 4),
                                  child: Text(
                                    '"${h.comment}"',
                                    style: TextStyle(
                                      fontSize: 12,
                                      fontStyle: FontStyle.italic,
                                      color: Colors.grey.shade700,
                                    ),
                                  ),
                                ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              }).toList(),
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Text('Error loading history: ${e.toString()}'),
        ),
      ],
    );
  }

  Widget _statusChip(String statusText) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: Colors.grey.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(statusText.replaceAll('_', ' '),
          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
    );
  }

  String _formatDate(String isoDate) {
    try {
      final d = DateTime.parse(isoDate).toLocal();
      return '${d.day}/${d.month}/${d.year} ${d.hour}:${d.minute.toString().padLeft(2, '0')}';
    } catch (_) {
      return isoDate;
    }
  }
}
