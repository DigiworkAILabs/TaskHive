import 'package:freezed_annotation/freezed_annotation.dart';

part 'productivity_score_response.freezed.dart';
part 'productivity_score_response.g.dart';

@freezed
class ProductivityScoreResponse with _$ProductivityScoreResponse {
  const factory ProductivityScoreResponse({
    required double score,
    required String grade,
    required Map<String, double> breakdown,
    required String trend,
    required String reasoning,
    required bool fallbackUsed,
  }) = _ProductivityScoreResponse;

  factory ProductivityScoreResponse.fromJson(Map<String, dynamic> json) =>
      _$ProductivityScoreResponseFromJson(json);
}
