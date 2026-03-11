import pandas as pd
import numpy as np
from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.preprocessing import StandardScaler

class ColumnSelector(BaseEstimator, TransformerMixin):
    """Select a single text column from a DataFrame."""
    def __init__(self, column: str):
        self.column = column

    def fit(self, X, y=None):
        return self

    def transform(self, X):
        return X[self.column].fillna("").astype(str)


class NumericFeatureExtractor(BaseEstimator, TransformerMixin):
    """Extract numeric columns and scale them."""
    NUMERIC_COLS = ["estimated_hours", "tags_count", "emp_completion_rate", "emp_avg_hours"]

    def fit(self, X, y=None):
        self.scaler_ = StandardScaler()
        self.scaler_.fit(X[self.NUMERIC_COLS].fillna(0))
        return self

    def transform(self, X):
        arr = X[self.NUMERIC_COLS].fillna(0).values
        return self.scaler_.transform(arr)


class DataPreprocessor(BaseEstimator, TransformerMixin):
    """Encodes categorical priority and extracts all numeric features for completion estimation."""
    def __init__(self):
        self.priority_map = {
            "LOW": 1,
            "MEDIUM": 2,
            "HIGH": 3,
            "CRITICAL": 4
        }
        self.numeric_cols = [
            "title_length", "description_length", 
            "emp_on_time_rate", "emp_avg_hours_by_priority", "emp_active_tasks"
        ]
        self.scaler = StandardScaler()
        
    def fit(self, X, y=None):
        X_copy = X.copy()
        X_copy["priority_encoded"] = X_copy["priority"].map(self.priority_map).fillna(2)
        
        cols_to_scale = ["priority_encoded"] + self.numeric_cols
        self.scaler.fit(X_copy[cols_to_scale].fillna(0))
        return self
        
    def transform(self, X):
        X_copy = X.copy()
        X_copy["priority_encoded"] = X_copy["priority"].map(self.priority_map).fillna(2)
        
        cols_to_scale = ["priority_encoded"] + self.numeric_cols
        return self.scaler.transform(X_copy[cols_to_scale].fillna(0))
