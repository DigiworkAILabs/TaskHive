import 'package:flutter/material.dart';

class CancelReasonDialog extends StatefulWidget {
  final String taskTitle;

  const CancelReasonDialog({super.key, required this.taskTitle});

  @override
  State<CancelReasonDialog> createState() => _CancelReasonDialogState();
}

class _CancelReasonDialogState extends State<CancelReasonDialog> {
  final _reasonController = TextEditingController();
  String? _error;

  @override
  void dispose() {
    _reasonController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      backgroundColor: const Color(0xFF161616),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(14),
        side: const BorderSide(color: Color(0xFF2A2A2A)),
      ),
      title: const Text(
        'Cancel Task',
        style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          RichText(
            text: TextSpan(
              style: const TextStyle(color: Color(0xFFA1A1AA), fontSize: 13),
              children: [
                const TextSpan(text: 'You are cancelling: '),
                TextSpan(
                  text: widget.taskTitle,
                  style: const TextStyle(color: Color(0xFFD4D4D8), fontWeight: FontWeight.bold),
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),
          const Text(
            'REASON FOR CANCELLATION *',
            style: TextStyle(
              color: Color(0xFF71717A),
              fontSize: 11,
              fontWeight: FontWeight.bold,
              letterSpacing: 1,
            ),
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _reasonController,
            maxLines: 4,
            autofocus: true,
            style: const TextStyle(color: Colors.white, fontSize: 13),
            decoration: InputDecoration(
              hintText: 'e.g. Requirements changed, task no longer needed...',
              hintStyle: const TextStyle(color: Colors.grey),
              fillColor: const Color(0xFF0F0F0F),
              filled: true,
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: _error != null ? Colors.red : const Color(0xFF2A2A2A)),
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: BorderSide(color: _error != null ? Colors.red : const Color(0xFF2A2A2A)),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
                borderSide: const BorderSide(color: Colors.orange),
              ),
            ),
            onChanged: (val) {
              if (_error != null) setState(() => _error = null);
            },
          ),
          if (_error != null)
            Padding(
              padding: const EdgeInsets.only(top: 6),
              child: Text(_error!, style: const TextStyle(color: Colors.red, fontSize: 12)),
            ),
        ],
      ),
      actionsPadding: const EdgeInsets.only(right: 16, bottom: 16),
      actions: [
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Go Back', style: TextStyle(color: Color(0xFF71717A))),
        ),
        const SizedBox(width: 8),
        FilledButton(
          onPressed: () {
            if (_reasonController.text.trim().isEmpty) {
              setState(() => _error = 'Please provide a reason for cancellation.');
              return;
            }
            Navigator.of(context).pop(_reasonController.text.trim());
          },
          style: FilledButton.styleFrom(
            backgroundColor: Colors.red,
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          ),
          child: const Text('Confirm Cancellation', style: TextStyle(fontWeight: FontWeight.bold)),
        ),
      ],
    );
  }
}
