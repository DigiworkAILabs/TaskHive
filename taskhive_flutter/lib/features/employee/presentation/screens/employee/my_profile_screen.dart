import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../core/widgets/app_button.dart';
import '../../../../../core/widgets/app_error_widget.dart';
import '../../../../../core/widgets/app_loading.dart';
import '../../../../../core/widgets/app_snackbar.dart';
import '../../../../../core/widgets/app_text_field.dart';
import '../../../../../features/auth/domain/providers/current_user_provider.dart';
import '../../../data/models/update_employee_profile_request.dart';
import '../../../domain/providers/employee_actions_provider.dart';
import '../../../domain/providers/employee_detail_provider.dart';
import '../../../domain/providers/profile_photo_provider.dart';
import '../../widgets/employee_status_badge.dart';
import '../../widgets/profile_photo_widget.dart';

/// The employee's own profile screen. Allows updating phone + address only.
/// Profile photo upload is also available here.
class MyProfileScreen extends ConsumerStatefulWidget {
  const MyProfileScreen({super.key});

  @override
  ConsumerState<MyProfileScreen> createState() => _MyProfileScreenState();
}

class _MyProfileScreenState extends ConsumerState<MyProfileScreen> {
  final _phoneController = TextEditingController();
  final _addressController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _dirty = false;

  @override
  void dispose() {
    _phoneController.dispose();
    _addressController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final currentUser = ref.watch(currentUserProvider);
    if (currentUser == null) {
      return const Scaffold(body: Center(child: Text('Not logged in.')));
    }

    final profileAsync = ref.watch(employeeDetailProvider(currentUser.id));
    final actionsState = ref.watch(employeeActionsProvider);
    final photoState = ref.watch(profilePhotoNotifierProvider);

    ref.listen<AsyncValue<void>>(employeeActionsProvider, (_, next) {
      if (next is AsyncError) {
        AppSnackbar.showError(context, next.error.toString());
      }
    });

    return Scaffold(
      appBar: AppBar(title: const Text('My Profile')),
      body: profileAsync.when(
        loading: () => const AppLoading(),
        error: (e, _) => AppErrorWidget(
          message: e.toString(),
          onRetry: () => ref.refresh(employeeDetailProvider(currentUser.id)),
        ),
        data: (employee) {
          // Pre-fill on first load (not on every rebuild if user is typing)
          if (!_dirty) {
            _phoneController.text = employee.phone ?? '';
            _addressController.text = employee.address ?? '';
          }

          return SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Center(
                    child: Column(
                      children: [
                        ProfilePhotoWidget(
                          photoUrl: employee.photoUrl,
                          initials:
                              '${employee.firstName[0]}${employee.lastName[0]}'
                                  .toUpperCase(),
                          size: 96,
                          onUpload: () => ref
                              .read(profilePhotoNotifierProvider.notifier)
                              .pickAndUpload(employee.id),
                          isUploading: photoState is AsyncLoading,
                        ),
                        const SizedBox(height: 12),
                        Text('${employee.firstName} ${employee.lastName}',
                            style: Theme.of(context).textTheme.titleLarge),
                        const SizedBox(height: 4),
                        EmployeeStatusBadge(status: employee.status),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),
                  // Read-only fields
                  _ReadOnlyField(label: 'Email', value: employee.email),
                  _ReadOnlyField(
                      label: 'Department', value: employee.department ?? '—'),
                  _ReadOnlyField(
                      label: 'Designation', value: employee.designation ?? '—'),
                  _ReadOnlyField(
                      label: 'Join Date', value: employee.joinDate ?? '—'),
                  const Divider(height: 32),
                  Text('Update Contact Info',
                      style: Theme.of(context).textTheme.titleSmall),
                  const SizedBox(height: 12),
                  AppTextField(
                    label: 'Phone',
                    controller: _phoneController,
                    keyboardType: TextInputType.phone,
                    onChanged: (_) => setState(() => _dirty = true),
                  ),
                  const SizedBox(height: 12),
                  AppTextField(
                    label: 'Address',
                    controller: _addressController,
                    onChanged: (_) => setState(() => _dirty = true),
                  ),
                  const SizedBox(height: 24),
                  AppButton(
                    label: 'Save Changes',
                    isLoading: actionsState is AsyncLoading,
                    onPressed: () async {
                      if (!_formKey.currentState!.validate()) return;
                      final updated = await ref
                          .read(employeeActionsProvider.notifier)
                          .updateOwnProfile(
                            employee.id,
                            UpdateEmployeeProfileRequest(
                              phone: _phoneController.text.trim().isEmpty
                                  ? null
                                  : _phoneController.text.trim(),
                              address: _addressController.text.trim().isEmpty
                                  ? null
                                  : _addressController.text.trim(),
                            ),
                          );
                      if (updated != null && context.mounted) {
                        setState(() => _dirty = false);
                        AppSnackbar.showSuccess(
                            context, 'Profile updated successfully.');
                      }
                    },
                  ),
                  const SizedBox(height: 16),
                  // Change password shortcut
                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton(
                      onPressed: () => context.go('/change-password'),
                      child: const Text('Change Password'),
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

class _ReadOnlyField extends StatelessWidget {
  final String label;
  final String value;
  const _ReadOnlyField({required this.label, required this.value});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 100,
              child: Text(label,
                  style: Theme.of(context)
                      .textTheme
                      .bodyMedium
                      ?.copyWith(color: Theme.of(context).hintColor)),
            ),
            Expanded(child: Text(value)),
          ],
        ),
      );
}
