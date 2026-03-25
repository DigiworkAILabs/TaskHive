import 'package:flutter/material.dart';
import '../../domain/enums/task_status.dart';

class TaskStatusTrail extends StatelessWidget {
  final TaskStatus status;

  const TaskStatusTrail({super.key, required this.status});

  static const List<({TaskStatus status, String label, int blocks})> _stages = [
    (status: TaskStatus.todo, label: 'To Do', blocks: 2),
    (status: TaskStatus.inProgress, label: 'In Progress', blocks: 3),
    (status: TaskStatus.inReview, label: 'In Review', blocks: 3),
    (status: TaskStatus.pendingApproval, label: 'Pending Approval', blocks: 3),
    (status: TaskStatus.done, label: 'Done', blocks: 3),
  ];

  @override
  Widget build(BuildContext context) {
    if (status == TaskStatus.cancelled) {
      return _buildCancelled(context);
    }

    final currentIndex = _stages.indexWhere((s) => s.status == status);
    final fillColor = status.color;
    final glowColor = fillColor.withValues(alpha: 0.4);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'PROGRESS',
              style: TextStyle(
                fontSize: 9,
                letterSpacing: 2,
                fontWeight: FontWeight.bold,
                color: Colors.grey.shade700,
                fontFamily: 'monospace',
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            // Left nub
            Container(
              width: 6,
              height: 14,
              color: fillColor,
            ),
            // Outer casing
            Expanded(
              child: Container(
                height: 24,
                decoration: BoxDecoration(
                  color: const Color(0xFF0A0A0A),
                  border: Border.all(color: const Color(0xFF3F3F46), width: 2),
                ),
                padding: const EdgeInsets.all(3),
                child: Stack(
                  children: [
                    // Scanline overlay
                    Positioned.fill(
                      child: CustomPaint(
                        painter: _ScanlinePainter(),
                      ),
                    ),
                    // Blocks
                    Row(
                      children: _stages.asMap().entries.map((entry) {
                        final idx = entry.key;
                        final stage = entry.value;
                        final isFuture = idx > currentIndex;

                        return Expanded(
                          flex: stage.blocks,
                          child: Row(
                            children: [
                              if (idx > 0)
                                Container(
                                  width: 2,
                                  height: double.infinity,
                                  color: const Color(0xFF1A1A1A),
                                ),
                              Expanded(
                                child: Row(
                                  children: List.generate(
                                    stage.blocks,
                                    (blockIdx) => Expanded(
                                      child: Padding(
                                        padding: const EdgeInsets.symmetric(
                                            horizontal: 1),
                                        child: Container(
                                          height: 14,
                                          decoration: BoxDecoration(
                                            color: isFuture
                                                ? Colors.transparent
                                                : fillColor,
                                            border: Border.all(
                                              color: isFuture
                                                  ? const Color(0xFF1F1F1F)
                                                  : fillColor,
                                              width: 1,
                                            ),
                                            boxShadow: !isFuture
                                                ? [
                                                    BoxShadow(
                                                      color: glowColor,
                                                      blurRadius: 6,
                                                      spreadRadius: 1,
                                                    )
                                                  ]
                                                : null,
                                          ),
                                          child: !isFuture
                                              ? Align(
                                                  alignment: Alignment.topCenter,
                                                  child: Container(
                                                    height: 2,
                                                    color: Colors.white
                                                        .withValues(alpha: 0.15),
                                                  ),
                                                )
                                              : null,
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ),
            ),
            // Right nub
            Container(
              width: 4,
              height: 10,
              decoration: const BoxDecoration(
                color: Color(0xFF1F1F1F),
                border: Border(
                  top: BorderSide(color: Color(0xFF2A2A2A)),
                  right: BorderSide(color: Color(0xFF2A2A2A)),
                  bottom: BorderSide(color: Color(0xFF2A2A2A)),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 5),
        Row(
          children: _stages.asMap().entries.map((entry) {
            final idx = entry.key;
            final stage = entry.value;
            final isCompleted = idx < currentIndex;
            final isCurrent = idx == currentIndex;

            return Expanded(
              flex: stage.blocks,
              child: Text(
                stage.label.toUpperCase(),
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 8,
                  fontFamily: 'monospace',
                  fontWeight: isCurrent ? FontWeight.bold : FontWeight.normal,
                  color: isCurrent
                      ? fillColor
                      : (isCompleted
                          ? Colors.grey.shade700
                          : const Color(0xFF1F1F1F)),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildCancelled(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'PROGRESS',
              style: TextStyle(
                fontSize: 9,
                letterSpacing: 2,
                fontWeight: FontWeight.bold,
                color: Colors.grey.shade700,
                fontFamily: 'monospace',
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              decoration: BoxDecoration(
                color: Colors.red.withValues(alpha: 0.08),
                border: Border.all(color: Colors.red.withValues(alpha: 0.4)),
              ),
              child: const Text(
                '✕ CANCELLED',
                style: TextStyle(
                  fontSize: 9,
                  letterSpacing: 2,
                  fontWeight: FontWeight.bold,
                  color: Colors.red,
                  fontFamily: 'monospace',
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        // Simplistic greyed out bar for cancelled
        Container(
          height: 24,
          margin: const EdgeInsets.only(left: 6),
          decoration: BoxDecoration(
            color: const Color(0xFF0A0A0A),
            border: Border.all(color: const Color(0xFF2A2A2A), width: 2),
          ),
          child: Center(
            child: Text(
              'TASK TERMINATED',
              style: TextStyle(
                fontSize: 8,
                color: Colors.grey.shade800,
                letterSpacing: 4,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _ScanlinePainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.black.withValues(alpha: 0.22)
      ..strokeWidth = 1;

    for (double i = 0; i < size.height; i += 2) {
      canvas.drawLine(Offset(0, i), Offset(size.width, i), paint);
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
