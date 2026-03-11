import 'package:flutter/material.dart';

class JsonDiffViewer extends StatelessWidget {
  final Map<String, dynamic>? before;
  final Map<String, dynamic>? after;

  const JsonDiffViewer({
    super.key,
    this.before,
    this.after,
  });

  @override
  Widget build(BuildContext context) {
    // If both null, nothing to show
    if (before == null && after == null) {
      return const SizedBox.shrink();
    }

    final Set<String> allKeys = {};
    if (before != null) allKeys.addAll(before!.keys);
    if (after != null) allKeys.addAll(after!.keys);

    final sortedKeys = allKeys.toList()..sort();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: sortedKeys.map((key) {
        final valBefore = before?[key];
        final valAfter = after?[key];

        // Did it change?
        final bool isAdded = valBefore == null && valAfter != null;
        final bool isRemoved = valBefore != null && valAfter == null;
        final bool isChanged = valBefore != valAfter && !isAdded && !isRemoved;

        if (isAdded) {
          return _buildRow(key, '+', null, valAfter.toString(), Colors.green);
        } else if (isRemoved) {
          return _buildRow(key, '-', valBefore.toString(), null, Colors.red);
        } else if (isChanged) {
          return _buildChangedRow(
              key, valBefore.toString(), valAfter.toString());
        }

        // Unchanged
        return _buildRow(
            key, ' ', null, valBefore.toString(), Colors.grey.shade700);
      }).toList(),
    );
  }

  Widget _buildRow(
      String key, String prefix, String? oldVal, String? newVal, Color color) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2.0),
      child: RichText(
        text: TextSpan(
          style: const TextStyle(
              fontFamily: 'monospace', fontSize: 13, color: Colors.black87),
          children: [
            TextSpan(
              text: '$prefix "$key": ',
              style: TextStyle(fontWeight: FontWeight.bold, color: color),
            ),
            if (oldVal != null)
              TextSpan(
                text: oldVal,
                style: TextStyle(
                  color: color,
                  decoration: TextDecoration.lineThrough,
                ),
              ),
            if (newVal != null)
              TextSpan(
                text: newVal,
                style: TextStyle(color: color),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildChangedRow(String key, String oldVal, String newVal) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 2.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          RichText(
            text: TextSpan(
              style: const TextStyle(
                  fontFamily: 'monospace', fontSize: 13, color: Colors.black87),
              children: [
                TextSpan(
                  text: '- "$key": ',
                  style: const TextStyle(
                      fontWeight: FontWeight.bold, color: Colors.red),
                ),
                TextSpan(
                  text: oldVal,
                  style: const TextStyle(
                      color: Colors.red,
                      decoration: TextDecoration.lineThrough),
                ),
              ],
            ),
          ),
          RichText(
            text: TextSpan(
              style: const TextStyle(
                  fontFamily: 'monospace', fontSize: 13, color: Colors.black87),
              children: [
                TextSpan(
                  text: '+ "$key": ',
                  style: const TextStyle(
                      fontWeight: FontWeight.bold, color: Colors.green),
                ),
                TextSpan(
                  text: newVal,
                  style: const TextStyle(color: Colors.green),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
