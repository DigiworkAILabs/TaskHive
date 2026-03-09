import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../features/auth/domain/providers/auth_provider.dart';
import 'auth_interceptor.dart';

import 'cookie_manager_setup_stub.dart'
    if (dart.library.io) 'cookie_manager_setup_native.dart'
    if (dart.library.html) 'cookie_manager_setup_web.dart';

/// Holds the CookieJar instance. Overridden in main.dart via ProviderScope.
/// The override happens BEFORE any provider reads this — no race condition.
final cookieJarProvider = Provider<CookieJar>((ref) {
  throw UnimplementedError(
    'cookieJarProvider must be overridden in main.dart.\n'
    'Add cookieJarProvider.overrideWithValue(cookieJar) to ProviderScope overrides.',
  );
});

/// The central Dio instance. All feature repositories use this.
final Provider<Dio> dioClientProvider = Provider<Dio>((ref) {
  final cookieJar = ref.watch(cookieJarProvider);
  final baseUrl = dotenv.env['API_BASE_URL'] ?? 'http://10.0.2.2:8080/api/v1';

  final dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 15),
      sendTimeout: const Duration(seconds: 15),
      headers: const {'Content-Type': 'application/json'},
      // Required for Web browsers to attach cross-origin cookies.
      extra: const {'withCredentials': true},
    ),
  );

  // Platform-specific cookie manager setup (does nothing on Web)
  setupCookieManager(dio, cookieJar);

  // AuthInterceptor uses a callback to avoid circular dependency:
  // dioClientProvider → authStateNotifierProvider → dioClientProvider
  // The callback uses ref.read (lazy, not reactive) — safe because it is
  // only invoked during a 401 event, well after all providers are built.
  dio.interceptors.add(
    AuthInterceptor(
      dio: dio,
      cookieJar: cookieJar,
      onForceLogout: () =>
          ref.read(authStateNotifierProvider.notifier).forceLogout(),
      refreshEndpoint: '/auth/refresh',
    ),
  );

  return dio;
});
