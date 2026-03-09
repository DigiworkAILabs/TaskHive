import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../data/models/employee_model.dart';
import '../../widgets/employee_form.dart';

class CreateEmployeeScreen extends ConsumerWidget {
  const CreateEmployeeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('Add Employee')),
      body: EmployeeForm(
        onSubmit: (data) async {
          final created = await ref
              .read(employeeActionsProvider.notifier)
              .createEmployee(data);
          if (created != null && context.mounted) {
            AppSnackbar.showSuccess(
                context, '${created.fullName} created. Activation email sent.');
            context.go(AppRoutes.adminEmployees);
          }
        },
        isLoading: ref.watch(employeeActionsProvider) is AsyncLoading,
      ),
    );
  }
}
