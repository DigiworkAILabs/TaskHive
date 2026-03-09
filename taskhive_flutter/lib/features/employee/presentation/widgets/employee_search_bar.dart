import 'dart:async';
import 'package:flutter/material.dart';

import '../../../../../core/widgets/app_text_field.dart';

class EmployeeSearchBar extends StatefulWidget {
  final ValueChanged<String> onSearch;

  const EmployeeSearchBar({
    super.key,
    required this.onSearch,
  });

  @override
  State<EmployeeSearchBar> createState() => EmployeeSearchBarState();
}

class EmployeeSearchBarState extends State<EmployeeSearchBar> {
  final _controller = TextEditingController();
  Timer? _debounce;

  void clear() {
    _controller.clear();
    widget.onSearch('');
  }

  @override
  void initState() {
    super.initState();
    _controller.addListener(_onTextChanged);
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _controller.dispose();
    super.dispose();
  }

  void _onTextChanged() {
    if (_debounce?.isActive ?? false) _debounce?.cancel();
    // Re-render to show/hide the clear button
    setState(() {});
    _debounce = Timer(const Duration(milliseconds: 400), () {
      widget.onSearch(_controller.text);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: AppTextField(
        label: 'Search',
        controller: _controller,
        prefixIcon: const Icon(Icons.search),
        suffixIcon: _controller.text.isNotEmpty
            ? IconButton(
                icon: const Icon(Icons.clear),
                onPressed: () {
                  _controller.clear();
                  widget.onSearch('');
                },
              )
            : null,
      ),
    );
  }
}
