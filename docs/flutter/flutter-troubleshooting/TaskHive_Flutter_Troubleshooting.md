# TaskHive Flutter — Development Troubleshooting Reference

**Digiwork | `com.digiwork.taskhive` | Phases 1–4**  
Flutter SDK ≥3.24.0 · Spring Boot 3.5 Backend · HttpOnly JWT Cookies

> This document is a consolidated, phase-ordered record of every real error, environment quirk, and platform incompatibility encountered during TaskHive Flutter development. All issues are sourced from actual error logs — nothing speculative.

---

## How to Read This Document

Each issue contains three fields:

- **Symptom** — What you see: the error message or broken behavior.
- **Root Cause** — Why it is happening at the technical level.
- **Resolution** — The exact steps or code changes that fixed it.

> 💡 **Platform Note:** Issues that behave differently on Web vs. Native are called out explicitly. This is the most common source of confusion in cross-platform Flutter development.

---

# Phase 1 — Auth & Core Infrastructure

---

## 1.1 Environment & Connectivity

### Issue 1.1.1 — Physical Device: Connection Refused

**Symptom:** Requests work on the Android Emulator but fail silently on a physical USB-connected phone — a brief loading state followed by a return to the Login screen with no error shown.

**Root Cause:** The `.env.dev` file used `10.0.2.2`, which is an alias specifically for the Android Emulator to reach the PC's localhost. Physical devices do not recognize this IP.

**Resolution:** Update `.env.dev` to use `localhost` and tunnel the phone's traffic to the PC via ADB:

```bash
# .env.dev
API_BASE_URL=http://localhost:8080/api/v1

# Run once per terminal session
adb reverse tcp:8080 tcp:8080
```

---

### Issue 1.1.2 — Environment Variable Returns Null

**Symptom:** `dotenv.env['API_BASE_URL']` returns `null` at runtime.

**Root Cause:** The `.env` file path was not registered in `pubspec.yaml` assets, or the path passed to `dotenv.load()` was incorrect.

**Resolution:**

```yaml
# pubspec.yaml
flutter:
  assets:
    - assets/env/.env.dev
    - assets/env/.env.prod
```

```dart
// main.dart
await dotenv.load(fileName: "assets/env/.env.dev");
```

---

## 1.2 Authentication & Session Management

### Issue 1.2.1 — Login Crashes: Type Cast Error

**Symptom:** `type 'Null' is not a subtype of type 'String' in type cast` during login.

**Root Cause:** `UserModel.fromJson` expected non-null `String` for `role`, `firstName`, or `lastName`, but the backend returned `null` for those fields.

**Resolution:** Mark nullable fields as `String?` in the Freezed model and verify backend data seeding:

```dart
@freezed
class UserModel with _$UserModel {
  const factory UserModel({
    required String id,
    String? firstName,
    String? lastName,
    String? role,
  }) = _UserModel;

  factory UserModel.fromJson(Map<String, dynamic> json) =>
      _$UserModelFromJson(json);
}
```

---

### Issue 1.2.2 — Circular Riverpod Provider Dependency

**Symptom:** App crashes on startup with a stack overflow or `Circular dependency detected` from Riverpod.

**Root Cause:** `DioClient` needed `AuthNotifier` (to force-logout on 401), and `AuthNotifier` needed `DioClient` (to make login requests) — a direct circular dependency.

**Resolution:** Use a callback pattern. The `AuthInterceptor` accepts a `VoidCallback onForceLogout` that is lazily resolved via `ref.read` at call time, breaking the cycle:

```dart
final dioClientProvider = Provider<DioClient>((ref) {
  return DioClient(
    onForceLogout: () => ref.read(authNotifierProvider.notifier).forceLogout(),
  );
});
```

---

### Issue 1.2.3 — Cookies Dropped on Physical Device

**Symptom:** User logs in but is immediately redirected back to the Login screen — session not persisting on a physical device.

**Root Cause:** Backend had `app.cookie.domain=localhost`. When the phone accessed the API via IP address, the cookie was rejected because the domain didn't match.

**Resolution:** Use `adb reverse` so the phone accesses the API via `localhost`, satisfying the cookie domain requirement. Keep `.env.dev` pointing to `localhost`, not an IP.

---

### Issue 1.2.4 — GoRouter Not Redirecting After Login

**Symptom:** `isAuthenticated` changes in state, but the screen doesn't change — GoRouter stays on Login.

**Root Cause:** GoRouter does not automatically rebuild based on Riverpod state unless explicitly told to.

**Resolution:** Create a `ValueNotifier` bridge and pass it to GoRouter via `refreshListenable`:

```dart
final routerRefreshProvider = Provider<ValueNotifier<int>>((ref) {
  final notifier = ValueNotifier(0);
  ref.listen(authNotifierProvider, (_, __) => notifier.value++);
  return notifier;
});

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    refreshListenable: ref.watch(routerRefreshProvider),
    redirect: (context, state) { /* ... */ },
    routes: [ /* ... */ ],
  );
});
```

---

## 1.3 Web vs. Native Platform Issues

### Issue 1.3.1 — Flutter Web: CookieManager Assertion Error

**Symptom:** An assertion error is triggered by `dio_cookie_manager` or `CookieJar` when running on Web.

**Root Cause:** The browser handles cookies natively through HTTP headers. Manual cookie jars conflict with browser security policies on web.

**Resolution:** Use conditional imports to provide platform-specific implementations, and skip the `CookieManager` interceptor on web:

```dart
// Conditional import pattern
import 'cookie_jar_factory_stub.dart'
    if (dart.library.io) 'cookie_jar_factory_native.dart';

// In dio_client.dart
if (!kIsWeb) {
  dio.interceptors.add(CookieManager(cookieJar));
}
```

```dart
// cookie_jar_factory_stub.dart (web — no dart:io)
CookieJar createCookieJar(String dir) => DefaultCookieJar();

// cookie_jar_factory_native.dart (android/ios/desktop)
import 'dart:io';
CookieJar createCookieJar(String dir) =>
    PersistCookieJar(storage: FileStorage(dir));
```

> ⚠️ **Never import `dart:io` at the top level** of any file that compiles for web — even inside an `if (!kIsWeb)` block. Dart compiles the entire file, not just the branch that runs.

---

### Issue 1.3.2 — Web Session Lost on Tab Reload

**Symptom:** User logs in on Flutter Web, sees the Dashboard, but reloading the Chrome tab instantly logs them out.

**Root Cause:** `CookieUtil.java` read `app.cookie.domain` from config but the value was never passed into the `ResponseCookie` builder. Chrome treated the cookie as **Host-Only**, locking it to port `8080`. A reload from the frontend port (`3000`) was blocked.

**Resolution:** Fix `CookieUtil.java` to explicitly set the domain:

```java
ResponseCookie cookie = ResponseCookie.from(name, value)
    .domain(domain != null && !domain.isEmpty() ? domain : null)
    .httpOnly(true)
    .path("/")
    .maxAge(maxAge)
    .sameSite("Lax")
    .build();
```

> 💡 **Why mobile wasn't affected:** Native networking clients ignore port-level domain locking. This is a browser-only enforcement.

---

### Issue 1.3.3 — IP Address Causes 403 Forbidden on Web

**Symptom:** After switching `.env.dev` to a machine IP (`192.168.x.x`), login succeeds but all subsequent requests return `403 Forbidden`.

**Root Cause:** `SameSite=Lax` means the browser only attaches cookies to same-site requests. The web security standard classifies IP addresses as inherently cross-site — so Chrome strips the cookie from every API call, causing the backend to return 403.

**Resolution:** For web development, always use `localhost` — never an IP address. IP-based URLs only work with `SameSite=None` + `Secure=true`, which requires HTTPS.

---

### Issue 1.3.4 — `SameSite=None` Still Drops Cookie (No HTTPS)

**Symptom:** After setting `app.cookie.same-site=None` to try to fix the IP issue, Chrome still drops the cookie.

**Root Cause:** Since 2020, browsers enforce an unbreakable rule: `SameSite=None` cookies **must** also be `Secure=true`. Running over plain HTTP means Chrome immediately discards the cookie and prints a yellow warning in the console.

**Resolution:** Two options:
1. **Stick to `localhost`** — Keep `SameSite=Lax`. Browsers treat `localhost` as a safe exception to the HTTPS rule.
2. **Set up local SSL** — Use `mkcert` to generate certificates for your Spring Boot app, enabling HTTPS. Then `SameSite=None; Secure=true` will work.

---

### Issue 1.3.5 — Android Missing Internet Permission

**Symptom:** All network requests on Android fail with `SocketException: OS Error: Connection refused`.

**Root Cause:** `android.permission.INTERNET` was missing from `AndroidManifest.xml`.

**Resolution:**

```xml
<!-- android/app/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

### Issue 1.3.6 — `path_provider` Fails on Web

**Symptom:** `getApplicationDocumentsDirectory()` throws on web.

**Root Cause:** `PersistCookieJar` uses `FileStorage` which depends on `dart:io` file system access — unavailable on web.

**Resolution:** Use the conditional import stub pattern (see Issue 1.3.1). On web, return `DefaultCookieJar` (in-memory). On native, return `PersistCookieJar` with `FileStorage`.

---

## 1.4 Build & Tooling

### Issue 1.4.1 — `build_runner` Fails to Generate Files

**Symptom:** `build_runner` exits with errors. `.freezed.dart` or `.g.dart` files are missing or stale.

**Root Cause:** Existing generated files from a previous failed run conflict with the new generation attempt.

**Resolution:**

```bash
dart run build_runner build --delete-conflicting-outputs
```

Always use this flag. Never run `build_runner build` without it.

---

### Issue 1.4.2 — Material 3 API Deprecation Warnings

**Symptom:** Type mismatch on `ThemeData.cardTheme`, or `withOpacity()` shows a deprecation warning.

**Root Cause:** Flutter 3.x introduced `CardThemeData` and deprecated `withOpacity()` on `Color`.

**Resolution:**

```dart
// Before (deprecated)
cardTheme: CardTheme(color: Colors.white.withOpacity(0.9))

// After
cardTheme: CardThemeData(color: Colors.white.withValues(alpha: 0.9))
```

---

## 1.5 Project Organization & Linting

### Issue 1.5.1 — Broken Imports (12+ Files)

**Symptom:** Multiple `"target of URI doesn't exist"` errors after refactoring or renaming files.

**Root Cause:** Relative import paths (`../../`) broke when files were moved — the `../` depth no longer matched the new file location.

**Resolution:** Use package-relative imports — they never break regardless of where a file lives:

```dart
// ❌ Relative — breaks when files move
import '../../../core/network/api_exception.dart';

// ✅ Package-relative — always stable
import 'package:taskhive_flutter/core/network/api_exception.dart';
```

---

### Issue 1.5.2 — Logger & API Exception Naming Inconsistencies

**Symptom:** `"Target of URI doesn't exist"` for `logger.dart` or `api_exception.dart` — some files imported `app_logger.dart`, others `logger.dart`; some used `api_exceptions.dart` (plural), others `api_exception.dart` (singular).

**Root Cause:** Files were created under slightly different names in early development and never consolidated.

**Resolution:** Standardize on `logger.dart` with the `appLogger.i()` API, and `api_exception.dart` (singular). Do a project-wide find-and-replace on all incorrect import paths.

---

### Issue 1.5.3 — Underscore Wildcard Causes Lint Warning

**Symptom:** Lint warning on `Navigator.pop(context, _)`.

**Root Cause:** Using `_` as a discard in a position where the linter expects a named or no variable.

**Resolution:**

```dart
// Before
Navigator.pop(context, _);

// After
Navigator.pop(context);
```

---

# Phase 2 — Employee Management

---

## 2.1 API & Data Layer

### Issue 2.1.1 — Pagination Parameter Name Mismatch

**Symptom:** Employee list ignores sort/order settings, or backend returns `400` on sort requests.

**Root Cause:** Backend expected `sortBy` and `sortDir`; frontend was sending different keys.

**Resolution:**

```dart
final response = await _dio.get('/employees', queryParameters: {
  'page': page,
  'size': size,
  'sortBy': sortBy,   // ← not 'sort'
  'sortDir': sortDir, // ← not 'direction'
});
```

---

### Issue 2.1.2 — Search Query Parameter Name Mismatch

**Symptom:** Search input has no effect — all records return regardless of the search term.

**Root Cause:** Frontend sent `search=...`, backend expected `query=...`.

**Resolution:** Rename the parameter in all repository methods to `'query'`.

---

### Issue 2.1.3 — Profile Image Upload Fails: Wrong FormData Key

**Symptom:** Image upload returns `400 Bad Request`.

**Root Cause:** Multipart `FormData` used the key `'image'`; the backend Spring controller expected `'file'`.

**Resolution:** Standardize all multipart upload keys to `'file'` across all repositories.

---

### Issue 2.1.4 — Image URL Endpoint Returns Raw String, Not JSON

**Symptom:** Dio throws a parse error on image URL endpoints — tries to deserialize a plain string as JSON.

**Root Cause:** The endpoint returned a raw `String` body, not a JSON object.

**Resolution:**

```dart
final response = await _dio.get(
  '/employees/{id}/photo-url',
  options: Options(responseType: ResponseType.plain),
);
final url = response.data as String;
```

---

## 2.2 File Handling (Web vs. Native)

### Issue 2.2.1 — `dart:io` File Not Available on Web

**Symptom:** Profile photo picker crashes on Flutter Web.

**Root Cause:** `File(path)` from `dart:io` doesn't exist on web. `ImagePicker` on web returns `XFile`, not `dart:io.File`.

**Resolution:**

```dart
final pickedFile = await _picker.pickImage(source: ImageSource.gallery);
if (pickedFile == null) return;

Uint8List bytes;
if (kIsWeb) {
  bytes = await pickedFile.readAsBytes();
} else {
  bytes = await File(pickedFile.path).readAsBytes();
}
```

---

# Phase 3 — Task Management

---

## 3.1 State Management & Forms

### Issue 3.1.1 — Form Fields Overwritten Mid-Typing

**Symptom:** User types in a task form field. A background refresh fires and clears or resets the field to the server value.

**Root Cause:** The provider watching task data updated all text controllers on every new state, regardless of whether the user was actively editing.

**Resolution:** Implement a `_dirty` flag — only sync controllers from network state if the user hasn't touched that field:

```dart
bool _titleDirty = false;
final _titleController = TextEditingController();

// In state listener:
if (!_titleDirty) {
  _titleController.text = newTask.title;
}

// On user input:
_titleController.addListener(() => _titleDirty = true);
```

---

### Issue 3.1.2 — Status Change Popup Stuck / Not Dismissing

**Symptom:** The task status comment dialog shows an infinite spinner, or doesn't close after success or failure.

**Root Cause:** `Navigator.pop()` was missing from the error branch — only the success path dismissed the dialog.

**Resolution:** Always call `Navigator.pop()` in both branches, guarded by `context.mounted`:

```dart
try {
  await ref.read(taskActionsProvider.notifier).updateStatus(taskId, newStatus, comment);
  if (context.mounted) Navigator.pop(context);
} catch (e) {
  if (context.mounted) Navigator.pop(context); // ← never skip this
  // show error snackbar
}
```

---

## 3.2 Navigation & Routing

### Issue 3.2.1 — Role-Based Redirect Infinite Loop

**Symptom:** After login, the app loops endlessly between redirect decisions without settling on a screen.

**Root Cause:** The redirect function checked multiple providers (`authState`, `userRole`, loading state) in an inconsistent order, producing conflicting decisions.

**Resolution:** Use a single provider (`currentUserProvider`) as the sole source of truth for all routing decisions:

```dart
redirect: (context, state) {
  final user = ref.read(currentUserProvider);
  final isAuth = user != null;
  final isLoginRoute = state.matchedLocation == AppRoutes.login;

  if (!isAuth && !isLoginRoute) return AppRoutes.login;
  if (isAuth && isLoginRoute) {
    return user.role == 'ADMIN' ? AppRoutes.adminDashboard : AppRoutes.dashboard;
  }
  return null;
},
```

---

# Phase 4 — Notifications & WebSocket (FCM)

---

## 4.1 Notifications & State

### Issue 4.1.1 — Notification Read Status Resets on Session Restart

**Symptom:** Notifications marked as read reappear as unread after the app is restarted.

**Root Cause:** Read status was only stored in-memory in the Riverpod provider — lost on every app launch.

**Resolution:** Persist read state with Hive and load from cache on session restore:

```dart
// On mark-read:
await notificationsBox.put(notification.id, {'read': true, ...});

// On app init — build initial state from Hive before first API call:
final cached = notificationsBox.toMap();
```

---

### Issue 4.1.2 — Notification Parameters Don't Match Backend

**Symptom:** Notification list doesn't paginate or filter — all records return on every call.

**Root Cause:** Same parameter name mismatch as Phase 2 — `sortBy`/`sortDir`/`query` keys were wrong.

**Resolution:** Apply the same fix as Issues 2.1.1 and 2.1.2 to `NotificationRepository`.

---


---

# Phase 5 — ML Service & AI Integration

---

## 5.1 ML Data & UI Visibility

### Issue 5.1.1 — Missing Backend Endpoint for Employee History

**Symptom:** "Cannot load history" error on Employee Detail screen. Browser console / terminal logs show `500 Internal Server Error` for `GET /api/v1/employees/{id}/history`.

**Root Cause:** The expected backend endpoint was not yet implemented in the Java service.

**Resolution:** Implemented a frontend fallback in `employee_repository.dart`. Modified `getStatusHistory()` to fetch data from the generic Audit Logs endpoint (`GET /api/v1/audit/logs/entity/EMPLOYEE/{id}`) and map the audit entries (especially `STATUS_CHANGE` and `CREATE`) to the `EmployeeStatusHistoryModel` format.

---

### Issue 5.1.2 — Null-Safety Crash on Score Data

**Symptom:** Red screen "Null check operator used on a null value" when loading Employee Detail or Profile screens.

**Root Cause:** The `ProductivityScoreCard` required a non-nullable `ProductivityScoreResponse`, but was being initialized with `score!` inside a provider's data block before the score was guaranteed to be present.

**Resolution:** Updated the card calls to handle nullability more defensively and ensured the `score` is passed through the `AsyncValue` lifecycle properly.

---

### Issue 5.1.3 — Syntax Error: Nested Method in Build

**Symptom:** Multiple build errors in `MyProfileScreen.dart`: "Expected to find '}'" and "The method isn't defined".

**Root Cause:** The helper method `_buildAiPerformanceSection` was accidentally defined *inside* the `build` method's return statement block rather than at the class level.

**Resolution:** Properly closed the `build` method with `); }` before starting the definition of the helper widget method.

---

### Issue 5.1.4 — ML Toggle Visible to Employees

**Symptom:** Employees were able to see the ML toggle in their App Bar and disable/enable the feature globally for their session.

**Root Cause:** The `MlFeatureToggleWidget` was included in the `SliverAppBar` of the `EmployeeDashboardScreen`.

**Resolution:** Removed the toggle widget from the employee-facing dashboard. The ML toggle is intended exclusively for System Administrators.

---

# Quick Reference: Most Common Issues

| Symptom | Root Cause | Resolution |
|---------|-----------|------------|
| Silent login failure on physical device | `10.0.2.2` is emulator-only | `localhost` + `adb reverse tcp:8080 tcp:8080` |
| Instant logout on web tab reload | `.domain()` missing in `CookieUtil.java` | Add `.domain(domain)` to `ResponseCookie` builder |
| 403 on API calls via IP (web) | `SameSite=Lax` blocks cross-site IP requests | Use `localhost` only for web dev |
| `SameSite=None` still drops cookie | `Secure=true` required with `SameSite=None` | Use `localhost` or set up local HTTPS with `mkcert` |
| `flutter build web` compile error | Top-level `dart:io` import in cross-platform file | Conditional imports with platform stubs |
| `build_runner` generation fails | Stale cached outputs | `dart run build_runner build --delete-conflicting-outputs` |
| Router doesn't navigate after login | GoRouter not listening to Riverpod state | Wire `ValueNotifier` to `refreshListenable` |
| Circular dependency on startup | `DioClient` ↔ `AuthNotifier` mutual dependency | Pass `onForceLogout` callback, read lazily via `ref.read` |
| API pagination/search has no effect | Parameter name mismatch vs backend spec | Match `sortBy`, `sortDir`, `query` exactly |
| Form field cleared while typing | Background refresh overwrites controller | `_dirty` flag — only sync from state if user hasn't edited |

---

> 🏆 **Golden Rule:** `flutter pub get` → `dart run build_runner build --delete-conflicting-outputs` → `flutter analyze` — in that exact order, every time.
