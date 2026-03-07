import 'dart:io';
import 'package:cookie_jar/cookie_jar.dart';
import 'package:path_provider/path_provider.dart';

// Used on Android, iOS, macOS, Windows, Linux
Future<CookieJar> createCookieJar() async {
  final appDocDir = await getApplicationDocumentsDirectory();
  final cookiePath = '${appDocDir.path}/.cookies/';
  await Directory(cookiePath).create(recursive: true);
  return PersistCookieJar(
    ignoreExpires: false,
    storage: FileStorage(cookiePath),
  );
}
