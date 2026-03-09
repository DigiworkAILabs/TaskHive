import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';

enum TaskStatus {
  @JsonValue('TODO')
  todo,
  @JsonValue('IN_PROGRESS')
  inProgress,
  @JsonValue('IN_REVIEW')
  inReview,
  @JsonValue('DONE')
  done,
  @JsonValue('CANCELLED')
  cancelled,
}

extension TaskStatusX on TaskStatus {
  String get label {
    switch (this) {
      case TaskStatus.todo:
        return 'To Do';
      case TaskStatus.inProgress:
        return 'In Progress';
      case TaskStatus.inReview:
        return 'In Review';
      case TaskStatus.done:
        return 'Done';
      case TaskStatus.cancelled:
        return 'Cancelled';
    }
  }

  String get backendValue {
    switch (this) {
      case TaskStatus.todo:
        return 'TODO';
      case TaskStatus.inProgress:
        return 'IN_PROGRESS';
      case TaskStatus.inReview:
        return 'IN_REVIEW';
      case TaskStatus.done:
        return 'DONE';
      case TaskStatus.cancelled:
        return 'CANCELLED';
    }
  }

  Color get color {
    switch (this) {
      case TaskStatus.todo:
        return Colors.grey;
      case TaskStatus.inProgress:
        return Colors.blue;
      case TaskStatus.inReview:
        return Colors.orange;
      case TaskStatus.done:
        return Colors.green;
      case TaskStatus.cancelled:
        return Colors.red;
    }
  }

  /// Valid next statuses per business rules:
  /// TODO → IN_PROGRESS, CANCELLED
  /// IN_PROGRESS → IN_REVIEW, CANCELLED
  /// IN_REVIEW → DONE, IN_PROGRESS, CANCELLED
  /// DONE → (terminal)
  /// CANCELLED → (terminal)
  List<TaskStatus> get allowedTransitions {
    switch (this) {
      case TaskStatus.todo:
        return [TaskStatus.inProgress, TaskStatus.cancelled];
      case TaskStatus.inProgress:
        return [TaskStatus.inReview, TaskStatus.cancelled];
      case TaskStatus.inReview:
        return [TaskStatus.done, TaskStatus.inProgress, TaskStatus.cancelled];
      case TaskStatus.done:
      case TaskStatus.cancelled:
        return [];
    }
  }

  bool get isTerminal =>
      this == TaskStatus.done || this == TaskStatus.cancelled;
}
