# TaskHive — Phase 6: Analytics, ML Placeholder & Production DevOps
## SRS v2.3 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 6 of 6 |
| **Name** | Analytics, ML Placeholder & Production DevOps |
| **Modules** | Analytics Module + ML Placeholder (Backend + Frontend + DevOps) |
| **DB Migrations** | V6.0 + V6.1 |
| **Duration** | ~1.5 weeks |
| **Depends On** | All previous phases (1–5) must be complete |

### Deliverable
Admin analytics dashboard with charts (task distribution, completion trend, employee performance). Employee personal dashboard with productivity stats. Async report export to CSV/Excel/PDF with 24hr download link. Nightly metrics scheduler pre-calculates and caches stats. ML FastAPI inference server placeholder is scaffolded. Docker, docker-compose-prod, and GitHub Actions CI/CD pipelines are finalized.

---

## API Endpoints (Analytics Module)

```
GET    /api/v1/analytics/dashboard/admin            # Admin overview stats
GET    /api/v1/analytics/dashboard/employee         # Employee personal stats
GET    /api/v1/analytics/tasks/distribution         # By status (pie chart data)
GET    /api/v1/analytics/tasks/by-priority          # By priority (donut chart data)
GET    /api/v1/analytics/tasks/completion-trend     # 30-day rolling (line chart data)
GET    /api/v1/analytics/employees/performance      # Performance table
POST   /api/v1/analytics/reports/export             # Async report generation
GET    /api/v1/analytics/reports/{id}/download      # Download (24hr retention)
```

---

## Functional Requirements Covered

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-ANA-01 | High | Admin dashboard: total tasks, active tasks, overdue tasks, completion rate, total employees, avg completion time |
| FR-ANA-02 | High | Admin charts: task distribution by status (pie), by priority (donut), employee performance (bar), completion trend 30-day (line), monthly activity (area) |
| FR-ANA-03 | Medium | Employee dashboard: my tasks count by status, monthly productivity trend, on-time completion rate |
| FR-ANA-04 | Medium | System shall pre-calculate and cache daily metrics nightly (no heavy real-time queries) |
| FR-ANA-05 | Medium | Reports exportable as CSV, Excel, PDF (async generation + download link) |
| FR-ANA-06 | Low | Report download links expire after 24 hours |

---

## Backend File & Folder Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/digiwork/taskhive/
│   │       └── module/
│   │           └── analytics/                             # ANALYTICS MODULE
│   │               ├── controller/
│   │               │   └── AnalyticsController.java
│   │               │
│   │               ├── service/
│   │               │   ├── AnalyticsService.java
│   │               │   ├── ReportService.java
│   │               │   └── MetricsAggregatorService.java  # Calculates and caches daily metrics
│   │               │
│   │               ├── dto/
│   │               │   ├── AdminDashboardResponse.java
│   │               │   ├── EmployeeDashboardResponse.java
│   │               │   ├── TaskDistributionResponse.java
│   │               │   ├── TaskCompletionTrendResponse.java
│   │               │   ├── EmployeePerformanceResponse.java
│   │               │   └── ReportExportRequest.java
│   │               │
│   │               ├── model/
│   │               │   ├── DailyMetrics.java              # Pre-calculated daily stats
│   │               │   └── EmployeePerformanceCache.java  # Cached performance per period
│   │               │
│   │               ├── repository/
│   │               │   ├── AnalyticsRepository.java       # Native queries for reports
│   │               │   ├── DailyMetricsRepository.java
│   │               │   └── EmployeePerformanceCacheRepository.java
│   │               │
│   │               └── scheduler/
│   │                   └── MetricsCalculationScheduler.java  # Nightly metrics aggregation
│   │
│   └── resources/
│       └── db/
│           └── migration/
│               ├── V6.0__create_analytics_tables.sql
│               └── V6.1__create_file_metadata_table.sql
│
└── test/
    └── java/
        └── com/digiwork/taskhive/
            ├── module/
            │   └── analytics/
            │       └── service/
            │           └── AnalyticsServiceTest.java
            │
            └── integration/
                └── FullFlowIntegrationTest.java
```

---

## Frontend File & Folder Structure

```
src/
├── app/
│   └── (admin)/
│       └── analytics/page.tsx
│
└── features/
    └── analytics/
        ├── components/
        │   ├── AdminDashboard.tsx
        │   ├── EmployeeDashboard.tsx
        │   ├── TaskDistributionChart.tsx
        │   ├── TaskCompletionTrend.tsx
        │   ├── EmployeePerformanceTable.tsx
        │   └── ReportExport.tsx
        ├── hooks/
        │   ├── useAdminDashboard.ts
        │   ├── useEmployeeDashboard.ts
        │   └── useReportExport.ts
        ├── services/
        │   └── analyticsService.ts
        └── types/
            └── analytics.types.ts
```

---

## DB Migrations — Phase 6

| File | Description |
|------|-------------|
| `V6.0__create_analytics_tables.sql` | daily_metrics — id, date (unique), total_tasks, active_tasks, overdue_tasks, completed_tasks, completion_rate, avg_completion_hours, total_employees, created_at. Also: employee_performance_cache — id, employee_id (FK), period_start, period_end, tasks_assigned, tasks_completed, on_time_rate, avg_completion_hours, created_at |
| `V6.1__create_file_metadata_table.sql` | file_metadata — id, entity_type, entity_id, file_name, file_url, file_size, mime_type, storage_type (local/s3), uploaded_by (FK users), created_at |

---

## ML Placeholder File & Folder Structure (Future)

```
ml/
├── inference-server/
│   ├── api/
│   │   ├── main.py
│   │   ├── routes/
│   │   │   ├── health.py
│   │   │   ├── predict_priority.py
│   │   │   └── predict_completion.py
│   │   └── schemas/
│   │       ├── request.py
│   │       └── response.py
│   ├── models/
│   │   ├── loader.py
│   │   ├── priority_model.pkl
│   │   └── completion_model.pkl
│   ├── preprocessing/
│   │   ├── feature_engineering.py
│   │   └── text_processing.py
│   ├── config/
│   │   └── settings.py
│   ├── requirements.txt
│   └── Dockerfile
│
├── training/
│   ├── data/
│   │   ├── raw/
│   │   ├── processed/
│   │   └── datasets/
│   ├── notebooks/
│   │   ├── 01_exploratory_analysis.ipynb
│   │   ├── 02_feature_engineering.ipynb
│   │   └── 03_model_experiments.ipynb
│   ├── experiments/
│   │   ├── priority_classification/
│   │   │   ├── config.yaml
│   │   │   ├── train.py
│   │   │   └── evaluate.py
│   │   └── completion_time_regression/
│   ├── models/
│   │   ├── checkpoints/
│   │   └── production/
│   └── pipelines/
│       ├── data_extraction.py
│       ├── preprocessing.py
│       ├── training.py
│       └── evaluation.py
│
└── shared/
    ├── features/
    ├── evaluation/
    └── utils/
```

---

## MetricsCalculationScheduler — Nightly Job

```
Runs: Every night at 2:00 AM (configurable via cron)
Steps:
1. Calculate task counts by status for previous day
2. Calculate avg completion time for tasks completed that day
3. Calculate per-employee performance metrics (tasks done, on-time rate)
4. Upsert results into daily_metrics table
5. Upsert results into employee_performance_cache table
6. Log execution summary
```

---

## Phase 6 File Count Summary

| Layer | Backend | Frontend |
|-------|---------|----------|
| Controller | 1 | — |
| Service | 3 | — |
| DTO | 6 | — |
| Model | 2 | — |
| Repository | 3 | — |
| Scheduler | 1 | — |
| DB Migrations | 2 | — |
| Tests | 2 | — |
| App Pages | — | 1 |
| Feature Components | — | 6 |
| Feature Hooks | — | 3 |
| Feature Services | — | 1 |
| Feature Types | — | 1 |
| ML Files | 16 | — |
