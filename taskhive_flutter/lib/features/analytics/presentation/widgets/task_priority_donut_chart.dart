import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../data/models/task_distribution_response.dart';

class TaskPriorityDonutChart extends StatelessWidget {
  final AsyncValue<List<TaskDistributionResponse>> dataState;
  final double height;

  const TaskPriorityDonutChart({
    super.key,
    required this.dataState,
    this.height = 180,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.only(bottom: 8.0),
              child: Text(
                'Task Priority',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            SizedBox(
              height: height,
              child: dataState.when(
                loading: () => const AppLoading(),
                error: (e, _) => AppErrorWidget(message: e.toString()),
                data: (list) {
                  if (list.isEmpty) {
                    return const AppEmptyState(message: 'No data available');
                  }

                  final total = list.fold<int>(0, (sum, item) => sum + (item.count ?? 0));
                  if (total == 0) {
                    return const AppEmptyState(message: 'No tasks to display yet');
                  }

                  final innerRadius = (height / 2) - 36;
                  final sectionRadius = height / 6;

                  return Column(
                    children: [
                      Expanded(
                        child: Stack(
                          alignment: Alignment.center,
                          children: [
                            PieChart(
                              PieChartData(
                                sectionsSpace: 2,
                                centerSpaceRadius: innerRadius > 0 ? innerRadius : 20,
                                sections: list.map((item) {
                                  final priority = item.status ?? 'UNKNOWN';
                                  final count = item.count ?? 0;

                                  Color color;
                                  switch (priority) {
                                    case 'LOW':
                                      color = const Color(0xFF4CAF50);
                                      break;
                                    case 'MEDIUM':
                                      color = const Color(0xFF2196F3);
                                      break;
                                    case 'HIGH':
                                      color = const Color(0xFFFF9800);
                                      break;
                                    case 'CRITICAL':
                                      color = const Color(0xFFF44336);
                                      break;
                                    default:
                                      color = Colors.grey;
                                  }

                                  return PieChartSectionData(
                                    color: color,
                                    value: count.toDouble(),
                                    title: '', // Don't show labels in slices
                                    radius: sectionRadius > 10 ? sectionRadius : 10,
                                  );
                                }).toList(),
                              ),
                              duration: const Duration(milliseconds: 500),
                            ),
                            Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Text(
                                  total.toString(),
                                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                                        fontWeight: FontWeight.bold,
                                      ),
                                ),
                                const Text('Total', style: TextStyle(fontSize: 10, color: Colors.grey)),
                              ],
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 8),
                      // Legend
                      Wrap(
                        spacing: 12,
                        runSpacing: 4,
                        alignment: WrapAlignment.center,
                        children: list.map((item) {
                          final priority = item.status ?? 'UNKNOWN';
                          final count = item.count ?? 0;
                          final percentage = total > 0 ? (count / total) * 100 : 0.0;

                          Color color;
                          switch (priority) {
                            case 'LOW':
                              color = const Color(0xFF4CAF50);
                              break;
                            case 'MEDIUM':
                              color = const Color(0xFF2196F3);
                              break;
                            case 'HIGH':
                              color = const Color(0xFFFF9800);
                              break;
                            case 'CRITICAL':
                              color = const Color(0xFFF44336);
                              break;
                            default:
                              color = Colors.grey;
                          }

                          return Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Container(
                                width: 8,
                                height: 8,
                                decoration: BoxDecoration(color: color, shape: BoxShape.circle),
                              ),
                              const SizedBox(width: 4),
                              Text('$priority (${percentage.toStringAsFixed(1)}%)',
                                  style: const TextStyle(fontSize: 10)),
                            ],
                          );
                        }).toList(),
                      ),
                    ],
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
