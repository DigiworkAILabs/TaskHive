# TaskHive — Phase 7.2: ML Feature 2 — Completion Time Estimation
## SRS v2.4 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 7.2 of 7.4 |
| **Name** | ML Feature 2 — Completion Time Estimation |
| **Depends On** | Phase 7.1 complete (MLController, MLClientService, MLClientConfig already exist) |
| **Duration** | ~2 days |
| **Model** | Gradient Boosting Regressor |
| **Synthetic Records** | 5,000 tasks |

### Deliverable
When Admin selects priority + assignee on Task Create form, estimated hours hint auto-appears below the Estimated Hours field. Shows a range like "~3–6 hours estimated by AI". Admin can override with their own value. Fully non-blocking.

---

## API Endpoints

```
# FastAPI (port 8000)
POST   /ml/predict/completion-time

# Spring Boot (port 8080)
POST   /api/v1/ml/predict/completion-time     # ADMIN only
```

---

## Functional Requirements

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-ML-02 | High | System shall estimate task completion time based on task data and employee performance |
| FR-ML-05 | Critical | Estimation is non-blocking — admin can always enter own value |
| FR-ML-06 | Critical | Circuit Breaker — app never fails if ML server is down |
| FR-ML-09 | Medium | Response shall include confidence range (low–high hours) |

---

## Request / Response

**Spring Boot receives from Next.js:**
```json
{
  "taskTitle": "Build dashboard charts",
  "taskDescription": "Add pie and bar charts to analytics page",
  "priority": "HIGH",
  "employeeId": "uuid",
  "estimatedHours": 5.0
}
```

**Spring Boot enriches + sends to FastAPI:**
```json
{
  "task_title": "Build dashboard charts",
  "task_description": "Add pie and bar charts to analytics page",
  "priority": "HIGH",
  "title_length": 22,
  "description_length": 45,
  "emp_on_time_rate": 0.78,
  "emp_avg_hours_high": 6.2,
  "emp_avg_hours_medium": 3.8,
  "emp_active_tasks": 3,
  "manual_estimate": 5.0
}
```

**FastAPI returns:**
```json
{
  "estimated_hours": 5.5,
  "confidence_range": { "low": 4.0, "high": 7.0 },
  "reasoning": "HIGH priority tasks for this employee avg 6.2hrs"
}
```

**Spring Boot returns to Next.js:**
```json
{
  "success": true,
  "data": {
    "estimatedHours": 5.5,
    "confidenceRange": { "low": 4.0, "high": 7.0 },
    "reasoning": "HIGH priority tasks for this employee avg 6.2hrs",
    "fallbackUsed": false
  }
}
```

---

## Synthetic Data

**File:** `training/data/completion_data.csv`

**Columns:** `priority, title_length, description_length, emp_on_time_rate, emp_avg_hours_by_priority, emp_active_tasks, actual_hours`

**Generation logic (`generate_completion_data.py`):**
- CRITICAL → actual_hours: 8–20 + noise
- HIGH → actual_hours: 4–10 + noise
- MEDIUM → actual_hours: 2–6 + noise
- LOW → actual_hours: 0.5–3 + noise
- emp_on_time_rate high (>0.8) → reduce hours by 10%
- emp_active_tasks high (>5) → increase hours by 15%

---

## Model

**Algorithm:** Gradient Boosting Regressor (scikit-learn)
**Features:** priority_encoded + title_length + description_length + emp_on_time_rate + emp_avg_hours_by_priority + emp_active_tasks
**Target:** actual_hours (continuous)
**Train script:** `training/train_completion.py`
**Output:** `inference-server/models/completion_model.pkl`

---

## FastAPI Files (this phase)

```
ml/inference-server/
├── main.py                              # MODIFY: include completion router
├── api/
│   ├── routes/
│   │   └── predict_completion.py        # NEW
│   └── schemas/
│       ├── request.py                   # ADD: CompletionTimeRequest
│       └── response.py                  # ADD: CompletionTimeResponse
├── models/
│   ├── loader.py                        # ADD: load completion_model.pkl
│   └── completion_model.pkl             # GENERATED
└── services/
    └── completion_service.py            # NEW

ml/training/
├── data/
│   └── completion_data.csv             # GENERATED
├── generate_completion_data.py          # NEW
└── train_completion.py                  # NEW
```

**Note:** `feature_engineering.py` and `text_processing.py` already exist from 7.1 — reuse them.

---

## Spring Boot Files (this phase)

```
module/ml/
├── controller/
│   └── MLController.java               # MODIFY: add completion endpoint
├── service/
│   └── MLService.java                  # MODIFY: add enrichment for completion
│   └── MLClientService.java            # MODIFY: add completion CB method
└── dto/
    ├── CompletionTimeRequest.java       # NEW
    └── CompletionTimeResponse.java      # NEW
```

**Note:** MLClientConfig, MLClientService, MLController already exist from 7.1 — only add new methods.

**Fallback:** `{ estimatedHours: <admin's manual input>, fallbackUsed: true }`

---

## Frontend Files (this phase)

```
src/features/ml/
├── components/
│   └── CompletionTimeEstimate.tsx      # NEW
├── hooks/
│   └── useCompletionTimePrediction.ts  # NEW
├── services/
│   └── mlService.ts                    # MODIFY: add completion method
└── types/
    └── ml.types.ts                     # MODIFY: add completion types
```

**Integration:** `src/app/(admin)/tasks/new/page.tsx` — MODIFY (add hint below Estimated Hours, auto-trigger when both priority + employee are selected)

---

## File Count Summary

| Layer | New Files | Modified Files |
|-------|-----------|----------------|
| FastAPI | 3 (route, service, train+generate scripts) | main.py, schemas, loader |
| Spring Boot | 2 DTOs | MLController, MLService, MLClientService |
| Next.js | 2 (component, hook) | mlService.ts, ml.types.ts, tasks/new/page.tsx |
