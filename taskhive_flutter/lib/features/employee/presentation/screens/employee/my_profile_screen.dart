import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../features/auth/domain/providers/auth_provider.dart';
import '../../../data/models/employee_model.dart';
import '../../widgets/employee_status_badge.dart';

/// The employee's own profile screen. Simplified to match Next.js frontend
/// which doesn't show a full editable profile for employees.
class MyProfileScreen extends ConsumerWidget {
  const MyProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authStateNotifierProvider);
    final currentUser = authState.user;

    if (currentUser == null) {
      return const Scaffold(body: Center(child: Text('Not logged in.')));
    }

    return Scaffold(
      appBar: AppBar(title: const Text('My Profile')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Column(
                children: [
                  Container(
                    width: 96,
                    height: 96,
                    decoration: BoxDecoration(
                      color:
                          Theme.of(context).colorScheme.primary.withOpacity(0.1),
                      shape: BoxShape.circle,
                    ),
                    child: Center(
                      child: Text(
                        '${currentUser.firstName[0]}${currentUser.lastName[0]}'
                            .toUpperCase(),
                        style: TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text('${currentUser.firstName} ${currentUser.lastName}',
                      style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 4),
                  const EmployeeStatusBadge(status: EmployeeStatus.active),
                ],
              ),
            ),
            const SizedBox(height: 24),
            // Read-only fields from Auth User
            _ReadOnlyField(label: 'Email', value: currentUser.email),
            _ReadOnlyField(label: 'Role', value: currentUser.role),
            const Divider(height: 32),

            // Change password shortcut
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: () => context.go('/change-password'),
                child: const Text('Change Password'),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () {
                  ref.read(authStateNotifierProvider.notifier).logout();
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red.shade50,
                  foregroundColor: Colors.red,
                  elevation: 0,
                ),
                child: const Text('Logout'),
              ),
            ),
            const SizedBox(height: 24),
            const Center(
              child: Text(
                'Profile contact info and photo updates are managed by Admin.',
                style: TextStyle(color: Colors.grey, fontSize: 12),
              ),
            ),
          ],
        ),
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
