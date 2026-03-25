import 'package:flutter/material.dart';

class TaskSortOption {
  final String label;
  final String sortBy;
  final String sortDir;
  final IconData icon;

  const TaskSortOption({
    required this.label,
    required this.sortBy,
    required this.sortDir,
    required this.icon,
  });
}

class TaskSortSheet extends StatelessWidget {
  final String? currentSortBy;
  final String? currentSortDir;
  final Function(String sortBy, String sortDir) onSortChanged;

  const TaskSortSheet({
    super.key,
    this.currentSortBy,
    this.currentSortDir,
    required this.onSortChanged,
  });

  static const options = [
    TaskSortOption(
      label: 'Due Date (Earliest)',
      sortBy: 'dueDate',
      sortDir: 'asc',
      icon: Icons.calendar_today,
    ),
    TaskSortOption(
      label: 'Due Date (Latest)',
      sortBy: 'dueDate',
      sortDir: 'desc',
      icon: Icons.event_busy,
    ),
    TaskSortOption(
      label: 'Priority (High to Low)',
      sortBy: 'priority',
      sortDir: 'desc',
      icon: Icons.priority_high,
    ),
    TaskSortOption(
      label: 'Priority (Low to High)',
      sortBy: 'priority',
      sortDir: 'asc',
      icon: Icons.low_priority,
    ),
    TaskSortOption(
      label: 'Status (A-Z)',
      sortBy: 'status',
      sortDir: 'asc',
      icon: Icons.sort_by_alpha,
    ),
    TaskSortOption(
      label: 'Created Date (Newest)',
      sortBy: 'createdAt',
      sortDir: 'desc',
      icon: Icons.history,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 20),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 20, vertical: 10),
            child: Text(
              'Sort By',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ),
          const Divider(),
          ...options.map((opt) {
            final isSelected =
                currentSortBy == opt.sortBy && currentSortDir == opt.sortDir;
            return ListTile(
              leading: Icon(opt.icon,
                  color: isSelected ? Colors.orange : Colors.grey),
              title: Text(
                opt.label,
                style: TextStyle(
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                  color: isSelected ? Colors.orange : null,
                ),
              ),
              trailing: isSelected
                  ? const Icon(Icons.check, color: Colors.orange)
                  : null,
              onTap: () {
                onSortChanged(opt.sortBy, opt.sortDir);
                Navigator.pop(context);
              },
            );
          }),
          const SizedBox(height: 20),
        ],
      ),
    );
  }
}
