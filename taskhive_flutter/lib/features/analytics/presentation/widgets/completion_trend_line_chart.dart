import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../data/models/task_completion_trend_response.dart';

class CompletionTrendLineChart extends StatelessWidget {
  final AsyncValue<List<TaskCompletionTrendResponse>> dataState;
  final double height;

  const CompletionTrendLineChart({
    super.key,
    required this.dataState,
    this.height = 250,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Text(
                '30-Day Completion Trend',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            const Text(
              'Tasks completed over the last 30 days',
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: height,
              child: dataState.when(
                loading: () => const AppLoading(),
                error: (e, _) => AppErrorWidget(message: e.toString()),
                data: (list) {
                  if (list.isEmpty) {
                    return const AppEmptyState(message: 'No trend data available');
                  }

                  // Find max value for Y-axis scaling
                  double maxY = 5.0;
                  for (var item in list) {
                    if ((item.count ?? 0) > maxY) {
                      maxY = (item.count ?? 0).toDouble();
                    }
                  }

                  // Adding 20% breathing room at the top
                  maxY = maxY * 1.2;

                  return Padding(
                    padding: const EdgeInsets.only(right: 16.0, top: 16.0),
                    child: LineChart(
                      LineChartData(
                        gridData: FlGridData(
                          show: true,
                          drawVerticalLine: false,
                          horizontalInterval: (maxY / 4) > 0 ? (maxY / 4) : 1,
                        ),
                        titlesData: FlTitlesData(
                          show: true,
                          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          bottomTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              reservedSize: 30,
                              interval: 1,
                              getTitlesWidget: (value, meta) {
                                if (value.toInt() < 0 || value.toInt() >= list.length) {
                                  return const SizedBox.shrink();
                                }
                                // Show date roughly every (list.length / 5) points
                                final interval = (list.length / 5).ceil();
                                if (value.toInt() % interval == 0) {
                                  final rawDate = list[value.toInt()].date ?? '';
                                  final dateParts = rawDate.split('-'); // 2023-10-05
                                  final display = dateParts.length >= 3
                                      ? '${dateParts[1]}/${dateParts[2]}'
                                      : rawDate;
                                  return Padding(
                                    padding: const EdgeInsets.only(top: 8.0),
                                    child: Text(display, style: const TextStyle(fontSize: 10)),
                                  );
                                }
                                return const SizedBox.shrink();
                              },
                            ),
                          ),
                          leftTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              reservedSize: 42,
                              interval: (maxY / 4) > 0 ? (maxY / 4) : 1,
                              getTitlesWidget: (value, meta) {
                                return Text(value.toInt().toString(),
                                    style: const TextStyle(fontSize: 10));
                              },
                            ),
                          ),
                        ),
                        borderData: FlBorderData(show: false),
                        minX: 0,
                        maxX: (list.length - 1).toDouble(),
                        minY: 0,
                        maxY: maxY,
                        lineBarsData: [
                          LineChartBarData(
                            spots: list.asMap().entries.map((e) {
                              return FlSpot(e.key.toDouble(), (e.value.count ?? 0).toDouble());
                            }).toList(),
                            isCurved: true,
                            color: const Color(0xFF22C55E), // Green for completed
                            barWidth: 3,
                            isStrokeCapRound: true,
                            dotData: FlDotData(
                              show: list.length < 20, // Only show dots if few points
                            ),
                            belowBarData: BarAreaData(
                              show: true,
                              color: const Color(0xFF22C55E).withValues(alpha: 0.2),
                            ),
                          ),
                        ],
                        lineTouchData: LineTouchData(
                          touchTooltipData: LineTouchTooltipData(
                            getTooltipItems: (touchedSpots) {
                              return touchedSpots.map((spot) {
                                final count = spot.y.toInt();
                                return LineTooltipItem(
                                  '$count Tasks',
                                  const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                                );
                              }).toList();
                            },
                          ),
                        ),
                      ),
                      duration: const Duration(milliseconds: 500),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
