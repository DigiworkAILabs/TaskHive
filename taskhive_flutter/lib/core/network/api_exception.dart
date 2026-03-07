class ApiException implements Exception {
  final String message;
  final String? errorCode;
  final int? statusCode;
  final Map<String, dynamic>? data;

  const ApiException({
    required this.message,
    this.errorCode,
    this.statusCode,
    this.data,
  });

  factory ApiException.fromDioError(dynamic error) {
    if (error?.response != null) {
      final responseData = error.response!.data;
      final message = (responseData is Map ? responseData['message'] : null) ??
          'An error occurred';
      final errorCode = responseData is Map ? responseData['errorCode'] : null;
      final extraData = responseData is Map ? responseData['data'] : null;
      return ApiException(
        message: message,
        errorCode: errorCode,
        statusCode: error.response!.statusCode,
        data: extraData is Map ? Map<String, dynamic>.from(extraData) : null,
      );
    }
    final type = error?.type?.toString() ?? '';
    if (type.contains('connectTimeout') || type.contains('receiveTimeout')) {
      return const ApiException(
        message: 'Connection timed out. Please check your network.',
        errorCode: 'TIMEOUT',
      );
    }
    return const ApiException(
      message: 'Network error. Please check your connection.',
      errorCode: 'NETWORK_ERROR',
    );
  }

  bool get isUnauthorized => statusCode == 401;
  bool get isAccountLocked => errorCode == 'ACCOUNT_LOCKED';
  bool get isTokenExpired => errorCode == 'TOKEN_EXPIRED';
  bool get isTokenAlreadyUsed => errorCode == 'TOKEN_ALREADY_USED';
  bool get isPasswordHistoryViolation =>
      errorCode == 'PASSWORD_HISTORY_VIOLATION';

  @override
  String toString() => 'ApiException($statusCode): $message [code: $errorCode]';
}
