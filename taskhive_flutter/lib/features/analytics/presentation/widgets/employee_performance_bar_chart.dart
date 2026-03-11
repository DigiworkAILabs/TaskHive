import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../data/models/employee_performance_response.dart';

class EmployeePerformanceBarChart extends StatelessWidget {
  final AsyncValue<List<EmployeePerformanceResponse>> dataState;
  final double height;

  const EmployeePerformanceBarChart({
    super.key,
    required this.dataState,
    this.height = 220,
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
                'Employee Performance',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: height,
              child: dataState.when(
                loading: () => const AppLoading(),
                error: (e, _) => AppErrorWidget(message: e.toString()),
                data: (list) {
                  if (list.isEmpty) {
                    return const AppEmptyState(message: 'No performance data');
                  }

                  double maxY = 5.0;
                  for (var item in list) {
                    if ((item.tasksCompleted ?? 0) > maxY) {
                      maxY = (item.tasksCompleted ?? 0).toDouble();
                    }
                  }
                  maxY = maxY * 1.2;

                  return Padding(
                    padding: const EdgeInsets.only(top: 16.0, right: 16.0, bottom: 8.0),
                    child: BarChart(
                      BarChartData(
                        gridData: const FlGridData(show: false),
                        titlesData: FlTitlesData(
                          show: true,
                          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          topTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              reservedSize: 20,
                              getTitlesWidget: (value, meta) {
                                if (value.toInt() >= 0 && value.toInt() < list.length) {
                                  final count = list[value.toInt()].tasksCompleted ?? 0;
                                  return Text(count.toString(),
                                      style: const TextStyle(fontSize: 10));
                                }
                                return const SizedBox.shrink();
                              },
                            ),
                          ),
                          leftTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          bottomTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              reservedSize: 30,
                              getTitlesWidget: (value, meta) {
                                if (value.toInt() >= 0 && value.toInt() < list.length) {
                                  final name = list[value.toInt()].employeeName ?? '';
                                  final shortName = name.split(' ').first;
                                  return Padding(
                                    padding: const EdgeInsets.only(top: 8.0),
                                    child: Text(
                                      shortName,
                                      style: const TextStyle(fontSize: 10),
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  );
                                }
                                return const SizedBox.shrink();
                              },
                            ),
                          ),
                        ),
                        borderData: FlBorderData(show: false),
                        maxY: maxY,
                        barGroups: list.asMap().entries.map((entry) {
                          final idx = entry.key;
                          final item = entry.value;
                          return BarChartGroupData(
                            x: idx,
                            barRods: [
                              BarChartRodData(
                                toY: (item.tasksCompleted ?? 0).toDouble(),
                                color: Theme.of(context).primaryColor,
                                width: 22,
                                borderRadius: BorderRadius.circular(4),
                              ),
                            ],
                          );
                        }).toList(),
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
