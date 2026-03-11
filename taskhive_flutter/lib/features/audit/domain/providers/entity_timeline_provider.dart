import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/repositories/audit_repository.dart';
import '../../data/models/audit_log_model.dart';
import '../../../../core/utils/logger.dart';

part 'entity_timeline_provider.g.dart';

@riverpod
Future<List<AuditLogModel>> entityTimeline(
  EntityTimelineRef ref,
  String entityType,
  String entityId,
) async {
  try {
    // // TODO: full pagination for large entity histories
    final response = await ref.read(auditRepositoryProvider).getEntityTimeline(
          entityType,
          entityId,
          page: 0,
          size: 100, // Fetch up to 100 items on a single page
        );

    final List<dynamic> content = response['content'] ?? [];
    final List<AuditLogModel> logs = content
        .map((e) => AuditLogModel.fromJson(e as Map<String, dynamic>))
        .toList();

    // Sort oldest-first based on createdAt
    logs.sort((a, b) {
      if (a.createdAt == null && b.createdAt == null) return 0;
      if (a.createdAt == null) return 1;
      if (b.createdAt == null) return -1;
      return a.createdAt!.compareTo(b.createdAt!);
    });

    return logs;
  } catch (e) {
    appLogger.e('Error fetching entity timeline for $entityType $entityId: $e');
    rethrow;
  }
}
