import 'package:flutter/material.dart';
import '../../../../../core/widgets/app_badge.dart';
import '../../data/models/employee_model.dart';

class EmployeeStatusBadge extends StatelessWidget {
  final EmployeeStatus status;

  const EmployeeStatusBadge({super.key, required this.status});

  @override
  Widget build(BuildContext context) {
    return AppBadge(
      label: status.name.toUpperCase(),
      color: _getColor(),
    );
  }

  Color _getColor() {
    switch (status) {
      case EmployeeStatus.active:
        return Colors.green;
      case EmployeeStatus.pending:
        return Colors.orange;
      case EmployeeStatus.inactive:
        return Colors.grey;
      case EmployeeStatus.deleted:
        return Colors.red;
    }
  }
}
