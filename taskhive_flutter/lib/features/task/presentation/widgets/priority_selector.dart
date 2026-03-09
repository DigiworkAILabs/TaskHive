import 'package:flutter/material.dart';

import '../../domain/enums/task_priority.dart';

/// Dropdown selector for TaskPriority natively matching Next.js.
class PrioritySelector extends StatelessWidget {
  final TaskPriority? selected;
  final ValueChanged<TaskPriority> onChanged;

  const PrioritySelector({
    super.key,
    required this.selected,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return DropdownButtonFormField<TaskPriority>(
      initialValue: selected,
      isExpanded: true,
      decoration: const InputDecoration(
        labelText: 'Priority *',
        border: OutlineInputBorder(),
      ),
      items: TaskPriority.values.map((p) {
        return DropdownMenuItem<TaskPriority>(
          value: p,
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(p.icon, size: 16, color: p.color),
              const SizedBox(width: 8),
              Text(p.label),
            ],
          ),
        );
      }).toList(),
      onChanged: (val) {
        if (val != null) onChanged(val);
      },
      validator: (v) => v == null ? 'Please select a priority' : null,
    );
  }
}
