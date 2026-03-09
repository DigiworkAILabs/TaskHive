import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

/// Read-only text field that opens a DatePicker followed by a TimePicker.
/// Matches the Next.js frontend capability to select exact Due Times.
class DueDatePicker extends StatefulWidget {
  final DateTime? initialDate;
  final ValueChanged<DateTime?> onChanged;

  const DueDatePicker({
    super.key,
    this.initialDate,
    required this.onChanged,
  });

  @override
  State<DueDatePicker> createState() => _DueDatePickerState();
}

class _DueDatePickerState extends State<DueDatePicker> {
  DateTime? _selected;
  late final TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _selected = widget.initialDate;
    _controller = TextEditingController(
      text: _selected != null
          ? DateFormat('MMM dd, yyyy h:mm a').format(_selected!)
          : '',
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _pick() async {
    final now = DateTime.now();
    // 1. Pick Date
    final datePicked = await showDatePicker(
      context: context,
      initialDate: _selected ?? now.add(const Duration(days: 1)),
      firstDate: now,
      lastDate: DateTime(now.year + 5),
    );

    if (datePicked == null) return;

    // 2. Pick Time (default to 17:00 or previously selected time)
    if (!mounted) return;
    final timePicked = await showTimePicker(
      context: context,
      initialTime: _selected != null
          ? TimeOfDay.fromDateTime(_selected!)
          : const TimeOfDay(hour: 17, minute: 0),
    );

    if (timePicked != null) {
      final finalDateTime = DateTime(
        datePicked.year,
        datePicked.month,
        datePicked.day,
        timePicked.hour,
        timePicked.minute,
      );

      setState(() {
        _selected = finalDateTime;
        _controller.text =
            DateFormat('MMM dd, yyyy h:mm a').format(finalDateTime);
      });
      widget.onChanged(finalDateTime);
    }
  }

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: _controller,
      readOnly: true,
      decoration: const InputDecoration(
        labelText: 'Due Date & Time *',
        prefixIcon: Icon(Icons.calendar_today_outlined),
        border: OutlineInputBorder(),
        hintText: 'Select future date & time',
      ),
      onTap: _pick,
      validator: (v) {
        if (v == null || v.isEmpty) return 'Due date & time are required';
        return null;
      },
    );
  }
}
