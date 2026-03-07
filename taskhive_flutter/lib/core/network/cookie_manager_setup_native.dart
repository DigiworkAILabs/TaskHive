import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';

/// Native implementation: adds the dio_cookie_manager interceptor.
void setupCookieManager(Dio dio, CookieJar cookieJar) {
  dio.interceptors.add(CookieManager(cookieJar));
}
