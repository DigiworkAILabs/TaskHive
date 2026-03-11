"""
train_workload.py
──────────────────
Phase 7.3 — Workload Balance Recommendation
Trains a Linear Regression model on workload_data.csv to learn the
weighted scoring formula and saves the model pipeline to:
    ML/inference-server/artifacts/workload_model.pkl

Features:
    active_tasks          - int (0–10)
    completion_rate       - float (0.40–1.00)
    on_time_rate          - float (0.30–1.00)
    dept_match            - int (0 or 1)
    task_priority_encoded - int (1–4)

Target:  score (0–100)
Output:  inference-server/artifacts/workload_model.pkl  (scikit-learn Pipeline)
"""

import os
import sys
import joblib
import numpy as np
import pandas as pd

from pathlib import Path
from sklearn.linear_model import LinearRegression
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

# ──────────────────────────────────────────────
# Paths
# ──────────────────────────────────────────────

SCRIPT_DIR   = Path(__file__).parent
DATA_PATH    = SCRIPT_DIR.parent / "data" / "workload_data.csv"
MODEL_OUTPUT = SCRIPT_DIR.parent.parent / "inference-server" / "artifacts" / "workload_model.pkl"

FEATURE_COLS = [
    "active_tasks",
    "completion_rate",
    "on_time_rate",
    "dept_match",
    "task_priority_encoded",
]
TARGET_COL = "score"


# ──────────────────────────────────────────────
# Build pipeline
# ──────────────────────────────────────────────

def build_pipeline() -> Pipeline:
    return Pipeline([
        ("scaler",    StandardScaler()),
        ("regressor", LinearRegression()),
    ])


# ──────────────────────────────────────────────
# Main training routine
# ──────────────────────────────────────────────

def main():
    # 1. Load data
    if not DATA_PATH.exists():
        print(f"[ERROR] Data file not found: {DATA_PATH}")
        print("Run generate_workload_data.py first.")
        sys.exit(1)

    print(f"Loading data from: {DATA_PATH}")
    df = pd.read_csv(DATA_PATH)
    print(f"  Rows: {len(df):,}  |  Columns: {list(df.columns)}")

    # 2. Prepare features and target
    X = df[FEATURE_COLS].copy()
    y = df[TARGET_COL].values

    print(f"\nFeature columns: {FEATURE_COLS}")
    print(f"Target: {TARGET_COL}  (min={y.min():.2f}, max={y.max():.2f}, mean={y.mean():.2f})")

    # 3. Train / test split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42
    )
    print(f"\nTrain: {len(X_train):,}  |  Test: {len(X_test):,}")

    # 4. Build + train pipeline
    print("\nTraining Linear Regression pipeline …")
    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)

    # 5. Cross-validation (5-fold)
    print("\nRunning 5-fold cross-validation …")
    cv_scores = cross_val_score(pipeline, X_train, y_train, cv=5, scoring="r2")
    print(f"  CV R² Score: {cv_scores.mean():.4f} ± {cv_scores.std():.4f}")

    # 6. Test set evaluation
    y_pred = pipeline.predict(X_test)
    mae  = mean_absolute_error(y_test, y_pred)
    rmse = np.sqrt(mean_squared_error(y_test, y_pred))
    r2   = r2_score(y_test, y_pred)

    print(f"\nTest Metrics:")
    print(f"  MAE:  {mae:.4f}")
    print(f"  RMSE: {rmse:.4f}")
    print(f"  R²:   {r2:.4f}")

    if r2 < 0.80:
        print(f"[WARNING] R² {r2:.4f} is below target 0.80 — consider tuning.")
    else:
        print(f"✅ R² {r2:.4f} meets target (>= 0.80)")

    # 7. Inspect learned coefficients
    regressor = pipeline.named_steps["regressor"]
    scaler = pipeline.named_steps["scaler"]
    print(f"\nLearned coefficients (scaled features):")
    for name, coef in zip(FEATURE_COLS, regressor.coef_):
        print(f"  {name:<25} {coef:>8.4f}")
    print(f"  {'intercept':<25} {regressor.intercept_:>8.4f}")

    # 8. Save model bundle
    MODEL_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    model_bundle = {
        "pipeline":     pipeline,
        "feature_cols": FEATURE_COLS,
        "target_col":   TARGET_COL,
        "metrics": {
            "mae":  round(mae, 4),
            "rmse": round(rmse, 4),
            "r2":   round(r2, 4),
        },
    }
    joblib.dump(model_bundle, MODEL_OUTPUT)
    print(f"\n✅ Model saved → {MODEL_OUTPUT}")
    print(f"   File size: {MODEL_OUTPUT.stat().st_size / 1024:.1f} KB")


if __name__ == "__main__":
    main()
