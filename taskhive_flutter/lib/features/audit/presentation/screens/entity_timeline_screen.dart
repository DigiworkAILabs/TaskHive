import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/providers/entity_timeline_provider.dart';
import '../widgets/entity_timeline_tile.dart';

class EntityTimelineScreen extends ConsumerWidget {
  final String entityType;
  final String entityId;

  const EntityTimelineScreen({
    super.key,
    required this.entityType,
    required this.entityId,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(entityTimelineProvider(entityType, entityId));

    return Scaffold(
      appBar: AppBar(
        title: Text('$entityType Timeline'),
      ),
      body: state.when(
        data: (logs) {
          if (logs.isEmpty) {
            return const Center(
              child: Text('No history found for this entity.'),
            );
          }

          // Logs are sorted oldest-first by the provider
          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: logs.length,
            itemBuilder: (context, index) {
              return EntityTimelineTile(log: logs[index]);
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
      ),
    );
  }
}
