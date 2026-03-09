import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../employee/data/models/employee_model.dart';
import '../../../employee/domain/providers/employee_search_provider.dart';

/// ConsumerWidget that lets the user pick an ACTIVE employee as a task assignee.
/// Uses a DropdownButtonFormField to match the Next.js frontend implementation.
class AssigneePicker extends ConsumerWidget {
  final String? initialAssigneeId;
  final ValueChanged<String?> onChanged;

  const AssigneePicker({
    super.key,
    this.initialAssigneeId,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Fetch all employees (empty query) to populate dropdown
    final employeesAsync = ref.watch(employeeSearchProvider(''));

    return employeesAsync.when(
      data: (list) {
        final active = list.where((e) => e.isActive).toList();

        // Ensure the initial value actually exists in the active list
        final isValidId = initialAssigneeId != null &&
            active.any((e) => e.id == initialAssigneeId);
        final value = isValidId ? initialAssigneeId : null;

        return DropdownButtonFormField<String>(
          initialValue: value,
          isExpanded: true,
          decoration: const InputDecoration(
            labelText: 'Assignee *',
            prefixIcon: Icon(Icons.person_outline),
            border: OutlineInputBorder(),
          ),
          hint: const Text('Select an employee...'),
          items: active.map((e) {
            return DropdownMenuItem<String>(
              value: e.id,
              child: Text('${e.firstName} ${e.lastName} (${e.department})'),
            );
          }).toList(),
          onChanged: onChanged,
          validator: (v) => v == null ? 'Please select an assignee' : null,
        );
      },
      loading: () => DropdownButtonFormField<String>(
        initialValue: null,
        decoration: const InputDecoration(
          labelText: 'Assignee *',
          prefixIcon: Icon(Icons.person_outline),
          border: OutlineInputBorder(),
        ),
        hint: const Text('Loading names...'),
        items: const [],
        onChanged: null,
      ),
      error: (err, _) => DropdownButtonFormField<String>(
        initialValue: null,
        decoration: const InputDecoration(
          labelText: 'Assignee *',
          prefixIcon: Icon(Icons.error_outline, color: Colors.red),
          border: OutlineInputBorder(),
          errorText: 'Failed to load employees',
        ),
        items: const [],
        onChanged: null,
      ),
    );
  }
}
