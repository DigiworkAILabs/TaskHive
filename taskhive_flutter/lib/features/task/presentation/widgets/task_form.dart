import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../data/models/create_task_request.dart';
import '../../data/models/update_task_request.dart';
import '../../domain/enums/task_priority.dart';
import '../../domain/providers/completion_time_prediction_provider.dart';
import '../../domain/providers/priority_prediction_provider.dart';
import 'assignee_picker.dart';
import 'completion_time_estimate.dart';
import 'due_date_picker.dart';
import 'priority_selector.dart';
import 'priority_suggestion_badge.dart';
import 'tags_input_field.dart';

/// Reusable form for both create and edit task flows.
///
/// - [initialTitle], [initialDescription], etc. are pre-filled for edit mode.
/// - [onCreateSubmit] is non-null for create; [onUpdateSubmit] for edit.
/// - Exactly one of them must be provided.
class TaskForm extends ConsumerStatefulWidget {
  // Initial values for edit mode
  final String? initialTitle;
  final String? initialDescription;
  final TaskPriority? initialPriority;
  final String? initialAssigneeId;
  final DateTime? initialDueDate;
  final double? initialEstimatedHours;
  final List<String> initialTags;

  /// Called on create form submission with the full request object.
  final Future<void> Function(CreateTaskRequest)? onCreateSubmit;

  /// Called on edit form submission with the partial update request.
  final Future<void> Function(UpdateTaskRequest)? onUpdateSubmit;

  const TaskForm({
    super.key,
    this.initialTitle,
    this.initialDescription,
    this.initialPriority,
    this.initialAssigneeId,
    this.initialDueDate,
    this.initialEstimatedHours,
    this.initialTags = const [],
    this.onCreateSubmit,
    this.onUpdateSubmit,
  }) : assert(
          (onCreateSubmit != null) ^ (onUpdateSubmit != null),
          'Exactly one of onCreateSubmit or onUpdateSubmit must be provided',
        );

  @override
  ConsumerState<TaskForm> createState() => _TaskFormState();
}

class _TaskFormState extends ConsumerState<TaskForm> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _titleController;
  late final TextEditingController _descriptionController;
  late final TextEditingController _hoursController;

  TaskPriority? _priority;
  String? _assigneeId;
  DateTime? _dueDate;
  List<String> _tags = [];
  bool _submitting = false;
  Timer? _debounce;

  bool get _isCreate => widget.onCreateSubmit != null;

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.initialTitle ?? '');
    _descriptionController =
        TextEditingController(text: widget.initialDescription ?? '');
    _hoursController = TextEditingController(
      text: widget.initialEstimatedHours?.toString() ?? '',
    );
    _priority = widget.initialPriority;
    _assigneeId = widget.initialAssigneeId;
    _dueDate = widget.initialDueDate;
    _tags = List<String>.from(widget.initialTags);

    _titleController.addListener(_onInputChanged);
    _descriptionController.addListener(_onInputChanged);
    _hoursController.addListener(_onInputChanged);
  }

  void _onInputChanged() {
    if (!_isCreate) return; // Only suggest on creation
    if (_debounce?.isActive ?? false) _debounce!.cancel();

    _debounce = Timer(const Duration(milliseconds: 600), () {
      final title = _titleController.text.trim();
      if (title.length < 2) {
        ref.read(priorityPredictionProvider.notifier).reset();
        ref.read(completionTimePredictionNotifierProvider.notifier).clear();
        return;
      }

      final description = _descriptionController.text.trim();
      final hours = double.tryParse(_hoursController.text);

      // Trigger Priority Prediction
      ref.read(priorityPredictionProvider.notifier).predict(
            TaskPriorityRequestDto(
              taskTitle: title,
              taskDescription: description,
              tags: _tags,
              estimatedHours: hours,
              employeeId: _assigneeId,
            ),
          );

      // Trigger Completion Time Prediction if priority and assignee are set
      if (_priority != null && _assigneeId != null) {
        ref.read(completionTimePredictionNotifierProvider.notifier).predict(
              TaskCompletionTimeRequestDto(
                taskTitle: title,
                taskDescription: description,
                priority: _priority!.backendValue,
                employeeId: _assigneeId!,
                estimatedHours: hours,
              ),
            );
      }
    });
  }

  void _onTargetFieldsChanged() {
    if (!_isCreate) return;
    // Debounce to avoid spamming when choosing priority/assignee quickly
    if (_debounce?.isActive ?? false) _debounce!.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), _onInputChanged);
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _titleController.dispose();
    _descriptionController.dispose();
    _hoursController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    if (_priority == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select a priority')),
      );
      return;
    }

    setState(() => _submitting = true);
    try {
      if (_isCreate) {
        final request = CreateTaskRequest(
          title: _titleController.text.trim(),
          description: _descriptionController.text.trim(),
          priority: _priority!,
          assignedTo: _assigneeId!,
          dueDate: DateFormat("yyyy-MM-dd'T'HH:mm:ss").format(
            _dueDate!.copyWith(hour: 17, minute: 0, second: 0),
          ),
          estimatedHours: double.tryParse(_hoursController.text),
          tags: _tags,
        );
        await widget.onCreateSubmit!(request);
      } else {
        final request = UpdateTaskRequest(
          title: _titleController.text.trim().isEmpty
              ? null
              : _titleController.text.trim(),
          description: _descriptionController.text.trim().isEmpty
              ? null
              : _descriptionController.text.trim(),
          priority: _priority,
          assignedTo: _assigneeId,
          dueDate: _dueDate != null
              ? DateFormat("yyyy-MM-dd'T'HH:mm:ss").format(_dueDate!)
              : null,
          estimatedHours: double.tryParse(_hoursController.text),
          tags: _tags,
        );
        await widget.onUpdateSubmit!(request);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${e.toString()}')),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final predictionState = ref.watch(priorityPredictionProvider);

    return Form(
      key: _formKey,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Title
          TextFormField(
            controller: _titleController,
            decoration: const InputDecoration(
              labelText: 'Title *',
              border: OutlineInputBorder(),
            ),
            validator: (v) =>
                (v == null || v.trim().isEmpty) ? 'Title is required' : null,
            maxLength: 200,
          ),
          const SizedBox(height: 16),

          // Description
          TextFormField(
            controller: _descriptionController,
            decoration: const InputDecoration(
              labelText: 'Description',
              border: OutlineInputBorder(),
              alignLabelWithHint: true,
            ),
            maxLines: 4,
          ),
          const SizedBox(height: 16),

          // Priority
          PrioritySelector(
            selected: _priority,
            onChanged: (p) {
              setState(() => _priority = p);
              _onTargetFieldsChanged();
            },
          ),
          if (_isCreate &&
              (predictionState.isLoading ||
                  (predictionState.hasValue && predictionState.value != null)))
            PrioritySuggestionBadge(
              prediction: predictionState.value,
              isLoading: predictionState.isLoading,
              error: predictionState.hasError
                  ? predictionState.error.toString()
                  : null,
              onAccept: (p) {
                setState(() => _priority = p);
                _onTargetFieldsChanged();
              },
              onIgnore: () {
                ref.read(priorityPredictionProvider.notifier).reset();
              },
            ),
          const SizedBox(height: 16),

          // Assignee
          if (_isCreate)
            Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: AssigneePicker(
                initialAssigneeId: _assigneeId,
                onChanged: (id) {
                  setState(() => _assigneeId = id);
                  _onTargetFieldsChanged();
                },
              ),
            ),

          // Due Date
          DueDatePicker(
            initialDate: _dueDate,
            onChanged: (d) => setState(() => _dueDate = d),
          ),
          const SizedBox(height: 16),

          // Estimated Hours
          TextFormField(
            controller: _hoursController,
            decoration: const InputDecoration(
              labelText: 'Estimated Hours',
              border: OutlineInputBorder(),
              suffixText: 'hrs',
            ),
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            validator: (v) {
              if (v == null || v.isEmpty) return null;
              if (double.tryParse(v) == null) return 'Enter a valid number';
              return null;
            },
          ),
          if (_isCreate)
            Consumer(
              builder: (context, ref, _) {
                final prediction =
                    ref.watch(completionTimePredictionNotifierProvider);
                return CompletionTimeEstimate(
                  prediction: prediction.value,
                  isLoading: prediction.isLoading,
                  error: prediction.hasError
                      ? prediction.error.toString()
                      : null,
                );
              },
            ),
          const SizedBox(height: 16),

          // Tags
          TagsInputField(
            initialTags: _tags,
            onChanged: (tags) => setState(() => _tags = tags),
          ),
          const SizedBox(height: 24),

          // Submit
          FilledButton(
            onPressed: _submitting ? null : _submit,
            child: _submitting
                ? const SizedBox(
                    height: 20,
                    width: 20,
                    child: CircularProgressIndicator(
                        strokeWidth: 2, color: Colors.white),
                  )
                : Text(_isCreate ? 'Create Task' : 'Save Changes'),
          ),
        ],
      ),
    );
  }
}

extension on DateTime {
  DateTime copyWith({int? hour, int? minute, int? second}) => DateTime(
        year,
        month,
        day,
        hour ?? this.hour,
        minute ?? this.minute,
        second ?? this.second,
      );
}
