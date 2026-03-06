# TaskHive — AI Migration Guide
## Non-AI Powered (Version 1) → AI Powered (Version 2)
### Company: Digiwork | SRS v2.4 | Base Package: `com.digiwork.taskhive`

---

## Document Overview

| Field | Detail |
|-------|--------|
| **Purpose** | Complete guide to migrate TaskHive from Version 1 (no ML) to Version 2 (AI powered) |
| **Scope** | What changes, what stays same, how to train, how to connect, how to deploy |
| **ML Stack** | Python 3.11+ · FastAPI · Scikit-learn · PyTorch · HuggingFace |
| **Java Stack** | Spring Boot 3.5.10 · Java 21 · Resilience4j |
| **Frontend Stack** | Next.js 14+ · TypeScript |

---

## Table of Contents

1. [4 ML Features Overview](#1-4-ml-features-overview)
2. [Architecture — How Frontend, Spring Boot & Python Communicate](#2-architecture)
3. [Complete Request-Response Flow](#3-complete-request-response-flow)
4. [What Changes in Existing Project](#4-what-changes-in-existing-project)
5. [What Does NOT Change](#5-what-does-not-change)
6. [Spring Boot Changes — Backend](#6-spring-boot-changes--backend)
7. [Frontend Changes — Next.js](#7-frontend-changes--nextjs)
8. [Python ML Server — FastAPI](#8-python-ml-server--fastapi)
9. [ML Training Pipeline](#9-ml-training-pipeline)
10. [Inference Server Testing (Isolated)](#10-inference-server-testing-isolated)
11. [Version 1 → Version 2 Migration Timeline](#11-version-1--version-2-migration-timeline)
12. [Deployment — docker-compose-prod.yml](#12-deployment--docker-compose-prodyml)
13. [Circuit Breaker — Graceful Fallback](#13-circuit-breaker--graceful-fallback)
14. [Complete File Changes Summary](#14-complete-file-changes-summary)

---

## 1. Four ML Features Overview

| # | Feature | Algorithm | Where it appears in UI |
|---|---------|-----------|------------------------|
| 1 | **Task Priority Suggestion** | Random Forest / BERT fine-tuned | `TaskForm.tsx` — suggests priority while admin types title + description |
| 2 | **Completion Time Estimation** | Gradient Boosting Regressor | `TaskForm.tsx` — predicts estimated hours after assignee + priority selected |
| 3 | **Workload Balancing** | Multi-Armed Bandit / RL | `TaskForm.tsx` — recommends best employee to assign task to |
| 4 | **Productivity Scoring** | Weighted Ensemble | `EmployeePerformanceTable.tsx` + `EmployeeDashboard.tsx` — shows ML score per employee |

### Key Principle
All 4 features are **advisory / suggestion only**. Admin always has final control. ML suggests — admin decides. If ML server is down, the app works normally without suggestions. Nothing breaks.

---

## 2. Architecture

### Who talks to whom

```
┌─────────────────────────────────────────────────────────────────────┐
│                         INTERNET                                     │
│                                                                      │
│   Browser / Mobile                                                   │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTPS
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    NEXT.JS FRONTEND  (port 3000)                     │
│                         TypeScript                                   │
│   TaskForm.tsx · EmployeePerformanceTable.tsx · EmployeeDashboard   │
│   useSuggestPriority · useEstimateCompletion                        │
│   useRecommendAssignee · useProductivityScore                        │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ POST /api/v1/ml/...
                               │ (HttpOnly Cookie auto-attached)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  SPRING BOOT BACKEND  (port 8080)                   │
│                         Java 21                                      │
│   MlController.java  →  validates request, checks ML_ENABLED        │
│   MlClientService.java  →  calls FastAPI via RestTemplate            │
│   Resilience4j  →  circuit breaker wraps every ML call              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ POST http://ml-inference:8000/...
                               │ (internal Docker network only —
                               │  never exposed to internet)
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  FASTAPI ML SERVER  (port 8000)                     │
│                        Python 3.11+                                  │
│   predict_priority.py  →  loads priority_model.pkl                  │
│   predict_completion.py  →  loads completion_model.pkl              │
│   recommend_workload.py  →  loads workload_model.pkl                │
│   productivity_score.py  →  loads productivity_model.pkl            │
└─────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     POSTGRESQL  (port 5432)                          │
│   tasks · employees · employee_performance_cache · daily_metrics     │
│   (ML reads from here for training — not for inference)             │
└─────────────────────────────────────────────────────────────────────┘
```

### Critical Points
- **Frontend never talks to Python directly** — only Spring Boot
- **Python server is internal only** — not accessible from internet
- **Spring Boot is a thin HTTP client** for ML — no ML logic in Java
- **PostgreSQL data feeds training** — not real-time inference

---

## 3. Complete Request-Response Flow

### Example: Task Priority Suggestion (Feature 1)

```
┌─────────────────────────────────────────────────────────────────┐
│  STEP 1 — FRONTEND                                               │
│                                                                  │
│  Admin types in TaskForm.tsx:                                    │
│    Title: "Fix login bug"                                        │
│    Description: "Users cannot login after password reset"        │
│                                                                  │
│  useSuggestPriority.ts hook fires automatically                  │
│  Sends: POST /api/v1/ml/suggest-priority                         │
│         { "title": "...", "description": "..." }                 │
└──────────────────────────┬──────────────────────────────────────┘
                           │ ↓ request goes to Spring Boot
┌──────────────────────────▼──────────────────────────────────────┐
│  STEP 2 — SPRING BOOT                                            │
│                                                                  │
│  MlController.java receives request                              │
│  Checks: ml.enabled=true → proceed                               │
│  MlClientService.java calls:                                     │
│    POST http://ml-inference:8000/predict/priority                │
│    { "title": "...", "description": "..." }                      │
└──────────────────────────┬──────────────────────────────────────┘
                           │ ↓ request goes to FastAPI
┌──────────────────────────▼──────────────────────────────────────┐
│  STEP 3 — FASTAPI (Python)                                       │
│                                                                  │
│  predict_priority.py receives request                            │
│  Loads priority_model.pkl                                        │
│  Runs inference on title + description                           │
│  Returns:                                                        │
│    { "priority": "HIGH", "confidence": 0.91 }                    │
└──────────────────────────┬──────────────────────────────────────┘
                           │ ↑ response back to Spring Boot
┌──────────────────────────▼──────────────────────────────────────┐
│  STEP 4 — SPRING BOOT                                            │
│                                                                  │
│  MlClientService.java receives Python response                   │
│  MlController.java wraps in standard ApiResponse                 │
│  Returns to frontend:                                            │
│    { "success": true,                                            │
│      "data": { "priority": "HIGH", "confidence": 0.91 } }       │
└──────────────────────────┬──────────────────────────────────────┘
                           │ ↑ response back to Frontend
┌──────────────────────────▼──────────────────────────────────────┐
│  STEP 5 — FRONTEND                                               │
│                                                                  │
│  useSuggestPriority.ts receives response                         │
│  TaskForm.tsx shows:                                             │
│    "ML suggests: HIGH (91% confidence)"  [Accept] [Ignore]       │
│                                                                  │
│  Admin clicks Accept → priority field auto-fills to HIGH         │
│  Admin clicks Ignore → types priority manually                   │
└─────────────────────────────────────────────────────────────────┘
```

### Same round-trip applies for all 4 features:
```
Frontend → Spring Boot → FastAPI → Spring Boot → Frontend
```

---

## 4. What Changes in Existing Project

### 4.1 Spring Boot Backend — New Files

| File | Location | Purpose |
|------|----------|---------|
| `MlController.java` | `module/ml/controller/` | Exposes 4 ML endpoints to frontend |
| `MlClientService.java` | `module/ml/service/` | HTTP client — calls FastAPI |
| `MlFallbackService.java` | `module/ml/service/` | Returns default responses when ML is down |
| `PrioritySuggestionRequest.java` | `module/ml/dto/` | DTO for Feature 1 request |
| `PrioritySuggestionResponse.java` | `module/ml/dto/` | DTO for Feature 1 response |
| `CompletionEstimationRequest.java` | `module/ml/dto/` | DTO for Feature 2 request |
| `CompletionEstimationResponse.java` | `module/ml/dto/` | DTO for Feature 2 response |
| `WorkloadRecommendationRequest.java` | `module/ml/dto/` | DTO for Feature 3 request |
| `WorkloadRecommendationResponse.java` | `module/ml/dto/` | DTO for Feature 3 response |
| `ProductivityScoreResponse.java` | `module/ml/dto/` | DTO for Feature 4 response |
| `MlClientConfig.java` | `module/ml/config/` | RestTemplate bean + timeout config |

### 4.2 Spring Boot Backend — Modified Files

| File | Change |
|------|--------|
| `application.properties` | Change `ml.enabled=false` → `ml.enabled=true` |

> **That's it for configuration.** Resilience4j config already exists. ML URL already exists. Just flip the flag.

### 4.3 Frontend — New Files

| File | Location | Purpose |
|------|----------|---------|
| `useSuggestPriority.ts` | `features/task/hooks/` | Calls Feature 1 endpoint |
| `useEstimateCompletion.ts` | `features/task/hooks/` | Calls Feature 2 endpoint |
| `useRecommendAssignee.ts` | `features/task/hooks/` | Calls Feature 3 endpoint |
| `useProductivityScore.ts` | `features/analytics/hooks/` | Calls Feature 4 endpoint |
| `MlSuggestionBadge.tsx` | `features/task/components/` | Reusable "ML suggests: X" badge UI component |
| `ml.types.ts` | `features/task/types/` | TypeScript types for all ML responses |

### 4.4 Frontend — Modified Files

| File | Change |
|------|--------|
| `TaskForm.tsx` | Add ML suggestion badges for priority, estimated hours, and assignee |
| `EmployeePerformanceTable.tsx` | Add `ML Score` column |
| `EmployeeDashboard.tsx` | Add productivity score widget |
| `taskService.ts` | Add 3 new ML API call functions |
| `analyticsService.ts` | Add 1 new ML API call function |

### 4.5 Python ML Server — New Files (Complete)

> These files were scaffolded as placeholders in Phase 6. Now they get actual implementation.

| File | Change |
|------|--------|
| `ml/inference-server/api/main.py` | Implement FastAPI app with all 4 routes |
| `ml/inference-server/api/routes/predict_priority.py` | Implement Feature 1 inference |
| `ml/inference-server/api/routes/predict_completion.py` | Implement Feature 2 inference |
| `ml/inference-server/api/routes/recommend_workload.py` | **New file** — Feature 3 inference |
| `ml/inference-server/api/routes/productivity_score.py` | **New file** — Feature 4 inference |
| `ml/inference-server/models/priority_model.pkl` | Trained model artifact (output of training) |
| `ml/inference-server/models/completion_model.pkl` | Trained model artifact |
| `ml/inference-server/models/workload_model.pkl` | **New** — trained model artifact |
| `ml/inference-server/models/productivity_model.pkl` | **New** — trained model artifact |
| `ml/inference-server/preprocessing/feature_engineering.py` | Implement feature transforms |
| `ml/inference-server/preprocessing/text_processing.py` | Implement text cleaning for BERT |
| `ml/training/pipelines/data_extraction.py` | Implement PostgreSQL data pull |
| `ml/training/pipelines/preprocessing.py` | Implement data cleaning + encoding |
| `ml/training/pipelines/training.py` | Implement model training for all 4 models |
| `ml/training/pipelines/evaluation.py` | Implement accuracy evaluation |
| `ml/inference-server/requirements.txt` | Add all Python dependencies |

### 4.6 Deployment — Modified Files

| File | Change |
|------|--------|
| `docker-compose-prod.yml` | Add `ml-inference` service block |

---

## 5. What Does NOT Change

| Component | Status |
|-----------|--------|
| All Phase 1–6 existing Java files | ✅ Untouched |
| All existing DB tables and migrations | ✅ Untouched |
| All existing frontend components (except 3 modified) | ✅ Untouched |
| Auth flow | ✅ Untouched |
| Task CRUD flow | ✅ Untouched |
| Notification module | ✅ Untouched |
| Audit module | ✅ Untouched |
| Resilience4j config | ✅ Already configured — just activates |
| `employee_performance_cache` table | ✅ Already built in Phase 6 — ML reads from it |
| `daily_metrics` table | ✅ Already built in Phase 6 — ML reads from it |
| `estimatedHours` field on Task | ✅ Already in Phase 3 — ML suggestion populates it |

---

## 6. Spring Boot Changes — Backend

### 6.1 New Module Folder Structure

```
src/main/java/com/digiwork/taskhive/
└── module/
    └── ml/                                        # NEW ML MODULE
        ├── controller/
        │   └── MlController.java                  # 4 ML endpoints
        ├── service/
        │   ├── MlClientService.java               # calls FastAPI via RestTemplate
        │   └── MlFallbackService.java             # returns defaults when ML is down
        ├── dto/
        │   ├── PrioritySuggestionRequest.java
        │   ├── PrioritySuggestionResponse.java
        │   ├── CompletionEstimationRequest.java
        │   ├── CompletionEstimationResponse.java
        │   ├── WorkloadRecommendationRequest.java
        │   ├── WorkloadRecommendationResponse.java
        │   └── ProductivityScoreResponse.java
        └── config/
            └── MlClientConfig.java                # RestTemplate + timeouts
```

### 6.2 MlController.java — 4 Endpoints

```java
@RestController
@RequestMapping("/api/v1/ml")
@RequiredArgsConstructor
public class MlController {

    private final MlClientService mlClientService;

    // Feature 1 — Task Priority Suggestion
    @PostMapping("/suggest-priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PrioritySuggestionResponse> suggestPriority(
            @RequestBody @Valid PrioritySuggestionRequest request) {
        return ApiResponse.success(mlClientService.suggestPriority(request));
    }

    // Feature 2 — Completion Time Estimation
    @PostMapping("/estimate-completion")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompletionEstimationResponse> estimateCompletion(
            @RequestBody @Valid CompletionEstimationRequest request) {
        return ApiResponse.success(mlClientService.estimateCompletion(request));
    }

    // Feature 3 — Workload Balancing / Assignee Recommendation
    @PostMapping("/recommend-assignee")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<WorkloadRecommendationResponse> recommendAssignee(
            @RequestBody @Valid WorkloadRecommendationRequest request) {
        return ApiResponse.success(mlClientService.recommendAssignee(request));
    }

    // Feature 4 — Productivity Score
    @GetMapping("/productivity-score/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ApiResponse<ProductivityScoreResponse> getProductivityScore(
            @PathVariable UUID employeeId) {
        return ApiResponse.success(mlClientService.getProductivityScore(employeeId));
    }
}
```

### 6.3 MlClientService.java — HTTP Client with Circuit Breaker

```java
@Service
@RequiredArgsConstructor
public class MlClientService {

    private final RestTemplate mlRestTemplate;
    private final MlFallbackService fallbackService;

    @Value("${ml.inference-url}")
    private String mlBaseUrl;

    @Value("${ml.enabled}")
    private boolean mlEnabled;

    // Feature 1
    @CircuitBreaker(name = "mlService", fallbackMethod = "suggestPriorityFallback")
    public PrioritySuggestionResponse suggestPriority(PrioritySuggestionRequest request) {
        if (!mlEnabled) return fallbackService.defaultPrioritySuggestion();
        return mlRestTemplate.postForObject(
            mlBaseUrl + "/predict/priority",
            request,
            PrioritySuggestionResponse.class
        );
    }

    // Feature 2
    @CircuitBreaker(name = "mlService", fallbackMethod = "estimateCompletionFallback")
    public CompletionEstimationResponse estimateCompletion(CompletionEstimationRequest request) {
        if (!mlEnabled) return fallbackService.defaultCompletionEstimation();
        return mlRestTemplate.postForObject(
            mlBaseUrl + "/predict/completion-time",
            request,
            CompletionEstimationResponse.class
        );
    }

    // Feature 3
    @CircuitBreaker(name = "mlService", fallbackMethod = "recommendAssigneeFallback")
    public WorkloadRecommendationResponse recommendAssignee(WorkloadRecommendationRequest request) {
        if (!mlEnabled) return fallbackService.defaultWorkloadRecommendation();
        return mlRestTemplate.postForObject(
            mlBaseUrl + "/recommend/workload-balance",
            request,
            WorkloadRecommendationResponse.class
        );
    }

    // Feature 4
    @CircuitBreaker(name = "mlService", fallbackMethod = "productivityScoreFallback")
    public ProductivityScoreResponse getProductivityScore(UUID employeeId) {
        if (!mlEnabled) return fallbackService.defaultProductivityScore();
        return mlRestTemplate.getForObject(
            mlBaseUrl + "/productivity-score/" + employeeId,
            ProductivityScoreResponse.class
        );
    }

    // Fallback methods — called automatically by circuit breaker when ML is down
    private PrioritySuggestionResponse suggestPriorityFallback(Exception e) {
        return fallbackService.defaultPrioritySuggestion();
    }
    private CompletionEstimationResponse estimateCompletionFallback(Exception e) {
        return fallbackService.defaultCompletionEstimation();
    }
    private WorkloadRecommendationResponse recommendAssigneeFallback(Exception e) {
        return fallbackService.defaultWorkloadRecommendation();
    }
    private ProductivityScoreResponse productivityScoreFallback(Exception e) {
        return fallbackService.defaultProductivityScore();
    }
}
```

### 6.4 MlFallbackService.java — Graceful Defaults

```java
@Service
public class MlFallbackService {

    public PrioritySuggestionResponse defaultPrioritySuggestion() {
        return PrioritySuggestionResponse.builder()
            .available(false)
            .message("ML suggestion not available")
            .build();
    }

    public CompletionEstimationResponse defaultCompletionEstimation() {
        return CompletionEstimationResponse.builder()
            .available(false)
            .message("ML estimation not available")
            .build();
    }

    public WorkloadRecommendationResponse defaultWorkloadRecommendation() {
        return WorkloadRecommendationResponse.builder()
            .available(false)
            .message("ML recommendation not available")
            .build();
    }

    public ProductivityScoreResponse defaultProductivityScore() {
        return ProductivityScoreResponse.builder()
            .available(false)
            .message("ML score not available")
            .build();
    }
}
```

### 6.5 application.properties — Only Change Needed

```properties
# BEFORE (Version 1)
ml.enabled=false

# AFTER (Version 2)
ml.enabled=true
ml.inference-url=http://ml-inference:8000
```

> Resilience4j config is already present — no change needed there.

---

## 7. Frontend Changes — Next.js

### 7.1 New Hooks

```typescript
// features/task/hooks/useSuggestPriority.ts
export const useSuggestPriority = () => {
  return useMutation({
    mutationFn: (data: { title: string; description: string }) =>
      taskService.suggestPriority(data),
  });
};

// features/task/hooks/useEstimateCompletion.ts
export const useEstimateCompletion = () => {
  return useMutation({
    mutationFn: (data: { title: string; priority: string; assignedTo: string }) =>
      taskService.estimateCompletion(data),
  });
};

// features/task/hooks/useRecommendAssignee.ts
export const useRecommendAssignee = () => {
  return useMutation({
    mutationFn: (data: { priority: string; dueDate: string }) =>
      taskService.recommendAssignee(data),
  });
};

// features/analytics/hooks/useProductivityScore.ts
export const useProductivityScore = (employeeId: string) => {
  return useQuery({
    queryKey: ['productivity-score', employeeId],
    queryFn: () => analyticsService.getProductivityScore(employeeId),
    enabled: !!employeeId,
  });
};
```

### 7.2 TaskForm.tsx — What Gets Added

```
Current TaskForm fields:
  Title · Description · Priority · Assignee · Due Date · EstimatedHours · Tags

After ML integration — same fields + ML suggestion badges:

  [Title field]          → after typing stops → ML suggests priority
  [Description field]    ↗

  [Priority field]  ← "ML suggests: HIGH (91% confidence)" [✓ Accept]

  [Assignee dropdown]  ← "Recommended: Alice (score: 0.92)" badge shown next to her name
                          "Bob (0.74)" · "Charlie (0.61)" ← ranked list

  [EstimatedHours field]  ← "ML estimates: 4.5 hours" [✓ Accept]

  Admin can accept any suggestion with one click or ignore and type manually.
  All suggestions are optional — form works normally without them.
```

### 7.3 EmployeePerformanceTable.tsx — What Gets Added

```
Current columns:
  Employee · Tasks Assigned · Tasks Completed · On-time Rate · Avg Hours

After ML integration:
  Employee · Tasks Assigned · Tasks Completed · On-time Rate · Avg Hours · ML Score
                                                                             ↑
                                                               "87/100"  color-coded badge
                                                               Green > 75 · Yellow 50-75 · Red < 50
```

### 7.4 EmployeeDashboard.tsx — What Gets Added

```
Current employee dashboard:
  My tasks count by status · Monthly trend · On-time rate

After ML integration:
  My tasks count by status · Monthly trend · On-time rate · My Productivity Score
                                                              ↑
                                                   "Your ML Score: 87/100"
                                                   "Based on last 30 days performance"
```

---

## 8. Python ML Server — FastAPI

### 8.1 Updated Folder Structure

```
ml/
├── inference-server/
│   ├── api/
│   │   ├── main.py                          # FastAPI app — registers all routes
│   │   ├── routes/
│   │   │   ├── health.py                    # GET /health
│   │   │   ├── predict_priority.py          # POST /predict/priority       ← Feature 1
│   │   │   ├── predict_completion.py        # POST /predict/completion-time ← Feature 2
│   │   │   ├── recommend_workload.py        # POST /recommend/workload-balance ← Feature 3 (NEW)
│   │   │   └── productivity_score.py        # GET  /productivity-score/{id} ← Feature 4 (NEW)
│   │   └── schemas/
│   │       ├── request.py                   # Pydantic request models
│   │       └── response.py                  # Pydantic response models
│   ├── models/
│   │   ├── loader.py                        # Loads .pkl files on startup
│   │   ├── priority_model.pkl               # Trained Random Forest / BERT
│   │   ├── completion_model.pkl             # Trained Gradient Boosting Regressor
│   │   ├── workload_model.pkl               # Trained Multi-Armed Bandit (NEW)
│   │   └── productivity_model.pkl           # Trained Weighted Ensemble (NEW)
│   ├── preprocessing/
│   │   ├── feature_engineering.py           # Feature transforms for inference
│   │   └── text_processing.py              # Text cleaning for BERT
│   ├── config/
│   │   └── settings.py                     # ENV vars, model paths, config
│   ├── requirements.txt
│   └── Dockerfile
│
├── training/
│   ├── data/
│   │   ├── raw/                             # Raw data from PostgreSQL
│   │   ├── processed/                       # Cleaned, encoded data
│   │   └── datasets/                        # Train / val / test splits
│   ├── notebooks/
│   │   ├── 01_exploratory_analysis.ipynb    # Understand your data
│   │   ├── 02_feature_engineering.ipynb     # Build features
│   │   └── 03_model_experiments.ipynb       # Try algorithms, compare results
│   ├── experiments/
│   │   ├── priority_classification/
│   │   │   ├── config.yaml
│   │   │   ├── train.py
│   │   │   └── evaluate.py
│   │   ├── completion_time_regression/
│   │   │   ├── config.yaml
│   │   │   ├── train.py
│   │   │   └── evaluate.py
│   │   ├── workload_balancing/              # NEW
│   │   │   ├── config.yaml
│   │   │   ├── train.py
│   │   │   └── evaluate.py
│   │   └── productivity_scoring/            # NEW
│   │       ├── config.yaml
│   │       ├── train.py
│   │       └── evaluate.py
│   ├── models/
│   │   ├── checkpoints/                     # Intermediate training saves
│   │   └── production/                      # Final .pkl files for deployment
│   └── pipelines/
│       ├── data_extraction.py               # Pulls from PostgreSQL
│       ├── preprocessing.py                 # Cleans + encodes
│       ├── training.py                      # Trains all 4 models
│       └── evaluation.py                    # Evaluates all 4 models
│
└── shared/
    ├── features/                            # Shared feature definitions
    ├── evaluation/                          # Shared metrics (accuracy, F1, MAE)
    └── utils/                               # Shared utility functions
```

### 8.2 FastAPI Endpoints

```python
# ml/inference-server/api/main.py

from fastapi import FastAPI
from api.routes import (
    health, predict_priority,
    predict_completion, recommend_workload, productivity_score
)

app = FastAPI(title="TaskHive ML Inference Server", version="1.0.0")

app.include_router(health.router)
app.include_router(predict_priority.router)
app.include_router(predict_completion.router)
app.include_router(recommend_workload.router)
app.include_router(productivity_score.router)
```

```
Available Endpoints:
GET  /health                          ← Spring Boot health check
POST /predict/priority                ← Feature 1
POST /predict/completion-time         ← Feature 2
POST /recommend/workload-balance      ← Feature 3
GET  /productivity-score/{employeeId} ← Feature 4
```

### 8.3 Request / Response Schemas

```python
# schemas/request.py

class PriorityRequest(BaseModel):
    title: str
    description: str

class CompletionRequest(BaseModel):
    title: str
    priority: str
    assigned_to: str
    historical_completion_rate: float

class WorkloadRequest(BaseModel):
    priority: str
    due_date: str
    estimated_hours: Optional[float]
    active_employees: List[str]       # list of employee UUIDs

# schemas/response.py

class PriorityResponse(BaseModel):
    priority: str                     # LOW / MEDIUM / HIGH / CRITICAL
    confidence: float                 # 0.0 to 1.0
    available: bool = True

class CompletionResponse(BaseModel):
    estimated_hours: float
    confidence: float
    available: bool = True

class WorkloadResponse(BaseModel):
    recommended_employee_id: str
    score: float
    ranked_employees: List[dict]      # all employees ranked with scores
    available: bool = True

class ProductivityResponse(BaseModel):
    employee_id: str
    score: int                        # 0 to 100
    grade: str                        # A / B / C / D
    available: bool = True
```

---

## 9. ML Training Pipeline

### 9.1 Data Requirements

| Table | Data Used | Min Records Needed |
|-------|-----------|-------------------|
| `tasks` | title, description, priority, status, estimated_hours, completed_at, due_date | 500+ tasks |
| `task_status_history` | time spent in each status | 1000+ history records |
| `employees` | department, designation | 20+ employees |
| `employee_performance_cache` | on_time_rate, avg_completion_hours, tasks_completed | 6 months of data |
| `daily_metrics` | completion trends | 180+ daily records |

> **Minimum prerequisite**: 6 months of real app usage with active task creation and completion.

### 9.2 Training Steps — In Order

```
STEP 1 — Data Extraction
─────────────────────────
Run: python training/pipelines/data_extraction.py

Connects to PostgreSQL
Pulls all tables listed above
Saves raw CSV files to training/data/raw/

STEP 2 — Exploratory Analysis
──────────────────────────────
Open: training/notebooks/01_exploratory_analysis.ipynb

Check data quality:
  - How many tasks? How many employees?
  - Priority distribution (is data balanced?)
  - Missing values?
  - Outliers in completion time?

STEP 3 — Feature Engineering
─────────────────────────────
Open: training/notebooks/02_feature_engineering.ipynb
Run: python training/pipelines/preprocessing.py

Build features:
  For Priority model:    title length, keyword presence, description length
  For Completion model:  priority score, employee on-time rate, department avg
  For Workload model:    current open tasks per employee, upcoming due dates
  For Productivity:      on-time rate (40%) + completion rate (30%) + speed (30%)

Saves processed data to training/data/processed/

STEP 4 — Model Experiments
────────────────────────────
Open: training/notebooks/03_model_experiments.ipynb

Try multiple algorithms for each feature:
  Priority:    Random Forest vs Logistic Regression vs BERT
  Completion:  Gradient Boosting vs Linear Regression vs XGBoost
  Workload:    Epsilon-Greedy MAB vs UCB MAB
  Productivity: Weighted formula vs Neural Network

Pick best performing algorithm for each.

STEP 5 — Training
──────────────────
Run: python training/pipelines/training.py

Trains all 4 models using best algorithms from Step 4.
Uses cross-validation (5-fold).
Saves best model to training/models/checkpoints/

STEP 6 — Evaluation
────────────────────
Run: python training/pipelines/evaluation.py

Checks accuracy on held-out test set.

Minimum thresholds before deploying:
  Priority model:     Accuracy > 80%, F1-score > 0.78
  Completion model:   MAE < 2 hours, R² > 0.70
  Workload model:     On-time rate improvement > 10% vs random
  Productivity model: Pearson correlation > 0.75 vs manual scores

If any model fails threshold → go back to Step 4, tune, retrain.
If all pass → proceed to Step 7.

STEP 7 — Copy models to inference server
─────────────────────────────────────────
Copy from training/models/production/ to inference-server/models/
  priority_model.pkl
  completion_model.pkl
  workload_model.pkl
  productivity_model.pkl
```

---

## 10. Inference Server Testing (Isolated)

### Before connecting to Spring Boot — test FastAPI standalone

```bash
# Step 1 — Start FastAPI server locally
cd ml/inference-server
pip install -r requirements.txt
uvicorn api.main:app --reload --port 8000

# Step 2 — Check health
curl http://localhost:8000/health
# Expected: { "status": "ok", "models_loaded": 4 }

# Step 3 — Test Feature 1: Priority Suggestion
curl -X POST http://localhost:8000/predict/priority \
  -H "Content-Type: application/json" \
  -d '{"title": "Fix login bug", "description": "Users cannot login after reset"}'
# Expected: { "priority": "HIGH", "confidence": 0.91 }

# Step 4 — Test Feature 2: Completion Time
curl -X POST http://localhost:8000/predict/completion-time \
  -H "Content-Type: application/json" \
  -d '{"title": "Fix login bug", "priority": "HIGH", "assigned_to": "uuid", "historical_completion_rate": 0.87}'
# Expected: { "estimated_hours": 4.5, "confidence": 0.82 }

# Step 5 — Test Feature 3: Workload Balance
curl -X POST http://localhost:8000/recommend/workload-balance \
  -H "Content-Type: application/json" \
  -d '{"priority": "HIGH", "due_date": "2026-04-01", "active_employees": ["uuid1", "uuid2", "uuid3"]}'
# Expected: { "recommended_employee_id": "uuid1", "score": 0.92, "ranked_employees": [...] }

# Step 6 — Test Feature 4: Productivity Score
curl http://localhost:8000/productivity-score/employee-uuid
# Expected: { "employee_id": "uuid", "score": 87, "grade": "A" }
```

### Acceptance Criteria before connecting to Spring Boot

| Test | Expected Result |
|------|----------------|
| Health check | `models_loaded: 4` |
| Priority suggestion | Returns valid priority enum value |
| Completion estimation | Returns positive float hours |
| Workload recommendation | Returns valid employee UUID from input list |
| Productivity score | Returns integer 0–100 |
| Response time | < 500ms for all endpoints |
| Server crash test | Restart server — models reload automatically |

If all pass → connect to Spring Boot.

---

## 11. Version 1 → Version 2 Migration Timeline

```
MONTH 1–6   Version 1 Live (Current 6 phases)
────────────────────────────────────────────────────
  App running normally
  Data accumulating in:
    tasks · task_status_history · employees
    employee_performance_cache · daily_metrics
  Goal: reach minimum data thresholds

MONTH 6–7   Data Extraction + EDA
────────────────────────────────────────────────────
  Run data_extraction.py
  Open notebooks 01 + 02
  Understand data quality
  Build features
  Duration: ~2–3 weeks

MONTH 7–8   Model Training + Evaluation
────────────────────────────────────────────────────
  Run experiments notebook (03)
  Compare algorithms
  Train all 4 models
  Evaluate against thresholds
  Retrain if needed
  Duration: ~3–4 weeks

MONTH 8     Inference Server Testing (Isolated)
────────────────────────────────────────────────────
  Deploy FastAPI locally
  Test all 4 endpoints manually (curl / Postman)
  Run acceptance criteria checklist
  Duration: ~1 week

MONTH 8–9   Integration with Spring Boot + Frontend
────────────────────────────────────────────────────
  Add ml/ module to Spring Boot
  Add 4 new hooks + modify 3 components in frontend
  Set ml.enabled=true in application.properties
  Test full round trip: Frontend → Spring Boot → FastAPI
  Duration: ~2 weeks

MONTH 9     Version 2 Goes Live
────────────────────────────────────────────────────
  Deploy docker-compose-prod.yml with ml-inference service
  Monitor circuit breaker — check for fallback triggers
  Monitor ML suggestion acceptance rate
  Collect feedback for next model iteration
```

---

## 12. Deployment — docker-compose-prod.yml

```yaml
version: '3.8'

services:

  # ─────────────── EXISTING SERVICES (unchanged) ───────────────

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: taskhive
      POSTGRES_USER: taskhive
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - taskhive-network

  backend:
    image: taskhive/backend:latest
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/taskhive
      ML_ENABLED: true                              # ← changed from false to true
      ML_INFERENCE_URL: http://ml-inference:8000    # ← resolves via Docker network
    depends_on:
      - postgres
      - ml-inference                                # ← new dependency
    networks:
      - taskhive-network

  frontend:
    image: taskhive/frontend:latest
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: https://api.taskhive.com/api/v1
      NEXT_PUBLIC_ENABLE_ML: true                   # ← changed from false to true
    networks:
      - taskhive-network

  # ─────────────── NEW SERVICE (Version 2 only) ────────────────

  ml-inference:
    image: taskhive/ml-inference:latest             # ← built from ml/inference-server/Dockerfile
    ports:
      - "8000:8000"                                 # internal only — not exposed to internet in prod
    volumes:
      - ./ml/inference-server/models:/app/models    # mount trained .pkl files
    environment:
      MODEL_PATH: /app/models
      LOG_LEVEL: info
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - taskhive-network
    restart: unless-stopped

networks:
  taskhive-network:
    driver: bridge

volumes:
  postgres_data:
```

> **Security note**: In production, `ml-inference` port 8000 should NOT be exposed to the internet. Only Spring Boot (inside the same Docker network) can reach it via `http://ml-inference:8000`.

---

## 13. Circuit Breaker — Graceful Fallback

Your Resilience4j config is already in `application.properties`. Here is what happens when ML server is down:

```
NORMAL (ML server up):
  Frontend → Spring Boot → FastAPI → Spring Boot → Frontend
  User sees ML suggestions ✅

ML SERVER DOWN:
  Frontend → Spring Boot → FastAPI (timeout/error)
                              ↓
                    Circuit breaker trips after 5 failures
                              ↓
                    MlFallbackService.java returns:
                    { "available": false, "message": "ML suggestion not available" }
                              ↓
                    Spring Boot returns fallback to Frontend
                              ↓
  User sees TaskForm normally — priority field empty, no suggestion badge
  User fills form manually — everything works as Version 1 ✅

CIRCUIT BREAKER RESETS:
  After 30 seconds (waitDurationInOpenState=30s from your config)
  Spring Boot tries ML again
  If ML server is back → circuit closes → suggestions resume
```

```
Resilience4j config already in application.properties (no change needed):

resilience4j.circuitbreaker.instances.mlService.slidingWindowSize=10
resilience4j.circuitbreaker.instances.mlService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.mlService.waitDurationInOpenState=30s
```

---

## 14. Complete File Changes Summary

### Spring Boot — New Files (11 files)

| File | Type |
|------|------|
| `module/ml/controller/MlController.java` | New |
| `module/ml/service/MlClientService.java` | New |
| `module/ml/service/MlFallbackService.java` | New |
| `module/ml/config/MlClientConfig.java` | New |
| `module/ml/dto/PrioritySuggestionRequest.java` | New |
| `module/ml/dto/PrioritySuggestionResponse.java` | New |
| `module/ml/dto/CompletionEstimationRequest.java` | New |
| `module/ml/dto/CompletionEstimationResponse.java` | New |
| `module/ml/dto/WorkloadRecommendationRequest.java` | New |
| `module/ml/dto/WorkloadRecommendationResponse.java` | New |
| `module/ml/dto/ProductivityScoreResponse.java` | New |

### Spring Boot — Modified Files (1 file)

| File | Change |
|------|--------|
| `application.properties` | `ml.enabled=false` → `ml.enabled=true` |

### Frontend — New Files (6 files)

| File | Type |
|------|------|
| `features/task/hooks/useSuggestPriority.ts` | New |
| `features/task/hooks/useEstimateCompletion.ts` | New |
| `features/task/hooks/useRecommendAssignee.ts` | New |
| `features/analytics/hooks/useProductivityScore.ts` | New |
| `features/task/components/MlSuggestionBadge.tsx` | New |
| `features/task/types/ml.types.ts` | New |

### Frontend — Modified Files (5 files)

| File | Change |
|------|--------|
| `features/task/components/TaskForm.tsx` | Add 3 ML suggestion badges |
| `features/analytics/components/EmployeePerformanceTable.tsx` | Add ML Score column |
| `features/analytics/components/EmployeeDashboard.tsx` | Add productivity score widget |
| `features/task/services/taskService.ts` | Add 3 ML API call functions |
| `features/analytics/services/analyticsService.ts` | Add 1 ML API call function |

### Python ML — New / Implemented Files (16 files)

| File | Type |
|------|------|
| `ml/inference-server/api/main.py` | Implement |
| `ml/inference-server/api/routes/predict_priority.py` | Implement |
| `ml/inference-server/api/routes/predict_completion.py` | Implement |
| `ml/inference-server/api/routes/recommend_workload.py` | New |
| `ml/inference-server/api/routes/productivity_score.py` | New |
| `ml/inference-server/api/schemas/request.py` | Implement |
| `ml/inference-server/api/schemas/response.py` | Implement |
| `ml/inference-server/models/loader.py` | Implement |
| `ml/inference-server/preprocessing/feature_engineering.py` | Implement |
| `ml/inference-server/preprocessing/text_processing.py` | Implement |
| `ml/training/pipelines/data_extraction.py` | Implement |
| `ml/training/pipelines/preprocessing.py` | Implement |
| `ml/training/pipelines/training.py` | Implement |
| `ml/training/pipelines/evaluation.py` | Implement |
| `ml/inference-server/requirements.txt` | Implement |
| `ml/inference-server/Dockerfile` | Implement |

### Deployment — Modified Files (1 file)

| File | Change |
|------|--------|
| `docker-compose-prod.yml` | Add `ml-inference` service block |

---

## Grand Total

| Category | New Files | Modified Files |
|----------|-----------|---------------|
| Spring Boot (Java) | 11 | 1 |
| Frontend (Next.js) | 6 | 5 |
| Python ML Server | 16 | 0 |
| Deployment | 0 | 1 |
| **Total** | **33** | **7** |

**All 6 existing phases — 0 files broken. 0 files restructured.**
The entire migration is purely additive except for 7 small modifications to existing files.
