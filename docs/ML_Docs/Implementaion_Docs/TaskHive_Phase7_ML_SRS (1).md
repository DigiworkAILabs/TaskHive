# TaskHive — Phase 7: ML Module
## SRS v2.4 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 7 of 7 |
| **Name** | ML Module — AI-Powered Features |
| **Modules** | ML Module (FastAPI + Spring Boot + Frontend) |
| **DB Migrations** | V7.0 |
| **Duration** | ~2 weeks |
| **Depends On** | All previous phases (1–6) must be complete |

### Deliverable
4 AI-powered features added to existing TaskHive: Task Priority Suggestion, Completion Time Estimation, Workload Balance Recommendation, Employee Productivity Scoring. FastAPI inference server runs separately on port 8000. Spring Boot (port 8080) handles auth, DB enrichment, and calls FastAPI with Resilience4j Circuit Breaker. Synthetic data used for training until real data accumulates (6+ months). Frontend shows suggestions non-blocking — user can always override. DB migration V7.0 adds ml_prediction_logs table. Docker compose updated with ml-server service.

---

## ML API Endpoints (FastAPI — port 8000)

```
GET    /health                                  # Health check + loaded models list
POST   /ml/predict/task-priority                # Feature 1
POST   /ml/predict/completion-time              # Feature 2
POST   /ml/recommend/workload-balance           # Feature 3
POST   /ml/score/employee                       # Feature 4
```

## Spring Boot ML Endpoints (port 8080)

```
POST   /api/v1/ml/predict/task-priority         # ADMIN only
POST   /api/v1/ml/predict/completion-time       # ADMIN only
POST   /api/v1/ml/recommend/workload-balance    # ADMIN only
GET    /api/v1/ml/score/employee/{id}           # ADMIN + self (EMPLOYEE own score)
```

---

## Functional Requirements

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-ML-01 | High | System shall suggest task priority based on title, description, and assignee history |
| FR-ML-02 | High | System shall estimate task completion time based on task data and employee performance |
| FR-ML-03 | High | System shall recommend best employee for a task based on current workload and performance |
| FR-ML-04 | High | System shall calculate employee productivity score (0–100) with grade and trend |
| FR-ML-05 | Critical | ML features shall be non-blocking — user can always override suggestions |
| FR-ML-06 | Critical | All ML calls shall use Circuit Breaker — app must never fail if ML server is down |
| FR-ML-07 | Critical | ML server toggled via ML_ENABLED flag (default: false) |
| FR-ML-08 | Medium | Models trained on synthetic data initially; retrained when real data available |
| FR-ML-09 | Medium | Each ML feature shall return confidence score with prediction |
| FR-ML-10 | Low | ML prediction requests/responses shall be logged for future retraining |

---

## ML Models

| Feature | Model | Input Features | Output |
|---------|-------|---------------|--------|
| Priority Suggestion | Random Forest Classifier | title_tfidf, description_tfidf, estimated_hours, tags_count, emp_completion_rate, emp_avg_hours | priority (LOW/MEDIUM/HIGH/CRITICAL), confidence |
| Completion Time | Gradient Boosting Regressor | priority_encoded, title_length, description_length, emp_on_time_rate, emp_avg_hours_by_priority, emp_active_tasks | estimated_hours, confidence_range |
| Workload Balance | Scoring Model (weighted formula) | emp_active_tasks, emp_completion_rate, emp_on_time_rate, dept_match, priority_encoded | ranked employee list, scores |
| Productivity Score | Weighted Ensemble | tasks_assigned, tasks_completed, tasks_overdue, on_time_rate, avg_completion_hours, comment_activity | score (0–100), grade, trend |

---

## Request / Response

### Feature 1 — Priority Suggestion

**Next.js → Spring Boot:**
```json
{ "taskTitle": "Fix login crash", "taskDescription": "...", "employeeId": "uuid", "tags": ["bug"], "estimatedHours": 3.0 }
```
**Spring Boot → FastAPI (enriched):**
```json
{ "task_title": "Fix login crash", "task_description": "...", "tags": ["bug"], "estimated_hours": 3.0, "emp_completion_rate": 0.87, "emp_avg_hours": 4.2, "department": "Engineering" }
```
**FastAPI → Spring Boot → Next.js:**
```json
{ "predictedPriority": "HIGH", "confidence": 0.84, "reasoning": "Bug-related task detected", "fallbackUsed": false }
```
**Fallback:** `{ predictedPriority: "MEDIUM", confidence: 0.5, fallbackUsed: true }`

### Feature 2 — Completion Time

**Next.js → Spring Boot:**
```json
{ "taskTitle": "Build charts", "priority": "HIGH", "employeeId": "uuid", "estimatedHours": 5.0 }
```
**Spring Boot → FastAPI (enriched):**
```json
{ "task_title": "Build charts", "priority": "HIGH", "emp_on_time_rate": 0.78, "emp_avg_hours_high": 6.2, "emp_active_tasks": 3, "manual_estimate": 5.0 }
```
**FastAPI → Spring Boot → Next.js:**
```json
{ "estimatedHours": 5.5, "confidenceRange": { "low": 4.0, "high": 7.0 }, "reasoning": "HIGH priority avg 6.2hrs for this employee", "fallbackUsed": false }
```
**Fallback:** `{ estimatedHours: <admin's manual input>, fallbackUsed: true }`

### Feature 3 — Workload Balance

**Next.js → Spring Boot:**
```json
{ "taskTitle": "Payment integration", "taskPriority": "HIGH", "taskEstimatedHours": 8.0, "candidateEmployeeIds": ["uuid1", "uuid2"] }
```
**Spring Boot → FastAPI (enriched):**
```json
{ "task_priority": "HIGH", "task_estimated_hours": 8.0, "candidates": [{ "employee_id": "uuid1", "active_tasks": 2, "completion_rate": 0.91, "on_time_rate": 0.85, "dept_match": true }] }
```
**FastAPI → Spring Boot → Next.js:**
```json
{ "recommendedEmployeeId": "uuid1", "scoreBreakdown": [{ "employeeId": "uuid1", "score": 87.4 }], "reasoning": "Low active tasks + high completion rate", "fallbackUsed": false }
```
**Fallback:** `{ recommendedEmployeeId: null, reasoning: "Please select manually", fallbackUsed: true }`

### Feature 4 — Productivity Score

**Next.js → Spring Boot:** `GET /api/v1/ml/score/employee/{id}?periodDays=30`

**Spring Boot → FastAPI (enriched):**
```json
{ "employee_id": "uuid", "period_days": 30, "tasks_assigned": 12, "tasks_completed": 10, "tasks_overdue": 1, "on_time_rate": 0.80, "avg_completion_hours": 4.8, "comment_activity": 24 }
```
**FastAPI → Spring Boot → Next.js:**
```json
{ "score": 82.5, "grade": "B", "breakdown": { "completion_rate_score": 29.2, "on_time_score": 24.0, "overdue_penalty": -1.7, "engagement_score": 11.0 }, "trend": "improving", "reasoning": "Strong completion rate", "fallbackUsed": false }
```
**Fallback:** `{ score: null, grade: null, reasoning: "Score unavailable", fallbackUsed: true }`

**Grade:** A (90–100), B (75–89), C (60–74), D (45–59), F (<45)
**Trend:** compare current period vs previous period from ml_prediction_logs

---

## Synthetic Data Strategy

Since real data requires 6+ months, models are trained on synthetic data generated by Python scripts.

| Model | File | Records | Generation Logic |
|-------|------|---------|-----------------|
| Priority | priority_data.csv | 5,000 | Keywords in title → priority label + 15% noise. CRITICAL: urgent/blocker/outage, HIGH: bug/fix/crash, MEDIUM: update/feature, LOW: docs/cleanup |
| Completion Time | completion_data.csv | 5,000 | Priority → base hours (CRITICAL:8–20, HIGH:4–10, MEDIUM:2–6, LOW:0.5–3) + emp_on_time_rate adjustment |
| Workload Balance | workload_data.csv | 2,000 | score = (completion_rate×30) + (on_time_rate×25) + ((10−active_tasks)×3) + (dept_match×20) + noise |
| Productivity Score | productivity_data.csv | 3,000 | score = (completion_rate×35) + (on_time_rate×30) − (overdue_rate×20) + (engagement×15) + noise |

**Retraining**: When real data accumulates, run `pipeline/run_pipeline.py` → new `.pkl` files are saved to `inference-server/artifacts/` → restart ML server. Zero code change.

---

## FastAPI File & Folder Structure

```
ml/
├── pipeline/
│   └── run_pipeline.py                  # Master orchestration script
│
├── training/
│   ├── requirements.txt                 # Training-specific deps
│   ├── .gitignore                       # Ignores data/*.csv
│   ├── data/
│   │   └── priority_data.csv            # Generated synthetic data
│   ├── training_generator/
│   │   └── generate_priority_data.py    # Data generation scripts
│   └── scripts/
│       └── train_priority.py            # Model training scripts
│
├── inference-server/
│   ├── main.py                          # FastAPI entry point
│   ├── check_health.py                  # CLI Health Check utility
│   ├── requirements.txt                 # Production-only deps
│   ├── .gitignore                       # Ignores artifacts/*.pkl
│   │
│   ├── artifacts/                       # NEW: Production model storage
│   │   └── priority_model.pkl           # Saved model pipeline
│   │
│   ├── api/
│   │   ├── routes/
│   │   │   ├── health.py
│   │   │   └── predict_priority.py
│   │   └── schemas/
│   │       ├── request.py
│   │       └── response.py
│   │
│   ├── models/
│   │   └── loader.py                    # Loads from ../artifacts/
│   │
│   ├── preprocessing/
│   │   └── transformers.py              # Shared ML transformers
│   │
│   └── config/
│       └── settings.py
```

---

## Spring Boot File & Folder Structure

```
src/main/java/com/digiwork/taskhive/
└── module/
    └── ml/
        ├── controller/
        │   └── MLController.java
        │
        ├── service/
        │   ├── MLService.java           # Enriches requests from existing repos
        │   └── MLClientService.java     # HTTP calls to FastAPI + @CircuitBreaker
        │
        ├── dto/
        │   ├── TaskPriorityRequest.java
        │   ├── TaskPriorityResponse.java
        │   ├── CompletionTimeRequest.java
        │   ├── CompletionTimeResponse.java
        │   ├── WorkloadBalanceRequest.java
        │   ├── WorkloadBalanceResponse.java
        │   ├── ProductivityScoreRequest.java
        │   └── ProductivityScoreResponse.java
        │
        ├── config/
        │   └── MLClientConfig.java      # RestTemplate, ML_ENABLED, ML_SERVICE_URL
        │
        └── exception/
            └── MLServiceUnavailableException.java
```

---

## Frontend File & Folder Structure

```
src/
└── features/
    └── ml/
        ├── components/
        │   ├── PrioritySuggestionBadge.tsx
        │   ├── CompletionTimeEstimate.tsx
        │   ├── WorkloadRecommendation.tsx
        │   └── ProductivityScoreCard.tsx
        ├── hooks/
        │   ├── usePriorityPrediction.ts
        │   ├── useCompletionTimePrediction.ts
        │   ├── useWorkloadRecommendation.ts
        │   └── useProductivityScore.ts
        ├── services/
        │   └── mlService.ts
        └── types/
            └── ml.types.ts
```

**UI Integration Points:**

| Feature | Where | Trigger |
|---------|-------|---------|
| Priority Suggestion | `tasks/new/page.tsx` → next to Priority dropdown | Button: "Suggest Priority" |
| Completion Time | `tasks/new/page.tsx` → below Estimated Hours field | Auto on priority + employee selected |
| Workload Balance | `tasks/new/page.tsx` → Assignee dropdown | Button: "Recommend Assignee" |
| Productivity Score | `employees/[id]/page.tsx` + `(employee)/dashboard/page.tsx` + `analytics/page.tsx` | On page load |

---

## DB Migration — Phase 7

| File | Description |
|------|-------------|
| `V7.0__create_ml_prediction_logs_table.sql` | ml_prediction_logs — id, feature_type, employee_id (FK nullable), input_data (JSONB), output_data (JSONB), confidence, fallback_used, created_at. Used for future retraining + productivity trend calc. |

```sql
CREATE TABLE ml_prediction_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_type    VARCHAR(50) NOT NULL,   -- PRIORITY, COMPLETION, WORKLOAD, PRODUCTIVITY
    employee_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    input_data      JSONB NOT NULL,
    output_data     JSONB NOT NULL,
    confidence      DECIMAL(5,4),
    fallback_used   BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ml_logs_feature_type ON ml_prediction_logs(feature_type);
CREATE INDEX idx_ml_logs_employee_id ON ml_prediction_logs(employee_id);
CREATE INDEX idx_ml_logs_created_at ON ml_prediction_logs(created_at);
```

---

## Circuit Breaker Config (application.properties)

```
ml.enabled=false
ml.service.url=http://localhost:8000

resilience4j.circuitbreaker.instances.mlService.sliding-window-size=10
resilience4j.circuitbreaker.instances.mlService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.mlService.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.mlService.minimum-number-of-calls=5
```

**Fallback per feature:**

| Feature | Fallback Response |
|---------|------------------|
| Priority | MEDIUM, confidence 0.5 |
| Completion Time | Admin's manual estimate unchanged |
| Workload Balance | null — "Please select manually" |
| Productivity Score | null — "Score unavailable" |

---

## Implementation Order (within Phase 7)

| Step | What |
|------|------|
| 7.1 | FastAPI bare bones + Feature 1 Priority (generate data, train, FastAPI route, Spring Boot, Next.js) |
| 7.2 | Feature 2 Completion Time (generate data, train, FastAPI route, Spring Boot, Next.js) |
| 7.3 | Feature 3 Workload Balance (generate data, train, FastAPI route, Spring Boot, Next.js) |
| 7.4 | Feature 4 Productivity Score (generate data, train, FastAPI route, Spring Boot, Next.js) + V7.0 migration + Docker |

---

## Docker Update

`docker-compose.yml` — add ml-server service:
```yaml
ml-server:
  build:
    context: ./ml/inference-server
    dockerfile: Dockerfile
  ports:
    - "8000:8000"
  volumes:
    - ./ml/inference-server/artifacts:/app/artifacts
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
    interval: 30s
    timeout: 10s
    retries: 3
  networks:
    - taskhive-network
```

Update backend service environment:
```yaml
backend:
  environment:
    - ML_ENABLED=true
    - ML_SERVICE_URL=http://ml-server:8000
```

---

## Phase 7 File Count Summary

| Layer | FastAPI (Python) | Spring Boot (Java) | Next.js (TypeScript) |
|-------|-----------------|-------------------|---------------------|
| Controllers / Routes | 5 (health + 4 routes) | 1 (MLController) | — |
| Services | 4 | 2 (MLService + MLClientService) | — |
| Schemas / DTOs | 2 files (8 classes) | 8 classes | 1 (ml.types.ts) |
| Config | 1 (settings.py) | 1 (MLClientConfig) | — |
| Model Loader | 1 (loader.py) | — | — |
| Preprocessing | 2 (feature_engineering + text_processing) | — | — |
| Hooks | — | — | 4 |
| Components | — | — | 4 |
| Service | — | — | 1 (mlService.ts) |
| Exception | — | 1 (MLServiceUnavailableException) | — |
| Training Scripts | 9 (4 generate + 4 train + train_all) | — | — |
| DB Migration | — | 1 (V7.0) | — |
| Docker/Config | 1 Dockerfile + .env.example | application.properties update | — |
