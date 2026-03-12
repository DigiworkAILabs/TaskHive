import 'package:flutter/material.dart';
import '../../../../core/widgets/responsive_layout.dart';
import '../../../../core/widgets/stat_card.dart';
import '../../data/models/employee_dashboard_response.dart';

class EmployeeKpiRow extends StatelessWidget {
  final EmployeeDashboardResponse data;

  const EmployeeKpiRow({super.key, required this.data});

  @override
  Widget build(BuildContext context) {
    final active = (data.todoTasks ?? 0) +
        (data.inProgressTasks ?? 0) +
        (data.inReviewTasks ?? 0);

    return LayoutBuilder(
      builder: (context, constraints) {
        if (constraints.maxWidth >= AppBreakpoints.desktop) {
          return GridView.count(
            crossAxisCount: 3,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 2.2,
            children: _buildCards(context, active),
          );
        } else if (constraints.maxWidth >= AppBreakpoints.mobile) {
          return GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 2.2,
            children: _buildCards(context, active),
          );
        } else {
          return SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: _buildCards(context, active)
                  .map((card) => Padding(
                        padding: const EdgeInsets.only(right: 12.0),
                        child: card,
                      ))
                  .toList(),
            ),
          );
        }
      },
    );
  }

  List<Widget> _buildCards(BuildContext context, int active) {
    return [
      StatCard(
        title: 'Total Tasks',
        value: (data.totalTasks ?? 0).toString(),
        icon: Icons.list_alt,
        color: const Color(0xFF6366F1), // Indigo
      ),
      StatCard(
        title: 'Active',
        value: active.toString(),
        icon: Icons.play_arrow,
        color: const Color(0xFF3B82F6), // Blue
      ),
      StatCard(
        title: 'Completed',
        value: (data.completedTasks ?? 0).toString(),
        icon: Icons.check_circle_outline,
        color: const Color(0xFF22C55E), // Green
      ),
      StatCard(
        title: 'On-Time Rate',
        value: '${(data.onTimeCompletionRate ?? 0).toStringAsFixed(1)}%',
        icon: Icons.trending_up,
        color: const Color(0xFFF97316), // Orange
      ),
      StatCard(
        title: 'Avg Hours',
        value: '${(data.avgCompletionHours ?? 0).toStringAsFixed(1)} h',
        icon: Icons.access_time,
        color: const Color(0xFFA855F7), // Purple
      ),
    ];
  }
}
