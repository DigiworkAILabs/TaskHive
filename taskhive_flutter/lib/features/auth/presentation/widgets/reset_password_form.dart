import 'package:flutter/material.dart';
import '../../../../core/widgets/app_text_field.dart';

class ResetPasswordForm extends StatelessWidget {
  final TextEditingController passwordController;
  final TextEditingController confirmController;
  final GlobalKey<FormState> formKey;

  const ResetPasswordForm({
    super.key,
    required this.passwordController,
    required this.confirmController,
    required this.formKey,
  });

  @override
  Widget build(BuildContext context) {
    return Form(
      key: formKey,
      child: Column(
        children: [
          AppTextField(
            label: 'New Password',
            controller: passwordController,
            obscureText: true,
            validator: (v) {
              if (v == null || v.isEmpty) return 'Password is required';
              if (v.length < 8) return 'Password must be at least 8 characters';
              return null;
            },
          ),
          const SizedBox(height: 16),
          AppTextField(
            label: 'Confirm Password',
            controller: confirmController,
            obscureText: true,
            validator: (v) {
              if (v == null || v.isEmpty) return 'Please confirm your password';
              if (v != passwordController.text) return 'Passwords do not match';
              return null;
            },
          ),
        ],
      ),
    );
  }
}
