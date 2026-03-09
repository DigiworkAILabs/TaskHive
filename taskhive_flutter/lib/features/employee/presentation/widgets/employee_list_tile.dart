import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../data/models/employee_model.dart';
import 'employee_status_badge.dart';

class EmployeeListTile extends StatelessWidget {
  final EmployeeModel employee;
  final VoidCallback onTap;
  final VoidCallback? onDelete;

  const EmployeeListTile({
    super.key,
    required this.employee,
    required this.onTap,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor: Theme.of(context).colorScheme.primary.withAlpha(26),
        foregroundColor: Theme.of(context).colorScheme.primary,
        backgroundImage: employee.photoUrl != null
            ? CachedNetworkImageProvider(employee.photoUrl!)
            : null,
        child: employee.photoUrl == null ? Text(employee.initials) : null,
      ),
      title: Text(
        employee.fullName,
        style: const TextStyle(fontWeight: FontWeight.w500),
      ),
      subtitle: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (employee.department != null) ...[
            Text(employee.department!),
            const SizedBox(width: 8),
          ],
          EmployeeStatusBadge(status: employee.status),
        ],
      ),
      trailing: PopupMenuButton<String>(
        onSelected: (value) {
          if (value == 'view') {
            onTap();
          } else if (value == 'delete' && onDelete != null) {
            onDelete!();
          }
        },
        itemBuilder: (context) => [
          const PopupMenuItem(
            value: 'view',
            child: Row(
              children: [
                Icon(Icons.visibility_outlined, size: 20),
                SizedBox(width: 8),
                Text('View'),
              ],
            ),
          ),
          if (onDelete != null)
            PopupMenuItem(
              value: 'delete',
              child: Row(
                children: [
                  Icon(
                    Icons.delete_outline,
                    size: 20,
                    color: Theme.of(context).colorScheme.error,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Delete',
                    style:
                        TextStyle(color: Theme.of(context).colorScheme.error),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
