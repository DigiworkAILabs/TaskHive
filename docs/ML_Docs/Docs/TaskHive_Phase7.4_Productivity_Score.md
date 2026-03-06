# TaskHive — Phase 7.4: ML Feature 4 — Employee Productivity Scoring
## SRS v2.4 | Company: Digiwork | Base Package: `com.digiwork.taskhive`

---

## Phase Summary

| Field | Detail |
|-------|--------|
| **Phase** | 7.4 of 7.4 |
| **Name** | ML Feature 4 — Employee Productivity Scoring |
| **Depends On** | Phase 7.1 + 7.2 + 7.3 complete |
| **Duration** | ~2 days |
| **Model** | Weighted Ensemble |
| **Synthetic Records** | 3,000 employee periods |
| **DB Migration** | V7.0 (ml_prediction_logs) |

### Deliverable
Employee detail page and Analytics dashboard show a Productivity Score card (0–100, A–F grade, trend: improving/stable/declining). Score calculated from task completion stats. Also final phase: Docker compose updated, DB migration V7.0 added for prediction logs.

---

## API Endpoints

```
# FastAPI (port 8000)
POST   /ml/score/employee

# Spring Boot (port 8080)
GET    /api/v1/ml/score/employee/{id}?periodDays=30   # ADMIN + self (EMPLOYEE own score)
```

---

## Functional Requirements

| ID | Priority | Requirement |
|----|----------|-------------|
| FR-ML-04 | High | System shall calculate employee productivity score (0–100) with grade and trend |
| FR-ML-06 | Critical | Circuit Breaker — app never fails if ML server is down |
| FR-ML-09 | Medium | Response shall include score breakdown and trend |
| FR-ML-10 | Low | ML prediction logs saved to DB for future retraining (V7.0 migration) |

---

## Score Breakdown

| Component | Weight | Source |
|-----------|--------|--------|
| Completion Rate | 35% | tasks_completed / tasks_assigned |
| On-Time Rate | 30% | tasks completed before due_date |
| Overdue Penalty | -20% | tasks_overdue / tasks_assigned |
| Engagement | 15% | comment_activity normalized |

**Grade:** A (90–100), B (75–89), C (60–74), D (45–59), F (<45)
**Trend:** compare current period vs previous period score → improving/stable/declining

---

## Request / Response

**Spring Boot receives from Next.js:**
```
GET /api/v1/ml/score/employee/{id}?periodDays=30
```

**Spring Boot fetches from DB + sends to FastAPI:**
```json
{
  "employee_id": "uuid",
  "period_days": 30,
  "tasks_assigned": 12,
  "tasks_completed": 10,
  "tasks_overdue": 1,
  "on_time_rate": 0.80,
  "avg_completion_hours": 4.8,
  "comment_activity": 24
}
```

**FastAPI returns:**
```json
{
  "score": 82.5,
  "grade": "B",
  "breakdown": {
    "completion_rate_score": 29.2,
    "on_time_score": 24.0,
    "overdue_penalty": -1.7,
    "engagement_score": 11.0
  },
  "trend": "improving",
  "reasoning": "Strong completion rate, minor overdue impact"
}
```

**Spring Boot returns to Next.js:**
```json
{
  "success": true,
  "data": {
    "score": 82.5,
    "grade": "B",
    "breakdown": { ... },
    "trend": "improving",
    "reasoning": "Strong completion rate, minor overdue impact",
    "fallbackUsed": false
  }
}
```

---

## Synthetic Data

**File:** `training/data/productivity_data.csv`

**Columns:** `tasks_assigned, tasks_completed, tasks_overdue, on_time_rate, avg_completion_hours, comment_activity, score`

**Generation logic (`generate_productivity_data.py`):**
- score = (completion_rate * 35) + (on_time_rate * 30) - (overdue_rate * 20) + (engagement * 15)
- completion_rate = tasks_completed / tasks_assigned
- engagement = min(comment_activity / 50, 1.0)
- Add noise ±5 to score

---

## Model

**Algorithm:** Linear Regression / Weighted Ensemble (scikit-learn)
**Features:** completion_rate + on_time_rate + overdue_rate + engagement_normalized + avg_completion_hours
**Target:** score (0–100 continuous)
**Train script:** `training/train_productivity.py`
**Output:** `inference-server/models/productivity_model.pkl`

---

## FastAPI Files (this phase)

```
ml/inference-server/
├── main.py                              # MODIFY: include productivity router
├── api/
│   ├── routes/
│   │   └── productivity_score.py        # NEW
│   └── schemas/
│       ├── request.py                   # ADD: ProductivityScoreRequest
│       └── response.py                  # ADD: ProductivityScoreResponse
├── models/
│   ├── loader.py                        # ADD: load productivity_model.pkl
│   └── productivity_model.pkl           # GENERATED
└── services/
    └── productivity_service.py          # NEW

ml/training/
├── data/
│   └── productivity_data.csv           # GENERATED
├── generate_productivity_data.py        # NEW
└── train_productivity.py                # NEW
```

---

## Spring Boot Files (this phase)

```
module/ml/
├── controller/
│   └── MLController.java               # MODIFY: add productivity endpoint
├── service/
│   └── MLService.java                  # MODIFY: add stats enrichment + trend calc
│   └── MLClientService.java            # MODIFY: add productivity CB method
└── dto/
    ├── ProductivityScoreRequest.java    # NEW
    └── ProductivityScoreResponse.java   # NEW
```

**Trend calculation in MLService:** fetch current period score + previous period score from `ml_prediction_logs` table → compare → return improving/stable/declining.

**Fallback:** `{ score: null, grade: null, reasoning: "Score unavailable", fallbackUsed: true }`

---

## DB Migration (this phase)

**File:** `V7.0__create_ml_prediction_logs_table.sql`

```sql
CREATE TABLE ml_prediction_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

## Frontend Files (this phase)

```
src/features/ml/
├── components/
│   └── ProductivityScoreCard.tsx       # NEW
├── hooks/
│   └── useProductivityScore.ts         # NEW
├── services/
│   └── mlService.ts                    # MODIFY: add productivity method
└── types/
    └── ml.types.ts                     # MODIFY: add productivity types
```

**Integration:**
- `src/app/(admin)/employees/[id]/page.tsx` — MODIFY (add score card)
- `src/app/(employee)/dashboard/page.tsx` — MODIFY (add own score card)
- `src/app/(admin)/analytics/page.tsx` — MODIFY (add score column in performance table)

---

## File Count Summary

| Layer | New Files | Modified Files |
|-------|-----------|----------------|
| FastAPI | 3 (route, service, train+generate scripts) | main.py, schemas, loader |
| Spring Boot | 2 DTOs | MLController, MLService, MLClientService |
| Next.js | 2 (component, hook) | mlService.ts, ml.types.ts, 3 pages |
| DB Migration | 1 (V7.0) | — |
| Docker | — | docker-compose.yml (see main SRS) |

---

## Phase 7 Complete — Final Checklist

| Step | Done |
|------|------|
| 7.1 FastAPI bare bones + Feature 1 (Priority) | ☐ |
| 7.2 Feature 2 (Completion Time) | ☐ |
| 7.3 Feature 3 (Workload Balance) | ☐ |
| 7.4 Feature 4 (Productivity Score) + V7.0 + Docker | ☐ |
| ml.enabled=true in production | ☐ |
| All 4 .pkl models in inference-server/models/ | ☐ |
