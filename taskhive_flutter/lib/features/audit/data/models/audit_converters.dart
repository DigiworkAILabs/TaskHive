import 'dart:convert';
import 'package:freezed_annotation/freezed_annotation.dart';
import '../../../../core/utils/logger.dart';

/// A converter that safely transforms a JSON [String] or [Map] into a [Map<String, dynamic>?].
/// This is necessary because the backend returns some fields (like beforeState, afterState, details)
/// as JSON strings instead of nested objects.
class AuditStateConverter
    implements JsonConverter<Map<String, dynamic>?, Object?> {
  const AuditStateConverter();

  @override
  Map<String, dynamic>? fromJson(Object? json) {
    if (json == null) return null;

    if (json is Map<String, dynamic>) {
      return json;
    }

    if (json is String) {
      if (json.isEmpty) return null;
      try {
        final decoded = jsonDecode(json);
        if (decoded is Map<String, dynamic>) {
          return decoded;
        }
        // Handle list or other types if necessary, but project expects Map
        return null;
      } catch (e) {
        appLogger.e('AuditStateConverter: Failed to decode JSON string: $e');
        return null;
      }
    }

    return null;
  }

  @override
  Object? toJson(Map<String, dynamic>? object) => object;
}
