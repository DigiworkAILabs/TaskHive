"""
preprocessing/feature_engineering.py
──────────────────────────────────────
Shared feature engineering utilities used during inference.
Works with the priority model pipeline for Phase 7.1.
"""

import re
import numpy as np
import pandas as pd
from typing import List, Optional


# ──────────────────────────────────────────────
# Priority keyword signals (for rule-based fallback / debugging)
# ──────────────────────────────────────────────

PRIORITY_KEYWORDS = {
    "CRITICAL": {"urgent", "critical", "production", "blocker", "outage", "down", "emergency"},
    "HIGH":     {"bug", "fix", "crash", "broken", "error", "important", "failure"},
    "MEDIUM":   {"update", "improve", "enhance", "feature", "change", "add", "support"},
    "LOW":      {"docs", "documentation", "minor", "cleanup", "refactor", "typo"},
}


def keyword_priority_hint(text: str) -> Optional[str]:
    """
    Lightweight keyword scan — returns the highest matching priority label
    or None if no keywords matched.
    Used only as a debug/fallback hint, NOT in the main ML pipeline.
    """
    text_lower = text.lower()
    for priority in ["CRITICAL", "HIGH", "MEDIUM", "LOW"]:
        for kw in PRIORITY_KEYWORDS[priority]:
            if kw in text_lower:
                return priority
    return None


# ──────────────────────────────────────────────
# Feature builder — inference input → DataFrame row
# ──────────────────────────────────────────────

def build_priority_feature_row(
    task_title: str,
    task_description: str,
    tags: List[str],
    estimated_hours: float,
    emp_completion_rate: float,
    emp_avg_hours: float,
) -> pd.DataFrame:
    """
    Convert raw request fields into a one-row DataFrame that matches
    the schema expected by the trained priority model pipeline.

    Parameters
    ----------
    task_title          : Raw task title string
    task_description    : Raw task description string
    tags                : List of tag strings
    estimated_hours     : Estimated effort (hours)
    emp_completion_rate : Assignee's historical task completion rate (0.0–1.0)
    emp_avg_hours       : Assignee's average hours per task

    Returns
    -------
    pd.DataFrame with columns:
        title, description, tags_count, estimated_hours,
        emp_completion_rate, emp_avg_hours
    """
    # Clamp numeric inputs to expected ranges
    estimated_hours     = max(0.0, float(estimated_hours or 0.0))
    emp_completion_rate = float(np.clip(emp_completion_rate or 0.75, 0.0, 1.0))
    emp_avg_hours       = float(np.clip(emp_avg_hours or 4.0, 0.5, 24.0))
    tags_count          = len(tags) if tags else 0

    return pd.DataFrame([{
        "title":               task_title.strip(),
        "description":         task_description.strip() if task_description else "",
        "tags_count":          tags_count,
        "estimated_hours":     estimated_hours,
        "emp_completion_rate": emp_completion_rate,
        "emp_avg_hours":       emp_avg_hours,
    }])


# ──────────────────────────────────────────────
# Confidence calibration helper
# ──────────────────────────────────────────────

def get_confidence(proba_array: np.ndarray) -> float:
    """
    Returns the maximum class probability as a rounded confidence score.
    Clips to [0.01, 0.99] to avoid reporting 0 or 1.0.
    """
    confidence = float(np.max(proba_array))
    return round(float(np.clip(confidence, 0.01, 0.99)), 4)


# ──────────────────────────────────────────────
# Reasoning generator
# ──────────────────────────────────────────────

def generate_reasoning(
    predicted_priority: str,
    task_title: str,
    task_description: str,
    tags: List[str],
    confidence: float,
) -> str:
    """
    Produces a short human-readable reasoning string for the prediction.
    """
    text = f"{task_title} {task_description}".lower()
    matched_kws = []

    for kw in PRIORITY_KEYWORDS.get(predicted_priority, set()):
        if kw in text:
            matched_kws.append(kw)

    tag_str = f" Tags: {', '.join(tags[:3])}." if tags else ""
    kw_str  = f" Keywords detected: {', '.join(matched_kws[:3])}." if matched_kws else ""
    conf_str = f" Confidence: {confidence:.0%}."

    base_map = {
        "CRITICAL": "Potential production-impacting or blocker task detected.",
        "HIGH":     "Bug, crash, or important fix detected.",
        "MEDIUM":   "Feature or improvement task detected.",
        "LOW":      "Low-impact documentation or cleanup task detected.",
    }
    base = base_map.get(predicted_priority, "ML model prediction.")

    return f"{base}{kw_str}{tag_str}{conf_str}".strip()
