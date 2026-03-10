import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/providers/unread_count_provider.dart';

class NotificationBadge extends ConsumerWidget {
  final Widget child;

  const NotificationBadge({
    Key? key,
    required this.child,
  }) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final count = ref.watch(unreadCountProvider).valueOrNull ?? 0;

    return Badge(
      isLabelVisible: count > 0,
      label: Text(count > 99 ? '99+' : count.toString()),
      backgroundColor: Colors.red,
      textColor: Colors.white,
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: child,
    );
  }
}
