import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/router/app_routes.dart';
import '../../../../../core/widgets/app_empty_state.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../../../core/widgets/app_confirm_dialog.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_list_provider.dart';
import '../../../data/models/employee_model.dart';
import '../../widgets/employee_filter_bar.dart';
import '../../widgets/employee_list_tile.dart';
import '../../widgets/employee_search_bar.dart';

class EmployeeListScreen extends ConsumerStatefulWidget {
  const EmployeeListScreen({super.key});

  @override
  ConsumerState<EmployeeListScreen> createState() => _EmployeeListScreenState();
}

class _EmployeeListScreenState extends ConsumerState<EmployeeListScreen> {
  final _scrollController = ScrollController();
  late final _searchBarKey = GlobalKey<EmployeeSearchBarState>();
  EmployeeListFilter _filter = const EmployeeListFilter();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      ref.read(employeeListNotifierProvider.notifier).loadMore();
    }
  }

  @override
  Widget build(BuildContext context) {
    final listAsync = ref.watch(employeeListNotifierProvider);
    final actionsState = ref.watch(employeeActionsProvider);

    // Side effects — error/success snackbars
    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(
        title: const Text('Employees'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add_alt_1_outlined),
            tooltip: 'Add Employee',
            onPressed: () => context.go(AppRoutes.adminCreateEmployee),
          ),
        ],
      ),
      body: Column(
        children: [
          EmployeeSearchBar(
            key: _searchBarKey,
            onSearch: (q) {
              setState(() => _filter = _filter.copyWith(search: q));
              ref
                  .read(employeeListNotifierProvider.notifier)
                  .applyFilter(_filter);
            },
          ),
          EmployeeFilterBar(
            filter: _filter,
            onFilterChanged: (f) {
              setState(() => _filter = f);
              ref
                  .read(employeeListNotifierProvider.notifier)
                  .applyFilter(_filter);
            },
            onClear: () {
              setState(() {
                _filter = const EmployeeListFilter();
                _searchBarKey.currentState?.clear();
              });
              ref
                  .read(employeeListNotifierProvider.notifier)
                  .applyFilter(_filter);
            },
          ),
          Expanded(
            child: listAsync.when(
              loading: () => const AppLoading(),
              error: (e, _) => AppErrorWidget(
                message: e.toString(),
                onRetry: () =>
                    ref.read(employeeListNotifierProvider.notifier).refresh(),
              ),
              data: (state) {
                if (state.employees.isEmpty) {
                  return AppEmptyState(
                    message: 'No employees found.',
                    icon: Icons.people_outline,
                    action: TextButton(
                      onPressed: () =>
                          context.go(AppRoutes.adminCreateEmployee),
                      child: const Text('Create first employee'),
                    ),
                  );
                }
                return RefreshIndicator(
                  onRefresh: () =>
                      ref.read(employeeListNotifierProvider.notifier).refresh(),
                  child: ListView.builder(
                    controller: _scrollController,
                    itemCount:
                        state.employees.length + (state.isLoadingMore ? 1 : 0),
                    itemBuilder: (ctx, index) {
                      if (index == state.employees.length) {
                        return const Padding(
                          padding: EdgeInsets.all(16),
                          child: Center(
                              child: CircularProgressIndicator.adaptive()),
                        );
                      }
                      final employee = state.employees[index];
                      return EmployeeListTile(
                        employee: employee,
                        onTap: () => context
                            .go(AppRoutes.adminEmployeeDetailPath(employee.id)),
                        onDelete: () async {
                          final confirmed = await AppConfirmDialog.show(
                            context,
                            title: 'Delete Employee',
                            message:
                                'Delete ${employee.fullName}? This action cannot be undone.',
                            confirmLabel: 'Delete',
                          );
                          if (confirmed && context.mounted) {
                            await ref
                                .read(employeeActionsProvider.notifier)
                                .deleteEmployee(employee.id);
                            if (context.mounted) {
                              AppSnackbar.showSuccess(context,
                                  '${employee.fullName} has been deleted.');
                            }
                          }
                        },
                      );
                    },
                  ),
                );
              },
            ),
          ),
          if (actionsState is AsyncLoading) const LinearProgressIndicator(),
        ],
      ),
    );
  }
}
