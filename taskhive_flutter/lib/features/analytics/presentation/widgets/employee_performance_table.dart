import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/widgets/app_empty_state.dart';
import '../../../../core/widgets/app_error_widget.dart';
import '../../../../core/widgets/app_loading.dart';
import '../../data/models/employee_performance_response.dart';

class EmployeePerformanceTable extends StatefulWidget {
  final AsyncValue<List<EmployeePerformanceResponse>> dataState;

  const EmployeePerformanceTable({
    super.key,
    required this.dataState,
  });

  @override
  State<EmployeePerformanceTable> createState() => _EmployeePerformanceTableState();
}

class _EmployeePerformanceTableState extends State<EmployeePerformanceTable> {
  int _sortColumnIndex = 0;
  bool _sortAscending = true;

  @override
  Widget build(BuildContext context) {
    return widget.dataState.when(
      loading: () => const AppLoading(),
      error: (e, _) => AppErrorWidget(message: e.toString()),
      data: (listData) {
        if (listData.isEmpty) {
          return const AppEmptyState(message: 'No performance data');
        }

        // Work on a copy to mutate
        final list = List<EmployeePerformanceResponse>.from(listData);

        // Sorting manually
        if (_sortColumnIndex == 0) {
          list.sort((a, b) => _sortAscending
              ? (a.employeeName ?? '').compareTo(b.employeeName ?? '')
              : (b.employeeName ?? '').compareTo(a.employeeName ?? ''));
        } else if (_sortColumnIndex == 1) {
          list.sort((a, b) => _sortAscending
              ? (a.tasksAssigned ?? 0).compareTo(b.tasksAssigned ?? 0)
              : (b.tasksAssigned ?? 0).compareTo(a.tasksAssigned ?? 0));
        } else if (_sortColumnIndex == 2) {
          list.sort((a, b) => _sortAscending
              ? (a.tasksCompleted ?? 0).compareTo(b.tasksCompleted ?? 0)
              : (b.tasksCompleted ?? 0).compareTo(a.tasksCompleted ?? 0));
        } else if (_sortColumnIndex == 3) {
          list.sort((a, b) => _sortAscending
              ? (a.onTimeRate ?? 0).compareTo(b.onTimeRate ?? 0)
              : (b.onTimeRate ?? 0).compareTo(a.onTimeRate ?? 0));
        }

        return Card(
          elevation: 2,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DataTable(
              sortColumnIndex: _sortColumnIndex,
              sortAscending: _sortAscending,
              columns: [
                DataColumn(
                  label: const Text('Employee'),
                  onSort: (columnIndex, ascending) {
                    setState(() {
                      _sortColumnIndex = columnIndex;
                      _sortAscending = ascending;
                    });
                  },
                ),
                DataColumn(
                  label: const Text('Assigned'),
                  numeric: true,
                  onSort: (columnIndex, ascending) {
                    setState(() {
                      _sortColumnIndex = columnIndex;
                      _sortAscending = ascending;
                    });
                  },
                ),
                DataColumn(
                  label: const Text('Completed'),
                  numeric: true,
                  onSort: (columnIndex, ascending) {
                    setState(() {
                      _sortColumnIndex = columnIndex;
                      _sortAscending = ascending;
                    });
                  },
                ),
                DataColumn(
                  label: const Text('On-Time %'),
                  numeric: true,
                  onSort: (columnIndex, ascending) {
                    setState(() {
                      _sortColumnIndex = columnIndex;
                      _sortAscending = ascending;
                    });
                  },
                ),
              ],
              rows: list.map((item) {
                return DataRow(
                  cells: [
                    DataCell(Text(item.employeeName ?? '')),
                    DataCell(Text((item.tasksAssigned ?? 0).toString())),
                    DataCell(Text((item.tasksCompleted ?? 0).toString())),
                    DataCell(Text('${(item.onTimeRate ?? 0.0).toStringAsFixed(1)}%')),
                  ],
                );
              }).toList(),
            ),
          ),
        );
      },
    );
  }
}
