import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_completion_trend_response.freezed.dart';
part 'task_completion_trend_response.g.dart';

@freezed
class TaskCompletionTrendResponse with _$TaskCompletionTrendResponse {
  const factory TaskCompletionTrendResponse({
    String? date,
    int? count,
  }) = _TaskCompletionTrendResponse;

  factory TaskCompletionTrendResponse.fromJson(Map<String, dynamic> json) =>
      _$TaskCompletionTrendResponseFromJson(json);
}
