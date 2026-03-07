import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/router/app_routes.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_snackbar.dart';
import '../../../../core/widgets/app_text_field.dart';
import '../../data/repositories/auth_repository.dart';

class ResetPasswordScreen extends ConsumerStatefulWidget {
  final String token;
  const ResetPasswordScreen({super.key, required this.token});

  @override
  ConsumerState<ResetPasswordScreen> createState() =>
      _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends ConsumerState<ResetPasswordScreen> {
  final _passwordCtrl = TextEditingController();
  final _confirmCtrl = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _isLoading = false;
  bool _tokenError = false;

  @override
  void dispose() {
    _passwordCtrl.dispose();
    _confirmCtrl.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isLoading = true);

    try {
      await ref.read(authRepositoryProvider).resetPassword(
            token: widget.token,
            newPassword: _passwordCtrl.text,
          );
      if (mounted) {
        AppSnackbar.showSuccess(context, 'Password reset successfully');
        await Future.delayed(const Duration(milliseconds: 1500));
        if (mounted) context.go(AppRoutes.login);
      }
    } on ApiException catch (e) {
      if (mounted) {
        if (e.isTokenExpired || e.isTokenAlreadyUsed) {
          setState(() => _tokenError = true);
        }
        AppSnackbar.showError(context, e.message);
      }
    } catch (_) {
      if (mounted) {
        AppSnackbar.showError(context, 'An error occurred. Please try again.');
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    // Invalid or missing token
    if (widget.token.isEmpty) {
      return Scaffold(
        appBar: AppBar(),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.link_off, size: 64, color: AppColors.error),
                const SizedBox(height: 16),
                const Text('Invalid or expired link',
                    style: AppTextStyles.headlineSmall),
                const SizedBox(height: 24),
                AppButton(
                  label: 'Go to Login',
                  onPressed: () => context.go(AppRoutes.login),
                ),
              ],
            ),
          ),
        ),
      );
    }

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
              const Text('Reset Password', style: AppTextStyles.headlineMedium),
              const SizedBox(height: 8),
              Text(
                'Enter your new password below.',
                style: AppTextStyles.bodyMedium
                    .copyWith(color: AppColors.textSecondary),
              ),
              const SizedBox(height: 32),
              Form(
                key: _formKey,
                child: Column(
                  children: [
                    AppTextField(
                      label: 'New Password',
                      controller: _passwordCtrl,
                      obscureText: true,
                      validator: (v) {
                        if (v == null || v.isEmpty) {
                          return 'Password is required';
                        }
                        if (v.length < 8) {
                          return 'Password must be at least 8 characters';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    AppTextField(
                      label: 'Confirm Password',
                      controller: _confirmCtrl,
                      obscureText: true,
                      validator: (v) {
                        if (v == null || v.isEmpty) {
                          return 'Please confirm your password';
                        }
                        if (v != _passwordCtrl.text) {
                          return 'Passwords do not match';
                        }
                        return null;
                      },
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),
              if (_tokenError) ...[
                AppButton(
                  label: 'Request New Link',
                  onPressed: () => context.go(AppRoutes.forgotPassword),
                  isOutlined: true,
                ),
                const SizedBox(height: 12),
              ],
              AppButton(
                label: 'Reset Password',
                isLoading: _isLoading,
                onPressed: (_isLoading || _tokenError) ? null : _submit,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
