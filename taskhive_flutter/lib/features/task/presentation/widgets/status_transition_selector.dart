import 'package:flutter/material.dart';

import '../../domain/enums/task_status.dart';

/// Shows only the valid next statuses for the current task status as selectable chips.
/// If the status is terminal (DONE / CANCELLED), shows a disabled message.
/// This is the single source of truth for status transition UI — do not duplicate logic.
class StatusTransitionSelector extends StatelessWidget {
  final TaskStatus currentStatus;
  final bool isAdmin;
  final bool approvalRequired;
  final ValueChanged<TaskStatus> onChanged;

  const StatusTransitionSelector({
    super.key,
    required this.currentStatus,
    this.isAdmin = false,
    this.approvalRequired = false,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    List<TaskStatus> transitions;
    if (currentStatus == TaskStatus.pendingApproval) {
      transitions = [];
    } else {
      transitions = currentStatus.allowedTransitions.where((s) {
        if (s == currentStatus) return false;
        if (s == TaskStatus.done && approvalRequired && !isAdmin) return false;
        if (s == TaskStatus.pendingApproval) return false;
        if (s == TaskStatus.cancelled && !isAdmin) return false;
        return true;
      }).toList();
    }


    if (transitions.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.grey.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(8),
        ),
        child: const Row(
          children: [
            Icon(Icons.lock_outline, size: 16, color: Colors.grey),
            SizedBox(width: 8),
            Text(
              'No further transitions available',
              style: TextStyle(color: Colors.grey, fontStyle: FontStyle.italic),
            ),
          ],
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Change Status:',
          style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: transitions.map((s) {
            return ActionChip(
              avatar: CircleAvatar(backgroundColor: s.color, radius: 6),
              label: Text(s.label),
              backgroundColor: s.color.withValues(alpha: 0.1),
              side: BorderSide(color: s.color.withValues(alpha: 0.4)),
              labelStyle:
                  TextStyle(color: s.color, fontWeight: FontWeight.w600),
              onPressed: () => onChanged(s),
            );
          }).toList(),
        ),
      ],
    );
  }
}
