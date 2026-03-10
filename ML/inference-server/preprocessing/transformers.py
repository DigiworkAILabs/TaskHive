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
