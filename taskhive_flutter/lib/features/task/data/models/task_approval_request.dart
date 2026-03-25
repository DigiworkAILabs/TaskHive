import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_approval_request.freezed.dart';
part 'task_approval_request.g.dart';

@freezed
class TaskApprovalRequest with _$TaskApprovalRequest {
  const factory TaskApprovalRequest({
    required String reason,
  }) = _TaskApprovalRequest;

  factory TaskApprovalRequest.fromJson(Map<String, dynamic> json) =>
      _$TaskApprovalRequestFromJson(json);
}
