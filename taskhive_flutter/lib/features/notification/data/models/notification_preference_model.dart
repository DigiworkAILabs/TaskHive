import 'package:freezed_annotation/freezed_annotation.dart';

part 'notification_preference_model.freezed.dart';
part 'notification_preference_model.g.dart';

@freezed
class NotificationPreferenceModel with _$NotificationPreferenceModel {
  const factory NotificationPreferenceModel({
    @Default(true) bool emailEnabled,
    @Default(true) bool inAppEnabled,
    @Default(true) bool taskAssigned,
    @Default(true) bool taskOverdue,
    @Default(false) bool dailyDigest,
    String? digestTime,
  }) = _NotificationPreferenceModel;

  factory NotificationPreferenceModel.fromJson(Map<String, dynamic> json) =>
      _$NotificationPreferenceModelFromJson(json);
}
