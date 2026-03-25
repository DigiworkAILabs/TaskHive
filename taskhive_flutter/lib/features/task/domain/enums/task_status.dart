import 'package:flutter/material.dart';
import 'package:json_annotation/json_annotation.dart';

enum TaskStatus {
  @JsonValue('TODO')
  todo,
  @JsonValue('IN_PROGRESS')
  inProgress,
  @JsonValue('IN_REVIEW')
  inReview,
  @JsonValue('PENDING_APPROVAL')
  pendingApproval,
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
      case TaskStatus.pendingApproval:
        return 'Pending Approval';
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
      case TaskStatus.pendingApproval:
        return 'PENDING_APPROVAL';
      case TaskStatus.done:
        return 'DONE';
      case TaskStatus.cancelled:
        return 'CANCELLED';
    }
  }

  Color get color {
    switch (this) {
      case TaskStatus.todo:
        return const Color(0xFFFF2200); // traffic red
      case TaskStatus.inProgress:
        return const Color(0xFFFCF005); // bright traffic amber
      case TaskStatus.inReview:
        return const Color(0xFF1E90FF); // bright dodger blue
      case TaskStatus.pendingApproval:
        return const Color(0xFFFF6600); // deep orange
      case TaskStatus.done:
        return const Color(0xFF00DD00); // traffic green
      case TaskStatus.cancelled:
        return const Color(0xFF3F3F46);
    }
  }

  /// Valid next statuses per business rules:
  /// TODO → IN_PROGRESS, CANCELLED
  /// IN_PROGRESS → IN_REVIEW, CANCELLED
  /// IN_REVIEW → DONE, PENDING_APPROVAL, IN_PROGRESS, CANCELLED
  /// PENDING_APPROVAL → DONE, IN_REVIEW, CANCELLED
  /// DONE → (terminal)
  /// CANCELLED → (terminal)
  List<TaskStatus> get allowedTransitions {
    switch (this) {
      case TaskStatus.todo:
        return [TaskStatus.inProgress, TaskStatus.cancelled];
      case TaskStatus.inProgress:
        return [TaskStatus.inReview, TaskStatus.cancelled];
      case TaskStatus.inReview:
        return [
          TaskStatus.done,
          TaskStatus.pendingApproval,
          TaskStatus.inProgress,
          TaskStatus.cancelled
        ];
      case TaskStatus.pendingApproval:
        return [TaskStatus.done, TaskStatus.inReview, TaskStatus.cancelled];
      case TaskStatus.done:
      case TaskStatus.cancelled:
        return [];
    }
  }

  bool get isTerminal =>
      this == TaskStatus.done || this == TaskStatus.cancelled;
}
