import 'dart:async';
import 'package:flutter/material.dart';
import '../../domain/enums/task_status.dart';

class LcdCountdownTimer extends StatefulWidget {
  final String dueDate;
  final TaskStatus status;

  const LcdCountdownTimer({
    super.key,
    required this.dueDate,
    required this.status,
  });

  @override
  State<LcdCountdownTimer> createState() => _LcdCountdownTimerState();
}

class _LcdCountdownTimerState extends State<LcdCountdownTimer> {
  late Timer _timer;
  late DateTime _due;
  Duration _remaining = Duration.zero;
  bool _overdue = false;

  @override
  void initState() {
    super.initState();
    _due = DateTime.parse(widget.dueDate);
    _updateRemaining();
    _timer = Timer.periodic(const Duration(seconds: 1), (_) => _updateRemaining());
  }

  @override
  void dispose() {
    _timer.cancel();
    super.dispose();
  }

  void _updateRemaining() {
    final now = DateTime.now();
    final diff = _due.difference(now);
    setState(() {
      _remaining = diff;
      _overdue = diff.isNegative;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (widget.status == TaskStatus.done || widget.status == TaskStatus.cancelled) {
      return const SizedBox.shrink();
    }

    final totalMs = _remaining.inMilliseconds;
    final isCritical = !_overdue && totalMs <= 1000 * 60 * 60; // < 1 hour
    final isWarning = !_overdue && totalMs <= 1000 * 60 * 60 * 24; // < 24 hours

    Color digitColor = const Color(0xFFFBBC24); // Gold (Safe)
    Color glowColor = const Color(0x40FBBC1A);
    Color dimColor = const Color(0x14FBBC1A);

    if (_overdue) {
      digitColor = const Color(0xFFFF4D4D);
      glowColor = const Color(0x59FF4D4D);
      dimColor = const Color(0x1AFF4D4D);
    } else if (isCritical) {
      digitColor = const Color(0xFFEF4444);
      glowColor = const Color(0x59EF4444);
      dimColor = const Color(0x1AEF4444);
    } else if (isWarning) {
      digitColor = const Color(0xFFF97316);
      glowColor = const Color(0x4DF97316);
      dimColor = const Color(0x14F97316);
    }

    final absRemaining = _remaining.abs();
    final days = absRemaining.inDays;
    final hours = absRemaining.inHours % 24;
    final minutes = absRemaining.inMinutes % 60;
    final seconds = absRemaining.inSeconds % 60;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: const Color(0xDA0C0C0C),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: digitColor.withValues(alpha: 0.2),
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: glowColor,
            blurRadius: 10,
            spreadRadius: 1,
          ),
          const BoxShadow(
            color: Colors.black54,
            offset: Offset(0, 4),
            blurRadius: 10,
          ),
        ],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (_overdue)
            Padding(
              padding: const EdgeInsets.only(right: 8.0),
              child: Text(
                'LATE',
                style: TextStyle(
                  color: digitColor,
                  fontSize: 10,
                  fontWeight: FontWeight.w900,
                  fontFamily: 'monospace',
                  letterSpacing: 1,
                ),
              ),
            )
          else
            Icon(Icons.timer_outlined, color: digitColor, size: 16),
          const SizedBox(width: 8),
          if (days > 0) ...[
            _DigitGroup(
              value: days.toString(),
              label: 'DAYS',
              color: digitColor,
              dimColor: dimColor,
            ),
            _Separator(color: digitColor),
          ],
          _DigitGroup(
            value: hours.toString().padLeft(2, '0'),
            label: 'HRS',
            color: digitColor,
            dimColor: dimColor,
          ),
          _Separator(color: digitColor),
          _DigitGroup(
            value: minutes.toString().padLeft(2, '0'),
            label: 'MINS',
            color: digitColor,
            dimColor: dimColor,
          ),
          _Separator(color: digitColor),
          _DigitGroup(
            value: seconds.toString().padLeft(2, '0'),
            label: 'SECS',
            color: digitColor,
            dimColor: dimColor,
          ),
        ],
      ),
    );
  }
}

class _DigitGroup extends StatelessWidget {
  final String value;
  final String label;
  final Color color;
  final Color dimColor;

  const _DigitGroup({
    required this.value,
    required this.label,
    required this.color,
    required this.dimColor,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Stack(
          children: [
            // Ghost 88
            Text(
              '8' * value.length,
              style: TextStyle(
                color: dimColor,
                fontSize: 18,
                fontWeight: FontWeight.w900,
                fontFamily: 'monospace',
                letterSpacing: 1,
              ),
            ),
            // Actual value
            Text(
              value,
              style: TextStyle(
                color: color,
                fontSize: 18,
                fontWeight: FontWeight.w900,
                fontFamily: 'monospace',
                letterSpacing: 1,
                shadows: [
                  Shadow(color: color.withValues(alpha: 0.5), blurRadius: 8),
                ],
              ),
            ),
          ],
        ),
        Text(
          label,
          style: TextStyle(
            color: color.withValues(alpha: 0.7),
            fontSize: 7,
            fontWeight: FontWeight.w800,
            fontFamily: 'monospace',
          ),
        ),
      ],
    );
  }
}

class _Separator extends StatelessWidget {
  final Color color;

  const _Separator({required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      margin: const EdgeInsets.only(bottom: 10),
      child: Text(
        ':',
        style: TextStyle(
          color: color,
          fontSize: 16,
          fontWeight: FontWeight.w900,
          fontFamily: 'monospace',
        ),
      ),
    );
  }
}
