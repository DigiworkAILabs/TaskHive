// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'productivity_score_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$ProductivityScoreResponseImpl _$$ProductivityScoreResponseImplFromJson(
        Map<String, dynamic> json) =>
    _$ProductivityScoreResponseImpl(
      score: (json['score'] as num).toDouble(),
      grade: json['grade'] as String,
      breakdown: (json['breakdown'] as Map<String, dynamic>).map(
        (k, e) => MapEntry(k, (e as num).toDouble()),
      ),
      trend: json['trend'] as String,
      reasoning: json['reasoning'] as String,
      fallbackUsed: json['fallbackUsed'] as bool,
    );

Map<String, dynamic> _$$ProductivityScoreResponseImplToJson(
        _$ProductivityScoreResponseImpl instance) =>
    <String, dynamic>{
      'score': instance.score,
      'grade': instance.grade,
      'breakdown': instance.breakdown,
      'trend': instance.trend,
      'reasoning': instance.reasoning,
      'fallbackUsed': instance.fallbackUsed,
    };
