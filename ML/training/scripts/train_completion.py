"""
train_completion.py
─────────────────────────
Phase 7.2 — Task Completion Time Estimation
Trains a Gradient Boosting Regressor on completion_data.csv and saves
the model pipeline to ML/inference-server/artifacts/completion_model.pkl

Features:
    priority_encoded          → mapped from LOW/MEDIUM/HIGH/CRITICAL
    title_length              → int
    description_length        → int
    emp_on_time_rate          → float
    emp_avg_hours_by_priority → float
    emp_active_tasks          → int

Target:  actual_hours         → continuous (Regression)
Output:  inference-server/artifacts/completion_model.pkl (scikit-learn Pipeline)
"""

import os
import sys
import joblib
import numpy as np
import pandas as pd

from pathlib import Path
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from sklearn.base import BaseEstimator, TransformerMixin

# Paths relative to this script
SCRIPT_DIR   = Path(__file__).parent
DATA_PATH    = SCRIPT_DIR.parent / "data" / "completion_data.csv"
MODEL_OUTPUT = SCRIPT_DIR.parent.parent / "inference-server" / "artifacts" / "completion_model.pkl"

# Ensure inference-server is on sys.path for shared imports
sys.path.append(str(SCRIPT_DIR.parent.parent / "inference-server"))
from preprocessing.transformers import DataPreprocessor


# ──────────────────────────────────────────────
# 2. Main Training Routine
# ──────────────────────────────────────────────
def main():
    if not DATA_PATH.exists():
        print(f"[ERROR] Data file not found: {DATA_PATH}")
        print("Run generate_completion_data.py first.")
        sys.exit(1)

    print(f"Loading data from: {DATA_PATH}")
    df = pd.read_csv(DATA_PATH)
    print(f"  Rows: {len(df):,}  |  Columns: {list(df.columns)}")

    # Target
    y = df["actual_hours"].values
    X = df.drop(columns=["actual_hours"])

    # Splitting
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42
    )
    print(f"\nTrain: {len(X_train):,}  |  Test: {len(X_test):,}")

    # Build Pipeline
    print("\nTraining Gradient Boosting Regressor pipeline …")
    
    pipeline = Pipeline([
        ("preprocessor", DataPreprocessor()),
        ("regressor", GradientBoostingRegressor(
            n_estimators=150,
            learning_rate=0.1,
            max_depth=4,
            random_state=42
        ))
    ])
    
    pipeline.fit(X_train, y_train)

    # 5-fold CV
    print("\nRunning 5-fold cross-validation …")
    cv_scores = cross_val_score(pipeline, X_train, y_train, cv=5, scoring="neg_mean_absolute_error", n_jobs=-1)
    mae_cv = -cv_scores.mean()
    print(f"  CV MAE: {mae_cv:.4f} hours")

    # Final evaluation
    y_pred = pipeline.predict(X_test)
    test_mae = mean_absolute_error(y_test, y_pred)
    test_rmse = np.sqrt(mean_squared_error(y_test, y_pred))
    test_r2 = r2_score(y_test, y_pred)
    
    print(f"\nTest Metrics:")
    print(f"  Mean Absolute Error (MAE): {test_mae:.4f} hours")
    print(f"  Root Mean Squared Error (RMSE): {test_rmse:.4f} hours")
    print(f"  R^2 Score: {test_r2:.4f}")

    # Error analysis
    print(f"\nOn average, predictions are off by ~{test_mae * 60:.0f} minutes.")

    # Save Model Bundle
    MODEL_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    
    model_bundle = {
        "pipeline": pipeline,
        "feature_cols": ["priority", "title_length", "description_length", 
                         "emp_on_time_rate", "emp_avg_hours_by_priority", "emp_active_tasks"]
    }
    
    joblib.dump(model_bundle, MODEL_OUTPUT)
    print(f"\n✅ Model saved → {MODEL_OUTPUT}")
    print(f"   File size: {MODEL_OUTPUT.stat().st_size / 1024:.1f} KB")

if __name__ == "__main__":
    main()
