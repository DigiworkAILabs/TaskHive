# TaskHive Flutter — Phase 5: Audit Module
## Version 1.0 | SRS v2.4 | Digiwork
## ⚠️ Always provide `TaskHive_Flutter_Master_Context.md` alongside this file

---

## ⚠️ Nullability Rule — Enforced Without Exception
Never use the Dart null assertion operator (`!`) on API response data. Every model field that is not a primary key must be nullable unless confirmed non-null in both:
1. `taskhive-backend/.../audit/dto/AuditLogResponse.java`
2. `taskhive-backend/.../audit/dto/SecurityEventResponse.java`
3. `taskhive-frontend/src/features/audit/` (if exists — mirror its types)

When in doubt, make it nullable.

---

## ⚠️ Lessons From Previous Phases — Read Before Coding

These are real errors encountered in Phases 1–4. Do not repeat them.

| Category | Rule |
|----------|------|
| **Import paths** | Use package-relative imports (`package:taskhive_flutter/...`) wherever possible. Verify relative path depth (`../../../`) before writing. |
| **File naming** | Singular filenames only. `audit_log_model.dart` not `audit_logs_model.dart`. Match Master Context exactly. |
| **Logger** | Use `logger.dart` and `appLogger.i()` API. Do not create a new logger file. |
| **API param names** | Do not guess query param names. Read the backend controller `@RequestParam` annotations directly. Postman collection is ground truth. |
| **Multipart field name** | All file uploads use field name `"file"` — confirmed in Phase 2+3. |
| **Null crashes** | `Null check operator used on a null value` — caused by `required` fields on nullable backend data. Make fields nullable first, tighten later. |
| **Form dirty flag** | Any screen with editable fields must use `_dirty` flag to prevent network refresh overwriting user input. |
| **context.mounted** | Always check `context.mounted` after every `async` gap before calling `AppSnackbar`, `context.go`, or `setState`. |
| **build_runner** | Always run `dart run build_runner build --delete-conflicting-outputs` after every model change. Never manually edit `.freezed.dart` or `.g.dart`. |
| **Router duplicates** | Check if route already exists in `StatefulShellBranch` before adding as top-level route. Do not duplicate routes. |
| **ADMIN only** | Audit module is ADMIN-only. No employee routes. No employee role checks needed inside screens. |

---

## Project Flow Context

```
Phase 1 — Auth + Core Infrastructure           ✅ COMPLETE
Phase 2 — Employee Module                      ✅ COMPLETE
Phase 3 — Task Module                          ✅ COMPLETE
Phase 4 — Notification Module                  ✅ COMPLETE
Phase 5 — Audit Module                         ← YOU ARE HERE
Phase 6 — Analytics + Dashboards               (depends on Phase 3)
```

### What Phase 5 Is

The Audit module is a **read-only ADMIN-only** module. Flutter does not write any audit logs — the backend `AuditEventListener` records all actions automatically when domain events fire (employee created, task status changed, login failed, etc.). Flutter only **reads and displays** this data.

This makes Phase 5 one of the simpler phases — no mutations, no form submissions (except compliance report download), no optimistic updates, no offline queuing. The main complexity is in filtering, the JSON diff viewer, and the entity timeline.

### What Gets Logged (Backend — Flutter Just Displays)

```
AuditLog created when:               SecurityEvent created when:
  Employee created/updated/deleted     Login failed
  Employee activated/deactivated       Account locked
  Task created/updated/deleted         Unauthorized access attempt
  Task status changed                  Password changed
  Task assigned
  Notification preferences changed
```

### Role Behaviour

| Feature | ADMIN | EMPLOYEE |
|---------|-------|----------|
| View audit logs | ✅ | ❌ |
| Search/filter logs | ✅ | ❌ |
| View entity timeline | ✅ | ❌ |
| View security events | ✅ | ❌ |
| Download compliance report | ✅ | ❌ |

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 5 of 6 |
| **Name** | Audit Module |
| **Scope** | Audit log list + search + filter, entity timeline, security events, compliance report download |
| **Depends On** | Phase 1 (Auth, routing, core widgets) only — does NOT depend on Phase 2, 3, or 4 |
| **Total New Files** | 18 (+ 1 modified) |
| **Platforms** | Android ✅ iOS ✅ macOS ✅ Web ✅ Windows ✅ Linux ✅ |

### Deliverable
Admin can view all audit logs with date/action/entity/user/IP filters. Admin can view entity-specific timelines (all changes to a specific task or employee). Admin can view security events (failed logins, lockouts). Admin can download a compliance report. No caching needed — audit logs are always fetched fresh (stale data is unacceptable for compliance).

---

## Phase 5 — Complete File List (18 files)

```
lib/
│
└── features/
    └── audit/
        ├── data/
        │   ├── models/
        │   │   ├── audit_log_model.dart             # ★ New
        │   │   └── security_event_model.dart        # ★ New
        │   └── repositories/
        │       └── audit_repository.dart            # ★ New
        │
        ├── domain/
        │   └── providers/
        │       ├── audit_log_list_provider.dart     # ★ New
        │       ├── audit_log_search_provider.dart   # ★ New
        │       ├── entity_timeline_provider.dart    # ★ New
        │       └── security_events_provider.dart    # ★ New
        │
        └── presentation/
            ├── screens/
            │   ├── audit_screen.dart                # ★ New
            │   └── entity_timeline_screen.dart      # ★ New
            └── widgets/
                ├── audit_log_tile.dart              # ★ New
                ├── audit_filter_bar.dart            # ★ New
                ├── security_event_tile.dart         # ★ New
                ├── entity_timeline_tile.dart        # ★ New
                ├── json_diff_viewer.dart            # ★ New
                ├── compliance_report_button.dart    # ★ New
                ├── compliance_download_stub.dart    # ★ New — no-op stub (required by conditional import)
                ├── compliance_download_native.dart  # ★ New — path_provider save (Android/iOS/desktop)
                └── compliance_download_web.dart     # ★ New — dart:html blob download (Web only)
```

> `compliance_download_stub.dart`, `compliance_download_native.dart`, and `compliance_download_web.dart` follow the exact same conditional import pattern established in Phase 1 with `cookie_jar_factory_stub.dart` / `cookie_jar_factory_native.dart` / `cookie_jar_factory_web.dart`. Before creating them, check if a generic file download helper already exists in the project from Phase 2/3 (profile photo upload may have created one). If a web download helper exists, reuse it rather than creating a duplicate.

**Modified files:**
- `lib/core/router/app_router.dart` — replace `adminAudit` placeholder + add `adminAuditEntityTimeline` route

> `api_endpoints.dart` already has all audit endpoints from Phase 1 — no changes needed.
> `app_routes.dart` already has `adminAudit` and `adminAuditEntityTimeline` constants — no changes needed.
> No Hive cache box needed — audit data is always fetched fresh.
> No `tasks_box` or `employees_box` changes — audit is independent.

---

## API Endpoints Used in Phase 5

```
GET    /api/v1/audit/logs                       # All logs paginated (ADMIN only)
GET    /api/v1/audit/logs/search                # Filter by date, action, entity, user, IP
GET    /api/v1/audit/logs/entity/{type}/{id}    # Entity-specific timeline
GET    /api/v1/audit/security-events            # Failed logins, lockouts, unauthorized access
GET    /api/v1/audit/compliance/report          # Generate + download compliance report
```

### Response Shapes

```json
// GET /api/v1/audit/logs
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "actorId": "uuid",
        "actorEmail": "admin@taskhive.com",
        "action": "EMPLOYEE_CREATED",
        "entityType": "EMPLOYEE",
        "entityId": "uuid",
        "beforeState": null,
        "afterState": { "firstName": "Jane", "status": "PENDING" },
        "metadata": null,
        "ipAddress": "192.168.1.1",
        "userAgent": "Mozilla/5.0...",
        "createdAt": "2026-03-01T10:00:00Z"
      }
    ],
    "totalElements": 240,
    "totalPages": 24,
    "size": 10,
    "number": 0
  }
}

// GET /api/v1/audit/security-events
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "eventType": "LOGIN_FAILED",
        "userId": "uuid",
        "ipAddress": "192.168.1.1",
        "userAgent": "Mozilla/5.0...",
        "success": false,
        "details": { "email": "hacker@test.com", "attempts": 3 },
        "createdAt": "2026-03-01T10:00:00Z"
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "size": 10,
    "number": 0
  }
}

// GET /api/v1/audit/logs/entity/{type}/{id}
// Same paginated shape as /audit/logs but filtered to one entity

// GET /api/v1/audit/compliance/report
// Returns a file download (PDF or CSV) — handle as binary response via Dio
// Content-Type: application/pdf or text/csv
// Save to device via path_provider (native) or trigger browser download (web)

// Search query params — verify exact names against backend @RequestParam:
// fromDate, toDate, action, entityType, actorEmail, ipAddress, page, size
```

> ⚠️ Before writing the repository, open:
> `taskhive-backend/.../audit/controller/AuditController.java`
> Read every `@RequestParam` annotation name exactly. These are the ground truth query parameter names. Do not guess.

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-AUDIT-01 | Critical | Display audit logs (actor, action, entity, before/after state, IP, userAgent) — backend writes them, Flutter reads |
| FR-AUDIT-02 | Critical | Display security events (failed logins, lockouts, unauthorized access) in separate tab |
| FR-AUDIT-03 | High | ADMIN searches/filters audit logs by date range, action type, entity type, actor email, IP |
| FR-AUDIT-04 | High | ADMIN views entity-specific timeline (all changes to one task or employee) |
| FR-AUDIT-05 | Medium | ADMIN downloads compliance report via `compliance_report_button.dart` |
| FR-AUDIT-06 | Critical | Audit logs are never cached — always fetch fresh (no Hive for this module) |

---

## Data Models — Specifications

### audit_log_model.dart
`@freezed`. Verify every field name against `AuditLog.java` and `AuditLogResponse.java` backend files.

Fields (all nullable except `id` — confirmed from DB schema):
- `id` — `String` required
- `actorId` — `String?`
- `actorEmail` — `String?`
- `action` — `String?` (e.g. `'EMPLOYEE_CREATED'`, `'TASK_STATUS_CHANGED'`, `'LOGIN_FAILED'`)
- `entityType` — `String?` (e.g. `'EMPLOYEE'`, `'TASK'`)
- `entityId` — `String?`
- `beforeState` — `Map<String, dynamic>?` (JSONB from backend — may be null for creation events)
- `afterState` — `Map<String, dynamic>?` (JSONB — may be null for deletion events)
- `metadata` — `Map<String, dynamic>?`
- `ipAddress` — `String?`
- `userAgent` — `String?`
- `createdAt` — `String?`

Extension `AuditLogModelX`:
- `bool get hasStateDiff` → `beforeState != null && afterState != null`
- `bool get isCreation` → `beforeState == null && afterState != null`
- `bool get isDeletion` → `beforeState != null && afterState == null`
- `IconData get actionIcon` → map common action strings to icons: `EMPLOYEE_*` → `Icons.person`, `TASK_*` → `Icons.assignment`, `LOGIN_*` → `Icons.lock`, default → `Icons.history`

> `beforeState` and `afterState` are `Map<String, dynamic>?` not `String?`. The backend stores JSONB — Dio will deserialize this as a Map automatically. Do not try to `jsonDecode` a Map.

### security_event_model.dart
`@freezed`. Verify against `SecurityEvent.java` backend model.

Fields (all nullable — including `eventType`, since the global nullability rule applies and backend nullability must be confirmed before making any field required):
- `id` — `String` required (primary key only)
- `eventType` — `String?` ← nullable. Extension methods use `event.eventType ?? ''` — see `SecurityEventModelX` below
- `userId` — `String?`
- `ipAddress` — `String?`
- `userAgent` — `String?`
- `success` — `bool?`
- `details` — `Map<String, dynamic>?`
- `createdAt` — `String?`

Extension `SecurityEventModelX`:
- `Color get severityColor` → switch on `eventType ?? ''`: `'LOGIN_FAILED'`: orange, `'ACCOUNT_LOCKED'`: red, `'UNAUTHORIZED_ACCESS'`: red, default: grey
- `IconData get severityIcon` → switch on `eventType ?? ''`: `'LOGIN_FAILED'`: `Icons.login`, `'ACCOUNT_LOCKED'`: `Icons.lock_clock`, `'UNAUTHORIZED_ACCESS'`: `Icons.gpp_bad`, default: `Icons.warning`

---

## Repository — Specifications

### audit_repository.dart
Uses `dioClientProvider`. Annotate the class with `@riverpod` to generate `auditRepositoryProvider` — same pattern as all Phase 2/3/4 repositories. All methods are read-only (GET only).

| Method | Endpoint | Returns |
|--------|----------|---------|
| `getAuditLogs({page, size, ...filters})` | `GET /audit/logs` | `Map<String, dynamic>` raw paginated |
| `searchAuditLogs(AuditSearchFilter)` | `GET /audit/logs/search` | `Map<String, dynamic>` raw paginated |
| `getEntityTimeline(entityType, entityId, {page, size})` | `GET /audit/logs/entity/{type}/{id}` | `Map<String, dynamic>` raw paginated |
| `getSecurityEvents({page, size})` | `GET /audit/security-events` | `Map<String, dynamic>` raw paginated |
| `downloadComplianceReport()` | `GET /audit/compliance/report` | `List<int>` bytes |

For `downloadComplianceReport()`:
```dart
// Use Dio responseType: ResponseType.bytes
final response = await _dio.get(
  ApiEndpoints.complianceReport,
  options: Options(responseType: ResponseType.bytes),
);
return response.data as List<int>;
// Caller saves file using path_provider (native) or dart:html (web)
```

`AuditSearchFilter` — plain Dart class (not freezed, not serialized):
```dart
class AuditSearchFilter {
  final String? fromDate;    // ISO date "2026-01-01"
  final String? toDate;
  final String? action;
  final String? entityType;
  final String? actorEmail;
  final String? ipAddress;
  final int page;
  final int size;
}
```

---

## Providers — Specifications

### audit_log_list_provider.dart
`@riverpod` `AsyncNotifier`. No Hive cache — always fetches fresh.

Define `AuditLogListState` as a plain Dart class (not freezed) at the top of this file:
```dart
class AuditLogListState {
  final List<AuditLogModel> logs;
  final int totalElements;
  final int totalPages;
  final int currentPage;
  final bool isLoadingMore;
  const AuditLogListState({...});
  AuditLogListState copyWith({...});
}
```
This class is defined once in `audit_log_list_provider.dart` and imported by `audit_log_search_provider.dart` — do not redefine it.

`build()` fetches page 0 of audit logs with no filter active (calls `getAuditLogs`). Methods:
- `refresh()` — refetch page 0, reset filter state
- `loadMore()` — infinite scroll, same pattern as Phase 2+3
- `applyFilter(AuditSearchFilter filter)` — when filter is non-empty, delegates to `searchAuditLogs(filter)` instead of `getAuditLogs`. Switching between filtered/unfiltered resets to page 0.

### audit_log_search_provider.dart
`@riverpod` `AsyncNotifier<AuditLogListState>`. This provider holds the currently active `AuditSearchFilter` and delegates all data fetching to `auditRepositoryProvider.searchAuditLogs(filter)`. It is a separate provider so `AuditScreen` can watch search state independently of the unfiltered list state, making it trivial to clear filters and return to the full list.

Import `AuditLogListState` from `audit_log_list_provider.dart`. Methods: `search(AuditSearchFilter)`, `loadMore()`, `clear()`.

> These two providers have distinct responsibilities and are kept separate. Do not unify them.

### entity_timeline_provider.dart
`@riverpod` family provider. `Future<List<AuditLogModel>> entityTimeline(ref, String entityType, String entityId)`.

Single API call: `getEntityTimeline(entityType, entityId, page: 0, size: 100)`. Returns up to 100 entries. This is a Phase 5 simplification — add a `// TODO: full pagination for large entity histories` comment. Sort result oldest-first before returning (reverse if backend returns newest-first).

### security_events_provider.dart
`@riverpod` `AsyncNotifier`. Same pattern as `audit_log_list_provider` but calls `getSecurityEvents`. Supports `loadMore()` and `refresh()`. No filtering in Phase 5 (future enhancement).

---

## Widgets — Specifications

### audit_log_tile.dart
`StatelessWidget`. Param: `AuditLogModel log`, `VoidCallback? onTap`.

Layout: `ListTile`:
- Leading: `CircleAvatar` with `log.actionIcon`
- Title: `log.action ?? 'Unknown Action'` (bold) + `log.entityType ?? ''` (smaller, grey)
- Subtitle: `log.actorEmail ?? '—'` + `AppDateUtils.formatDateTime(log.createdAt)`
- Trailing: if `log.hasStateDiff` → small `Icon(Icons.compare_arrows)` indicating diff is viewable
- `onTap`: if provided, navigates to detail or opens diff bottom sheet

### audit_filter_bar.dart
`StatefulWidget`. Param: `ValueChanged<AuditSearchFilter> onFilterChanged`.

Layout: Vertical form (not a horizontal chip bar — too many fields). Fields:
- Date range: two `AppTextField` (readOnly, tap opens `showDatePicker`) for From and To dates
- Action type: `DropdownButtonFormField` — **hardcode the options list** (there is no backend endpoint to fetch action types). Values:
  ```
  All (null), EMPLOYEE_CREATED, EMPLOYEE_UPDATED, EMPLOYEE_DELETED,
  EMPLOYEE_ACTIVATED, EMPLOYEE_DEACTIVATED,
  TASK_CREATED, TASK_UPDATED, TASK_DELETED, TASK_STATUS_CHANGED, TASK_ASSIGNED,
  LOGIN_FAILED, ACCOUNT_LOCKED, UNAUTHORIZED_ACCESS,
  PASSWORD_CHANGED, NOTIFICATION_PREFERENCES_UPDATED
  ```
  Confirm these string values match the `action` values returned in actual API responses by reading `AuditEventListener.java` before coding.
- Entity type: `DropdownButtonFormField` — options: All, EMPLOYEE, TASK, AUTH
- Actor email: `AppTextField` (free text)
- IP address: `AppTextField` (free text)
- Apply button + Clear button row

Shown inside a collapsible panel (use `ExpansionTile`) in the audit screen so it does not take up permanent screen space.

### security_event_tile.dart
`StatelessWidget`. Param: `SecurityEventModel event`.

Layout: `ListTile`:
- Leading: `CircleAvatar(backgroundColor: event.severityColor)` with `event.severityIcon`
- Title: `event.eventType ?? 'Unknown Event'`
- Subtitle: IP address + timestamp
- Trailing: `AppBadge` showing `event.success == true ? 'SUCCESS' : 'FAILED'` with green/red color

### entity_timeline_tile.dart
`StatelessWidget`. Param: `AuditLogModel log`.

Similar to `audit_log_tile` but styled as a timeline entry:
- Left side: vertical line + dot (use `CustomPaint` or simple `Container` with border)
- Right side: action label, actor email, timestamp, and if `log.hasStateDiff` → `JsonDiffViewer(before: log.beforeState, after: log.afterState)` shown inline (collapsed by default, expandable on tap)

### json_diff_viewer.dart
`StatelessWidget`. Params: `Map<String, dynamic>? before`, `Map<String, dynamic>? after`.

Shows a simple key-by-key diff:
- For each key present in either map: show key name + old value (red, struck-through if changed) + new value (green if changed)
- Keys only in `after` (additions): shown in green
- Keys only in `before` (removals): shown in red struck-through
- Unchanged keys: shown in default color

Do not use any external diff library — implement manually by iterating the maps. Keep it simple — this is a display widget, not a code editor.

> `beforeState` and `afterState` arrive as `Map<String, dynamic>?` directly from the freezed model. Do not attempt to `jsonDecode` them — they are already parsed by Dio.

### compliance_report_button.dart
`ConsumerStatefulWidget`. No params.

Layout: `FilledButton.icon(icon: Icons.download, label: Text('Download Compliance Report'))`.

On tap:
1. Show loading indicator on button
2. Call `audit_repository.downloadComplianceReport()` → get `List<int>` bytes
3. On native: save to downloads folder via `path_provider` → show `AppSnackbar.showSuccess('Report saved to Downloads')`
4. On Web (`kIsWeb`): trigger browser download via `dart:html` `AnchorElement` with blob URL
5. On error: `AppSnackbar.showError`

```dart
// Web download pattern:
import 'dart:html' as html;  // conditional import needed — see Rule 1 Master Context
final blob = html.Blob([bytes]);
final url = html.Url.createObjectUrlFromBlob(blob);
final anchor = html.AnchorElement(href: url)
  ..setAttribute('download', 'compliance_report.pdf')
  ..click();
html.Url.revokeObjectUrl(url);
```

> Use conditional import for `dart:html` — same pattern as `cookie_jar_factory_web.dart` from Phase 1. Never import `dart:html` at top level. Use `dart:html` only in the web implementation file.

---

## Screens — Specifications

### audit_screen.dart
`ConsumerStatefulWidget`. ADMIN only. This is the main audit screen navigated to from the Admin shell Audit tab.

Layout: `DefaultTabController` with 2 tabs:
- **Tab 1 — Audit Logs**: `AuditFilterBar` (collapsible) + `ListView.builder` (infinite scroll) of `AuditLogTile`. Each tile tap calls `showModalBottomSheet` with a private widget `_AuditLogDetailSheet` defined at the bottom of `audit_screen.dart` (not a separate file). `_AuditLogDetailSheet` shows: full log fields, `JsonDiffViewer` if `log.hasStateDiff`, and a "View Entity Timeline" button.

  The "View Entity Timeline" button navigation call:
  ```dart
  context.go('/admin/audit/entity/${log.entityType}/${log.entityId}');
  ```
  Only show this button when `log.entityType != null && log.entityId != null`.

- **Tab 2 — Security Events**: `ListView.builder` of `SecurityEventTile`. Pull-to-refresh.

AppBar actions:
- `ComplianceReportButton` (download icon button)
- Refresh icon

`ref.listen` on both providers for error snackbars. Pull-to-refresh on both tabs independently.

### entity_timeline_screen.dart
`ConsumerWidget`. Params: `String entityType`, `String entityId`.

AppBar title: `'$entityType Timeline'`. Body: watches `entityTimelineProvider(entityType, entityId)`.

On loading: `AppLoading`. On error: `AppErrorWidget`. On data: `ListView` of `EntityTimelineTile` widgets in chronological order (oldest first — reverse the list if backend returns newest first).

If list is empty: `AppEmptyState(message: 'No history found for this entity.')`.

---

## Router Extension — app_router.dart

**Replace `adminAudit` placeholder:**
```dart
// BEFORE (Phase 1 placeholder inside StatefulShellBranch)
GoRoute(
  path: AppRoutes.adminAudit,
  builder: (_, __) => const _PlaceholderScreen(title: 'Audit'),
),

// AFTER
GoRoute(
  path: AppRoutes.adminAudit,
  builder: (_, __) => const AuditScreen(),
),
```

**Add `adminAuditEntityTimeline` as a sub-route:**
```dart
// Inside the adminAudit StatefulShellBranch, nest the timeline route:
GoRoute(
  path: AppRoutes.adminAudit,
  builder: (_, __) => const AuditScreen(),
  routes: [
    GoRoute(
      path: 'entity/:type/:id',
      builder: (_, state) => EntityTimelineScreen(
        entityType: state.pathParameters['type']!,
        entityId: state.pathParameters['id']!,
      ),
    ),
  ],
),
```

**Add imports:**
```dart
import '../../features/audit/presentation/screens/audit_screen.dart';
import '../../features/audit/presentation/screens/entity_timeline_screen.dart';
```

---

## Offline Behaviour (Phase 5)

Audit data is **never cached**. This is intentional — stale audit data is unacceptable for compliance.

| State | Behaviour |
|-------|-----------|
| Online | Fetch fresh from API always |
| Offline | Show `AppErrorWidget` with message "Audit logs require an internet connection" |
| Offline banner | Already shown globally by `app.dart` — no additional handling needed in audit screens |

---

## Sub-Phase Split (Recommended Build Order)

| Sub-Phase | Files | Dependency |
|-----------|-------|------------|
| **5A** | 2 models → build_runner | Nothing outside Phase 1 |
| **5B** | 1 repository + 4 providers | Needs 5A |
| **5C** | 6 feature widgets + 3 conditional import helpers (compliance download) | Needs 5A + 5B |
| **5D** | 2 screens + router + full verify | Needs 5C |

> **5D special check**: Before implementing `compliance_report_button.dart` and the web download path, confirm whether `dart:html` conditional import file already exists in the project from Phase 2/3 file uploads. If a web stub already exists, reuse it. Do not create a duplicate.

---

## Phase 5 File Count

| Layer | Count |
|-------|-------|
| Models | 2 |
| Repository | 1 |
| Providers | 4 |
| Widgets (feature) | 6 |
| Widgets (conditional import helpers) | 3 |
| Screens | 2 |
| Router (modified) | 1 |
| **Total New Files** | **18** |
| **Total Modified Files** | **1** |

> No pubspec changes needed — all required packages (`path_provider`, `dio`) are already declared.
> `flutter_local_notifications`, `firebase_*` — not touched in this phase.
> Generated files (`.freezed.dart`, `.g.dart`) not counted.
