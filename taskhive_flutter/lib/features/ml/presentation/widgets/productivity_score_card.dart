import 'package:flutter/material.dart';
import '../../data/models/productivity_score_response.dart';

class ProductivityScoreCard extends StatelessWidget {
  final ProductivityScoreResponse scoreData;

  const ProductivityScoreCard({
    super.key,
    required this.scoreData,
  });

  Color _getGradeColor(String grade) {
    switch (grade.toUpperCase()) {
      case 'A':
        return const Color(0xFF10B981); // Emerald-ish
      case 'B':
        return Colors.blue;
      case 'C':
        return Colors.amber;
      case 'D':
        return Colors.orange;
      default:
        return Colors.red;
    }
  }

  IconData _getTrendIcon(String trend) {
    switch (trend.toLowerCase()) {
      case 'improving':
        return Icons.trending_up;
      case 'declining':
        return Icons.trending_down;
      default:
        return Icons.remove;
    }
  }

  Color _getTrendColor(String trend) {
    switch (trend.toLowerCase()) {
      case 'improving':
        return const Color(0xFF10B981);
      case 'declining':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    final gradeColor = _getGradeColor(scoreData.grade);
    final trendColor = _getTrendColor(scoreData.trend);

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: Theme.of(context).dividerColor.withOpacity(0.1)),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'PRODUCTIVITY SCORE',
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(
                            letterSpacing: 1.2,
                            fontWeight: FontWeight.bold,
                            color: Colors.grey,
                          ),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Text(
                          scoreData.score.toStringAsFixed(1),
                          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                        ),
                        const SizedBox(width: 12),
                        Container(
                          padding:
                              const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: gradeColor.withOpacity(0.1),
                            borderRadius: BorderRadius.circular(6),
                            border: Border.all(color: gradeColor.withOpacity(0.2)),
                          ),
                          child: Text(
                            'Grade ${scoreData.grade}',
                            style: TextStyle(
                              color: gradeColor,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.grey.withOpacity(0.05),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        scoreData.trend.toUpperCase(),
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: Colors.grey.shade600,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Icon(
                        _getTrendIcon(scoreData.trend),
                        size: 14,
                        color: trendColor,
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            // Metrics Grid
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              mainAxisSpacing: 16,
              crossAxisSpacing: 24,
              childAspectRatio: 3,
              children: [
                _buildMetricItem(
                  context,
                  'Task Completion',
                  scoreData.breakdown['completion_rate_score'] ?? 0,
                  35,
                  Icons.check_circle_outline,
                  const Color(0xFF10B981),
                ),
                _buildMetricItem(
                  context,
                  'On-Time Rate',
                  scoreData.breakdown['on_time_score'] ?? 0,
                  30,
                  Icons.access_time,
                  Colors.blue,
                ),
                _buildMetricItem(
                  context,
                  'Overdue Impact',
                  scoreData.breakdown['overdue_penalty'] ?? 0,
                  0,
                  Icons.error_outline,
                  Colors.red,
                ),
                _buildMetricItem(
                  context,
                  'Engagement',
                  scoreData.breakdown['engagement_score'] ?? 0,
                  15,
                  Icons.chat_bubble_outline,
                  Colors.indigo,
                ),
              ],
            ),
            const SizedBox(height: 24),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.withOpacity(0.03),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.blue.withOpacity(0.05)),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.only(top: 4),
                    child: CircleAvatar(
                      radius: 3,
                      backgroundColor: Colors.blue.shade400,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          scoreData.reasoning,
                          style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                color: Colors.grey.shade700,
                                height: 1.5,
                              ),
                        ),
                        if (scoreData.fallbackUsed) ...[
                          const SizedBox(height: 8),
                          const Text(
                            '* Basic calculation used as ML model is initializing',
                            style: TextStyle(
                              color: Colors.grey,
                              fontSize: 10,
                              fontStyle: FontStyle.italic,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            InkWell(
              onTap: () {},
              child: Padding(
                padding: const EdgeInsets.symmetric(vertical: 8),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      'View detailed analytics',
                      style: TextStyle(
                        color: Theme.of(context).primaryColor,
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(width: 4),
                    Icon(
                      Icons.chevron_right,
                      size: 16,
                      color: Theme.of(context).primaryColor,
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetricItem(
    BuildContext context,
    String label,
    double value,
    double max,
    IconData icon,
    Color color,
  ) {
    final isPenalty = max <= 0;
    final double percentage = isPenalty
        ? (value.abs() * 5).clamp(0, 100).toDouble()
        : (value / max).clamp(0, 1).toDouble();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Icon(icon, size: 12, color: color),
                const SizedBox(width: 6),
                Text(
                  label.toUpperCase(),
                  style: const TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.w600,
                    color: Colors.grey,
                  ),
                ),
              ],
            ),
            Text(
              value > 0 ? '+$value' : value.toString(),
              style: const TextStyle(
                fontSize: 10,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: isPenalty ? percentage / 100 : percentage,
            backgroundColor: Colors.grey.withOpacity(0.1),
            valueColor: AlwaysStoppedAnimation<Color>(color.withOpacity(0.7)),
            minHeight: 6,
          ),
        ),
      ],
    );
  }
}
