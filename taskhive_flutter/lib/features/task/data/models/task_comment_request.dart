import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_comment_request.freezed.dart';
part 'task_comment_request.g.dart';

@freezed
class TaskCommentRequest with _$TaskCommentRequest {
  const factory TaskCommentRequest({
    required String content,
  }) = _TaskCommentRequest;

  factory TaskCommentRequest.fromJson(Map<String, dynamic> json) =>
      _$TaskCommentRequestFromJson(json);
}
