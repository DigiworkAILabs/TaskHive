import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../domain/enums/task_status.dart';
import '../../domain/providers/task_actions_provider.dart';
import '../../data/models/task_model.dart';

class TaskApprovalPanel extends ConsumerStatefulWidget {
  final TaskModel task;
  final VoidCallback onActionSuccess;

  const TaskApprovalPanel({
    super.key,
    required this.task,
    required this.onActionSuccess,
  });

  @override
  ConsumerState<TaskApprovalPanel> createState() => _TaskApprovalPanelState();
}

class _TaskApprovalPanelState extends ConsumerState<TaskApprovalPanel> {
  final _reasonController = TextEditingController();
  bool _showRejectForm = false;
  bool _isProcessing = false;

  @override
  void dispose() {
    _reasonController.dispose();
    super.dispose();
  }

  Future<void> _handleApprove() async {
    setState(() => _isProcessing = true);
    try {
      await ref.read(taskActionsProvider.notifier).approveTask(widget.task.id);
      widget.onActionSuccess();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Approval failed: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  Future<void> _handleReject() async {
    if (_reasonController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please provide a reason for revision.')),
      );
      return;
    }

    setState(() => _isProcessing = true);
    try {
      await ref
          .read(taskActionsProvider.notifier)
          .rejectTask(widget.task.id, _reasonController.text.trim());
      widget.onActionSuccess();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Rejection failed: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (widget.task.status != TaskStatus.pendingApproval) {
      return const SizedBox.shrink();
    }

    return Container(
      margin: const EdgeInsets.only(top: 24),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.orange.withValues(alpha: 0.05),
        border: Border.all(color: Colors.orange.withValues(alpha: 0.2)),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(
            children: [
              Icon(Icons.access_time, size: 16, color: Colors.orange),
              SizedBox(width: 8),
              Text(
                'Awaiting Your Approval',
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                  color: Colors.orange,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          if (!_showRejectForm)
            Row(
              children: [
                FilledButton.icon(
                  onPressed: _isProcessing ? null : _handleApprove,
                  icon: const Icon(Icons.check_circle_outline, size: 16),
                  label: const Text('Approve'),
                  style: FilledButton.styleFrom(
                    backgroundColor: Colors.green,
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                  ),
                ),
                const SizedBox(width: 12),
                OutlinedButton.icon(
                  onPressed: () => setState(() => _showRejectForm = true),
                  icon: const Icon(Icons.cancel_outlined, size: 16),
                  label: const Text('Request Revision'),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.red,
                    side: const BorderSide(color: Colors.red),
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                  ),
                ),
              ],
            )
          else
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                TextField(
                  controller: _reasonController,
                  maxLines: 3,
                  decoration: const InputDecoration(
                    hintText: 'Describe what needs to be revised...',
                    border: OutlineInputBorder(),
                    contentPadding: EdgeInsets.all(12),
                  ),
                  style: const TextStyle(fontSize: 13),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    FilledButton(
                      onPressed: _isProcessing ? null : _handleReject,
                      style: FilledButton.styleFrom(backgroundColor: Colors.red),
                      child: Text(_isProcessing ? 'Sending...' : 'Send Revision Request'),
                    ),
                    const SizedBox(width: 8),
                    TextButton(
                      onPressed: () => setState(() {
                        _showRejectForm = false;
                        _reasonController.clear();
                      }),
                      child: const Text('Cancel'),
                    ),
                  ],
                ),
              ],
            ),
        ],
      ),
    );
  }
}
