import 'package:flutter/material.dart';
import '../../../../core/widgets/responsive_layout.dart';
import '../../../../core/widgets/stat_card.dart';
import '../../data/models/admin_dashboard_response.dart';

class AdminKpiRow extends StatelessWidget {
  final AdminDashboardResponse data;

  const AdminKpiRow({super.key, required this.data});

  @override
  Widget build(BuildContext context) {
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
            children: _buildCards(context),
          );
        } else if (constraints.maxWidth >= AppBreakpoints.mobile) {
          return GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: 2.2,
            children: _buildCards(context),
          );
        } else {
          return SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: _buildCards(context)
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

  List<Widget> _buildCards(BuildContext context) {
    return [
      StatCard(
        title: 'Total Tasks',
        value: (data.totalTasks ?? 0).toString(),
        icon: Icons.list_alt,
        color: const Color(0xFF6366F1), // Indigo
      ),
      StatCard(
        title: 'Active',
        value: (data.activeTasks ?? 0).toString(),
        icon: Icons.play_arrow,
        color: const Color(0xFF3B82F6), // Blue
      ),
      StatCard(
        title: 'Overdue',
        value: (data.overdueTasks ?? 0).toString(),
        icon: Icons.warning_amber_rounded,
        color: const Color(0xFFEF4444), // Red
      ),
      StatCard(
        title: 'Completed',
        value: (data.completedTasks ?? 0).toString(),
        icon: Icons.check_circle_outline,
        color: const Color(0xFF22C55E), // Green
      ),
      StatCard(
        title: 'Completion Rate',
        value: '${(data.completionRate ?? 0).toStringAsFixed(1)}%',
        icon: Icons.trending_up,
        color: const Color(0xFFF97316), // Orange
      ),
      StatCard(
        title: 'Total Employees',
        value: (data.totalEmployees ?? 0).toString(),
        icon: Icons.people_outline,
        color: const Color(0xFF06B6D4), // Cyan
      ),
      if (data.avgCompletionHours != null)
        StatCard(
          title: 'Avg Completion',
          value: '${(data.avgCompletionHours ?? 0).toStringAsFixed(1)} h',
          icon: Icons.access_time,
          color: const Color(0xFFA855F7), // Purple
        ),
    ];
  }
}
