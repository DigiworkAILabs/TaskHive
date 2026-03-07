import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';

/// Web implementation: browsers manage cookies automatically,
/// so we do NOT add the cookie manager interceptor.
void setupCookieManager(Dio dio, CookieJar cookieJar) {
  // No-op on web
}
