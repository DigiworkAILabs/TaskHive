"""
generate_workload_data.py
─────────────────────────
Phase 7.3 — Workload Balance Recommendation
Generates 2,000 synthetic workload assignment scenarios and saves them to:
    ML/training/data/workload_data.csv

Columns:
    active_tasks          - Number of currently assigned active tasks (0–10)
    completion_rate       - Historical task completion rate (0.40–1.00)
    on_time_rate          - Historical on-time delivery rate (0.30–1.00)
    dept_match            - 1 if employee's department matches task, 0 otherwise
    task_priority_encoded - Encoded task priority (1=LOW, 2=MEDIUM, 3=HIGH, 4=CRITICAL)
    score                 - Weighted suitability score (0–100) — TARGET for regression
    was_best_fit          - 1 if this candidate had highest score in their group, else 0

Scoring formula (per SRS):
    score = (completion_rate * 30) + (on_time_rate * 25)
          + ((10 - active_tasks) * 3) + (dept_match * 20)
    ±10% random noise applied to avoid perfect linear separation.

Candidates are grouped into assignment scenarios (3–6 candidates per group).
Only the one with the highest noisy score is marked as was_best_fit = 1.
"""

import os
import random
import pandas as pd
import numpy as np

random.seed(42)
np.random.seed(42)

# ──────────────────────────────────────────────
# Configuration
# ──────────────────────────────────────────────

TOTAL_RECORDS = 2000
CANDIDATES_PER_GROUP = (3, 6)     # Each assignment scenario has 3–6 candidates
NOISE_FACTOR = 0.10               # ±10% noise on raw score

PRIORITY_MAP = {
    "LOW": 1,
    "MEDIUM": 2,
    "HIGH": 3,
    "CRITICAL": 4,
}
PRIORITY_WEIGHTS = [0.20, 0.40, 0.25, 0.15]  # Distribution: LOW, MEDIUM, HIGH, CRITICAL


# ──────────────────────────────────────────────
# Scoring function (per SRS Phase 7.3)
# ──────────────────────────────────────────────

def compute_raw_score(
    completion_rate: float,
    on_time_rate: float,
    active_tasks: int,
    dept_match: int,
) -> float:
    """
    Compute the raw suitability score for a candidate.
    Formula from SRS:
        score = (completion_rate * 30) + (on_time_rate * 25)
              + ((10 - active_tasks) * 3) + (dept_match * 20)
    """
    score = (
        (completion_rate * 30.0)
        + (on_time_rate * 25.0)
        + ((10 - active_tasks) * 3.0)
        + (dept_match * 20.0)
    )
    return score


def add_noise(score: float) -> float:
    """Apply ±10% random noise to a score, clamped to [0, 100]."""
    noise = score * random.uniform(-NOISE_FACTOR, NOISE_FACTOR)
    return max(0.0, min(100.0, score + noise))


# ──────────────────────────────────────────────
# Candidate row generator
# ──────────────────────────────────────────────

def generate_candidate(task_priority_encoded: int) -> dict:
    """Generate a single candidate row with random but realistic features."""

    active_tasks = random.randint(0, 10)
    completion_rate = round(random.uniform(0.40, 1.00), 2)
    on_time_rate = round(random.uniform(0.30, 1.00), 2)
    dept_match = random.choice([0, 1])

    raw_score = compute_raw_score(completion_rate, on_time_rate, active_tasks, dept_match)
    noisy_score = round(add_noise(raw_score), 2)

    return {
        "active_tasks": active_tasks,
        "completion_rate": completion_rate,
        "on_time_rate": on_time_rate,
        "dept_match": dept_match,
        "task_priority_encoded": task_priority_encoded,
        "score": noisy_score,
        "was_best_fit": 0,  # Will be set after grouping
    }


# ──────────────────────────────────────────────
# Dataset generator
# ──────────────────────────────────────────────

def generate_dataset(target_rows: int = TOTAL_RECORDS) -> pd.DataFrame:
    """
    Generate grouped assignment scenarios.
    Each group has 3–6 candidates competing for the same task.
    The candidate with the highest noisy score gets was_best_fit = 1.
    """
    all_rows = []
    priorities = list(PRIORITY_MAP.keys())
    priority_values = list(PRIORITY_MAP.values())

    while len(all_rows) < target_rows:
        # Pick a random task priority for this scenario
        chosen_priority = random.choices(priorities, weights=PRIORITY_WEIGHTS, k=1)[0]
        task_priority_encoded = PRIORITY_MAP[chosen_priority]

        # Generate 3–6 candidates for this task
        group_size = random.randint(*CANDIDATES_PER_GROUP)
        group = [generate_candidate(task_priority_encoded) for _ in range(group_size)]

        # Mark the highest-scoring candidate as best fit
        best_idx = max(range(len(group)), key=lambda i: group[i]["score"])
        group[best_idx]["was_best_fit"] = 1

        all_rows.extend(group)

    # Trim to exactly target_rows
    all_rows = all_rows[:target_rows]

    df = pd.DataFrame(all_rows)

    # Shuffle rows
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)

    return df


# ──────────────────────────────────────────────
# Save to CSV
# ──────────────────────────────────────────────

def main():
    # Output path: ML/training/data/workload_data.csv
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(script_dir, "..", "data")
    os.makedirs(output_dir, exist_ok=True)

    output_path = os.path.join(output_dir, "workload_data.csv")

    print("Generating synthetic workload balance data …")
    df = generate_dataset(TOTAL_RECORDS)

    df.to_csv(output_path, index=False)

    # ── Summary ──
    print(f"\n✅ Saved {len(df):,} records → {output_path}")

    print("\nColumn types:")
    for col in df.columns:
        print(f"  {col:<25} {df[col].dtype}")

    print(f"\nScore statistics (target variable):")
    print(f"  Min:  {df['score'].min():.2f}")
    print(f"  Max:  {df['score'].max():.2f}")
    print(f"  Mean: {df['score'].mean():.2f}")
    print(f"  Std:  {df['score'].std():.2f}")

    print(f"\nwas_best_fit distribution:")
    dist = df["was_best_fit"].value_counts().sort_index()
    for label, count in dist.items():
        pct = count / len(df) * 100
        print(f"  {label}  →  {count:>5,}  ({pct:5.1f}%)")

    print(f"\nPriority distribution:")
    priority_dist = df["task_priority_encoded"].value_counts().sort_index()
    rev_map = {v: k for k, v in PRIORITY_MAP.items()}
    for enc, count in priority_dist.items():
        pct = count / len(df) * 100
        print(f"  {enc} ({rev_map[enc]:<8})  →  {count:>5,}  ({pct:5.1f}%)")

    print(f"\nSample rows:")
    print(df.head(10).to_string(index=False))


if __name__ == "__main__":
    main()
