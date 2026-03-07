import 'package:cookie_jar/cookie_jar.dart';

// Used on Web — browser manages cookie persistence automatically
// via Set-Cookie response headers. DefaultCookieJar is a no-op pass-through.
Future<CookieJar> createCookieJar() async {
  return DefaultCookieJar();
}
