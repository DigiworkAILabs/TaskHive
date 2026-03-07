import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';

/// Stub implementation
void setupCookieManager(Dio dio, CookieJar cookieJar) {
  throw UnsupportedError(
    'Cannot initialize cookie manager on this platform.',
  );
}
