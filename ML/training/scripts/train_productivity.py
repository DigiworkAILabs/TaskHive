import pandas as pd
import numpy as np
import joblib
import os
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor, VotingRegressor
from sklearn.metrics import mean_absolute_error, r2_score
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

def train_productivity_model():
    # 1. Load data
    data_path = os.path.join(os.path.dirname(__file__), "..", "data", "productivity_data.csv")
    if not os.path.exists(data_path):
        print(f"Error: {data_path} not found. Run generator first.")
        return

    df = pd.read_csv(data_path)

    # 2. Feature Engineering
    # We transform raw counts into rates for the model
    df['completion_rate'] = df['tasks_completed'] / df['tasks_assigned']
    df['overdue_rate'] = df['tasks_overdue'] / df['tasks_assigned']
    df['engagement_normalized'] = df['comment_activity'].apply(lambda x: min(x / 50.0, 1.0))

    features = [
        'completion_rate', 
        'on_time_rate', 
        'overdue_rate', 
        'engagement_normalized', 
        'avg_completion_hours'
    ]
    X = df[features]
    y = df['score']

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # 3. Build Weighted Ensemble
    # We use VotingRegressor as our weighted ensemble
    model1 = RandomForestRegressor(n_estimators=100, random_state=42)
    model2 = GradientBoostingRegressor(n_estimators=100, random_state=42)
    
    ensemble = VotingRegressor(
        estimators=[('rf', model1), ('gb', model2)],
        weights=[0.4, 0.6]
    )

    pipeline = Pipeline([
        ('scaler', StandardScaler()),
        ('regressor', ensemble)
    ])

    # 4. Train
    print("Training Productivity Weighted Ensemble...")
    pipeline.fit(X_train, y_train)

    # 5. Evaluate
    y_pred = pipeline.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    print(f"Model Evaluation: MAE={mae:.4f}, R2={r2:.4f}")

    # 6. Save
    output_path = os.path.join(os.path.dirname(__file__), "..", "..", "inference-server", "artifacts", "productivity_model.pkl")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    # Save as a bundle similar to other models
    model_bundle = {
        "pipeline": pipeline,
        "features": features,
        "model_type": "weighted_ensemble_regressor"
    }
    
    joblib.dump(model_bundle, output_path)
    print(f"Model saved to {output_path}")

if __name__ == "__main__":
    train_productivity_model()
