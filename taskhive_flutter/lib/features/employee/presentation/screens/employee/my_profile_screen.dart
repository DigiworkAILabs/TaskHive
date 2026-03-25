import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../../features/auth/domain/providers/auth_provider.dart';
import '../../../data/models/employee_model.dart';
import '../../widgets/employee_status_badge.dart';
import '../../../../ml/domain/providers/productivity_score_provider.dart';
import '../../../../ml/presentation/widgets/productivity_score_card.dart';
import '../../../../ml/domain/providers/ml_feature_provider.dart';

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

    final scoreAsync = ref.watch(productivityScoreProvider(currentUser.id));
    final isMlEnabledAsync = ref.watch(mlFeatureToggleProvider);

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
            const SizedBox(height: 32),
            
            // AI Productivity Section
            isMlEnabledAsync.when(
              data: (mlState) => mlState.isMlEnabled
                  ? _buildAiPerformanceSection(context, scoreAsync)
                  : const SizedBox.shrink(),
              loading: () => const SizedBox.shrink(),
              error: (_, __) => const SizedBox.shrink(),
            ),
            const SizedBox(height: 32),

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

  Widget _buildAiPerformanceSection(
      BuildContext context, AsyncValue scoreAsync) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            const Icon(Icons.psychology, size: 20, color: Colors.indigo),
            const SizedBox(width: 8),
            Text(
              'AI PERFORMANCE INSIGHTS',
              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    letterSpacing: 1.1,
                    color: Colors.grey.shade600,
                  ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        scoreAsync.when(
          data: (score) => ProductivityScoreCard(scoreData: score!),
          loading: () => const Center(
            child: Padding(
              padding: EdgeInsets.all(32),
              child: CircularProgressIndicator(),
            ),
          ),
          error: (e, _) => Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.red.withOpacity(0.05),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: Colors.red.withOpacity(0.1)),
            ),
            child: Row(
              children: [
                const Icon(Icons.error_outline, color: Colors.red, size: 20),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Unable to load productivity score: ${e.toString()}',
                    style: const TextStyle(color: Colors.red, fontSize: 13),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 32),
      ],
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
