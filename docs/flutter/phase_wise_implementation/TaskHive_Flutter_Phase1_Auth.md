# TaskHive Flutter — Phase 1: Auth Module + Core Infrastructure
## Version 3 | SRS v2.4 | Digiwork
## ⚠️ Always provide `TaskHive_Flutter_Master_Context.md` alongside this file

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 1 of 6 |
| **Name** | Auth Module + Project Foundation |
| **Scope** | Core infrastructure, conditional imports, theme, auth screens, shell scaffolds |
| **Depends On** | Nothing — foundation phase |
| **Platforms** | Android ✅ iOS ✅ macOS ✅ Web ✅ Windows ✅ Linux ✅ |

### Deliverable
Flutter project compiles and runs on all 6 platforms with zero errors. Login, forgot password, reset password (deeplink), account activation (deeplink), change password, logout all work. Cookie-based JWT auth functioning. Role-based routing working (ADMIN shell / EMPLOYEE shell). Session restored on app restart without login flash.

---

## Phase 1 — Files To Create (48 files)

### Complete Folder Structure for This Phase

```
taskhive-flutter/
│
├── android/app/src/main/
│   └── AndroidManifest.xml                    # ★ FROM MASTER CONTEXT — copy exactly
│
├── ios/Runner/
│   ├── Info.plist                             # ★ FROM MASTER CONTEXT — copy exactly
│   └── AppDelegate.swift                      # ★ FROM MASTER CONTEXT — copy exactly
│
├── macos/Runner/
│   ├── DebugProfile.entitlements              # ★ FROM MASTER CONTEXT — copy exactly
│   └── Release.entitlements                   # ★ FROM MASTER CONTEXT — copy exactly
│
├── assets/
│   ├── images/
│   │   ├── logo.png                           # Provide manually
│   │   └── logo_dark.png                      # Provide manually
│   └── env/
│       ├── .env.dev                           # ★ FROM MASTER CONTEXT
│       └── .env.prod                          # ★ FROM MASTER CONTEXT
│
├── lib/
│   ├── main.dart                              # ★ Full implementation below
│   ├── app.dart                               # ★ Full implementation below
│   │
│   ├── core/
│   │   ├── network/
│   │   │   ├── api_endpoints.dart             # ★ Full implementation below
│   │   │   ├── api_exception.dart             # ★ Full implementation below
│   │   │   ├── dio_client.dart                # ★ Full implementation below
│   │   │   ├── cookie_jar_factory_stub.dart   # ★ Full implementation below
│   │   │   ├── cookie_jar_factory_native.dart # ★ Full implementation below
│   │   │   ├── cookie_jar_factory_web.dart    # ★ Full implementation below
│   │   │   ├── auth_interceptor.dart          # ★ Full implementation below
│   │   │   └── stomp_client.dart              # STUB — implementation below
│   │   │
│   │   ├── storage/
│   │   │   ├── secure_storage_service.dart    # ★ Full implementation below
│   │   │   └── cache_service.dart             # ★ Full implementation below
│   │   │
│   │   ├── connectivity/
│   │   │   └── connectivity_provider.dart     # ★ Full implementation below
│   │   │
│   │   ├── router/
│   │   │   ├── app_router.dart                # ★ Full implementation below
│   │   │   └── app_routes.dart                # ★ Full implementation below
│   │   │
│   │   ├── services/
│   │   │   ├── fcm_service.dart               # STUB — implementation below
│   │   │   └── notification_navigation_service.dart  # STUB — implementation below
│   │   │
│   │   ├── theme/
│   │   │   ├── app_theme.dart                 # ★ Full implementation below
│   │   │   ├── app_colors.dart                # ★ Full implementation below
│   │   │   └── app_text_styles.dart           # ★ Full implementation below
│   │   │
│   │   ├── utils/
│   │   │   ├── date_utils.dart                # Spec below
│   │   │   ├── string_utils.dart              # Spec below
│   │   │   └── logger.dart                    # ★ Full implementation below
│   │   │
│   │   └── widgets/
│   │       ├── app_button.dart                # Spec below
│   │       ├── app_text_field.dart            # Spec below
│   │       ├── app_loading.dart               # Spec below
│   │       ├── app_error_widget.dart          # Spec below
│   │       ├── app_empty_state.dart           # Spec below
│   │       ├── app_snackbar.dart              # Spec below
│   │       ├── app_confirm_dialog.dart        # Spec below
│   │       ├── app_badge.dart                 # Spec below
│   │       └── stat_card.dart                 # STUB — implementation below
│   │
│   └── features/
│       ├── auth/
│       │   ├── data/
│       │   │   ├── models/
│       │   │   │   ├── user_model.dart        # ★ Full implementation below
│       │   │   │   ├── login_request.dart     # ★ Full implementation below
│       │   │   │   └── auth_response.dart     # ★ Full implementation below
│       │   │   └── repositories/
│       │   │       └── auth_repository.dart   # ★ Full implementation below
│       │   ├── domain/
│       │   │   └── providers/
│       │   │       ├── auth_provider.dart     # ★ Full implementation below
│       │   │       └── current_user_provider.dart  # ★ Full implementation below
│       │   └── presentation/
│       │       ├── screens/
│       │       │   ├── login_screen.dart      # ★ Full implementation below
│       │       │   ├── forgot_password_screen.dart   # Spec below
│       │       │   ├── reset_password_screen.dart    # Spec below
│       │       │   ├── activate_account_screen.dart  # Spec below
│       │       │   └── change_password_screen.dart   # Spec below
│       │       └── widgets/
│       │           ├── login_form.dart        # Spec below
│       │           ├── forgot_password_form.dart
│       │           ├── reset_password_form.dart
│       │           ├── activate_account_form.dart
│       │           └── change_password_form.dart
│       │
│       ├── admin/
│       │   └── presentation/screens/
│       │       └── admin_shell_screen.dart    # ★ Full implementation below
│       │
│       └── employee/
│           └── presentation/screens/
│               └── employee_shell_screen.dart # ★ Full implementation below
│
├── test/
│   └── features/auth/
│       ├── auth_repository_test.dart          # Spec below
│       ├── auth_provider_test.dart            # Spec below
│       └── login_screen_test.dart             # Spec below
│
├── pubspec.yaml                               # ★ FROM MASTER CONTEXT — copy exactly
├── analysis_options.yaml                      # ★ FROM MASTER CONTEXT — copy exactly
├── build.yaml                                 # ★ FROM MASTER CONTEXT — copy exactly
└── README.md                                  # Write basic project description
```

> Files NOT listed here (Phase 2–6 features) do NOT exist yet. Do not create them.
> The `employee/` feature folder only gets `employee_shell_screen.dart` in Phase 1.
> All other employee files (models, repository, providers, screens) are Phase 2.

---

## API Endpoints Used in Phase 1

```
POST   /api/v1/auth/login               → Sets accessToken + refreshToken HttpOnly cookies
POST   /api/v1/auth/logout              → Revokes refreshToken, clears all cookies
POST   /api/v1/auth/refresh             → Issues new accessToken (auto-called by interceptor)
POST   /api/v1/auth/forgot-password     → Body: {email} | Always 200
POST   /api/v1/auth/reset-password      → Body: {token, newPassword}
POST   /api/v1/auth/activate-account    → Body: {token, password}
POST   /api/v1/auth/change-password     → Body: {currentPassword, newPassword}
GET    /api/v1/auth/me                  → Returns current user info
```

### Response Shapes Used in Phase 1

```json
// POST /api/v1/auth/login — 200 OK
// Server sets cookies in response headers automatically
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "admin@taskhive.com",
    "firstName": "System",
    "lastName": "Admin",
    "role": "ADMIN"
  },
  "message": "Login successful"
}

// GET /api/v1/auth/me — 200 OK (same data shape)
// POST /api/v1/auth/logout — 200 OK (data: null)
// POST /api/v1/auth/forgot-password — 200 OK always (data: null)
// POST /api/v1/auth/reset-password — 200 OK (data: null)
// POST /api/v1/auth/activate-account — 200 OK (data: null)
// POST /api/v1/auth/change-password — 200 OK (data: null)

// All errors:
{
  "success": false,
  "message": "Error description",
  "errorCode": "ACCOUNT_LOCKED",
  "data": { "lockedUntil": "2026-03-05T10:15:00Z" }  // only for ACCOUNT_LOCKED
}
```

---

## Functional Requirements Covered in Phase 1

| ID | Requirement |
|----|-------------|
| FR-AUTH-01 | Admin exists on backend by default; Flutter handles login only |
| FR-AUTH-02 | Login with email + password |
| FR-AUTH-03 | Cookie jar stores accessToken + refreshToken from server response |
| FR-AUTH-04 | Access token auto-refreshed on 401 by Dio interceptor |
| FR-AUTH-05 | Refresh token sent automatically via cookie jar on /auth/refresh |
| FR-AUTH-06 | All requests carry cookie jar (equivalent of withCredentials: true) |
| FR-AUTH-07 | On 401, interceptor calls /auth/refresh, retries original on success |
| FR-AUTH-08 | On refresh failure → force logout → login screen |
| FR-AUTH-09 | Account lockout — show lockout error with time remaining from backend |
| FR-AUTH-10 | Activation email deeplink opens Flutter app on activate-account screen |
| FR-AUTH-11 | Activation token single-use 24hr — handle TOKEN_EXPIRED + TOKEN_ALREADY_USED errors |
| FR-AUTH-12 | Reset token single-use 1hr — handle TOKEN_EXPIRED + TOKEN_ALREADY_USED errors |
| FR-AUTH-13 | Forgot password always shows success (no email enumeration) |
| FR-AUTH-14 | Logout: clear cookie jar + secure storage + Riverpod state |
| FR-AUTH-15 | Password change revokes all sessions — force logout after success |
| FR-AUTH-16 | Flutter sends plain password over HTTPS; backend BCrypts it |
| FR-AUTH-17 | PASSWORD_HISTORY_VIOLATION error shown as snackbar from API message |
| FR-AUTH-18 | ADMIN → AdminShellScreen | EMPLOYEE → EmployeeShellScreen |
| FR-AUTH-19 | Session restored on app restart via secureStorage + /auth/me verify |
| FR-AUTH-20 | SplashScreen shown during session restore (isLoading guard in router) |

---

## ★ main.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';

import 'app.dart';
import 'core/network/dio_client.dart';

// Conditional import — safe on ALL platforms including web
import 'core/network/cookie_jar_factory_stub.dart'
    if (dart.library.io) 'core/network/cookie_jar_factory_native.dart'
    if (dart.library.html) 'core/network/cookie_jar_factory_web.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // 1. Load environment variables (change to .env.prod for production)
  await dotenv.load(fileName: 'assets/env/.env.dev');

  // 2. Initialize Hive offline cache — open all boxes before runApp
  await Hive.initFlutter();
  await Future.wait([
    Hive.openBox('employees_box'),
    Hive.openBox('tasks_box'),
    Hive.openBox('my_tasks_box'),
    Hive.openBox('notifications_box'),
    Hive.openBox('analytics_box'),
  ]);

  // 3. Create cookie jar BEFORE ProviderScope — async, platform-aware
  // createCookieJar() comes from the conditional import above
  final cookieJar = await createCookieJar();

  // 4. Run app — inject cookieJar as a ProviderScope override
  runApp(
    ProviderScope(
      overrides: [
        cookieJarProvider.overrideWithValue(cookieJar),
      ],
      child: const TaskHiveApp(),
    ),
  );
}
```

---

## ★ app.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';
import 'core/connectivity/connectivity_provider.dart';

class TaskHiveApp extends ConsumerWidget {
  const TaskHiveApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);
    // AsyncValue<bool> — use whenData to safely extract the bool
    final connectivityAsync = ref.watch(connectivityProvider);
    final isOnline = connectivityAsync.valueOrNull ?? true;

    return MaterialApp.router(
      title: 'TaskHive',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      themeMode: ThemeMode.system,
      routerConfig: router,
      builder: (context, child) {
        return Column(
          children: [
            if (!isOnline)
              Material(
                color: Colors.amber.shade700,
                child: SafeArea(
                  bottom: false,
                  child: SizedBox(
                    width: double.infinity,
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 6),
                      child: Text(
                        "You're offline — showing cached data",
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 12,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            Expanded(child: child ?? const SizedBox.shrink()),
          ],
        );
      },
    );
  }
}
```

---

## ★ core/network/cookie_jar_factory_stub.dart

```dart
import 'package:cookie_jar/cookie_jar.dart';

// Fallback stub — never actually called at runtime.
// Required by Dart's conditional import system for static analysis.
Future<CookieJar> createCookieJar() async {
  return DefaultCookieJar();
}
```

## ★ core/network/cookie_jar_factory_native.dart

```dart
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
```

## ★ core/network/cookie_jar_factory_web.dart

```dart
import 'package:cookie_jar/cookie_jar.dart';

// Used on Web — browser manages cookie persistence automatically
// via Set-Cookie response headers. DefaultCookieJar is a no-op pass-through.
Future<CookieJar> createCookieJar() async {
  return DefaultCookieJar();
}
```

---

## ★ core/network/dio_client.dart

```dart
import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';
import 'package:dio_cookie_manager/dio_cookie_manager.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../features/auth/domain/providers/auth_provider.dart';
import 'auth_interceptor.dart';

/// Holds the CookieJar instance. Overridden in main.dart via ProviderScope.
/// The override happens BEFORE any provider reads this — no race condition.
final cookieJarProvider = Provider<CookieJar>((ref) {
  throw UnimplementedError(
    'cookieJarProvider must be overridden in main.dart.\n'
    'Add cookieJarProvider.overrideWithValue(cookieJar) to ProviderScope overrides.',
  );
});

/// The central Dio instance. All feature repositories use this.
final dioClientProvider = Provider<Dio>((ref) {
  final cookieJar = ref.watch(cookieJarProvider);
  final baseUrl = dotenv.env['API_BASE_URL'] ?? 'http://10.0.2.2:8080/api/v1';

  final dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 15),
      sendTimeout: const Duration(seconds: 15),
      headers: const {'Content-Type': 'application/json'},
    ),
  );

  // CookieManager MUST be added before AuthInterceptor
  dio.interceptors.add(CookieManager(cookieJar));

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
```

---

## ★ core/network/auth_interceptor.dart

```dart
import 'dart:async';

import 'package:cookie_jar/cookie_jar.dart';
import 'package:dio/dio.dart';

typedef VoidCallback = void Function();

/// Handles 401 responses by refreshing the access token.
/// Does NOT hold a Riverpod ref — uses a callback to prevent circular dependency.
class AuthInterceptor extends Interceptor {
  final Dio _dio;
  final CookieJar _cookieJar;
  final VoidCallback onForceLogout;
  final String refreshEndpoint;

  bool _isRefreshing = false;
  final List<_PendingRequest> _queue = [];

  AuthInterceptor({
    required Dio dio,
    required CookieJar cookieJar,
    required this.onForceLogout,
    required this.refreshEndpoint,
  })  : _dio = dio,
        _cookieJar = cookieJar;

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    // Only intercept 401s — skip the refresh endpoint itself (infinite loop guard)
    if (err.response?.statusCode != 401 ||
        err.requestOptions.path.contains(refreshEndpoint)) {
      return handler.next(err);
    }

    final originalRequest = err.requestOptions;

    // Another refresh already in flight — queue this request
    if (_isRefreshing) {
      final completer = Completer<void>();
      _queue.add(_PendingRequest(completer: completer, options: originalRequest));
      try {
        await completer.future;
        final retried = await _retry(originalRequest);
        return handler.resolve(retried);
      } catch (_) {
        return handler.next(err);
      }
    }

    _isRefreshing = true;

    try {
      // refreshToken cookie is in the jar — sent automatically by CookieManager
      await _dio.post(refreshEndpoint);

      // Refresh succeeded — unblock all queued requests
      for (final pending in _queue) {
        pending.completer.complete();
      }
      _queue.clear();

      final retried = await _retry(originalRequest);
      handler.resolve(retried);
    } catch (_) {
      // Refresh failed — reject queue and force logout
      for (final pending in _queue) {
        pending.completer.completeError('Session expired');
      }
      _queue.clear();
      await _cookieJar.deleteAll();
      onForceLogout(); // Notifies AuthNotifier → router redirects to login
      handler.next(err);
    } finally {
      _isRefreshing = false;
    }
  }

  Future<Response<dynamic>> _retry(RequestOptions options) {
    return _dio.request(
      options.path,
      data: options.data,
      queryParameters: options.queryParameters,
      options: Options(method: options.method, headers: options.headers),
    );
  }
}

class _PendingRequest {
  final Completer<void> completer;
  final RequestOptions options;
  _PendingRequest({required this.completer, required this.options});
}
```

---

## ★ core/network/api_endpoints.dart

```dart
/// All API endpoint paths for the entire app (all phases).
/// Declared in Phase 1 so imports never need to change in later phases.
class ApiEndpoints {
  ApiEndpoints._();

  static const String _base = ''; // BaseURL is set in DioClient

  // Auth
  static const String login = '/auth/login';
  static const String logout = '/auth/logout';
  static const String refresh = '/auth/refresh';
  static const String forgotPassword = '/auth/forgot-password';
  static const String resetPassword = '/auth/reset-password';
  static const String activateAccount = '/auth/activate-account';
  static const String changePassword = '/auth/change-password';
  static const String me = '/auth/me';

  // Employee (Phase 2)
  static const String employees = '/employees';
  static String employeeById(String id) => '/employees/$id';
  static String employeeActivate(String id) => '/employees/$id/activate';
  static String employeeDeactivate(String id) => '/employees/$id/deactivate';
  static String employeePhoto(String id) => '/employees/$id/photo';
  static String employeeProfile(String id) => '/employees/$id/profile';
  static const String employeeSearch = '/employees/search';

  // Task (Phase 3)
  static const String tasks = '/tasks';
  static String taskById(String id) => '/tasks/$id';
  static String taskStatus(String id) => '/tasks/$id/status';
  static String taskComments(String id) => '/tasks/$id/comments';
  static String taskAttachments(String id) => '/tasks/$id/attachments';
  static String taskHistory(String id) => '/tasks/$id/history';
  static const String myTasks = '/tasks/my-tasks';
  static const String overdueTasks = '/tasks/overdue';
  static const String taskSearch = '/tasks/search';

  // Notification (Phase 4)
  static const String notifications = '/notifications';
  static const String notificationsUnread = '/notifications/unread';
  static const String notificationsUnreadCount = '/notifications/unread-count';
  static String notificationMarkRead(String id) => '/notifications/$id/read';
  static const String notificationsMarkAllRead = '/notifications/mark-all-read';
  static const String notificationPreferences = '/notifications/preferences';

  // Analytics (Phase 6)
  static const String adminDashboard = '/analytics/dashboard/admin';
  static const String employeeDashboard = '/analytics/dashboard/employee';
  static const String taskDistribution = '/analytics/tasks/distribution';
  static const String tasksByPriority = '/analytics/tasks/by-priority';
  static const String completionTrend = '/analytics/tasks/completion-trend';
  static const String employeePerformance = '/analytics/employees/performance';
  static const String reportsExport = '/analytics/reports/export';
  static String reportDownload(String id) => '/analytics/reports/$id/download';

  // Audit (Phase 5)
  static const String auditLogs = '/audit/logs';
  static const String auditLogsSearch = '/audit/logs/search';
  static String auditLogsByEntity(String type, String id) =>
      '/audit/logs/entity/$type/$id';
  static const String securityEvents = '/audit/security-events';
  static const String complianceReport = '/audit/compliance/report';
}
```

---

## ★ core/network/api_exception.dart

```dart
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
      final message =
          (responseData is Map ? responseData['message'] : null) ??
              'An error occurred';
      final errorCode =
          responseData is Map ? responseData['errorCode'] : null;
      final extraData =
          responseData is Map ? responseData['data'] : null;
      return ApiException(
        message: message,
        errorCode: errorCode,
        statusCode: error.response!.statusCode,
        data: extraData is Map
            ? Map<String, dynamic>.from(extraData)
            : null,
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
  String toString() =>
      'ApiException($statusCode): $message [code: $errorCode]';
}
```

---

## ★ core/network/stomp_client.dart (Phase 1 Stub)

```dart
// STOMP WebSocket Client — STUB for Phase 1
// Full implementation in Phase 4 (Notifications + WebSocket)
// This file must be importable — do NOT leave empty.

class StompClientService {
  /// Connect to WebSocket — Phase 4 implementation
  Future<void> connect({required String url, required String token}) async {}

  /// Disconnect — Phase 4 implementation
  Future<void> disconnect() async {}

  /// Subscribe to a topic — Phase 4 implementation
  void subscribe({required String topic, required void Function(dynamic) onMessage}) {}
}
```

---

## ★ core/storage/secure_storage_service.dart

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

final secureStorageServiceProvider = Provider<SecureStorageService>(
  (_) => SecureStorageService(),
);

/// Stores ONLY user info (id, email, firstName, lastName, role).
/// JWT tokens are NEVER stored here — they live exclusively in the cookie jar.
class SecureStorageService {
  // Platform-specific options — see Master Context for per-platform notes
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
    iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock),
    mOptions: MacOsOptions(),
    wOptions: WindowsOptions(),
    lOptions: LinuxOptions(),
  );

  static const _keyId = 'user_id';
  static const _keyEmail = 'user_email';
  static const _keyFirstName = 'user_first_name';
  static const _keyLastName = 'user_last_name';
  static const _keyRole = 'user_role';

  Future<void> saveUserInfo({
    required String id,
    required String email,
    required String firstName,
    required String lastName,
    required String role,
  }) async {
    await Future.wait([
      _storage.write(key: _keyId, value: id),
      _storage.write(key: _keyEmail, value: email),
      _storage.write(key: _keyFirstName, value: firstName),
      _storage.write(key: _keyLastName, value: lastName),
      _storage.write(key: _keyRole, value: role),
    ]);
  }

  /// Returns null if any required field is missing (user not logged in)
  Future<Map<String, String>?> getUserInfo() async {
    final results = await Future.wait([
      _storage.read(key: _keyId),
      _storage.read(key: _keyEmail),
      _storage.read(key: _keyFirstName),
      _storage.read(key: _keyLastName),
      _storage.read(key: _keyRole),
    ]);

    if (results.any((v) => v == null)) return null;

    return {
      'id': results[0]!,
      'email': results[1]!,
      'firstName': results[2]!,
      'lastName': results[3]!,
      'role': results[4]!,
    };
  }

  Future<void> clearUserInfo() async {
    await Future.wait([
      _storage.delete(key: _keyId),
      _storage.delete(key: _keyEmail),
      _storage.delete(key: _keyFirstName),
      _storage.delete(key: _keyLastName),
      _storage.delete(key: _keyRole),
    ]);
  }
}
```

---

## ★ core/storage/cache_service.dart

```dart
import 'package:hive_flutter/hive_flutter.dart';

/// Hive-backed cache service. All boxes are opened in main.dart before runApp.
/// Phase 1 opens all boxes; each phase uses its own box.
class CacheService {
  static const _employeesBox = 'employees_box';
  static const _tasksBox = 'tasks_box';
  static const _myTasksBox = 'my_tasks_box';
  static const _notificationsBox = 'notifications_box';
  static const _analyticsBox = 'analytics_box';

  // Employees (Phase 2)
  Future<void> cacheEmployees(List<Map<String, dynamic>> data) =>
      Hive.box(_employeesBox).put('list', data);

  List<Map<String, dynamic>>? getCachedEmployees() {
    final raw = Hive.box(_employeesBox).get('list');
    return _castList(raw);
  }

  // Tasks (Phase 3)
  Future<void> cacheTasks(List<Map<String, dynamic>> data) =>
      Hive.box(_tasksBox).put('list', data);

  List<Map<String, dynamic>>? getCachedTasks() =>
      _castList(Hive.box(_tasksBox).get('list'));

  Future<void> cacheMyTasks(List<Map<String, dynamic>> data) =>
      Hive.box(_myTasksBox).put('list', data);

  List<Map<String, dynamic>>? getCachedMyTasks() =>
      _castList(Hive.box(_myTasksBox).get('list'));

  // Notifications (Phase 4)
  Future<void> cacheNotifications(List<Map<String, dynamic>> data) =>
      Hive.box(_notificationsBox).put('list', data);

  List<Map<String, dynamic>>? getCachedNotifications() =>
      _castList(Hive.box(_notificationsBox).get('list'));

  // Analytics (Phase 6)
  Future<void> cacheAnalytics(Map<String, dynamic> data) =>
      Hive.box(_analyticsBox).put('dashboard', data);

  Map<String, dynamic>? getCachedAnalytics() {
    final raw = Hive.box(_analyticsBox).get('dashboard');
    if (raw == null) return null;
    return Map<String, dynamic>.from(raw as Map);
  }

  /// Called on logout — clears ALL cached data so no stale data persists between sessions
  Future<void> clearAll() => Future.wait([
        Hive.box(_employeesBox).clear(),
        Hive.box(_tasksBox).clear(),
        Hive.box(_myTasksBox).clear(),
        Hive.box(_notificationsBox).clear(),
        Hive.box(_analyticsBox).clear(),
      ]);

  List<Map<String, dynamic>>? _castList(dynamic raw) {
    if (raw == null) return null;
    return (raw as List)
        .map((e) => Map<String, dynamic>.from(e as Map))
        .toList();
  }
}
```

---

## ★ core/connectivity/connectivity_provider.dart

```dart
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// StreamProvider<bool> — true when online, false when offline.
/// Returns AsyncValue<bool> when watched.
/// In app.dart: use connectivityAsync.valueOrNull ?? true
final connectivityProvider = StreamProvider<bool>((ref) {
  return Connectivity().onConnectivityChanged.map(
    (results) => !results.contains(ConnectivityResult.none),
  );
});
```

---

## ★ core/router/app_routes.dart

```dart
/// All route path constants for the entire app (all 6 phases).
/// Declared in Phase 1 — future phases only add new entries here.
class AppRoutes {
  AppRoutes._();

  // System
  static const String splash = '/splash';

  // Auth (public)
  static const String login = '/login';
  static const String forgotPassword = '/forgot-password';
  static const String resetPassword = '/reset-password';
  static const String activateAccount = '/activate-account';

  // Authenticated (both roles)
  static const String changePassword = '/change-password';

  // Admin shell branches
  static const String adminDashboard = '/admin/dashboard';
  static const String adminEmployees = '/admin/employees';
  static const String adminCreateEmployee = '/admin/employees/create';
  static const String adminEmployeeDetail = '/admin/employees/:id';
  static const String adminEditEmployee = '/admin/employees/:id/edit';
  static const String adminTasks = '/admin/tasks';
  static const String adminCreateTask = '/admin/tasks/create';
  static const String adminTaskDetail = '/admin/tasks/:id';
  static const String adminEditTask = '/admin/tasks/:id/edit';
  static const String adminAnalytics = '/admin/analytics';
  static const String adminAudit = '/admin/audit';
  static const String adminAuditEntityTimeline = '/admin/audit/entity/:type/:id';

  // Employee shell branches
  static const String employeeDashboard = '/employee/dashboard';
  static const String employeeTasks = '/employee/tasks';
  static const String employeeTaskDetail = '/employee/tasks/:id';
  static const String employeeNotifications = '/employee/notifications';
  static const String employeeNotificationPreferences =
      '/employee/notifications/preferences';
  static const String employeeProfile = '/employee/profile';

  // Helper: build parameterized paths at runtime
  static String adminEmployeeDetailPath(String id) =>
      '/admin/employees/$id';
  static String adminEditEmployeePath(String id) =>
      '/admin/employees/$id/edit';
  static String adminTaskDetailPath(String id) => '/admin/tasks/$id';
  static String adminEditTaskPath(String id) => '/admin/tasks/$id/edit';
  static String employeeTaskDetailPath(String id) =>
      '/employee/tasks/$id';
  static String adminAuditEntityPath(String type, String id) =>
      '/admin/audit/entity/$type/$id';
}
```

---

## ★ core/router/app_router.dart

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/domain/providers/auth_provider.dart';
import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/auth/presentation/screens/forgot_password_screen.dart';
import '../../features/auth/presentation/screens/reset_password_screen.dart';
import '../../features/auth/presentation/screens/activate_account_screen.dart';
import '../../features/auth/presentation/screens/change_password_screen.dart';
import '../../features/admin/presentation/screens/admin_shell_screen.dart';
import '../../features/employee/presentation/screens/employee_shell_screen.dart';
import 'app_routes.dart';

// ── Placeholder screens (replaced phase by phase) ──────────────────────────

class _PlaceholderScreen extends StatelessWidget {
  final String title;
  const _PlaceholderScreen({required this.title});
  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text(title)),
        body: Center(
          child: Text('$title\n(Coming in a future phase)',
              textAlign: TextAlign.center),
        ),
      );
}

class _SplashScreen extends StatelessWidget {
  const _SplashScreen();
  @override
  Widget build(BuildContext context) => const Scaffold(
        body: Center(child: CircularProgressIndicator.adaptive()),
      );
}

// ── Router Provider ─────────────────────────────────────────────────────────

final appRouterProvider = Provider<GoRouter>((ref) {
  // ValueNotifier bridges Riverpod state changes to GoRouter's refreshListenable
  final authNotifier = ValueNotifier<int>(0);
  ref.listen<AuthState>(authStateNotifierProvider, (_, __) {
    authNotifier.value++;
  });

  return GoRouter(
    initialLocation: AppRoutes.splash,
    refreshListenable: authNotifier,

    redirect: (context, state) {
      // IMPORTANT: Use ref.read — never ref.watch inside redirect
      final authState = ref.read(authStateNotifierProvider);
      final path = state.uri.path;

      final publicRoutes = [
        AppRoutes.login,
        AppRoutes.forgotPassword,
        AppRoutes.resetPassword,
        AppRoutes.activateAccount,
      ];
      final isPublic = publicRoutes.any((r) => path.startsWith(r));
      final isDeeplink = path.startsWith(AppRoutes.resetPassword) ||
          path.startsWith(AppRoutes.activateAccount);

      // While session restore is in progress → show splash (no redirect)
      if (authState.isLoading) {
        return path == AppRoutes.splash ? null : AppRoutes.splash;
      }

      final authenticated = authState.isAuthenticated;
      final role = authState.user?.role; // 'ADMIN' or 'EMPLOYEE'

      // Not authenticated and on a protected route → login
      if (!authenticated && !isPublic) return AppRoutes.login;

      // Authenticated and on a plain public route → their dashboard
      if (authenticated && isPublic && !isDeeplink) {
        return role == 'ADMIN'
            ? AppRoutes.adminDashboard
            : AppRoutes.employeeDashboard;
      }

      // Wrong role for shell route
      if (authenticated && path.startsWith('/admin') && role != 'ADMIN') {
        return AppRoutes.employeeDashboard;
      }
      if (authenticated && path.startsWith('/employee') && role != 'EMPLOYEE') {
        return AppRoutes.adminDashboard;
      }

      // Splash but loading is done → route to correct destination
      if (path == AppRoutes.splash && !authState.isLoading) {
        if (!authenticated) return AppRoutes.login;
        return role == 'ADMIN'
            ? AppRoutes.adminDashboard
            : AppRoutes.employeeDashboard;
      }

      return null; // No redirect needed
    },

    routes: [
      // Splash
      GoRoute(
        path: AppRoutes.splash,
        builder: (_, __) => const _SplashScreen(),
      ),

      // Auth — public
      GoRoute(path: AppRoutes.login, builder: (_, __) => const LoginScreen()),
      GoRoute(
          path: AppRoutes.forgotPassword,
          builder: (_, __) => const ForgotPasswordScreen()),
      GoRoute(
        path: AppRoutes.resetPassword,
        builder: (_, state) => ResetPasswordScreen(
            token: state.uri.queryParameters['token'] ?? ''),
      ),
      GoRoute(
        path: AppRoutes.activateAccount,
        builder: (_, state) => ActivateAccountScreen(
            token: state.uri.queryParameters['token'] ?? ''),
      ),

      // Authenticated — both roles
      GoRoute(
          path: AppRoutes.changePassword,
          builder: (_, __) => const ChangePasswordScreen()),

      // ── Admin Shell (StatefulShellRoute — go_router v14) ──────────────
      StatefulShellRoute.indexedStack(
        builder: (_, __, shell) => AdminShellScreen(navigationShell: shell),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminDashboard,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'Admin Dashboard'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminEmployees,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'Employees'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminTasks,
              builder: (_, __) => const _PlaceholderScreen(title: 'Tasks'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminAnalytics,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'Analytics'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.adminAudit,
              builder: (_, __) => const _PlaceholderScreen(title: 'Audit'),
            ),
          ]),
        ],
      ),

      // ── Employee Shell ─────────────────────────────────────────────────
      StatefulShellRoute.indexedStack(
        builder: (_, __, shell) =>
            EmployeeShellScreen(navigationShell: shell),
        branches: [
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeDashboard,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'My Dashboard'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeTasks,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'My Tasks'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeNotifications,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'Notifications'),
            ),
          ]),
          StatefulShellBranch(routes: [
            GoRoute(
              path: AppRoutes.employeeProfile,
              builder: (_, __) =>
                  const _PlaceholderScreen(title: 'My Profile'),
            ),
          ]),
        ],
      ),
    ],
  );
});
```

---

## ★ Auth Models

### features/auth/data/models/user_model.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'user_model.freezed.dart';
part 'user_model.g.dart';

@freezed
class UserModel with _$UserModel {
  const factory UserModel({
    required String id,
    required String email,
    required String firstName,
    required String lastName,
    required String role, // 'ADMIN' or 'EMPLOYEE'
  }) = _UserModel;

  factory UserModel.fromJson(Map<String, dynamic> json) =>
      _$UserModelFromJson(json);
}

extension UserModelX on UserModel {
  String get fullName => '$firstName $lastName';
  bool get isAdmin => role == 'ADMIN';
  bool get isEmployee => role == 'EMPLOYEE';
}
```

### features/auth/data/models/login_request.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';

part 'login_request.freezed.dart';
part 'login_request.g.dart';

@freezed
class LoginRequest with _$LoginRequest {
  const factory LoginRequest({
    required String email,
    required String password,
  }) = _LoginRequest;

  factory LoginRequest.fromJson(Map<String, dynamic> json) =>
      _$LoginRequestFromJson(json);
}
```

### features/auth/data/models/auth_response.dart

```dart
import 'package:freezed_annotation/freezed_annotation.dart';
import 'user_model.dart';

part 'auth_response.freezed.dart';
part 'auth_response.g.dart';

/// Wraps the `data` field of the login/me API response
@freezed
class AuthResponse with _$AuthResponse {
  const factory AuthResponse({
    required UserModel user,
  }) = _AuthResponse;

  factory AuthResponse.fromJson(Map<String, dynamic> json) =>
      _$AuthResponseFromJson(json);
}
```

---

## ★ features/auth/data/repositories/auth_repository.dart

```dart
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_endpoints.dart';
import '../../../../core/network/api_exception.dart';
import '../../../../core/network/dio_client.dart';
import '../models/user_model.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(dio: ref.watch(dioClientProvider));
});

class AuthRepository {
  final Dio _dio;
  AuthRepository({required Dio dio}) : _dio = dio;

  Future<UserModel> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiEndpoints.login,
        data: {'email': email, 'password': password},
      );
      return UserModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> logout() async {
    try {
      await _dio.post(ApiEndpoints.logout);
    } on DioException catch (e) {
      // 401/403 means already logged out — not an error
      if (e.response?.statusCode != 401 && e.response?.statusCode != 403) {
        throw ApiException.fromDioError(e);
      }
    }
  }

  Future<UserModel> getMe() async {
    try {
      final response = await _dio.get(ApiEndpoints.me);
      return UserModel.fromJson(
          response.data['data'] as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> forgotPassword(String email) async {
    try {
      await _dio.post(
          ApiEndpoints.forgotPassword, data: {'email': email});
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> resetPassword({
    required String token,
    required String newPassword,
  }) async {
    try {
      await _dio.post(ApiEndpoints.resetPassword,
          data: {'token': token, 'newPassword': newPassword});
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> activateAccount({
    required String token,
    required String password,
  }) async {
    try {
      await _dio.post(ApiEndpoints.activateAccount,
          data: {'token': token, 'password': password});
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }

  Future<void> changePassword({
    required String currentPassword,
    required String newPassword,
  }) async {
    try {
      await _dio.post(ApiEndpoints.changePassword, data: {
        'currentPassword': currentPassword,
        'newPassword': newPassword,
      });
    } on DioException catch (e) {
      throw ApiException.fromDioError(e);
    }
  }
}
```

---

## ★ features/auth/domain/providers/auth_provider.dart

```dart
import 'package:cookie_jar/cookie_jar.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

import '../../../../core/network/dio_client.dart';
import '../../../../core/storage/cache_service.dart';
import '../../../../core/storage/secure_storage_service.dart';
import '../../data/models/user_model.dart';
import '../../data/repositories/auth_repository.dart';

part 'auth_provider.freezed.dart';

// ── AuthState ────────────────────────────────────────────────────────────────

@freezed
class AuthState with _$AuthState {
  const factory AuthState({
    @Default(true) bool isLoading,   // true on app start (session restore)
    @Default(false) bool isAuthenticated,
    UserModel? user,
    String? error,
  }) = _AuthState;
}

// ── Provider ─────────────────────────────────────────────────────────────────

// NOTE: Manual StateNotifierProvider — do NOT add @riverpod annotation.
// AuthNotifier calls restoreSession() in its constructor which requires
// the notifier to be fully constructed first — @riverpod does not support this.
final authStateNotifierProvider =
    StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier(
    authRepository: ref.watch(authRepositoryProvider),
    secureStorage: ref.watch(secureStorageServiceProvider),
    cookieJar: ref.watch(cookieJarProvider),
    cacheService: CacheService(),
  )..restoreSession();
});

// ── AuthNotifier ──────────────────────────────────────────────────────────────

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _repo;
  final SecureStorageService _storage;
  final CookieJar _cookieJar;
  final CacheService _cache;

  AuthNotifier({
    required AuthRepository authRepository,
    required SecureStorageService secureStorage,
    required CookieJar cookieJar,
    required CacheService cacheService,
  })  : _repo = authRepository,
        _storage = secureStorage,
        _cookieJar = cookieJar,
        _cache = cacheService,
        super(const AuthState(isLoading: true));

  // ── Session Restore ──────────────────────────────────────────────────────

  /// Called via ..restoreSession() in the provider constructor.
  /// isLoading=true keeps the router on SplashScreen until this completes.
  Future<void> restoreSession() async {
    try {
      final cached = await _storage.getUserInfo();
      if (cached == null) {
        state = const AuthState(isLoading: false, isAuthenticated: false);
        return;
      }
      // Verify session is still valid — cookie jar auto-sends accessToken
      final user = await _repo.getMe();
      await _storage.saveUserInfo(
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      );
      state = AuthState(
          isLoading: false, isAuthenticated: true, user: user);
    } catch (_) {
      // /auth/me failed — session expired or no network; treat as logged out
      await _clearAll();
      state = const AuthState(isLoading: false, isAuthenticated: false);
    }
  }

  // ── Login ────────────────────────────────────────────────────────────────

  Future<void> login({
    required String email,
    required String password,
  }) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final user = await _repo.login(email: email, password: password);
      await _storage.saveUserInfo(
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      );
      state = AuthState(
          isLoading: false, isAuthenticated: true, user: user);
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e is ApiException ? e.message : 'Login failed. Try again.',
      );
    }
  }

  // ── Logout ───────────────────────────────────────────────────────────────

  Future<void> logout() async {
    try {
      await _repo.logout();
    } catch (_) {
      // Proceed with local cleanup even if server call fails
    }
    await _clearAll();
    state = const AuthState(isLoading: false, isAuthenticated: false);
  }

  /// Called by AuthInterceptor callback when refresh token is expired.
  /// Must not make any API calls — cookie jar is already cleared by interceptor.
  Future<void> forceLogout() async {
    await _storage.clearUserInfo();
    await _cache.clearAll();
    state = const AuthState(isLoading: false, isAuthenticated: false);
  }

  // ── Refresh User ─────────────────────────────────────────────────────────

  Future<void> refreshCurrentUser() async {
    try {
      final user = await _repo.getMe();
      await _storage.saveUserInfo(
        id: user.id,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
      );
      state = state.copyWith(user: user);
    } catch (_) {
      // Silent fail — user info stays stale but session remains
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  Future<void> _clearAll() async {
    await Future.wait([
      _storage.clearUserInfo(),
      _cookieJar.deleteAll(),
      _cache.clearAll(),
    ]);
  }
}

// Needed for the catch block to access ApiException
import '../../../../core/network/api_exception.dart';
```

> ⚠️ The last `import` at the bottom of `auth_provider.dart` must be moved to the TOP of the file with all other imports. It is shown at the bottom here for reading clarity only.

---

## ★ features/auth/domain/providers/current_user_provider.dart

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/models/user_model.dart';
import 'auth_provider.dart';

final currentUserProvider = Provider<UserModel?>((ref) {
  return ref.watch(authStateNotifierProvider).user;
});

final isAdminProvider = Provider<bool>((ref) {
  return ref.watch(currentUserProvider)?.isAdmin ?? false;
});

final isEmployeeProvider = Provider<bool>((ref) {
  return ref.watch(currentUserProvider)?.isEmployee ?? false;
});
```

---

## ★ Shell Screens

### features/admin/presentation/screens/admin_shell_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class AdminShellScreen extends StatelessWidget {
  final StatefulNavigationShell navigationShell;
  const AdminShellScreen({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: navigationShell.currentIndex,
        onDestinationSelected: (i) => navigationShell.goBranch(
          i,
          initialLocation: i == navigationShell.currentIndex,
        ),
        destinations: const [
          NavigationDestination(
              icon: Icon(Icons.dashboard_outlined),
              selectedIcon: Icon(Icons.dashboard),
              label: 'Dashboard'),
          NavigationDestination(
              icon: Icon(Icons.people_outline),
              selectedIcon: Icon(Icons.people),
              label: 'Employees'),
          NavigationDestination(
              icon: Icon(Icons.task_alt_outlined),
              selectedIcon: Icon(Icons.task_alt),
              label: 'Tasks'),
          NavigationDestination(
              icon: Icon(Icons.bar_chart_outlined),
              selectedIcon: Icon(Icons.bar_chart),
              label: 'Analytics'),
          NavigationDestination(
              icon: Icon(Icons.security_outlined),
              selectedIcon: Icon(Icons.security),
              label: 'Audit'),
        ],
      ),
    );
  }
}
```

### features/employee/presentation/screens/employee_shell_screen.dart

```dart
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class EmployeeShellScreen extends StatelessWidget {
  final StatefulNavigationShell navigationShell;
  const EmployeeShellScreen({super.key, required this.navigationShell});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: navigationShell.currentIndex,
        onDestinationSelected: (i) => navigationShell.goBranch(
          i,
          initialLocation: i == navigationShell.currentIndex,
        ),
        destinations: const [
          NavigationDestination(
              icon: Icon(Icons.dashboard_outlined),
              selectedIcon: Icon(Icons.dashboard),
              label: 'Dashboard'),
          NavigationDestination(
              icon: Icon(Icons.task_alt_outlined),
              selectedIcon: Icon(Icons.task_alt),
              label: 'My Tasks'),
          NavigationDestination(
              icon: Icon(Icons.notifications_outlined),
              selectedIcon: Icon(Icons.notifications),
              label: 'Notifications'),
          NavigationDestination(
              icon: Icon(Icons.person_outline),
              selectedIcon: Icon(Icons.person),
              label: 'Profile'),
        ],
      ),
    );
  }
}
```

---

## ★ Login Screen — Full Implementation

```dart
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/router/app_routes.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_text_styles.dart';
import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_snackbar.dart';
import '../../../../core/widgets/app_text_field.dart';
import '../../domain/providers/auth_provider.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _emailCtrl = TextEditingController();
  final _passwordCtrl = TextEditingController();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _emailCtrl.dispose();
    _passwordCtrl.dispose();
    super.dispose();
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    ref.read(authStateNotifierProvider.notifier).login(
          email: _emailCtrl.text.trim(),
          password: _passwordCtrl.text,
        );
    // Router handles navigation automatically on auth state change
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authStateNotifierProvider);

    // ref.listen MUST be inside build() — show snackbar on error
    ref.listen<AuthState>(authStateNotifierProvider, (prev, next) {
      if (next.error != null && next.error != prev?.error) {
        AppSnackbar.showError(context, next.error!);
      }
    });

    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 60),
                Center(
                  child: Image.asset(
                    'assets/images/logo.png',
                    height: 64,
                  ),
                ),
                const SizedBox(height: 32),
                Text('Welcome back', style: AppTextStyles.headlineMedium),
                const SizedBox(height: 4),
                Text('Sign in to TaskHive',
                    style: AppTextStyles.bodyMedium
                        .copyWith(color: AppColors.textSecondary)),
                const SizedBox(height: 32),
                AppTextField(
                  label: 'Email',
                  controller: _emailCtrl,
                  keyboardType: TextInputType.emailAddress,
                  hint: 'you@company.com',
                  validator: (v) {
                    if (v == null || v.trim().isEmpty) {
                      return 'Email is required';
                    }
                    if (!v.contains('@')) return 'Enter a valid email';
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                AppTextField(
                  label: 'Password',
                  controller: _passwordCtrl,
                  obscureText: true,
                  validator: (v) {
                    if (v == null || v.isEmpty) return 'Password is required';
                    return null;
                  },
                ),
                const SizedBox(height: 8),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: () => context.go(AppRoutes.forgotPassword),
                    child: const Text('Forgot password?'),
                  ),
                ),
                const SizedBox(height: 16),
                AppButton(
                  label: 'Sign In',
                  isLoading: authState.isLoading,
                  onPressed: authState.isLoading ? null : _submit,
                ),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
```

---

## Remaining Auth Screen Specifications

> These follow the same pattern as LoginScreen. Use `ConsumerStatefulWidget`. Use `ref.listen` inside `build()` for snackbars.

### forgot_password_screen.dart
- Email field + "Send Reset Link" button
- Uses local `StateProvider<bool>` for loading + `StateProvider<bool>` for submitted
- On success: set submitted=true, show "If this email is registered, you'll receive a reset link." message, disable button
- Calls `authRepository.forgotPassword(email)` directly (not authNotifier — user is not authenticated)
- Never shows an error for unregistered email (backend always returns 200)

### reset_password_screen.dart
- Constructor: `final String token` (from deeplink queryParams — provided by router)
- If token is empty string: show "Invalid or expired link" and a "Go to Login" button
- New password + confirm password fields
- Validate: match, min 8 chars
- On success: snackbar "Password reset successfully" → `context.go(AppRoutes.login)` after 1.5s delay
- On TOKEN_EXPIRED or TOKEN_ALREADY_USED: snackbar with error message + show "Request new link" button that navigates to forgotPassword
- Calls `authRepository.resetPassword(token, newPassword)` directly

### activate_account_screen.dart
- Constructor: `final String token`
- If token is empty: show "Invalid activation link" + "Contact admin" message
- Password + confirm password fields
- On success: snackbar "Account activated! Please sign in." → `context.go(AppRoutes.login)` after 1.5s
- On TOKEN_EXPIRED: "Activation link has expired. Ask your admin to resend the invitation."
- On TOKEN_ALREADY_USED: "Account already activated. Please sign in."
- Calls `authRepository.activateAccount(token, password)` directly

### change_password_screen.dart
- Requires auth — only reachable from within shells
- Current password + New password + Confirm new password
- Validate: new passwords match, min 8 chars, current ≠ new
- On success: snackbar "Password changed. Please sign in again." → call `authNotifier.logout()` → router sends to login
- On PASSWORD_HISTORY_VIOLATION: snackbar with backend's exact message
- On INVALID_CREDENTIALS (wrong current password): show inline error under current password field

---

## Theme Implementations

### core/theme/app_colors.dart

```dart
import 'package:flutter/material.dart';

class AppColors {
  AppColors._();

  static const Color primary       = Color(0xFF2563EB);
  static const Color primaryDark   = Color(0xFF1D4ED8);
  static const Color secondary     = Color(0xFF7C3AED);
  static const Color accent        = Color(0xFF0EA5E9);

  static const Color success       = Color(0xFF16A34A);
  static const Color warning       = Color(0xFFD97706);
  static const Color error         = Color(0xFFDC2626);
  static const Color info          = Color(0xFF0284C7);

  static const Color priorityLow      = Color(0xFF16A34A);
  static const Color priorityMedium   = Color(0xFFD97706);
  static const Color priorityHigh     = Color(0xFFDC2626);
  static const Color priorityCritical = Color(0xFF7C3AED);

  static const Color statusTodo       = Color(0xFF6B7280);
  static const Color statusInProgress = Color(0xFF2563EB);
  static const Color statusInReview   = Color(0xFF7C3AED);
  static const Color statusDone       = Color(0xFF16A34A);
  static const Color statusBlocked    = Color(0xFFDC2626);
  static const Color statusCancelled  = Color(0xFF9CA3AF);

  static const Color surface         = Color(0xFFF8FAFC);
  static const Color background      = Color(0xFFFFFFFF);
  static const Color divider         = Color(0xFFE2E8F0);
  static const Color textPrimary     = Color(0xFF0F172A);
  static const Color textSecondary   = Color(0xFF64748B);
  static const Color textDisabled    = Color(0xFF94A3B8);
}
```

### core/theme/app_text_styles.dart

```dart
import 'package:flutter/material.dart';
import 'app_colors.dart';

class AppTextStyles {
  AppTextStyles._();

  static const TextStyle headlineLarge  = TextStyle(fontSize: 28, fontWeight: FontWeight.bold,   color: AppColors.textPrimary,   height: 1.3);
  static const TextStyle headlineMedium = TextStyle(fontSize: 22, fontWeight: FontWeight.bold,   color: AppColors.textPrimary,   height: 1.3);
  static const TextStyle headlineSmall  = TextStyle(fontSize: 18, fontWeight: FontWeight.w600,   color: AppColors.textPrimary,   height: 1.4);
  static const TextStyle titleMedium    = TextStyle(fontSize: 16, fontWeight: FontWeight.w600,   color: AppColors.textPrimary,   height: 1.4);
  static const TextStyle bodyLarge      = TextStyle(fontSize: 16, fontWeight: FontWeight.normal, color: AppColors.textPrimary,   height: 1.5);
  static const TextStyle bodyMedium     = TextStyle(fontSize: 14, fontWeight: FontWeight.normal, color: AppColors.textPrimary,   height: 1.5);
  static const TextStyle bodySmall      = TextStyle(fontSize: 12, fontWeight: FontWeight.normal, color: AppColors.textSecondary, height: 1.5);
  static const TextStyle labelLarge     = TextStyle(fontSize: 14, fontWeight: FontWeight.w600,   color: AppColors.textPrimary);
  static const TextStyle caption        = TextStyle(fontSize: 11, fontWeight: FontWeight.normal, color: AppColors.textSecondary);
}
```

### core/theme/app_theme.dart

```dart
import 'package:flutter/material.dart';
import 'app_colors.dart';

class AppTheme {
  AppTheme._();

  static ThemeData get light => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
            seedColor: AppColors.primary, brightness: Brightness.light),
        scaffoldBackgroundColor: AppColors.background,
        appBarTheme: const AppBarTheme(
          elevation: 0,
          centerTitle: false,
          backgroundColor: AppColors.background,
          foregroundColor: AppColors.textPrimary,
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: AppColors.surface,
          border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: const BorderSide(color: AppColors.divider)),
          enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: const BorderSide(color: AppColors.divider)),
          focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide:
                  const BorderSide(color: AppColors.primary, width: 2)),
          errorBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(10),
              borderSide: const BorderSide(color: AppColors.error)),
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.primary,
            foregroundColor: Colors.white,
            minimumSize: const Size(double.infinity, 48),
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10)),
            textStyle: const TextStyle(
                fontSize: 16, fontWeight: FontWeight.w600),
          ),
        ),
        cardTheme: CardTheme(
          elevation: 0,
          color: AppColors.surface,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: const BorderSide(color: AppColors.divider),
          ),
        ),
      );

  static ThemeData get dark => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
            seedColor: AppColors.primary, brightness: Brightness.dark),
      );
}
```

---

## Shared Widget + Util Specs

### core/utils/logger.dart

```dart
import 'package:logger/logger.dart';

final appLogger = Logger(
  printer: PrettyPrinter(methodCount: 0, errorMethodCount: 5, lineLength: 80),
  level: Level.debug, // Change to Level.warning in production builds
);
```

### core/widgets/app_button.dart
`StatefulWidget`. Params: `String label`, `VoidCallback? onPressed`, `bool isLoading = false`, `bool isOutlined = false`. When `isLoading`: show `SizedBox(h:20,w:20)` wrapping `CircularProgressIndicator.adaptive(strokeWidth: 2.5)`, disable tap. When `isOutlined`: use `OutlinedButton` styled with primary color border. Full width, 48px height, 10px radius.

### core/widgets/app_text_field.dart
`StatefulWidget` (needed for password toggle). Params: `String label`, `TextEditingController controller`, `bool obscureText = false`, `String? hint`, `TextInputType keyboardType = TextInputType.text`, `Widget? prefixIcon`, `String? Function(String?)? validator`, `bool readOnly = false`. When `obscureText=true`, include internal bool `_hidden=true` toggled by suffix icon `Icons.visibility` / `Icons.visibility_off`. Use `TextFormField` (not `TextField`) so it works inside `Form`.

### core/widgets/app_loading.dart
`StatelessWidget`. Full-screen `Stack` overlay: semi-transparent black `Container` + centered `CircularProgressIndicator.adaptive()`. Used as `Stack` child, not a dialog.

### core/widgets/app_error_widget.dart
Params: `String message`, `VoidCallback? onRetry`. Centered `Column`: `Icon(Icons.error_outline, color: AppColors.error, size: 48)`, 16px gap, `Text(message)` centered, if `onRetry != null`: 16px gap + `AppButton(label: 'Retry', onPressed: onRetry, isOutlined: true)`.

### core/widgets/app_empty_state.dart
Params: `String message`, `IconData icon = Icons.inbox_outlined`, `Widget? action`. Centered `Column`: large grey icon (size 64), 16px, body text centered in secondary color, if action != null: 16px + action widget.

### core/widgets/app_snackbar.dart
Static class. Three static methods:
- `show(BuildContext ctx, String msg)` → dark grey background
- `showSuccess(BuildContext ctx, String msg)` → `AppColors.success` background, white text
- `showError(BuildContext ctx, String msg)` → `AppColors.error` background, white text
All use `ScaffoldMessenger.of(ctx).showSnackBar(...)`. Duration: 3 seconds.

### core/widgets/app_confirm_dialog.dart
Static method: `static Future<bool> show(BuildContext ctx, {required String title, required String message, String confirmLabel = 'Confirm', String cancelLabel = 'Cancel'})`. Returns `true` if confirmed, `false` if cancelled. Uses `showModalBottomSheet`. Layout: drag handle, title (bold), message, row of Cancel + Confirm buttons (Confirm uses primary color).

### core/widgets/app_badge.dart
`StatelessWidget`. Params: `String label`, `Color color`, `Color textColor = Colors.white`. Pill-shaped container: `BorderRadius.circular(100)`, `padding: EdgeInsets.symmetric(horizontal: 8, vertical: 3)`, `color` as background, `TextStyle(fontSize: 11, fontWeight: w600, color: textColor)`.

### core/utils/date_utils.dart
Static class `AppDateUtils`. Methods: `formatDate(DateTime)` → "Mar 5, 2026"; `formatDateTime(DateTime)` → "Mar 5, 2026 10:30 AM"; `timeAgo(DateTime)` → "just now" / "X minutes ago" / "X hours ago" / "X days ago"; `isOverdue(DateTime due)` → `due.isBefore(DateTime.now())`. Use `intl` package `DateFormat`.

### core/utils/string_utils.dart
Static class `AppStringUtils`. Methods: `capitalize(String)`, `initials(String first, String last)` → "JD", `isValidEmail(String)` → regex, `truncate(String, int max)` → adds "...".

### core/services/fcm_service.dart (Stub)
```dart
class FcmService {
  Future<void> initialize() async {}
  Future<String?> getToken() async => null;
}
```

### core/services/notification_navigation_service.dart (Stub)
```dart
class NotificationNavigationService {
  void handleNotificationTap(Map<String, dynamic> data) {}
}
```

### core/widgets/stat_card.dart (Stub)
```dart
import 'package:flutter/material.dart';
class StatCard extends StatelessWidget {
  final String title;
  final String value;
  const StatCard({super.key, required this.title, required this.value});
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(padding: const EdgeInsets.all(16),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(title, style: Theme.of(context).textTheme.bodySmall),
        const SizedBox(height: 8),
        Text(value, style: Theme.of(context).textTheme.headlineSmall),
      ]),
    ),
  );
}
```

---

## Auth Form Widget Specs

Each form widget contains the form fields only (not the full screen). The parent screen calls `formKey.currentState!.validate()`.

### login_form.dart
Params: `TextEditingController emailController`, `TextEditingController passwordController`, `GlobalKey<FormState> formKey`. Contains `Form(key: formKey)` wrapping the two `AppTextField` widgets with their validators.

### forgot_password_form.dart
Params: `TextEditingController emailController`, `GlobalKey<FormState> formKey`. Single email `AppTextField` with not-empty + email format validator.

### reset_password_form.dart
Params: `TextEditingController passwordController`, `TextEditingController confirmController`, `GlobalKey<FormState> formKey`. Two `AppTextField` (obscureText=true) + validators: min 8 chars, must match.

### activate_account_form.dart
Same structure as reset_password_form.

### change_password_form.dart
Params: `TextEditingController currentController`, `TextEditingController newController`, `TextEditingController confirmController`, `GlobalKey<FormState> formKey`. Three fields. Validators: current not empty; new min 8 chars, must not equal current; confirm must match new.

---

## Code Generation — Required After Writing Models

```bash
# Run from project root — taskhive-flutter/
dart run build_runner build --delete-conflicting-outputs
```

Files this generates (do NOT write manually):
- `user_model.freezed.dart`, `user_model.g.dart`
- `login_request.freezed.dart`, `login_request.g.dart`
- `auth_response.freezed.dart`, `auth_response.g.dart`
- `auth_provider.freezed.dart`

---

## Build & Run Verification Checklist

Run in order. Fix any failure before proceeding.

```bash
# Step 1 — Dependencies
flutter pub get
# Expected: Got dependencies! — zero errors

# Step 2 — Code generation
dart run build_runner build --delete-conflicting-outputs
# Expected: Build completed successfully!

# Step 3 — Static analysis
flutter analyze
# Expected: No issues found!

# Step 4 — Android emulator
flutter run -d emulator-5554
# Expected: Splash (1-2s) → Login screen

# Step 5 — Web
flutter run -d chrome
# Expected: Same as Step 4 — no dart:io compile errors

# Step 6 — macOS (if on Mac)
flutter run -d macos
# Expected: Same flow — API calls work (entitlements set correctly)
```

### Manual Test Checklist

| Test | Expected Result |
|------|----------------|
| Login: `admin@taskhive.com` / `Admin@123` | AdminShellScreen, 5-tab bottom nav |
| Login: employee account | EmployeeShellScreen, 4-tab bottom nav |
| Wrong password ×5 | Lockout error message with time remaining |
| Logout | Login screen, cookie jar cleared |
| Close app fully, reopen | Splash → correct shell (session restored) |
| Forgot password | Always shows success message |
| Deeplink test (Android) | Opens on correct screen with token |

### Android Deeplink Test
```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://taskhive.digiwork.com/reset-password?token=testtoken" \
  com.digiwork.taskhive
```

---

## Phase 1 File Count

| Layer | Count |
|-------|-------|
| Core — Network | 7 (dio_client, auth_interceptor, api_endpoints, api_exception, 3 cookie factory files) |
| Core — Storage | 2 |
| Core — Connectivity | 1 |
| Core — Router | 2 |
| Core — Services | 2 (stubs) |
| Core — Theme | 3 |
| Core — Utils | 3 |
| Core — Widgets | 9 (8 + stat_card stub) |
| Auth — Models | 3 |
| Auth — Repository | 1 |
| Auth — Providers | 2 |
| Auth — Screens | 5 |
| Auth — Widgets | 5 |
| Admin Shell | 1 |
| Employee Shell | 1 |
| Entry / Config | 2 (main.dart, app.dart) |
| **Total** | **49** |

> Config files (pubspec.yaml, analysis_options.yaml, build.yaml) are in Master Context — copy from there.
> Platform files (AndroidManifest.xml, Info.plist, entitlements, AppDelegate.swift) are in Master Context — copy from there.
