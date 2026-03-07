import 'package:flutter/material.dart';
import '../../../../core/widgets/app_text_field.dart';

class ChangePasswordForm extends StatelessWidget {
  final TextEditingController currentController;
  final TextEditingController newController;
  final TextEditingController confirmController;
  final GlobalKey<FormState> formKey;

  const ChangePasswordForm({
    super.key,
    required this.currentController,
    required this.newController,
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
            label: 'Current Password',
            controller: currentController,
            obscureText: true,
            validator: (v) {
              if (v == null || v.isEmpty) return 'Current password is required';
              return null;
            },
          ),
          const SizedBox(height: 16),
          AppTextField(
            label: 'New Password',
            controller: newController,
            obscureText: true,
            validator: (v) {
              if (v == null || v.isEmpty) return 'New password is required';
              if (v.length < 8) return 'Password must be at least 8 characters';
              if (v == currentController.text) {
                return 'New password must be different from current';
              }
              return null;
            },
          ),
          const SizedBox(height: 16),
          AppTextField(
            label: 'Confirm New Password',
            controller: confirmController,
            obscureText: true,
            validator: (v) {
              if (v == null || v.isEmpty) return 'Please confirm your password';
              if (v != newController.text) return 'Passwords do not match';
              return null;
            },
          ),
        ],
      ),
    );
  }
}
