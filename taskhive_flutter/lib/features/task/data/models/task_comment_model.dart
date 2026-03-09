import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_comment_model.freezed.dart';
part 'task_comment_model.g.dart';

@freezed
class TaskCommentModel with _$TaskCommentModel {
  const factory TaskCommentModel({
    required String id,
    required String taskId,
    required String authorId,
    required String authorName,
    required String content,
    String? createdAt,
  }) = _TaskCommentModel;

  factory TaskCommentModel.fromJson(Map<String, dynamic> json) =>
      _$TaskCommentModelFromJson(json);
}
