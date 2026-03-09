import 'package:flutter/material.dart';

/// Tag input field — shows existing tags as deletable Chips in a Wrap.
/// A hidden text field below chips adds new tags on Enter or comma.
class TagsInputField extends StatefulWidget {
  final List<String> initialTags;
  final ValueChanged<List<String>> onChanged;

  const TagsInputField({
    super.key,
    required this.initialTags,
    required this.onChanged,
  });

  @override
  State<TagsInputField> createState() => _TagsInputFieldState();
}

class _TagsInputFieldState extends State<TagsInputField> {
  late List<String> _tags;
  final _controller = TextEditingController();
  final _focusNode = FocusNode();

  @override
  void initState() {
    super.initState();
    _tags = List<String>.from(widget.initialTags);
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _addTag(String raw) {
    final trimmed = raw.trim().replaceAll(',', '');
    if (trimmed.isEmpty || _tags.contains(trimmed)) return;
    setState(() => _tags.add(trimmed));
    widget.onChanged(List.unmodifiable(_tags));
    _controller.clear();
  }

  void _removeTag(String tag) {
    setState(() => _tags.remove(tag));
    widget.onChanged(List.unmodifiable(_tags));
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text('Tags',
            style: TextStyle(fontWeight: FontWeight.w600, fontSize: 13)),
        const SizedBox(height: 6),
        Container(
          decoration: BoxDecoration(
            border: Border.all(color: Colors.grey.shade400),
            borderRadius: BorderRadius.circular(8),
          ),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (_tags.isNotEmpty)
                Wrap(
                  spacing: 6,
                  runSpacing: 4,
                  children: _tags.map((t) {
                    return Chip(
                      label: Text(t, style: const TextStyle(fontSize: 12)),
                      deleteIcon: const Icon(Icons.close, size: 14),
                      onDeleted: () => _removeTag(t),
                      visualDensity: VisualDensity.compact,
                    );
                  }).toList(),
                ),
              if (_tags.isNotEmpty) const SizedBox(height: 6),
              TextField(
                controller: _controller,
                focusNode: _focusNode,
                decoration: const InputDecoration(
                  hintText: 'Add tag and press Enter',
                  border: InputBorder.none,
                  isDense: true,
                  contentPadding: EdgeInsets.zero,
                ),
                onSubmitted: _addTag,
                onChanged: (v) {
                  if (v.endsWith(',')) _addTag(v);
                },
              ),
            ],
          ),
        ),
      ],
    );
  }
}
