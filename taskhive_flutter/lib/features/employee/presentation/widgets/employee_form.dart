import 'package:flutter/material.dart';

import '../../../../../core/widgets/app_button.dart';
import '../../../../../core/widgets/app_text_field.dart';
import '../../data/models/create_employee_request.dart';
import '../../data/models/employee_model.dart';
import '../../data/models/update_employee_request.dart';

extension CreateToUpdate on CreateEmployeeRequest {
  UpdateEmployeeRequest toUpdateRequest() => UpdateEmployeeRequest(
        firstName: firstName,
        lastName: lastName,
        phone: phone,
        department: department,
        designation: designation,
        joinDate: joinDate,
      );
}

class EmployeeForm extends StatefulWidget {
  final EmployeeModel? initialEmployee;
  final Future<void> Function(CreateEmployeeRequest) onSubmit;
  final bool isLoading;

  const EmployeeForm({
    super.key,
    this.initialEmployee,
    required this.onSubmit,
    this.isLoading = false,
  });

  @override
  State<EmployeeForm> createState() => _EmployeeFormState();
}

class _EmployeeFormState extends State<EmployeeForm> {
  final _formKey = GlobalKey<FormState>();

  late final TextEditingController _firstNameController;
  late final TextEditingController _lastNameController;
  late final TextEditingController _emailController;
  late final TextEditingController _phoneController;
  late final TextEditingController _departmentController;
  late final TextEditingController _designationController;
  late final TextEditingController _joinDateController;

  bool get _isEdit => widget.initialEmployee != null;

  @override
  void initState() {
    super.initState();
    _firstNameController =
        TextEditingController(text: widget.initialEmployee?.firstName ?? '');
    _lastNameController =
        TextEditingController(text: widget.initialEmployee?.lastName ?? '');
    _emailController =
        TextEditingController(text: widget.initialEmployee?.email ?? '');
    _phoneController =
        TextEditingController(text: widget.initialEmployee?.phone ?? '');
    _departmentController =
        TextEditingController(text: widget.initialEmployee?.department ?? '');
    _designationController =
        TextEditingController(text: widget.initialEmployee?.designation ?? '');
    _joinDateController =
        TextEditingController(text: widget.initialEmployee?.joinDate ?? '');
  }

  @override
  void dispose() {
    _firstNameController.dispose();
    _lastNameController.dispose();
    _emailController.dispose();
    _phoneController.dispose();
    _departmentController.dispose();
    _designationController.dispose();
    _joinDateController.dispose();
    super.dispose();
  }

  Future<void> _selectJoinDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now(),
      firstDate: DateTime(2000),
      lastDate: DateTime(2100),
    );
    if (picked != null) {
      setState(() {
        // Assuming backend expects yyyy-MM-dd
        _joinDateController.text =
            "${picked.year}-${picked.month.toString().padLeft(2, '0')}-${picked.day.toString().padLeft(2, '0')}";
      });
    }
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;

    final request = CreateEmployeeRequest(
      firstName: _firstNameController.text.trim(),
      lastName: _lastNameController.text.trim(),
      email: _emailController.text.trim(),
      phone: _phoneController.text.trim().isEmpty
          ? null
          : _phoneController.text.trim(),
      department: _departmentController.text.trim().isEmpty
          ? null
          : _departmentController.text.trim(),
      designation: _designationController.text.trim().isEmpty
          ? null
          : _designationController.text.trim(),
      joinDate: _joinDateController.text.trim().isEmpty
          ? null
          : _joinDateController.text.trim(),
    );

    widget.onSubmit(request);
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                Expanded(
                  child: AppTextField(
                    label: 'First Name *',
                    controller: _firstNameController,
                    validator: (v) =>
                        v == null || v.trim().isEmpty ? 'Required' : null,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: AppTextField(
                    label: 'Last Name *',
                    controller: _lastNameController,
                    validator: (v) =>
                        v == null || v.trim().isEmpty ? 'Required' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            AppTextField(
              label: 'Email *',
              controller: _emailController,
              readOnly: _isEdit,
              keyboardType: TextInputType.emailAddress,
              validator: (v) {
                if (v == null || v.trim().isEmpty) return 'Required';
                if (!RegExp(r'^[^@]+@[^@]+\.[^@]+').hasMatch(v)) {
                  return 'Invalid email';
                }
                return null;
              },
            ),
            if (_isEdit) ...[
              const SizedBox(height: 4),
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  'Email cannot be changed after creation.',
                  style: TextStyle(
                    fontSize: 12,
                    color: Theme.of(context).hintColor,
                  ),
                ),
              ),
            ],
            const SizedBox(height: 16),
            AppTextField(
              label: 'Phone',
              controller: _phoneController,
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 16),
            AppTextField(
              label: 'Department',
              controller: _departmentController,
            ),
            const SizedBox(height: 16),
            AppTextField(
              label: 'Designation',
              controller: _designationController,
            ),
            const SizedBox(height: 16),
            GestureDetector(
              onTap: _selectJoinDate,
              child: AbsorbPointer(
                child: AppTextField(
                  label: 'Join Date',
                  controller: _joinDateController,
                  hint: 'YYYY-MM-DD',
                  readOnly: true,
                ),
              ),
            ),
            const SizedBox(height: 32),
            AppButton(
              label: _isEdit ? 'Update Employee' : 'Create Employee',
              onPressed: _submit,
              isLoading: widget.isLoading,
            ),
          ],
        ),
      ),
    );
  }
}
