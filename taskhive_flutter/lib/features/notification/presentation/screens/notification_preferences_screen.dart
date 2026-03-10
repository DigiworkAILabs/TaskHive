import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/providers/notification_preference_provider.dart';
import '../widgets/notification_preference_tile.dart';

class NotificationPreferencesScreen extends ConsumerWidget {
  const NotificationPreferencesScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsAsync = ref.watch(notificationPreferenceNotifierProvider);

    return Scaffold(
        appBar: AppBar(
          title: const Text('Notification Preferences'),
        ),
        body: prefsAsync.when(
          data: (prefs) {
            return ListView(
              children: [
                const Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Text('Delivery Channels',
                      style: TextStyle(fontWeight: FontWeight.bold)),
                ),
                NotificationPreferenceTile(
                  title: 'In-App Notifications',
                  value: prefs.inAppEnabled,
                  onChanged: (val) {
                    ref
                        .read(notificationPreferenceNotifierProvider.notifier)
                        .updatePreferences(prefs.copyWith(inAppEnabled: val));
                  },
                ),
                NotificationPreferenceTile(
                  title: 'Email Notifications',
                  value: prefs.emailEnabled,
                  onChanged: (val) {
                    ref
                        .read(notificationPreferenceNotifierProvider.notifier)
                        .updatePreferences(prefs.copyWith(emailEnabled: val));
                  },
                ),
                const Divider(),
                const Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Text('Alert Types',
                      style: TextStyle(fontWeight: FontWeight.bold)),
                ),
                NotificationPreferenceTile(
                  title: 'Task Assigned',
                  value: prefs.taskAssigned,
                  onChanged: (val) {
                    ref
                        .read(notificationPreferenceNotifierProvider.notifier)
                        .updatePreferences(prefs.copyWith(taskAssigned: val));
                  },
                ),
                NotificationPreferenceTile(
                  title: 'Task Overdue',
                  value: prefs.taskOverdue,
                  onChanged: (val) {
                    ref
                        .read(notificationPreferenceNotifierProvider.notifier)
                        .updatePreferences(prefs.copyWith(taskOverdue: val));
                  },
                ),
                const Divider(),
                const Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Text('Daily Digest',
                      style: TextStyle(fontWeight: FontWeight.bold)),
                ),
                NotificationPreferenceTile(
                  title: 'Enable Daily Digest',
                  value: prefs.dailyDigest,
                  onChanged: (val) {
                    ref
                        .read(notificationPreferenceNotifierProvider.notifier)
                        .updatePreferences(prefs.copyWith(dailyDigest: val));
                  },
                ),
                if (prefs.dailyDigest)
                  ListTile(
                    title: const Text('Digest Time'),
                    trailing: Text(prefs.digestTime ?? '08:00'),
                    onTap: () async {
                      final parts = (prefs.digestTime ?? '08:00').split(':');
                      final initialTime = TimeOfDay(
                          hour: int.tryParse(parts[0]) ?? 8,
                          minute:
                              int.tryParse(parts.length > 1 ? parts[1] : '0') ??
                                  0);

                      final selectedTime = await showTimePicker(
                        context: context,
                        initialTime: initialTime,
                      );

                      if (selectedTime != null && context.mounted) {
                        final formattedTime =
                            '${selectedTime.hour.toString().padLeft(2, '0')}:${selectedTime.minute.toString().padLeft(2, '0')}';
                        ref
                            .read(
                                notificationPreferenceNotifierProvider.notifier)
                            .updatePreferences(
                                prefs.copyWith(digestTime: formattedTime));
                      }
                    },
                  ),
              ],
            );
          },
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (err, st) =>
              const Center(child: Text('Failed to load preferences')),
        ));
  }
}
