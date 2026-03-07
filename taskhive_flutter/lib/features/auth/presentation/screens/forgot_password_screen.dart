import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/router/app_routes.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_snackbar.dart';
import '../../../../core/widgets/app_text_field.dart';
import '../../data/repositories/auth_repository.dart';

final _isLoadingProvider = StateProvider<bool>((ref) => false);
final _isSubmittedProvider = StateProvider<bool>((ref) => false);

class ForgotPasswordScreen extends ConsumerStatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  ConsumerState<ForgotPasswordScreen> createState() =>
      _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends ConsumerState<ForgotPasswordScreen> {
  final _emailCtrl = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _emailCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    ref.read(_isLoadingProvider.notifier).state = true;

    try {
      await ref.read(authRepositoryProvider).forgotPassword(
            _emailCtrl.text.trim(),
          );
      if (mounted) {
        ref.read(_isSubmittedProvider.notifier).state = true;
      }
    } catch (e) {
      // Backend always returns 200 for forgot password —
      // but handle network errors gracefully
      if (mounted) {
        AppSnackbar.showError(context, 'Network error. Please try again.');
      }
    } finally {
      if (mounted) {
        ref.read(_isLoadingProvider.notifier).state = false;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final isLoading = ref.watch(_isLoadingProvider);
    final isSubmitted = ref.watch(_isSubmittedProvider);

    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go(AppRoutes.login),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: 24),
              const Text('Forgot Password',
                  style: AppTextStyles.headlineMedium),
              const SizedBox(height: 8),
              Text(
                'Enter your email address and we\'ll send you a link to reset your password.',
                style: AppTextStyles.bodyMedium
                    .copyWith(color: AppColors.textSecondary),
              ),
              const SizedBox(height: 32),
              if (isSubmitted) ...[
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: AppColors.success.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(
                        color: AppColors.success.withValues(alpha: 0.3)),
                  ),
                  child: Text(
                    'If this email is registered, you\'ll receive a reset link.',
                    style: AppTextStyles.bodyMedium
                        .copyWith(color: AppColors.success),
                  ),
                ),
                const SizedBox(height: 24),
                AppButton(
                  label: 'Back to Login',
                  onPressed: () => context.go(AppRoutes.login),
                  isOutlined: true,
                ),
              ] else ...[
                Form(
                  key: _formKey,
                  child: AppTextField(
                    label: 'Email',
                    controller: _emailCtrl,
                    keyboardType: TextInputType.emailAddress,
                    hint: 'you@company.com',
                    validator: (v) {
                      if (v == null || v.trim().isEmpty) {
                        return 'Email is required';
                      }
                      if (!v.contains('@')) return 'Enter a valid email';
                      return null;
                    },
                  ),
                ),
                const SizedBox(height: 24),
                AppButton(
                  label: 'Send Reset Link',
                  isLoading: isLoading,
                  onPressed: isLoading ? null : _submit,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
