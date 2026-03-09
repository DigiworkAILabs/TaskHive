import 'package:flutter/material.dart';

import '../../../../../core/theme/app_colors.dart';
import '../../domain/providers/employee_list_provider.dart';

class EmployeeFilterBar extends StatelessWidget {
  final EmployeeListFilter filter;
  final ValueChanged<EmployeeListFilter> onFilterChanged;
  final VoidCallback onClear;

  const EmployeeFilterBar({
    super.key,
    required this.filter,
    required this.onFilterChanged,
    required this.onClear,
  });

  bool get _hasFilters =>
      filter.status != null ||
      filter.department != null ||
      filter.sortBy != 'firstName' ||
      filter.sortDir != 'asc';

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: [
          if (_hasFilters) ...[
            TextButton.icon(
              onPressed: onClear,
              icon: const Icon(Icons.clear_all, size: 20),
              label: const Text('Clear'),
              style: TextButton.styleFrom(
                foregroundColor: Colors.redAccent,
                padding: const EdgeInsets.symmetric(horizontal: 12),
              ),
            ),
            const SizedBox(width: 8),
            Container(
              height: 24,
              width: 1,
              color: Colors.grey.withAlpha(50),
            ),
            const SizedBox(width: 16),
          ],
          // Status Filters
          _FilterGroup(
            title: 'Status',
            options: const ['ACTIVE', 'PENDING', 'INACTIVE'],
            selectedValue: filter.status,
            onSelected: (val) => onFilterChanged(filter.copyWith(status: val)),
          ),
          const SizedBox(width: 16),
          // Department Filters
          _FilterGroup(
            title: 'Department',
            options: const ['Engineering', 'HR', 'Finance', 'Marketing'],
            selectedValue: filter.department,
            onSelected: (val) =>
                onFilterChanged(filter.copyWith(department: val)),
          ),
          const SizedBox(width: 16),
          // Sort Filters
          _FilterGroup(
            title: 'Sort By',
            options: const ['firstName', 'department', 'joinDate', 'status'],
            selectedValue: filter.sortBy,
            onSelected: (val) =>
                onFilterChanged(filter.copyWith(sortBy: val ?? 'firstName')),
          ),
          const SizedBox(width: 16),
          _FilterGroup(
            title: 'Order',
            options: const ['asc', 'desc'],
            selectedValue: filter.sortDir,
            onSelected: (val) =>
                onFilterChanged(filter.copyWith(sortDir: val ?? 'asc')),
          ),
        ],
      ),
    );
  }
}

class _FilterGroup extends StatelessWidget {
  final String title;
  final List<String> options;
  final String? selectedValue;
  final ValueChanged<String?> onSelected;

  const _FilterGroup({
    required this.title,
    required this.options,
    required this.selectedValue,
    required this.onSelected,
  });

  String _formatOption(String opt) {
    if (opt == 'firstName') return 'NAME';
    if (opt == 'joinDate') return 'JOIN DATE';
    return opt.toUpperCase();
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          '$title: ',
          style: TextStyle(
            color: Theme.of(context).hintColor,
            fontWeight: FontWeight.w500,
          ),
        ),
        const SizedBox(width: 8),
        if (title == 'Status' || title == 'Department') ...[
          InputChip(
            label: const Text('All'),
            selected: selectedValue == null,
            selectedColor: AppColors.primary.withAlpha(38),
            onSelected: (_) => onSelected(null),
          ),
          const SizedBox(width: 8),
        ],
        ...options.map((opt) => Padding(
              padding: const EdgeInsets.only(right: 8),
              child: InputChip(
                label: Text(_formatOption(opt)),
                selected: selectedValue == opt,
                selectedColor: AppColors.primary.withAlpha(38),
                onSelected: (_) => onSelected(opt),
              ),
            )),
      ],
    );
  }
}
