# TaskHive Flutter — Phase 6: Analytics Module
## Version 1.0 | SRS v2.4 | Digiwork
## ⚠️ Always provide `TaskHive_Flutter_Master_Context.md` alongside this file

---

## ⚠️ NON-NEGOTIABLE RULES — Read Before Anything Else

1. **Never use `!` on API response data.** Use `?.` and `??` everywhere. Only `id` fields may be non-null.
2. **Never call `jsonDecode` on fields already parsed by Dio** (any `Map<String, dynamic>?` field).
3. **Always check `context.mounted` after every `async` gap** before calling `AppSnackbar`, `context.go`, or `setState`.
4. **Do not use patterns from training data.** Read existing Phase 3/4 repository + provider files and copy their exact structure.
5. **`flutter analyze` must return `No issues found` after every sub-phase** before proceeding.
6. **`stat_card.dart` is a Phase 1 stub** — replace it with the full implementation in Phase 6. Do not create a new file.
7. **`analytics_box` already exists** in `CacheService` from Phase 1 — do not create a new Hive box.
8. **`fl_chart: ^0.70.2` is already in `pubspec.yaml`** from Phase 1 — do not add it again.

---

## ⚠️ Lessons From Previous Phases — Read Before Coding

| Category | Rule |
|----------|------|
| **Null crashes** | `Null check operator used on a null value` — make all model fields nullable except `id`. Tighten only after backend DTO confirms non-null. |
| **Import paths** | Use package-relative imports. Verify `../` depth before writing. |
| **File naming** | Singular. `admin_dashboard_model.dart` not `admin_dashboard_models.dart`. Match Master Context exactly. |
| **Logger** | `appLogger.i()` from `logger.dart` only. Do not create a new logger. |
| **API param names** | Read `@RequestParam` annotations in `AnalyticsController.java` directly. Do not guess. |
| **500 errors** | If an endpoint returns 500, check backend logs first. Do not change Flutter error handling to swallow it — find the root cause. |
| **build_runner** | `dart run build_runner build --delete-conflicting-outputs` after every model change. Never edit `.freezed.dart` or `.g.dart` manually. |
| **Router duplicates** | Check if route already exists in `StatefulShellBranch` before adding. Do not duplicate. |
| **context.mounted** | Check after every `async` gap before UI interaction. |
| **Hive cache** | `analytics_box` is the only box for this module. Already declared in `CacheService`. |
| **fl_chart** | Already in pubspec. Do not add again. Import: `import 'package:fl_chart/fl_chart.dart';` |

---

## Project Flow Context

```
Phase 1 — Auth + Core Infrastructure           ✅ COMPLETE
Phase 2 — Employee Module                      ✅ COMPLETE
Phase 3 — Task Module                          ✅ COMPLETE
Phase 4 — Notification Module                  ✅ COMPLETE
Phase 5 — Audit Module                         ✅ COMPLETE
Phase 6 — Analytics + Dashboards               ← YOU ARE HERE (FINAL PHASE)
```

### What Phase 6 Builds On

- **Phase 1**: `stat_card.dart` (stub → full), `analytics_box` Hive box, `fl_chart` in pubspec, admin + employee dashboard placeholder routes
- **Phase 2**: `EmployeeModel` — referenced in `EmployeePerformanceModel`
- **Phase 3**: Task status/priority enums — referenced in chart color mapping
- **Phase 4**: Nothing directly
- **Phase 5**: Nothing directly

### Architecture Overview

```
Backend nightly scheduler (2AM)
  → Pre-calculates metrics → stores in daily_metrics + employee_performance_cache tables

Flutter request (on screen open)
  → GET /analytics/dashboard/admin or /employee
  → Backend reads from pre-calculated cache tables (fast — no heavy queries)
  → Flutter caches response in analytics_box (Hive)
  → Charts render from cached data instantly on next open
```

### Role Behaviour

| Feature | ADMIN | EMPLOYEE |
|---------|-------|----------|
| Admin overview KPIs | ✅ | ❌ |
| Task distribution pie chart | ✅ | ❌ |
| Task priority donut chart | ✅ | ❌ |
| 30-day completion trend line | ✅ | ❌ |
| Employee performance bar + table | ✅ | ❌ |
| Export compliance report (CSV/Excel/PDF) | ✅ | ❌ |
| Personal task stats (my tasks by status) | ❌ | ✅ |
| Monthly productivity trend (employee) | ❌ | ✅ |
| On-time completion rate (employee) | ❌ | ✅ |

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 6 of 6 — FINAL |
| **Name** | Analytics + Dashboards |
| **Scope** | Admin KPI dashboard + charts, employee personal stats, report export |
| **Depends On** | Phase 1 (core, routing, stat_card stub, analytics_box, fl_chart) + Phase 3 (task enums for colors) |
| **Total New Files** | 22 |
| **Total Modified Files** | 3 (`stat_card.dart`, `app_router.dart`, `admin_shell_screen.dart` or `employee_shell_screen.dart` if dashboard tab needs wiring) |
| **Platforms** | Android ✅ iOS ✅ macOS ✅ Web ✅ Windows ✅ Linux ✅ |

---

## Phase 6 — Complete File List

```
lib/
│
├── core/
│   └── widgets/
│       └── stat_card.dart                          # MODIFY — replace Phase 1 stub with full impl
│
└── features/
    └── analytics/
        ├── data/
        │   ├── models/
        │   │   ├── admin_dashboard_model.dart       # ★ New
        │   │   ├── employee_dashboard_model.dart    # ★ New
        │   │   ├── task_distribution_model.dart     # ★ New
        │   │   ├── task_completion_trend_model.dart # ★ New
        │   │   ├── employee_performance_model.dart  # ★ New
        │   │   └── report_export_request.dart       # ★ New (plain Dart class — not freezed)
        │   └── repositories/
        │       └── analytics_repository.dart        # ★ New
        │
        ├── domain/
        │   └── providers/
        │       ├── admin_dashboard_provider.dart    # ★ New
        │       ├── employee_dashboard_provider.dart # ★ New
        │       ├── task_distribution_provider.dart  # ★ New
        │       ├── completion_trend_provider.dart   # ★ New
        │       ├── employee_performance_provider.dart  # ★ New
        │       └── report_export_provider.dart      # ★ New
        │
        └── presentation/
            ├── screens/
            │   ├── admin_analytics_screen.dart      # ★ New
            │   └── employee_analytics_screen.dart   # ★ New
            └── widgets/
                ├── admin_kpi_row.dart               # ★ New
                ├── task_status_pie_chart.dart        # ★ New
                ├── task_priority_donut_chart.dart    # ★ New
                ├── completion_trend_line_chart.dart  # ★ New
                ├── employee_performance_bar_chart.dart  # ★ New
                ├── employee_performance_table.dart   # ★ New
                ├── employee_stats_row.dart           # ★ New
                ├── monthly_trend_chart.dart          # ★ New
                └── report_export_sheet.dart          # ★ New
```

**Modified files:**
- `lib/core/widgets/stat_card.dart` — Phase 1 stub → full parameterized implementation
- `lib/core/router/app_router.dart` — replace admin + employee dashboard placeholders and analytics placeholders with real screens
- No pubspec changes — `fl_chart` already declared from Phase 1

> `api_endpoints.dart` already has all analytics endpoints from Phase 1 — no changes needed.
> `app_routes.dart` already has all analytics + dashboard route constants — no changes needed.
> `analytics_box` already declared in `cache_service.dart` — no changes needed.

---

## API Endpoints Used in Phase 6

```
GET    /api/v1/analytics/dashboard/admin            # Admin KPI overview
GET    /api/v1/analytics/dashboard/employee         # Employee personal stats
GET    /api/v1/analytics/tasks/distribution         # Pie chart — by status
GET    /api/v1/analytics/tasks/by-priority          # Donut chart — by priority
GET    /api/v1/analytics/tasks/completion-trend     # Line chart — 30-day rolling
GET    /api/v1/analytics/employees/performance      # Bar chart + table
POST   /api/v1/analytics/reports/export             # Trigger async report generation
GET    /api/v1/analytics/reports/{id}/download      # Download generated report (24hr link)
```

### Response Shapes

```json
// GET /api/v1/analytics/dashboard/admin
{
  "success": true,
  "data": {
    "totalTasks": 120,
    "activeTasks": 45,
    "overdueTasks": 8,
    "completedTasks": 67,
    "completionRate": 55.8,
    "avgCompletionHours": 18.4,
    "totalEmployees": 12
  }
}

// GET /api/v1/analytics/dashboard/employee
{
  "success": true,
  "data": {
    "myTotalTasks": 15,
    "myActiveTasks": 6,
    "myCompletedTasks": 9,
    "myOverdueTasks": 2,
    "onTimeRate": 80.0,
    "avgCompletionHours": 14.2
  }
}

// GET /api/v1/analytics/tasks/distribution
{
  "success": true,
  "data": [
    { "status": "TODO", "count": 30, "percentage": 25.0 },
    { "status": "IN_PROGRESS", "count": 45, "percentage": 37.5 },
    { "status": "IN_REVIEW", "count": 15, "percentage": 12.5 },
    { "status": "DONE", "count": 25, "percentage": 20.8 },
    { "status": "CANCELLED", "count": 5, "percentage": 4.2 }
  ]
}

// GET /api/v1/analytics/tasks/by-priority
{
  "success": true,
  "data": [
    { "priority": "LOW", "count": 20, "percentage": 16.7 },
    { "priority": "MEDIUM", "count": 55, "percentage": 45.8 },
    { "priority": "HIGH", "count": 35, "percentage": 29.2 },
    { "priority": "CRITICAL", "count": 10, "percentage": 8.3 }
  ]
}

// GET /api/v1/analytics/tasks/completion-trend
{
  "success": true,
  "data": [
    { "date": "2026-02-08", "completed": 3, "created": 5 },
    { "date": "2026-02-09", "completed": 2, "created": 1 },
    ...
  ]
}

// GET /api/v1/analytics/employees/performance
{
  "success": true,
  "data": [
    {
      "employeeId": "uuid",
      "employeeName": "Jane Doe",
      "tasksAssigned": 20,
      "tasksCompleted": 18,
      "onTimeRate": 90.0,
      "avgCompletionHours": 12.5
    }
  ]
}

// POST /api/v1/analytics/reports/export
// Request body:
{ "reportType": "TASK_SUMMARY", "format": "PDF", "fromDate": "2026-01-01", "toDate": "2026-03-01" }
// Response:
{ "success": true, "data": { "reportId": "uuid", "status": "PENDING", "estimatedSeconds": 30 } }

// GET /api/v1/analytics/reports/{id}/download
// Returns: binary file download (same pattern as Phase 5 compliance report)
// Content-Type: application/pdf or text/csv or application/vnd.openxmlformats...
```

> ⚠️ Verify ALL field names against:
> - `taskhive-backend/.../analytics/dto/AdminDashboardResponse.java`
> - `taskhive-backend/.../analytics/dto/EmployeeDashboardResponse.java`
> - `taskhive-frontend/src/features/analytics/types/analytics.types.ts`
>
> The Next.js frontend is fully working — mirror its TypeScript field names exactly. Field names in this spec are best-guess — the backend DTOs are the ground truth.

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-ANA-01 | High | Admin dashboard KPIs: total/active/overdue/completed tasks, completion rate, avg completion time, total employees |
| FR-ANA-02 | High | Admin charts: task status pie, task priority donut, completion trend 30-day line, employee performance bar |
| FR-ANA-03 | Medium | Employee dashboard: my tasks by status, monthly productivity trend, on-time completion rate |
| FR-ANA-04 | Medium | Data comes from pre-calculated backend cache — Flutter does not trigger heavy queries |
| FR-ANA-05 | Medium | Report export as CSV/Excel/PDF — async generation + polling + download |
| FR-ANA-06 | Low | Report download links expire after 24 hours — show expiry info in UI |

---

## Data Models — Specifications

### admin_dashboard_model.dart
`@freezed`. All numeric fields nullable — backend may return null for metrics if no data exists yet.

Fields:
- `totalTasks` — `int?`
- `activeTasks` — `int?`
- `overdueTasks` — `int?`
- `completedTasks` — `int?`
- `completionRate` — `double?`
- `avgCompletionHours` — `double?`
- `totalEmployees` — `int?`

### employee_dashboard_model.dart
`@freezed`. All fields nullable.

Fields:
- `myTotalTasks` — `int?`
- `myActiveTasks` — `int?`
- `myCompletedTasks` — `int?`
- `myOverdueTasks` — `int?`
- `onTimeRate` — `double?`
- `avgCompletionHours` — `double?`

### task_distribution_model.dart
`@freezed`. Represents one slice of a pie/donut chart.

Fields:
- `status` — `String?` (for status distribution) OR `priority` — `String?` (for priority distribution)

> Use a single model for both distribution endpoints. Add both `status` and `priority` as nullable fields — whichever is returned, the other is null.

- `status` — `String?`
- `priority` — `String?`
- `count` — `int?`
- `percentage` — `double?`

### task_completion_trend_model.dart
`@freezed`. One data point on the line chart.

Fields:
- `date` — `String?` (ISO date string `"2026-02-08"`)
- `completed` — `int?`
- `created` — `int?`

### employee_performance_model.dart
`@freezed`. One row in the performance table/bar chart.

Fields:
- `employeeId` — `String?`
- `employeeName` — `String?`
- `tasksAssigned` — `int?`
- `tasksCompleted` — `int?`
- `onTimeRate` — `double?`
- `avgCompletionHours` — `double?`

### report_export_request.dart
Plain Dart class — **not freezed**, not code-generated. Simple request model.

```dart
class ReportExportRequest {
  final String reportType;   // 'TASK_SUMMARY', 'EMPLOYEE_PERFORMANCE', 'AUDIT_SUMMARY'
  final String format;       // 'PDF', 'CSV', 'EXCEL'
  final String? fromDate;    // ISO date "2026-01-01"
  final String? toDate;
  const ReportExportRequest({required this.reportType, required this.format, this.fromDate, this.toDate});
  Map<String, dynamic> toJson() => { 'reportType': reportType, 'format': format, if (fromDate != null) 'fromDate': fromDate, if (toDate != null) 'toDate': toDate };
}
```

> Verify exact field names and allowed `reportType` / `format` values in `ReportExportRequest.java` backend DTO before coding.

---

## Repository — Specification

### analytics_repository.dart
`@riverpod` annotation required — generates `analyticsRepositoryProvider`. Uses `dioClientProvider`.

| Method | Endpoint | Returns |
|--------|----------|---------|
| `getAdminDashboard()` | `GET /analytics/dashboard/admin` | `AdminDashboardModel` |
| `getEmployeeDashboard()` | `GET /analytics/dashboard/employee` | `EmployeeDashboardModel` |
| `getTaskDistribution()` | `GET /analytics/tasks/distribution` | `List<TaskDistributionModel>` |
| `getTasksByPriority()` | `GET /analytics/tasks/by-priority` | `List<TaskDistributionModel>` |
| `getCompletionTrend()` | `GET /analytics/tasks/completion-trend` | `List<TaskCompletionTrendModel>` |
| `getEmployeePerformance()` | `GET /analytics/employees/performance` | `List<EmployeePerformanceModel>` |
| `exportReport(ReportExportRequest)` | `POST /analytics/reports/export` | `Map<String, dynamic>` (contains reportId) |
| `downloadReport(reportId)` | `GET /analytics/reports/{id}/download` | `List<int>` bytes (same pattern as Phase 5 compliance report) |

---

## Providers — Specifications

### admin_dashboard_provider.dart
`@riverpod` `AsyncNotifier<AdminDashboardModel>`. Cache-then-network using `analytics_box`.

```
build():
  1. Read analytics_box key 'admin_dashboard' → emit cached data immediately if present
  2. Fetch getAdminDashboard() from repository in background
  3. On success → update analytics_box['admin_dashboard'] + update state
  4. On failure + cache exists → keep cache, connectivity provider shows offline banner
  5. On failure + no cache → error state
```

Method: `refresh()` — force refetch bypassing cache.

### employee_dashboard_provider.dart
Same pattern as `admin_dashboard_provider` but uses `getEmployeeDashboard()` and key `'employee_dashboard'` in `analytics_box`.

### task_distribution_provider.dart
`@riverpod` `AsyncNotifier`. Fetches BOTH `getTaskDistribution()` and `getTasksByPriority()` in parallel using `Future.wait`. Returns a state class `TaskDistributionState` (plain Dart class defined at top of this file):

```dart
class TaskDistributionState {
  final List<TaskDistributionModel> byStatus;
  final List<TaskDistributionModel> byPriority;
  const TaskDistributionState({required this.byStatus, required this.byPriority});
}
```

Cached in `analytics_box` under key `'task_distribution'` as a serialized map.

### completion_trend_provider.dart
`@riverpod` `AsyncNotifier<List<TaskCompletionTrendModel>>`. Fetches `getCompletionTrend()`. Cached in `analytics_box` under key `'completion_trend'`.

### employee_performance_provider.dart
`@riverpod` `AsyncNotifier<List<EmployeePerformanceModel>>`. Fetches `getEmployeePerformance()`. Cached in `analytics_box` under key `'employee_performance'`.

### report_export_provider.dart
`@riverpod` `AsyncNotifier<void>`. No caching.

Methods:
- `exportReport(ReportExportRequest)` → POST to backend → returns `reportId`
- `downloadReport(reportId)` → GET bytes → saves file (same conditional import pattern as Phase 5 `compliance_download_web/native/stub.dart` — reuse those files)
- State tracks: idle / exporting / downloading / done / error
- Show `AppSnackbar.showSuccess` on download complete, `AppSnackbar.showError` on failure

> Report polling: If the backend returns `status: "PENDING"` on export, implement a simple poll: wait 3 seconds, retry `downloadReport` up to 5 times. If still pending after 5 attempts, show snackbar "Report is taking longer than expected — try downloading again in a moment."

---

## core/widgets/stat_card.dart — Full Implementation (replaces Phase 1 stub)

```dart
// Full StatCard replaces the Phase 1 stub.
// Parameters:
//   title: String           — label shown above value
//   value: String           — main number/text displayed large
//   subtitle: String?       — optional smaller text below value
//   icon: IconData?         — optional leading icon
//   iconColor: Color?       — icon color (defaults to primary)
//   trend: double?          — if provided, shows a small up/down arrow with percentage
//   trendPositive: bool     — true = green arrow up, false = red arrow down
//   onTap: VoidCallback?    — optional tap handler
//
// Layout: Card with padding:
//   Row: [Icon (if provided)] + Column: [title (small grey), value (large bold), subtitle (optional), trend arrow (optional)]
//
// Used in admin_kpi_row.dart and employee_stats_row.dart
```

---

## Widgets — Specifications

### admin_kpi_row.dart
`ConsumerWidget`. Watches `adminDashboardProvider`. Shows a horizontal scrollable row of `StatCard` widgets (scroll because on small screens 7 cards won't fit):

```
StatCard(title: 'Total Tasks',     value: '${data.totalTasks ?? 0}',   icon: Icons.assignment)
StatCard(title: 'Active',          value: '${data.activeTasks ?? 0}',   icon: Icons.play_circle)
StatCard(title: 'Overdue',         value: '${data.overdueTasks ?? 0}',  icon: Icons.warning, iconColor: Colors.red)
StatCard(title: 'Completed',       value: '${data.completedTasks ?? 0}',icon: Icons.check_circle)
StatCard(title: 'Completion Rate', value: '${(data.completionRate ?? 0).toStringAsFixed(1)}%', icon: Icons.percent)
StatCard(title: 'Avg Hours',       value: '${(data.avgCompletionHours ?? 0).toStringAsFixed(1)}h', icon: Icons.timer)
StatCard(title: 'Employees',       value: '${data.totalEmployees ?? 0}',icon: Icons.people)
```

### employee_stats_row.dart
Same pattern as `admin_kpi_row` but watches `employeeDashboardProvider` and shows employee-specific stats.

### task_status_pie_chart.dart
`ConsumerWidget`. Watches `taskDistributionProvider` for `byStatus` data.

Uses `fl_chart` `PieChart` widget. Color mapping for status:
```dart
'TODO'        → Colors.grey
'IN_PROGRESS' → Colors.blue
'IN_REVIEW'   → Colors.orange
'DONE'        → Colors.green
'CANCELLED'   → Colors.red
```

Show legend below the chart (status name + color dot + count + percentage). Handle empty data with `AppEmptyState`. Handle loading with `AppLoading`. Handle null/zero data gracefully — show placeholder pie with a single grey section labeled "No data".

### task_priority_donut_chart.dart
Same pattern as `task_status_pie_chart` but uses `byPriority` data. Color mapping:
```dart
'LOW'      → Colors.green
'MEDIUM'   → Colors.blue
'HIGH'     → Colors.orange
'CRITICAL' → Colors.red
```
Donut style: set `PieChartData` `sectionsSpace` and `centerSpaceRadius` to produce a donut hole. Show total task count in the center hole.

### completion_trend_line_chart.dart
`ConsumerWidget`. Watches `completionTrendProvider`. Uses `fl_chart` `LineChart`.

Two lines: completed (green) and created (blue). X-axis: date labels (show every 5th date to avoid crowding). Y-axis: task count. Show tooltip on touch with date + completed + created values. If list is empty: `AppEmptyState(message: 'No trend data available')`.

> Date label formatting: use `AppDateUtils` from `date_utils.dart`. Do not import `intl` — it is already a transitive dependency but check before using.

### employee_performance_bar_chart.dart
`ConsumerWidget`. Watches `employeePerformanceProvider`. Uses `fl_chart` `BarChart`.

X-axis: employee names (truncated to 8 chars if too long). Y-axis: tasks completed. Bar color: primary theme color. On bar tap: show tooltip with full name + completed + on-time rate. If empty: `AppEmptyState`.

### employee_performance_table.dart
`ConsumerWidget`. Watches `employeePerformanceProvider`. Shows same data as bar chart but in `DataTable` format for detail. Columns: Name, Assigned, Completed, On-Time Rate, Avg Hours. Sortable by completed count (tap column header). Shown below the bar chart on the admin analytics screen.

### monthly_trend_chart.dart
`ConsumerWidget`. Employee-only. Uses `completion_trend_provider` data (same data, different visual). Renders as `fl_chart` `BarChart` (monthly bars instead of daily line). Groups data by month before rendering.

### report_export_sheet.dart
`ConsumerStatefulWidget`. Shown via `showModalBottomSheet` from the admin analytics screen AppBar action.

Layout:
- Title: "Export Report"
- `DropdownButtonFormField` for report type: Task Summary, Employee Performance, Audit Summary
- `DropdownButtonFormField` for format: PDF, CSV, Excel
- Date range pickers: From + To (optional)
- Export button → calls `reportExportProvider.exportReport()` → shows loading → on success shows download button
- Download button → calls `reportExportProvider.downloadReport(reportId)` → saves file
- Use same conditional import helper from Phase 5 (`compliance_download_web/native/stub.dart`) for file saving — do not create duplicates

---

## Screens — Specifications

### admin_analytics_screen.dart
`ConsumerStatefulWidget`. ADMIN only. Navigated to from Admin shell Analytics tab.

Layout: `CustomScrollView` with `SliverAppBar` (pinned, title "Analytics") + `SliverList`:

```
Section 1: AdminKpiRow (horizontal scrollable stat cards)
Section 2: SectionHeader("Task Distribution")
           Row: [TaskStatusPieChart, TaskPriorityDonutChart] (side by side on wide screens, stacked on narrow)
Section 3: SectionHeader("30-Day Completion Trend")
           CompletionTrendLineChart (full width)
Section 4: SectionHeader("Employee Performance")
           EmployeePerformanceBarChart
           EmployeePerformanceTable
```

AppBar actions:
- Export icon → `showModalBottomSheet` with `ReportExportSheet`
- Refresh icon → calls `ref.refresh` on all admin analytics providers

`ref.listen` on all providers for error snackbars. Pull-to-refresh via `RefreshIndicator` wrapping the `CustomScrollView`.

> Responsive layout for pie/donut charts: use `LayoutBuilder` to check available width. If `constraints.maxWidth > 600` → `Row` side by side. If narrower → `Column` stacked.

### employee_analytics_screen.dart
`ConsumerStatefulWidget`. EMPLOYEE only. Navigated to from Employee shell Dashboard tab (this IS the employee dashboard — replace the placeholder).

Layout: `CustomScrollView`:
```
Section 1: EmployeeStatsRow (personal KPI cards)
Section 2: SectionHeader("Monthly Productivity")
           MonthlyTrendChart
```

Pull-to-refresh. No export button (employee cannot export reports).

---

## Router Extension — app_router.dart

Phase 6 replaces ALL remaining placeholders. These are the routes that still have `_PlaceholderScreen`:

```dart
// REPLACE admin dashboard placeholder:
GoRoute(
  path: AppRoutes.adminDashboard,
  builder: (_, __) => const AdminAnalyticsScreen(),
),

// REPLACE admin analytics placeholder (same screen — admin dashboard IS the analytics screen):
GoRoute(
  path: AppRoutes.adminAnalytics,
  builder: (_, __) => const AdminAnalyticsScreen(),
),

// REPLACE employee dashboard placeholder:
GoRoute(
  path: AppRoutes.employeeDashboard,
  builder: (_, __) => const EmployeeAnalyticsScreen(),
),
```

> ⚠️ Check `app_router.dart` carefully. Some of these routes may be inside `StatefulShellBranch` blocks. Replace the placeholder builder in-place — do not add new top-level routes for these. Duplicating a route inside a shell branch will cause a go_router assertion error at startup.

**Add imports:**
```dart
import '../../features/analytics/presentation/screens/admin_analytics_screen.dart';
import '../../features/analytics/presentation/screens/employee_analytics_screen.dart';
```

---

## Offline Caching Behaviour (Phase 6)

| Provider | Cache Key | Behaviour |
|----------|-----------|-----------|
| `adminDashboardProvider` | `analytics_box['admin_dashboard']` | Show cached instantly → background refresh |
| `employeeDashboardProvider` | `analytics_box['employee_dashboard']` | Show cached instantly → background refresh |
| `taskDistributionProvider` | `analytics_box['task_distribution']` | Show cached instantly → background refresh |
| `completionTrendProvider` | `analytics_box['completion_trend']` | Show cached instantly → background refresh |
| `employeePerformanceProvider` | `analytics_box['employee_performance']` | Show cached instantly → background refresh |
| `reportExportProvider` | None | Always fresh — no caching |

> Charts render from cached data immediately on app open. If backend is unreachable and cache exists, charts show stale data with the global offline banner. This is acceptable — analytics is not real-time.

---

## fl_chart Usage Notes

`fl_chart: ^0.70.2` is already in pubspec. Key API notes for this version:

```dart
// PieChart — correct import and basic structure:
import 'package:fl_chart/fl_chart.dart';

PieChart(
  PieChartData(
    sections: data.map((d) => PieChartSectionData(
      value: (d.percentage ?? 0).toDouble(),
      color: _colorForStatus(d.status),
      title: '${(d.percentage ?? 0).toStringAsFixed(1)}%',
      radius: 80,
    )).toList(),
    sectionsSpace: 2,
    centerSpaceRadius: 0,  // 0 for pie, 40 for donut
  ),
)

// LineChart — correct structure:
LineChart(
  LineChartData(
    lineBarsData: [
      LineChartBarData(spots: completedSpots, color: Colors.green, isCurved: true),
      LineChartBarData(spots: createdSpots, color: Colors.blue, isCurved: true),
    ],
  ),
)

// BarChart:
BarChart(
  BarChartData(
    barGroups: data.mapIndexed((i, d) => BarChartGroupData(
      x: i,
      barRods: [BarChartRodData(toY: (d.tasksCompleted ?? 0).toDouble(), color: primaryColor)],
    )).toList(),
  ),
)
```

> Do not use deprecated `fl_chart` APIs. If a class or property name causes an analyzer error, check the fl_chart 0.70.x changelog before guessing an alternative.

---

## Sub-Phase Split (Recommended Build Order)

| Sub-Phase | Files | Dependency |
|-----------|-------|------------|
| **6A** | 5 freezed models + `report_export_request.dart` (plain class) → build_runner | Nothing new outside Phase 1 |
| **6B** | `analytics_repository.dart` + 6 providers | Needs 6A |
| **6C** | `stat_card.dart` full impl + 9 widgets | Needs 6A + 6B |
| **6D** | 2 screens + router replacements + full verify | Needs 6C |

> **6C note**: `report_export_sheet.dart` reuses the conditional import download helpers from Phase 5. Before writing it, run:
> ```bash
> find lib/ -name "compliance_download*" | sort
> ```
> These files are the download helpers. Import them from their existing paths — do not recreate.

---

## Phase 6 File Count

| Layer | Count |
|-------|-------|
| Models (freezed) | 5 |
| Models (plain Dart) | 1 |
| Repository | 1 |
| Providers | 6 |
| Widgets | 9 |
| Screens | 2 |
| `stat_card.dart` (modified) | 1 |
| Router (modified) | 1 |
| **Total New Files** | **24** |
| **Total Modified Files** | **2** |

> No new pubspec packages. No new Hive boxes. No new conditional import files (reuse Phase 5).
> Generated files (`.freezed.dart`, `.g.dart`) not counted.
> This is the final phase — after 6D passes `flutter analyze` with no issues, the app is feature-complete.
