import 'package:flutter/material.dart';
import '../../data/models/security_event_model.dart';

class SecurityEventTile extends StatelessWidget {
  final SecurityEventModel event;

  const SecurityEventTile({
    super.key,
    required this.event,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: event.severityColor.withValues(alpha: 0.1),
        child: Icon(
          event.severityIcon,
          color: event.severityColor,
        ),
      ),
      title: Text(
        event.eventType ?? 'Unknown Event',
        style: const TextStyle(fontWeight: FontWeight.bold),
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('IP: ${event.ipAddress ?? '—'}'),
          if (event.createdAt != null)
            Text(
              event.createdAt!,
              style: theme.textTheme.bodySmall,
            ),
        ],
      ),
      trailing: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color: (event.success == true ? Colors.green : Colors.red)
              .withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(4),
          border: Border.all(
            color: (event.success == true ? Colors.green : Colors.red)
                .withValues(alpha: 0.5),
          ),
        ),
        child: Text(
          event.success == true ? 'SUCCESS' : 'FAILED',
          style: TextStyle(
            color: event.success == true ? Colors.green : Colors.red,
            fontSize: 10,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
