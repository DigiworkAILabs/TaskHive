import 'package:cookie_jar/cookie_jar.dart';

// Fallback stub — never actually called at runtime.
// Required by Dart's conditional import system for static analysis.
Future<CookieJar> createCookieJar() async {
  return DefaultCookieJar();
}
