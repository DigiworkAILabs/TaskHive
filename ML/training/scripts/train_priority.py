"""
train_priority.py
─────────────────
Phase 7.1 — Task Priority Suggestion
Trains a Random Forest Classifier on priority_data.csv and saves
the model pipeline to ML/models/priority_model.pkl

Features:
    title_tfidf        → 50 TF-IDF features from task title
    description_tfidf  → 30 TF-IDF features from task description
    estimated_hours    → float
    tags_count         → int
    emp_completion_rate → float
    emp_avg_hours      → float

Target:  priority  (LOW / MEDIUM / HIGH / CRITICAL)
Output:  models/priority_model.pkl  (scikit-learn Pipeline)
"""

import os
import sys
import joblib
import numpy as np
import pandas as pd

from pathlib import Path
from sklearn.ensemble import RandomForestClassifier
from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.metrics import classification_report, accuracy_score

# SCRIPT_DIR is ML/training/scripts
SCRIPT_DIR   = Path(__file__).parent
DATA_PATH    = SCRIPT_DIR.parent / "data" / "priority_data.csv"
MODEL_OUTPUT = SCRIPT_DIR.parent.parent / "inference-server" / "artifacts" / "priority_model.pkl"

# Ensure inference-server is on sys.path for shared imports
sys.path.append(str(SCRIPT_DIR.parent.parent / "inference-server"))
from preprocessing.transformers import ColumnSelector, NumericFeatureExtractor


# ──────────────────────────────────────────────
# Build feature pipeline
# ──────────────────────────────────────────────

def build_pipeline() -> Pipeline:
    title_tfidf = Pipeline([
        ("selector", ColumnSelector("title")),
        ("tfidf",    TfidfVectorizer(
            max_features=50,
            ngram_range=(1, 2),
            sublinear_tf=True,
            min_df=2,
        )),
    ])

    desc_tfidf = Pipeline([
        ("selector", ColumnSelector("description")),
        ("tfidf",    TfidfVectorizer(
            max_features=30,
            ngram_range=(1, 1),
            sublinear_tf=True,
            min_df=2,
        )),
    ])

    feature_union = FeatureUnion([
        ("title_tfidf",  title_tfidf),
        ("desc_tfidf",   desc_tfidf),
        ("numeric",      NumericFeatureExtractor()),
    ])

    classifier = RandomForestClassifier(
        n_estimators=200,
        max_depth=None,
        min_samples_split=4,
        min_samples_leaf=2,
        max_features="sqrt",
        class_weight="balanced",
        random_state=42,
        n_jobs=-1,
    )

    return Pipeline([
        ("features",   feature_union),
        ("classifier", classifier),
    ])


# ──────────────────────────────────────────────
# Main training routine
# ──────────────────────────────────────────────

def main():
    # 1. Load data
    if not DATA_PATH.exists():
        print(f"[ERROR] Data file not found: {DATA_PATH}")
        print("Run generate_priority_data.py first.")
        sys.exit(1)

    print(f"Loading data from: {DATA_PATH}")
    df = pd.read_csv(DATA_PATH)
    print(f"  Rows: {len(df):,}  |  Columns: {list(df.columns)}")

    # 2. Encode labels
    label_order = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    le = LabelEncoder()
    le.classes_ = np.array(label_order)
    y = le.transform(df["priority"])

    X = df.drop(columns=["priority"])

    print(f"\nLabel distribution:")
    for cls, count in zip(*np.unique(y, return_counts=True)):
        print(f"  {label_order[cls]:<10}  {count:>5,}")

    # 3. Train / test split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )
    print(f"\nTrain: {len(X_train):,}  |  Test: {len(X_test):,}")

    # 4. Build + train pipeline
    print("\nTraining Random Forest pipeline …")
    pipeline = build_pipeline()
    pipeline.fit(X_train, y_train)

    # 5. Cross-validation (5-fold)
    print("\nRunning 5-fold cross-validation …")
    cv_scores = cross_val_score(pipeline, X_train, y_train, cv=5, scoring="accuracy", n_jobs=-1)
    print(f"  CV Accuracy: {cv_scores.mean():.4f} ± {cv_scores.std():.4f}")

    # 6. Test set evaluation
    y_pred = pipeline.predict(X_test)
    test_acc = accuracy_score(y_test, y_pred)
    print(f"\nTest Accuracy: {test_acc:.4f}")
    print("\nClassification Report:")
    print(classification_report(y_test, y_pred, target_names=label_order))

    # SRS minimum threshold
    if test_acc < 0.80:
        print(f"[WARNING] Accuracy {test_acc:.4f} is below target 0.80 — consider tuning.")
    else:
        print(f"✅ Accuracy {test_acc:.4f} meets target (>= 0.80)")

    # 7. Save model + label encoder together
    MODEL_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    model_bundle = {
        "pipeline":     pipeline,
        "label_encoder": le,
        "label_order":  label_order,
        "feature_cols": ["title", "description", "tags_count",
                         "estimated_hours", "emp_completion_rate", "emp_avg_hours"],
    }
    joblib.dump(model_bundle, MODEL_OUTPUT)
    print(f"\n✅ Model saved → {MODEL_OUTPUT}")
    print(f"   File size: {MODEL_OUTPUT.stat().st_size / 1024:.1f} KB")


if __name__ == "__main__":
    main()
