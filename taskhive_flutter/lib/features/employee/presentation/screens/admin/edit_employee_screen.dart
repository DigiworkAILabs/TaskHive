import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_detail_provider.dart';
import '../../../data/models/employee_model.dart';
import '../../widgets/employee_form.dart';

class EditEmployeeScreen extends ConsumerWidget {
  final String employeeId;
  const EditEmployeeScreen({super.key, required this.employeeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(employeeDetailProvider(employeeId));

    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('Edit Employee')),
      body: detailAsync.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(
          message: e.toString(),
          onRetry: () => ref.refresh(employeeDetailProvider(employeeId)),
        ),
        data: (employee) => EmployeeForm(
          initialEmployee: employee,
          onSubmit: (data) async {
            final updateReq = data.toUpdateRequest();
            final updated = await ref
                .read(employeeActionsProvider.notifier)
                .updateEmployee(employeeId, updateReq);
            if (updated != null && context.mounted) {
              AppSnackbar.showSuccess(
                  context, '${updated.fullName} updated successfully.');
              context.go(AppRoutes.adminEmployeeDetailPath(employeeId));
            }
          },
          isLoading: ref.watch(employeeActionsProvider) is AsyncLoading,
        ),
      ),
    );
  }
}
