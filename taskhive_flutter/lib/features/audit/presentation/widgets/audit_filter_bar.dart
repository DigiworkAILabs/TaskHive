import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../data/repositories/audit_repository.dart';
import '../../../../core/widgets/app_text_field.dart';

class AuditFilterBar extends StatefulWidget {
  final AuditSearchFilter filter;
  final ValueChanged<AuditSearchFilter> onFilterChanged;

  const AuditFilterBar({
    super.key,
    required this.filter,
    required this.onFilterChanged,
  });

  @override
  State<AuditFilterBar> createState() => _AuditFilterBarState();
}

class _AuditFilterBarState extends State<AuditFilterBar> {
  late TextEditingController _fromDateController;
  late TextEditingController _toDateController;
  late TextEditingController _actorEmailController;
  late TextEditingController _ipAddressController;

  String? _selectedAction;
  String? _selectedEntityType;

  static const List<String?> _actionTypes = [
    null,
    'EMPLOYEE_CREATED',
    'EMPLOYEE_UPDATED',
    'EMPLOYEE_DELETED',
    'EMPLOYEE_ACTIVATED',
    'EMPLOYEE_DEACTIVATED',
    'TASK_CREATED',
    'TASK_UPDATED',
    'TASK_DELETED',
    'TASK_STATUS_CHANGED',
    'TASK_ASSIGNED',
    'LOGIN_FAILED',
    'ACCOUNT_LOCKED',
    'UNAUTHORIZED_ACCESS',
    'PASSWORD_CHANGED',
    'NOTIFICATION_PREFERENCES_UPDATED',
  ];

  static const List<String?> _entityTypes = [
    null,
    'EMPLOYEE',
    'TASK',
    'AUTH',
  ];

  @override
  void initState() {
    super.initState();
    _fromDateController = TextEditingController(text: widget.filter.fromDate);
    _toDateController = TextEditingController(text: widget.filter.toDate);
    _actorEmailController =
        TextEditingController(text: widget.filter.actorEmail);
    _ipAddressController = TextEditingController(text: widget.filter.ipAddress);
    _selectedAction = widget.filter.action;
    _selectedEntityType = widget.filter.entityType;
  }

  @override
  void didUpdateWidget(covariant AuditFilterBar oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.filter != oldWidget.filter) {
      _fromDateController.text = widget.filter.fromDate ?? '';
      _toDateController.text = widget.filter.toDate ?? '';
      _actorEmailController.text = widget.filter.actorEmail ?? '';
      _ipAddressController.text = widget.filter.ipAddress ?? '';
      _selectedAction = widget.filter.action;
      _selectedEntityType = widget.filter.entityType;
    }
  }

  @override
  void dispose() {
    _fromDateController.dispose();
    _toDateController.dispose();
    _actorEmailController.dispose();
    _ipAddressController.dispose();
    super.dispose();
  }

  Future<void> _selectDate(TextEditingController controller) async {
    final initialDate = controller.text.isNotEmpty
        ? DateTime.tryParse(controller.text) ?? DateTime.now()
        : DateTime.now();

    final pickedDate = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );

    if (pickedDate != null && context.mounted) {
      setState(() {
        controller.text = DateFormat('yyyy-MM-dd').format(pickedDate);
      });
    }
  }

  void _applyFilter() {
    final newFilter = AuditSearchFilter(
      fromDate:
          _fromDateController.text.isEmpty ? null : _fromDateController.text,
      toDate: _toDateController.text.isEmpty ? null : _toDateController.text,
      action: _selectedAction,
      entityType: _selectedEntityType,
      actorEmail: _actorEmailController.text.isEmpty
          ? null
          : _actorEmailController.text,
      ipAddress:
          _ipAddressController.text.isEmpty ? null : _ipAddressController.text,
      page: 0,
      size: 20,
    );
    widget.onFilterChanged(newFilter);
  }

  void _clearFilter() {
    setState(() {
      _fromDateController.clear();
      _toDateController.clear();
      _actorEmailController.clear();
      _ipAddressController.clear();
      _selectedAction = null;
      _selectedEntityType = null;
    });
    widget.onFilterChanged(AuditSearchFilter());
  }

  @override
  Widget build(BuildContext context) {
    return ExpansionTile(
      title: const Text('Filters'),
      leading: const Icon(Icons.filter_list),
      childrenPadding: const EdgeInsets.all(16.0),
      children: [
        Row(
          children: [
            Expanded(
              child: GestureDetector(
                onTap: () => _selectDate(_fromDateController),
                child: AbsorbPointer(
                  child: AppTextField(
                    controller: _fromDateController,
                    label: 'From Date',
                    hint: 'YYYY-MM-DD',
                    readOnly: true,
                    suffixIcon: const Icon(Icons.calendar_today),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: GestureDetector(
                onTap: () => _selectDate(_toDateController),
                child: AbsorbPointer(
                  child: AppTextField(
                    controller: _toDateController,
                    label: 'To Date',
                    hint: 'YYYY-MM-DD',
                    readOnly: true,
                    suffixIcon: const Icon(Icons.calendar_today),
                  ),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        DropdownButtonFormField<String?>(
          isExpanded: true,
          // ignore: deprecated_member_use
          value: _selectedAction,
          items: _actionTypes
              .map((action) => DropdownMenuItem<String?>(
                    value: action,
                    child: Text(
                      action ?? 'All Actions',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ))
              .toList(),
          onChanged: (val) => setState(() => _selectedAction = val),
        ),
        const SizedBox(height: 16),
        DropdownButtonFormField<String?>(
          isExpanded: true,
          // ignore: deprecated_member_use
          value: _selectedEntityType,
          items: _entityTypes
              .map((type) => DropdownMenuItem<String?>(
                    value: type,
                    child: Text(
                      type ?? 'All Types',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ))
              .toList(),
          onChanged: (val) => setState(() => _selectedEntityType = val),
        ),
        const SizedBox(height: 16),
        AppTextField(
          controller: _actorEmailController,
          label: 'Actor Email',
          keyboardType: TextInputType.emailAddress,
        ),
        const SizedBox(height: 16),
        AppTextField(
          controller: _ipAddressController,
          label: 'IP Address',
        ),
        const SizedBox(height: 24),
        Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: [
            TextButton(
              onPressed: _clearFilter,
              child: const Text('Clear'),
            ),
            const SizedBox(width: 16),
            FilledButton(
              onPressed: _applyFilter,
              child: const Text('Apply Filter'),
            ),
          ],
        )
      ],
    );
  }
}
