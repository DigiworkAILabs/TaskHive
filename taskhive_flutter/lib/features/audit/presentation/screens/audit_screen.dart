import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../data/models/audit_log_model.dart';
import '../../data/repositories/audit_repository.dart';
import '../../domain/providers/audit_log_list_provider.dart';
import '../../domain/providers/audit_log_search_provider.dart';
import '../../domain/providers/security_events_provider.dart';
import '../widgets/audit_filter_bar.dart';
import '../widgets/audit_log_tile.dart';
import '../widgets/compliance_report_button.dart';
import '../widgets/json_diff_viewer.dart';
import '../widgets/security_event_tile.dart';

class AuditScreen extends ConsumerStatefulWidget {
  const AuditScreen({super.key});

  @override
  ConsumerState<AuditScreen> createState() => _AuditScreenState();
}

class _AuditScreenState extends ConsumerState<AuditScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  AuditSearchFilter _currentFilter = AuditSearchFilter();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _onFilterChanged(AuditSearchFilter filter) {
    setState(() => _currentFilter = filter);
    if (filter.isEmpty) {
      ref.read(auditLogListNotifierProvider.notifier).refresh();
    } else {
      ref.read(auditLogSearchNotifierProvider.notifier).search(filter);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Audit logs'),
        actions: const [
          ComplianceReportButton(),
          SizedBox(width: 8),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Activity Logs'),
            Tab(text: 'Security Events'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _AuditLogsTab(
            filter: _currentFilter,
            onFilterChanged: _onFilterChanged,
          ),
          const _SecurityEventsTab(),
        ],
      ),
    );
  }
}

class _AuditLogsTab extends ConsumerStatefulWidget {
  final AuditSearchFilter filter;
  final ValueChanged<AuditSearchFilter> onFilterChanged;

  const _AuditLogsTab({
    required this.filter,
    required this.onFilterChanged,
  });

  @override
  ConsumerState<_AuditLogsTab> createState() => _AuditLogsTabState();
}

class _AuditLogsTabState extends ConsumerState<_AuditLogsTab> {
  late ScrollController _scrollController;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (!_scrollController.hasClients) return;

    final bool isSearching = !widget.filter.isEmpty;
    final state = ref.read(
      isSearching
          ? auditLogSearchNotifierProvider
          : auditLogListNotifierProvider,
    );

    state.whenData((data) {
      if (!data.isLoadingMore &&
          _scrollController.position.pixels >=
              _scrollController.position.maxScrollExtent - 200) {
        if (isSearching) {
          ref.read(auditLogSearchNotifierProvider.notifier).loadMore();
        } else {
          ref.read(auditLogListNotifierProvider.notifier).loadMore();
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final bool isSearching = !widget.filter.isEmpty;
    final state = ref.watch(
      isSearching
          ? auditLogSearchNotifierProvider
          : auditLogListNotifierProvider,
    );

    return state.when(
      data: (data) {
        return RefreshIndicator(
          onRefresh: () async {
            if (isSearching) {
              await ref
                  .read(auditLogSearchNotifierProvider.notifier)
                  .search(widget.filter);
            } else {
              await ref.read(auditLogListNotifierProvider.notifier).refresh();
            }
          },
          child: CustomScrollView(
            controller: _scrollController,
            physics: const AlwaysScrollableScrollPhysics(),
            slivers: [
              SliverToBoxAdapter(
                child: AuditFilterBar(
                  filter: widget.filter,
                  onFilterChanged: widget.onFilterChanged,
                ),
              ),
              if (data.logs.isEmpty)
                const SliverFillRemaining(
                  child: Center(child: Text('No audit logs found.')),
                )
              else ...[
                SliverList(
                  delegate: SliverChildBuilderDelegate(
                    (context, index) {
                      final log = data.logs[index];
                      return AuditLogTile(
                        log: log,
                        onTap: () => _showAuditLogDetails(context, log),
                      );
                    },
                    childCount: data.logs.length,
                  ),
                ),
                if (data.isLoadingMore)
                  const SliverToBoxAdapter(
                    child: Padding(
                      padding: EdgeInsets.all(16.0),
                      child: Center(child: CircularProgressIndicator()),
                    ),
                  ),
              ],
            ],
          ),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }

  void _showAuditLogDetails(BuildContext context, AuditLogModel log) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      builder: (context) => _AuditLogDetailSheet(log: log),
    );
  }
}

class _AuditLogDetailSheet extends StatelessWidget {
  final AuditLogModel log;

  const _AuditLogDetailSheet({required this.log});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return DraggableScrollableSheet(
      initialChildSize: 0.6,
      minChildSize: 0.4,
      maxChildSize: 0.95,
      expand: false,
      builder: (context, scrollController) {
        return SingleChildScrollView(
          controller: scrollController,
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Log Details', style: theme.textTheme.headlineSmall),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
              const Divider(),
              _detailRow('Action', log.action ?? '—'),
              _detailRow('Entity Type', log.entityType ?? '—'),
              _detailRow('Entity ID', log.entityId ?? '—'),
              _detailRow('Actor', log.actorEmail ?? '—'),
              _detailRow('IP Address', log.ipAddress ?? '—'),
              _detailRow('User Agent', log.userAgent ?? '—'),
              _detailRow('Timestamp', log.createdAt ?? '—'),
              if (log.hasStateDiff) ...[
                const SizedBox(height: 20),
                Text('Changes', style: theme.textTheme.titleMedium),
                const SizedBox(height: 8),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.surfaceContainerHighest
                        .withValues(alpha: 0.3),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: theme.colorScheme.outlineVariant),
                  ),
                  child: JsonDiffViewer(
                    before: log.beforeState,
                    after: log.afterState,
                  ),
                ),
              ],
              if (log.entityType != null && log.entityId != null) ...[
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: () {
                      Navigator.pop(context);
                      context.push(
                        '/admin/audit/entity/${log.entityType}/${log.entityId}',
                      );
                    },
                    icon: const Icon(Icons.history),
                    label: const Text('View Entity Timeline'),
                  ),
                ),
              ],
            ],
          ),
        );
      },
    );
  }

  Widget _detailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              label,
              style: const TextStyle(
                  fontWeight: FontWeight.bold, color: Colors.grey),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}

class _SecurityEventsTab extends ConsumerStatefulWidget {
  const _SecurityEventsTab();

  @override
  ConsumerState<_SecurityEventsTab> createState() => _SecurityEventsTabState();
}

class _SecurityEventsTabState extends ConsumerState<_SecurityEventsTab> {
  late ScrollController _scrollController;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (!_scrollController.hasClients) return;

    final state = ref.read(securityEventsNotifierProvider);
    state.whenData((data) {
      if (!data.isLoadingMore &&
          _scrollController.position.pixels >=
              _scrollController.position.maxScrollExtent - 200) {
        ref.read(securityEventsNotifierProvider.notifier).loadMore();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(securityEventsNotifierProvider);

    return state.when(
      data: (data) {
        return RefreshIndicator(
          onRefresh: () =>
              ref.read(securityEventsNotifierProvider.notifier).refresh(),
          child: CustomScrollView(
            controller: _scrollController,
            physics: const AlwaysScrollableScrollPhysics(),
            slivers: [
              if (data.events.isEmpty)
                const SliverFillRemaining(
                  child: Center(child: Text('No security events found.')),
                )
              else ...[
                SliverList(
                  delegate: SliverChildBuilderDelegate(
                    (context, index) =>
                        SecurityEventTile(event: data.events[index]),
                    childCount: data.events.length,
                  ),
                ),
                if (data.isLoadingMore)
                  const SliverToBoxAdapter(
                    child: Padding(
                      padding: EdgeInsets.all(16.0),
                      child: Center(child: CircularProgressIndicator()),
                    ),
                  ),
              ],
            ],
          ),
        );
      },
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Error: $e')),
    );
  }
}
