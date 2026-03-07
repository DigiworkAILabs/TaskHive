import 'package:flutter/material.dart';
import '../../../../core/widgets/app_text_field.dart';

class ForgotPasswordForm extends StatelessWidget {
  final TextEditingController emailController;
  final GlobalKey<FormState> formKey;

  const ForgotPasswordForm({
    super.key,
    required this.emailController,
    required this.formKey,
  });

  @override
  Widget build(BuildContext context) {
    return Form(
      key: formKey,
      child: AppTextField(
        label: 'Email',
        controller: emailController,
        keyboardType: TextInputType.emailAddress,
        hint: 'you@company.com',
        validator: (v) {
          if (v == null || v.trim().isEmpty) return 'Email is required';
          if (!v.contains('@')) return 'Enter a valid email';
          return null;
        },
      ),
    );
  }
}
