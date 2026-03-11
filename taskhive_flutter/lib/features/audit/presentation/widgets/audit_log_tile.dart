import 'package:flutter/material.dart';
import '../../data/models/audit_log_model.dart';

class AuditLogTile extends StatelessWidget {
  final AuditLogModel log;
  final VoidCallback? onTap;

  const AuditLogTile({
    super.key,
    required this.log,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return ListTile(
      onTap: onTap,
      leading: CircleAvatar(
        backgroundColor:
            theme.colorScheme.primaryContainer.withValues(alpha: 0.4),
        child: Icon(
          log.actionIcon,
          color: theme.colorScheme.primary,
        ),
      ),
      title: Row(
        children: [
          Expanded(
            child: Text(
              log.action ?? 'Unknown Action',
              style: const TextStyle(fontWeight: FontWeight.bold),
            ),
          ),
          if (log.entityType != null)
            Padding(
              padding: const EdgeInsets.only(left: 8.0),
              child: Text(
                log.entityType!,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.outline,
                ),
              ),
            ),
        ],
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(log.actorEmail ?? '—'),
          if (log.createdAt != null)
            Text(
              log.createdAt!,
              style: theme.textTheme.bodySmall,
            ),
        ],
      ),
      trailing: log.hasStateDiff
          ? Icon(
              Icons.compare_arrows,
              size: 20,
              color: theme.colorScheme.secondary,
            )
          : null,
    );
  }
}
