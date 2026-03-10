"""
generate_completion_data.py
───────────────────────────
Phase 7.2 — Task Completion Time Estimation (v2 — Improved Prediction Quality)
Generates 5,000 synthetic task records for training and saves to:
    ML/training/data/completion_data.csv

Key improvements over v1:
  - title_length and description_length now DIRECTLY influence actual_hours
  - emp_avg_hours_by_priority is derived FROM actual_hours (correlated, not random)
  - Wider range of scenarios for better model generalization
"""

import os
import random
import pandas as pd
import numpy as np

random.seed(42)
np.random.seed(42)

TOTAL_RECORDS = 5000

# ──────────────────────────────────────────────
# Task complexity keywords (used to simulate real variation)
# ──────────────────────────────────────────────
QUICK_TASKS = [
    "fix typo", "update readme", "change label", "rename variable",
    "adjust padding", "fix spacing", "update version", "fix link",
    "correct spelling", "small css fix", "update copyright",
]
MEDIUM_TASKS = [
    "add form validation", "create API endpoint", "update dashboard",
    "implement search filter", "add pagination", "write unit tests",
    "refactor service layer", "add error handling", "update database schema",
    "integrate email notification", "add export to csv", "implement caching",
]
LARGE_TASKS = [
    "build complete payment integration with stripe webhook handling",
    "implement real-time notification system with websockets and push",
    "redesign entire authentication system with oauth2 and sso integration",
    "build analytics dashboard with charts and data export features",
    "implement multi-tenant architecture with role based access control",
    "develop automated testing framework with ci cd pipeline integration",
    "create microservice communication layer with message queue system",
    "build comprehensive audit logging system with compliance reporting",
]


def generate_row():
    # Priority
    priority = random.choices(
        ["CRITICAL", "HIGH", "MEDIUM", "LOW"],
        weights=[0.10, 0.25, 0.45, 0.20],
        k=1
    )[0]

    # ── Task complexity determines text lengths AND hours ──
    # Pick a complexity tier (this creates the correlation)
    complexity = random.choices(
        ["quick", "medium", "large"],
        weights=[0.30, 0.45, 0.25],
        k=1
    )[0]

    if complexity == "quick":
        title_length = random.randint(8, 25)
        description_length = random.randint(10, 60)
        complexity_multiplier = random.uniform(0.3, 0.7)
    elif complexity == "medium":
        title_length = random.randint(25, 55)
        description_length = random.randint(60, 200)
        complexity_multiplier = random.uniform(0.7, 1.3)
    else:  # large
        title_length = random.randint(50, 120)
        description_length = random.randint(150, 500)
        complexity_multiplier = random.uniform(1.3, 2.5)

    # ── Employee metrics ──
    emp_on_time_rate = round(random.uniform(0.45, 0.99), 2)
    emp_active_tasks = random.randint(0, 10)

    # ── Base hours driven by PRIORITY ──
    if priority == "CRITICAL":
        priority_base = random.uniform(6, 16)
    elif priority == "HIGH":
        priority_base = random.uniform(3, 8)
    elif priority == "MEDIUM":
        priority_base = random.uniform(1.5, 5)
    else:  # LOW
        priority_base = random.uniform(0.5, 2.5)

    # ── actual_hours = priority_base × complexity + employee effects ──
    actual_hours = priority_base * complexity_multiplier

    # Employee performance effects
    if emp_on_time_rate > 0.85:
        actual_hours *= random.uniform(0.82, 0.95)  # fast worker
    elif emp_on_time_rate < 0.60:
        actual_hours *= random.uniform(1.05, 1.25)  # slower worker

    if emp_active_tasks > 6:
        actual_hours *= random.uniform(1.10, 1.30)  # overloaded = slower
    elif emp_active_tasks <= 1:
        actual_hours *= random.uniform(0.85, 0.95)  # free = faster

    # Description length bonus (longer descriptions = more complex work)
    actual_hours += description_length * random.uniform(0.002, 0.008)

    # Add realistic noise (±10%)
    noise = actual_hours * random.uniform(-0.10, 0.10)
    actual_hours = max(0.5, actual_hours + noise)

    # ── emp_avg_hours_by_priority: CORRELATED to actual_hours ──
    # This simulates the "historical average" — it should be similar to actual
    emp_avg = actual_hours * random.uniform(0.7, 1.3)
    emp_avg = max(0.5, emp_avg)

    return {
        "priority": priority,
        "title_length": title_length,
        "description_length": description_length,
        "emp_on_time_rate": emp_on_time_rate,
        "emp_avg_hours_by_priority": round(emp_avg, 1),
        "emp_active_tasks": emp_active_tasks,
        "actual_hours": round(actual_hours, 1)
    }


# ──────────────────────────────────────────────
# Build & Save Dataset
# ──────────────────────────────────────────────
def main():
    rows = [generate_row() for _ in range(TOTAL_RECORDS)]
    df = pd.DataFrame(rows)

    # Shuffle
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)

    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(script_dir, "..", "data")
    os.makedirs(output_dir, exist_ok=True)

    output_path = os.path.join(output_dir, "completion_data.csv")
    df.to_csv(output_path, index=False)

    print(f"✅ Generated {len(df):,} records for Completion Time Estimation")
    print(f"Saved to: {output_path}")
    print(f"\nHour Distribution by Priority:")
    print(df.groupby("priority")["actual_hours"].describe()[["mean", "min", "max", "std"]].to_string())
    print(f"\nHour Distribution by Complexity (title_length buckets):")
    df["complexity"] = pd.cut(df["title_length"], bins=[0, 25, 55, 200], labels=["quick", "medium", "large"])
    print(df.groupby("complexity")["actual_hours"].describe()[["mean", "min", "max", "std"]].to_string())

if __name__ == "__main__":
    main()
