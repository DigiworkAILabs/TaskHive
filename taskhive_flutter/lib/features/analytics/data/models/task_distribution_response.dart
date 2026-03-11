import 'package:freezed_annotation/freezed_annotation.dart';

part 'task_distribution_response.freezed.dart';
part 'task_distribution_response.g.dart';

@freezed
class TaskDistributionResponse with _$TaskDistributionResponse {
  const factory TaskDistributionResponse({
    String? status,
    int? count,
  }) = _TaskDistributionResponse;

  factory TaskDistributionResponse.fromJson(Map<String, dynamic> json) =>
      _$TaskDistributionResponseFromJson(json);
}
