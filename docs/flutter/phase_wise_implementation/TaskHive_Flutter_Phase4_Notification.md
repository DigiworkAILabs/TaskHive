# TaskHive Flutter — Phase 4: Notification Module
## Version 1.0 | SRS v2.4 | Digiwork
## ⚠️ Always provide `TaskHive_Flutter_Master_Context.md` alongside this file

---

## ⚠️ Nullability Rule — Enforced Without Exception
Every model field that is not a primary key (`id`) must be nullable unless the backend DTO explicitly marks it non-null. Never use the Dart null assertion operator (`!`) on API response data. Use null-aware operators (`?.`, `??`) everywhere. Before writing any model field, verify nullability in:
1. `taskhive-backend/.../notification/dto/NotificationResponse.java`
2. `taskhive-frontend/src/features/notification/types/notification.types.ts`

---

## Project Flow Context

```
Phase 1 — Auth + Core Infrastructure           ✅ COMPLETE
Phase 2 — Employee Module                      ✅ COMPLETE
Phase 3 — Task Module                          ✅ COMPLETE
Phase 4 — Notification Module                  ← YOU ARE HERE
Phase 5 — Audit Logs                           (depends on Phase 1)
Phase 6 — Analytics + Dashboards               (depends on Phase 3)
```

### What Phase 4 Builds On
- **Phase 1**: `stomp_client.dart` (stub → full implementation here), `fcm_service.dart` (stub → full), `notification_navigation_service.dart` (stub → full), `connectivityProvider`, `authStateNotifierProvider`, `currentUserProvider`, `CacheService.notifications_box`
- **Phase 2**: `EmployeeModel`, employee routes for navigation
- **Phase 3**: Task routes (`/admin/tasks/:id`, `/employee/tasks/:id`) used in notification tap navigation

### Notification Delivery Architecture

```
Backend Event (e.g. TaskAssigned)
        │
        ├──→ WebSocket STOMP → /topic/notifications/{userId}
        │         └── Flutter receives frame → updates badge + list (foreground only)
        │
        └──→ FCM Push → Firebase Cloud Messaging
                  ├── App foreground → flutter_local_notifications shows heads-up banner
                  ├── App background → OS shows push notification
                  └── App killed     → OS shows push notification → tap opens app
```

### Role Behaviour

| Feature | ADMIN | EMPLOYEE |
|---------|-------|----------|
| View notifications | ✅ | ✅ |
| Real-time STOMP badge | ✅ | ✅ |
| FCM push | ✅ | ✅ |
| Notification preferences | ✅ | ✅ |
| Tap TASK notification | → `/admin/tasks/:id` | → `/employee/tasks/:id` |
| Tap EMPLOYEE notification | → `/admin/employees/:id` | ❌ (not applicable) |

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 4 of 6 |
| **Name** | Notification Module |
| **Scope** | STOMP WebSocket (real-time), FCM push, notification list, badge count, preferences, tap navigation |
| **Depends On** | Phase 1 + Phase 2 + Phase 3 |
| **Platforms** | Android ✅ iOS ✅ macOS ✅ Web ✅ Windows ✅ Linux ✅ |

### Deliverable
Real-time in-app notifications via STOMP WebSocket work in the foreground. FCM push notifications deliver when app is background or killed. Badge count on the bottom nav Notifications tab updates in real-time. Users view paginated notification history, mark notifications read, tap to navigate to the relevant entity. Notification preferences configurable per category.

---

## Phase 4 — Complete File List (18 files)

```
lib/
│
├── core/
│   ├── network/
│   │   └── stomp_client.dart                          # MODIFY — replace Phase 1 stub with full impl
│   │
│   └── services/
│       ├── fcm_service.dart                           # MODIFY — replace Phase 1 stub with full impl
│       └── notification_navigation_service.dart       # MODIFY — replace Phase 1 stub with full impl
│
└── features/
    └── notification/
        ├── data/
        │   ├── models/
        │   │   ├── notification_model.dart             # ★ New
        │   │   └── notification_preference_model.dart  # ★ New
        │   └── repositories/
        │       └── notification_repository.dart        # ★ New
        │
        ├── domain/
        │   └── providers/
        │       ├── notification_list_provider.dart     # ★ New
        │       ├── unread_count_provider.dart          # ★ New
        │       ├── notification_preference_provider.dart  # ★ New
        │       ├── stomp_provider.dart                 # ★ New
        │       └── notification_actions_provider.dart  # ★ New
        │
        └── presentation/
            ├── screens/
            │   ├── notification_list_screen.dart       # ★ New
            │   └── notification_preferences_screen.dart  # ★ New
            └── widgets/
                ├── notification_badge.dart             # ★ New
                ├── notification_list_tile.dart         # ★ New
                └── notification_preference_tile.dart   # ★ New
```

**Files modified (not new):**
- `lib/core/network/stomp_client.dart` — Phase 1 stub replaced with full STOMP implementation
- `lib/core/services/fcm_service.dart` — Phase 1 stub replaced with full FCM implementation
- `lib/core/services/notification_navigation_service.dart` — Phase 1 stub replaced with navigation logic
- `lib/core/router/app_router.dart` — add notification routes
- `lib/main.dart` — add Firebase initialization + FCM setup
- `android/app/src/main/AndroidManifest.xml` — FCM config already present from Phase 1 Master Context
- `ios/Runner/Info.plist` — add background modes (see section below)
- `ios/Runner/AppDelegate.swift` — uncomment Firebase lines (see section below)

> `api_endpoints.dart` already has all notification endpoints from Phase 1 — no changes needed.
> `cache_service.dart` already has `notifications_box` — no changes needed.
> `app_routes.dart` already has all notification route constants — no changes needed.

---

## API Endpoints Used in Phase 4

```
GET    /api/v1/notifications                      # Paginated list (newest first)
GET    /api/v1/notifications/unread               # Unread list only
GET    /api/v1/notifications/unread-count         # { "data": { "count": 5 } }
PATCH  /api/v1/notifications/{id}/read            # Mark single as read
PATCH  /api/v1/notifications/mark-all-read        # Mark all as read
GET    /api/v1/notifications/preferences          # Get user preference settings
PUT    /api/v1/notifications/preferences          # Update preferences
```

### Response Shapes

```json
// GET /api/v1/notifications
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "userId": "uuid",
        "type": "TASK_ASSIGNED",
        "title": "New Task Assigned",
        "message": "You have been assigned to Fix login bug",
        "isRead": false,
        "readAt": null,
        "entityType": "TASK",
        "entityId": "uuid",
        "createdAt": "2026-03-01T10:00:00Z"
      }
    ],
    "totalElements": 20,
    "totalPages": 2,
    "size": 10,
    "number": 0
  }
}

// GET /api/v1/notifications/unread-count
{
  "success": true,
  "data": { "count": 5 }
}

// GET /api/v1/notifications/preferences
{
  "success": true,
  "data": {
    "emailEnabled": true,
    "inAppEnabled": true,
    "taskAssigned": true,
    "taskOverdue": true,
    "dailyDigest": false,
    "digestTime": "08:00"
  }
}

// PATCH mark-as-read — 200 OK { "success": true, "data": null }
// PUT preferences — 200 OK { "success": true, "data": { /* updated prefs */ } }

// STOMP frame payload (received on /topic/notifications/{userId})
// Frame body is JSON string — parse with jsonDecode:
{
  "id": "uuid",
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "...",
  "entityType": "TASK",
  "entityId": "uuid",
  "createdAt": "2026-03-01T10:00:00Z"
}

// All errors follow global shape from Master Context
// Phase 4 specific error codes: none beyond standard RESOURCE_NOT_FOUND
```

> ⚠️ Verify ALL field names above against:
> - `taskhive-backend/.../notification/dto/NotificationResponse.java`
> - `taskhive-frontend/src/features/notification/types/notification.types.ts`
> The Next.js frontend is fully working — mirror its type definitions exactly.

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-NOTIF-01 | High | Real-time in-app notifications via STOMP when app is foreground |
| FR-NOTIF-02 | High | FCM push when app is background or killed |
| FR-NOTIF-04 | High | Badge count on Notifications tab updates in real-time via STOMP |
| FR-NOTIF-05 | Medium | Paginated notification list (newest first) |
| FR-NOTIF-06 | Medium | Unread notifications show blue dot |
| FR-NOTIF-07 | Medium | Tap notification → mark read + navigate to entity |
| FR-NOTIF-08 | Low | Configure preferences per category (in-app + email toggles) |
| FR-NOTIF-09 | Low | Daily digest toggle + time picker |
| FR-NOTIF-10 | High | STOMP reconnects on app resume via `AppLifecycleObserver` |
| FR-NOTIF-11 | High | FCM token registered with backend on login and on token refresh |

> FR-NOTIF-03 (email async queue) is backend-only. Flutter has no role in email delivery.

---

## Data Models — Specifications

### notification_model.dart
`@freezed`. Verify every field name and nullability against backend DTO and Next.js types before writing.

Fields:
- `id` — `String` required
- `userId` — `String?`
- `type` — `String?` (e.g. `'TASK_ASSIGNED'`, `'TASK_OVERDUE'`, `'TASK_STATUS_CHANGED'`, `'TASK_COMMENT_ADDED'`)
- `title` — `String?`
- `message` — `String?`
- `isRead` — `bool` default `false`
- `readAt` — `String?`
- `entityType` — `String?` (e.g. `'TASK'`, `'EMPLOYEE'`, `'SYSTEM'`)
- `entityId` — `String?`
- `createdAt` — `String?`

Extension `NotificationModelX`:
- `bool get isUnread` → `!isRead`
- `IconData get icon` → based on `type`: `TASK_ASSIGNED` → `Icons.assignment_ind`, `TASK_OVERDUE` → `Icons.warning_amber`, `TASK_STATUS_CHANGED` → `Icons.sync`, `TASK_COMMENT_ADDED` → `Icons.comment`, default → `Icons.notifications`

### notification_preference_model.dart
`@freezed`. Fields:
- `emailEnabled` — `bool` default `true`
- `inAppEnabled` — `bool` default `true`
- `taskAssigned` — `bool` default `true`
- `taskOverdue` — `bool` default `true`
- `dailyDigest` — `bool` default `false`
- `digestTime` — `String?` (e.g. `"08:00"`)

---

## Repositories — Specifications

### notification_repository.dart
Uses `dioClientProvider`. Methods:

| Method | Endpoint | Returns |
|--------|----------|---------|
| `getNotifications({page, size})` | `GET /notifications` | `Map<String, dynamic>` (raw paginated) |
| `getUnreadCount()` | `GET /notifications/unread-count` | `int` |
| `markAsRead(id)` | `PATCH /notifications/{id}/read` | `void` |
| `markAllRead()` | `PATCH /notifications/mark-all-read` | `void` |
| `getPreferences()` | `GET /notifications/preferences` | `NotificationPreferenceModel` |
| `updatePreferences(NotificationPreferenceModel)` | `PUT /notifications/preferences` | `NotificationPreferenceModel` |

---

## Providers — Specifications

### stomp_provider.dart
`Provider<StompClientWrapper>`. This is the STOMP lifecycle manager.

Key behaviour:
- Watches `currentUserProvider` — if user is null (logged out), returns `StompClientWrapper.empty()` (no-op)
- If user is present, creates `StompClientWrapper` with `wsBaseUrl` from `.env` and `userId`
- On connection: subscribes to `/topic/notifications/{userId}`
- On notification frame received: calls `ref.read(unreadCountProvider.notifier).increment()` + `ref.read(notificationListNotifierProvider.notifier).prependNew(notification)`
- `ref.onDispose` → calls `wrapper.disconnect()`

`StompClientWrapper` class (inside `stomp_client.dart`):
- `connect()` — establishes STOMP connection, reads auth cookies from `cookieJarProvider` and injects as STOMP headers for the handshake
- `disconnect()` — cleanly disconnects
- `reconnect()` — called by `AppLifecycleObserver` on `AppLifecycleState.resumed`
- `subscribe(topic, callback)` — subscribes to a topic
- `StompClientWrapper.empty()` — returns a no-op instance (used when logged out)

Auth over STOMP: The STOMP connection upgrade request must include the `accessToken` cookie. Read cookies from `cookieJarProvider` and pass as STOMP connect headers. The `stomp_dart_client` package supports custom headers in `StompConfig`.

### unread_count_provider.dart
`@riverpod` `AsyncNotifier<int>`. On `build()`: fetches `getUnreadCount()` from repository. Also caches count in `analytics_box` under key `'unread_count'` for instant badge display on app start.

Methods:
- `refresh()` — re-fetches from API
- `increment()` — in-place `state = AsyncData((state.valueOrNull ?? 0) + 1)` (called by STOMP on new notification)
- `decrement()` — called when single notification marked read
- `reset()` — called when mark-all-read

### notification_list_provider.dart
`@riverpod` `AsyncNotifier`. Cache-then-network using `notifications_box`. Same stale-while-revalidate pattern as Phase 2 employee list and Phase 3 task list.

Methods:
- `refresh()` — pull-to-refresh
- `loadMore()` — infinite scroll pagination
- `prependNew(NotificationModel)` — called by STOMP when new notification arrives; adds to top of list
- `markRead(id)` — in-place update of single notification `isRead = true`
- `markAllRead()` — in-place update of all notifications `isRead = true`

### notification_preference_provider.dart
`@riverpod` `AsyncNotifier<NotificationPreferenceModel>`. On `build()`: fetches preferences. `updatePreferences(NotificationPreferenceModel)` calls repository + updates state in-place.

### notification_actions_provider.dart
`@riverpod` `AsyncNotifier<void>`. Handles `markAsRead(id)` and `markAllRead()`. After each:
- Calls repository
- Calls `notificationListNotifierProvider.notifier.markRead(id)` or `markAllRead()`
- Calls `unreadCountProvider.notifier.decrement()` or `reset()`
- If offline: shows snackbar "You are offline — try again when connected"

---

## core/network/stomp_client.dart — Full Spec (replaces Phase 1 stub)

```
StompClientWrapper:
  - Uses stomp_dart_client package (already in pubspec)
  - StompConfig:
      url: wsBaseUrl from .env (ws:// dev, wss:// prod)
      onConnect: subscribes to topics
      onStompError: logs error, schedules reconnect after 5s
      onDisconnect: logs
      onWebSocketError: logs, schedules reconnect after 5s
      reconnectDelay: Duration(seconds: 5)
      webSocketConnectHeaders: { 'Cookie': '<accessToken cookie value>' }
        ← read from cookieJarProvider at connect() time

  Subscriptions registered in onConnect callback:
    /topic/notifications/{userId} → onNotificationFrame(frame)
    /topic/system                 → onSystemFrame(frame)

  AppLifecycleObserver:
    - StompClientWrapper implements WidgetsBindingObserver
    - In didChangeAppLifecycleState: if resumed → reconnect()
    - Register observer in connect(), remove in disconnect()

  Web note:
    stomp_dart_client uses WebSocket which works on Web.
    No conditional import needed for STOMP itself.
    Cookie injection via headers works on native.
    On Web, the browser handles cookies automatically — skip manual header injection.
    Use kIsWeb to skip the cookie header injection step only.
```

---

## core/services/fcm_service.dart — Full Spec (replaces Phase 1 stub)

```
FcmService:
  Fields:
    - GoRouter _router (injected)
    - NotificationNavigationService _navService (injected)

  initialize():
    1. FirebaseMessaging.instance.requestPermission() — iOS requires this
    2. Get FCM token → store in SecureStorageService under key 'fcm_token'
    3. FirebaseMessaging.instance.onTokenRefresh.listen → update stored token
       + call _registerTokenWithBackend(newToken)
    4. FirebaseMessaging.onMessage.listen → foreground handler:
       → show local notification banner via flutter_local_notifications
    5. FirebaseMessaging.onMessageOpenedApp.listen → background tap handler:
       → _navService.navigateFromNotification(data)
    6. Check getInitialMessage() → killed-state tap handler:
       → _navService.navigateFromNotification(data)

  _registerTokenWithBackend(token):
    → PUT /notifications/preferences with fcmToken field
    → Confirm this field exists in NotificationPreferenceRequest backend DTO
    → If backend uses a separate endpoint, use that instead

  Web note:
    FCM works on Web via firebase_messaging Web SDK.
    flutter_local_notifications does NOT support Web — skip foreground banner on Web.
    Use kIsWeb guard around flutter_local_notifications calls.

  flutter_local_notifications setup:
    - Android: create notification channel 'taskhive_notifications'
    - iOS: uses APNs — no extra setup beyond firebase_messaging
    - Initialize in FcmService.initialize() before registering listeners
```

> ⚠️ `flutter_local_notifications` is NOT in the current pubspec. Add it before coding this file. Also add `google-services.json` (Android) and `GoogleService-Info.plist` (iOS) — these are Firebase config files that must be provided manually before the FCM code will work. The agent should flag this to the developer if these files are missing.

---

## core/services/notification_navigation_service.dart — Full Spec (replaces Phase 1 stub)

```
NotificationNavigationService:
  Fields: GoRouter _router (injected), String _userRole (from currentUserProvider)

  navigateFromNotification(Map<String, dynamic> data):
    entityType = data['entityType']
    entityId   = data['entityId']

    switch entityType:
      'TASK'     → role == 'ADMIN' ? push('/admin/tasks/$entityId')
                                   : push('/employee/tasks/$entityId')
      'EMPLOYEE' → role == 'ADMIN' ? push('/admin/employees/$entityId')
                                   : do nothing (employee cannot view employee detail)
      'SYSTEM'   → push('/employee/notifications')
      default    → push('/employee/notifications')

  Note: Use router.push() not router.go() so user can go back.
  Note: Check entityId != null before navigating — null entityId = go to notification list.
```

---

## Widgets — Specifications

### notification_badge.dart
`ConsumerWidget`. Param: `Widget child` (the nav icon). Watches `unreadCountProvider`. If count > 0: wraps `child` in a `Stack` with a red `CircleAvatar` (radius 8) in the top-right corner showing count (capped at "99+" if > 99). If count == 0 or loading: returns `child` unchanged. Used inside `AdminShellScreen` and `EmployeeShellScreen` to wrap the notifications `NavigationDestination` icon.

### notification_list_tile.dart
`StatelessWidget`. Param: `NotificationModel notification`, `VoidCallback onTap`. Layout: `ListTile` with:
- Leading: `Icon(notification.icon)` inside a `CircleAvatar` with light primary color background
- Title: `notification.title ?? ''`
- Subtitle: `notification.message ?? ''` (max 2 lines, overflow ellipsis)
- Trailing: Column with `AppDateUtils.timeAgo(createdAt)` + blue dot `CircleAvatar(radius: 4)` if `notification.isUnread`
- `ListTile` background: slightly tinted if unread (`AppColors.primary.withOpacity(0.05)`)
- `onTap`: calls parent's onTap (which marks read + navigates)

### notification_preference_tile.dart
`StatelessWidget`. Params: `String title`, `String? subtitle`, `bool value`, `ValueChanged<bool> onChanged`. Layout: `SwitchListTile.adaptive` with title, optional subtitle, and switch. Pure display — no providers.

---

## Screens — Specifications

### notification_list_screen.dart
`ConsumerStatefulWidget`. Shared between ADMIN and EMPLOYEE (same screen, same data — backend filters by userId automatically).

Layout:
- AppBar: "Notifications" + `TextButton("Mark all read")` action (calls `notificationActionsProvider.markAllRead()`, shown only if unread count > 0)
- Body: `ListView.builder` with infinite scroll (`ScrollController` same pattern as Phase 2+3)
- Each item: `NotificationListTile` with `onTap`:
  1. Call `notificationActionsProvider.markAsRead(id)`
  2. Call `NotificationNavigationService.navigateFromNotification`
- Pull-to-refresh
- Empty state: `AppEmptyState(message: 'No notifications yet', icon: Icons.notifications_none)`
- `ref.listen` on `notificationActionsProvider` for error snackbars

### notification_preferences_screen.dart
`ConsumerWidget`. AppBar: "Notification Preferences". Watches `notificationPreferenceProvider`.

Layout sections:
1. **In-App Notifications** header → `NotificationPreferenceTile("In-App Notifications", value: prefs.inAppEnabled)`
2. **Email Notifications** header → `NotificationPreferenceTile("Email Notifications", value: prefs.emailEnabled)`
3. **Alert Types** header → tiles for `taskAssigned`, `taskOverdue`
4. **Daily Digest** section → toggle + `TimePicker` row (shown only when `dailyDigest == true`)
   - Tap time row → `showTimePicker` → updates `digestTime` string

On any toggle change: call `notificationPreferenceProvider.notifier.updatePreferences(updatedPrefs)` immediately (no save button — auto-save on each toggle). Show `AppSnackbar.showSuccess` on save. Show `AppSnackbar.showError` on failure and revert toggle.

---

## Router Extension — app_router.dart

Add these routes. They sit outside the `StatefulShellRoute` branches (they are pushed on top, not part of the bottom nav shell):

```dart
// Add to routes list — outside StatefulShellRoute (pushed on top)
GoRoute(
  path: AppRoutes.employeeNotifications,
  builder: (_, __) => const NotificationListScreen(),
),
GoRoute(
  path: AppRoutes.employeeNotificationPreferences,
  builder: (_, __) => const NotificationPreferencesScreen(),
),
```

> Note: The Notifications tab in `EmployeeShellScreen` already navigates to `AppRoutes.employeeNotifications` via the bottom nav — this just wires the route to the real screen replacing the Phase 1 placeholder. Check whether `employeeNotifications` is currently inside a `StatefulShellBranch` — if so, replace the placeholder `GoRoute` builder there instead of adding a new top-level route. Do not duplicate the route.

**Add imports to `app_router.dart`:**
```dart
import '../../features/notification/presentation/screens/notification_list_screen.dart';
import '../../features/notification/presentation/screens/notification_preferences_screen.dart';
```

---

## main.dart Modifications

Phase 1 `main.dart` has Firebase commented out. Uncomment and add:

```dart
// Uncomment in main.dart:
await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);

// After ProviderScope is created, initialize FCM:
// (do this inside the app startup, after ProviderScope, in a post-frame callback
//  or via a Riverpod provider that auto-initializes)
```

> The FCM initialization requires `google-services.json` (Android) and `GoogleService-Info.plist` (iOS). If these files are missing from the project, the agent must stop and flag this to the developer — do not proceed with FCM code without these files.

---

## ios/Runner/AppDelegate.swift Modifications

Phase 1 has these lines commented out — uncomment them:
```swift
// Uncomment:
import Firebase
import FirebaseMessaging
// In application(_:didFinishLaunchingWithOptions:):
FirebaseApp.configure()
```

## ios/Runner/Info.plist Additions

Add background modes (not present in Phase 1 Info.plist):
```xml
<key>FirebaseAppDelegateProxyEnabled</key>
<false/>
<key>UIBackgroundModes</key>
<array>
    <string>fetch</string>
    <string>remote-notification</string>
</array>
```

---

## Shell Screen Modifications (Badge Integration)

`AdminShellScreen` and `EmployeeShellScreen` (Phase 1 files) need their Notifications `NavigationDestination` icon wrapped with `NotificationBadge`.

**Find the Notifications destination in both shell screens:**
```dart
// BEFORE
NavigationDestination(
  icon: Icon(Icons.notifications_outlined),
  selectedIcon: Icon(Icons.notifications),
  label: 'Notifications',
),

// AFTER
NavigationDestination(
  icon: NotificationBadge(child: Icon(Icons.notifications_outlined)),
  selectedIcon: NotificationBadge(child: Icon(Icons.notifications)),
  label: 'Notifications',
),
```

Add import for `NotificationBadge` in both shell screen files.

---

## Offline Caching Behaviour (Phase 4)

| Action | Cache Behaviour |
|--------|----------------|
| Open notification list | Show `notifications_box` instantly → background fetch → update silently |
| New STOMP notification | Prepended to list in-memory; `notifications_box` updated |
| Unread count on app start | Read from `analytics_box['unread_count']` instantly |
| Mark as read | Requires connectivity — show snackbar if offline |
| STOMP connection | Only attempted when online — no offline STOMP connection |
| FCM push | OS delivers regardless of app state — no Flutter cache needed |

---

## pubspec.yaml Additions

These packages need to be added before coding:

```yaml
# Add to dependencies:
flutter_local_notifications: ^17.0.0   # Foreground FCM banner
```

> `firebase_core`, `firebase_messaging`, `stomp_dart_client` are already declared in the Phase 1 pubspec.
> `flutter_local_notifications` is NOT present — add it.
> After adding, run `flutter pub get` before any code generation.

---

## Sub-Phase Split (Recommended Build Order)

| Sub-Phase | Files | Dependency |
|-----------|-------|------------|
| **4A** | 2 models → build_runner | Nothing new outside Phase 1–3 |
| **4B** | `stomp_client.dart` (full) + `notification_repository.dart` + 5 providers | Needs 4A models |
| **4C** | 3 widgets + 2 screens | Needs 4A + 4B |
| **4D** | Router + shell badge + `main.dart` + `AppDelegate.swift` + `Info.plist` + `fcm_service.dart` (full) + `notification_navigation_service.dart` (full) + full verify | Needs 4C + Firebase config files present |

> **4D is gated on Firebase config files.** Before starting 4D, confirm `google-services.json` and `GoogleService-Info.plist` exist in the project. If missing, agent must stop and ask the developer to provide them.

---

## Phase 4 File Count

| Layer | Count |
|-------|-------|
| Models | 2 |
| Repository | 1 |
| Providers | 5 |
| Core — STOMP (modified) | 1 |
| Core — FCM (modified) | 1 |
| Core — Nav Service (modified) | 1 |
| Widgets | 3 |
| Screens | 2 |
| Router (modified) | 1 |
| Shell screens (modified) | 2 |
| main.dart (modified) | 1 |
| iOS config (modified) | 2 |
| **Total Files Touched** | **23** |

> New files: 14. Modified existing files: 9.
> Generated files (`.freezed.dart`, `.g.dart`) not counted.
