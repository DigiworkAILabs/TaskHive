import 'package:intl/intl.dart';

class AppDateUtils {
  AppDateUtils._();

  /// Formats a DateTime as "Mar 5, 2026"
  static String formatDate(DateTime date) {
    return DateFormat.yMMMd().format(date);
  }

  /// Formats a DateTime as "Mar 5, 2026 10:30 AM"
  static String formatDateTime(DateTime date) {
    return DateFormat.yMMMd().add_jm().format(date);
  }

  /// Returns a human-readable time-ago string
  static String timeAgo(DateTime dateTime) {
    final now = DateTime.now();
    final diff = now.difference(dateTime);

    if (diff.inSeconds < 60) return 'just now';
    if (diff.inMinutes < 60) {
      return '${diff.inMinutes} minute${diff.inMinutes == 1 ? '' : 's'} ago';
    }
    if (diff.inHours < 24) {
      return '${diff.inHours} hour${diff.inHours == 1 ? '' : 's'} ago';
    }
    final days = diff.inDays;
    return '$days day${days == 1 ? '' : 's'} ago';
  }

  /// Returns true if the given due date is in the past
  static bool isOverdue(DateTime due) {
    return due.isBefore(DateTime.now());
  }
}
