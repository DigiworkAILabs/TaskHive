import 'package:flutter/material.dart';

import '../../data/models/notification_model.dart';

class NotificationListTile extends StatelessWidget {
  final NotificationModel notification;
  final VoidCallback onTap;

  const NotificationListTile({
    Key? key,
    required this.notification,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isUnread = notification.isUnread;

    return ListTile(
      onTap: onTap,
      tileColor: isUnread ? theme.colorScheme.primary.withOpacity(0.05) : null,
      leading: CircleAvatar(
        backgroundColor: theme.colorScheme.primaryContainer,
        child: Icon(
          notification.icon,
          color: theme.colorScheme.primary,
        ),
      ),
      title: Text(
        notification.title ?? '',
        style: TextStyle(
          fontWeight: isUnread ? FontWeight.bold : FontWeight.normal,
        ),
      ),
      subtitle: Text(
        notification.message ?? '',
        maxLines: 2,
        overflow: TextOverflow.ellipsis,
      ),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (notification.createdAt != null)
            Text(
              // Use built-in or assuming Phase 1 has AppDateUtils
              _formatDate(notification.createdAt!),
              style: theme.textTheme.bodySmall,
            ),
          if (isUnread) ...[
            const SizedBox(height: 4),
            CircleAvatar(
              radius: 4,
              backgroundColor: theme.colorScheme.primary,
            )
          ]
        ],
      ),
    );
  }

  String _formatDate(String isoDate) {
    try {
      final date = DateTime.parse(isoDate);
      final difference = DateTime.now().difference(date);

      if (difference.inDays > 7) {
        return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
      } else if (difference.inDays > 0) {
        return '${difference.inDays}d ago';
      } else if (difference.inHours > 0) {
        return '${difference.inHours}h ago';
      } else if (difference.inMinutes > 0) {
        return '${difference.inMinutes}m ago';
      } else {
        return 'Just now';
      }
    } catch (_) {
      return '';
    }
  }
}
