"""
generate_priority_data.py
─────────────────────────
Phase 7.1 — Task Priority Suggestion
Generates 5,000 synthetic task records and saves them to:
    ML/training/data/priority_data.csv

Columns:
    title               - Task title string
    description         - Task description string
    tags_count          - Number of tags (int)
    estimated_hours     - Estimated effort in hours (float)
    emp_completion_rate - Assignee historical completion rate (0.4–1.0)
    emp_avg_hours       - Assignee average hours per task (1–12)
    priority            - Label: LOW / MEDIUM / HIGH / CRITICAL

Label rules (per SRS):
    CRITICAL → keywords: urgent, critical, production down, blocker, outage
    HIGH     → keywords: bug, fix, crash, broken, error, important
    MEDIUM   → keywords: update, improve, enhance, feature, change
    LOW      → keywords: docs, documentation, minor, cleanup, refactor
    15% random noise applied to avoid perfect keyword separation.
"""

import os
import random
import pandas as pd
import numpy as np

random.seed(42)
np.random.seed(42)

# ──────────────────────────────────────────────
# 1. Template pools per priority class
# ──────────────────────────────────────────────

TEMPLATES = {
    "CRITICAL": {
        "title_prefixes": [
            "URGENT: {subject}",
            "Critical: {subject}",
            "Production down – {subject}",
            "Blocker: {subject}",
            "Outage alert – {subject}",
            "URGENT fix needed for {subject}",
            "Critical blocker on {subject}",
        ],
        "subjects": [
            "payment gateway failure",
            "login service unavailable",
            "database connection pool exhausted",
            "API rate limiter broken",
            "notification service down",
            "order processing halted",
            "authentication token expiry bug",
            "data sync pipeline failure",
            "real-time websocket outage",
            "mobile app crashes on launch",
            "email delivery system stopped",
            "session management failure",
        ],
        "descriptions": [
            "Production system is completely down. All users affected. Immediate action required.",
            "Critical outage detected. SLA breach imminent. Must resolve within 1 hour.",
            "Blocker identified in the critical path. Escalate to senior engineer immediately.",
            "System unavailable for all users. Revenue impact ongoing. Emergency patch needed.",
            "Production down. Customer-facing feature broken. Business operations halted.",
            "Urgent: Multiple clients reporting complete service unavailability.",
            "Critical data integrity issue detected. Rollback may be required.",
        ],
        "estimated_hours_range": (1.0, 6.0),
        "tags_count_range": (2, 5),
    },
    "HIGH": {
        "title_prefixes": [
            "Fix: {subject}",
            "Bug: {subject}",
            "Crash in {subject}",
            "Broken {subject}",
            "Error in {subject}",
            "Important: fix {subject}",
            "Fix broken {subject}",
            "Resolve error in {subject}",
        ],
        "subjects": [
            "user profile update",
            "dashboard charts rendering",
            "task assignment workflow",
            "file upload validation",
            "search functionality",
            "report export feature",
            "notification delivery",
            "role-based access control",
            "password reset flow",
            "mobile push notifications",
            "data export API",
            "timezone conversion logic",
            "pagination on task list",
            "bulk task update endpoint",
        ],
        "descriptions": [
            "Users are experiencing consistent failures. This is causing significant disruption to daily operations.",
            "Reproducible bug affecting a key feature. Multiple users have reported the issue.",
            "Error occurs reliably under specific conditions. Needs investigation and patch.",
            "This broken feature is blocking several teams from completing their work.",
            "Important regression introduced in the last release. Needs immediate fix.",
            "High-priority bug affecting 20%+ of users based on error logs.",
            "Crash occurs on mobile devices running Android 12+. Fix required urgently.",
        ],
        "estimated_hours_range": (2.0, 10.0),
        "tags_count_range": (1, 4),
    },
    "MEDIUM": {
        "title_prefixes": [
            "Update {subject}",
            "Improve {subject}",
            "Enhance {subject}",
            "New feature: {subject}",
            "Change {subject}",
            "Feature request: {subject}",
            "Enhancement for {subject}",
        ],
        "subjects": [
            "user onboarding flow",
            "dashboard layout",
            "email templates",
            "task filter options",
            "employee profile page",
            "notification preferences",
            "report generation UI",
            "mobile navigation bar",
            "admin settings panel",
            "task comment section",
            "dark mode support",
            "data export formats",
            "API response structure",
            "search results ranking",
            "activity log display",
        ],
        "descriptions": [
            "This enhancement will improve the overall user experience for new users.",
            "Updating this feature based on recent user feedback and analytics data.",
            "Adding support for new functionality requested during the last sprint review.",
            "Improvement to make the feature more intuitive and easier to use.",
            "Feature change aligned with the updated product roadmap for Q2.",
            "Enhancement requested by multiple team leads to streamline their workflow.",
            "Modifying existing behavior to better match updated business requirements.",
        ],
        "estimated_hours_range": (3.0, 12.0),
        "tags_count_range": (1, 3),
    },
    "LOW": {
        "title_prefixes": [
            "Docs: {subject}",
            "Documentation for {subject}",
            "Minor fix in {subject}",
            "Cleanup: {subject}",
            "Refactor {subject}",
            "Minor update to {subject}",
            "Update documentation for {subject}",
        ],
        "subjects": [
            "API endpoint docs",
            "README instructions",
            "code comments",
            "test helper utilities",
            "deprecated method removal",
            "unused import cleanup",
            "variable naming consistency",
            "logging statements",
            "unit test descriptions",
            "inline code documentation",
            "config file comments",
            "changelog entries",
            "contribution guidelines",
            "local dev setup guide",
            "swagger annotations",
        ],
        "descriptions": [
            "Minor documentation update to keep the wiki accurate and up to date.",
            "Small cleanup task to remove dead code and improve readability.",
            "Refactoring internal utilities for better code maintainability.",
            "Updating documentation to reflect recent changes made in the codebase.",
            "Low-priority cleanup that will be addressed in the next available sprint.",
            "Minor improvement to developer documentation with no user-facing impact.",
            "Removing deprecated code paths no longer needed after the last major update.",
        ],
        "estimated_hours_range": (0.5, 5.0),
        "tags_count_range": (0, 2),
    },
}

PRIORITY_WEIGHTS = {
    "CRITICAL": 0.15,
    "HIGH": 0.25,
    "MEDIUM": 0.40,
    "LOW": 0.20,
}

NOISE_RATE = 0.15  # 15% labels randomly flipped
TOTAL_RECORDS = 5000

# ──────────────────────────────────────────────
# 2. Row generator
# ──────────────────────────────────────────────

def generate_row(priority: str) -> dict:
    tpl = TEMPLATES[priority]

    subject = random.choice(tpl["subjects"])
    title_fmt = random.choice(tpl["title_prefixes"])
    title = title_fmt.format(subject=subject)

    description = random.choice(tpl["descriptions"])

    est_h_min, est_h_max = tpl["estimated_hours_range"]
    estimated_hours = round(random.uniform(est_h_min, est_h_max), 1)

    tc_min, tc_max = tpl["tags_count_range"]
    tags_count = random.randint(tc_min, tc_max)

    emp_completion_rate = round(random.uniform(0.40, 1.00), 2)
    emp_avg_hours = round(random.uniform(1.0, 12.0), 1)

    return {
        "title": title,
        "description": description,
        "tags_count": tags_count,
        "estimated_hours": estimated_hours,
        "emp_completion_rate": emp_completion_rate,
        "emp_avg_hours": emp_avg_hours,
        "priority": priority,
    }


# ──────────────────────────────────────────────
# 3. Build the dataset
# ──────────────────────────────────────────────

def generate_dataset(n: int = TOTAL_RECORDS) -> pd.DataFrame:
    priorities = list(PRIORITY_WEIGHTS.keys())
    weights = list(PRIORITY_WEIGHTS.values())

    rows = []
    for _ in range(n):
        chosen_priority = random.choices(priorities, weights=weights, k=1)[0]
        row = generate_row(chosen_priority)
        rows.append(row)

    df = pd.DataFrame(rows)

    # Apply 15% label noise to prevent perfect separation
    noise_mask = np.random.rand(len(df)) < NOISE_RATE
    noise_indices = df.index[noise_mask]
    df.loc[noise_indices, "priority"] = np.random.choice(
        priorities, size=noise_mask.sum(), p=weights
    )

    # Shuffle so classes are interleaved
    df = df.sample(frac=1, random_state=42).reset_index(drop=True)
    return df


# ──────────────────────────────────────────────
# 4. Save to CSV
# ──────────────────────────────────────────────

def main():
    # Determine output path relative to this script (now in ML/training/training_generator/)
    script_dir = os.path.dirname(os.path.abspath(__file__))
    output_dir = os.path.join(script_dir, "..", "data")
    os.makedirs(output_dir, exist_ok=True)

    output_path = os.path.join(output_dir, "priority_data.csv")

    print("Generating synthetic task priority data …")
    df = generate_dataset(TOTAL_RECORDS)

    df.to_csv(output_path, index=False)

    # ── Summary ──
    print(f"\n✅ Saved {len(df):,} records → {output_path}")
    print("\nPriority distribution:")
    dist = df["priority"].value_counts().sort_index()
    for label, count in dist.items():
        pct = count / len(df) * 100
        print(f"  {label:<10} {count:>5,}  ({pct:5.1f}%)")

    print(f"\nSample rows:")
    print(df[["title", "estimated_hours", "emp_completion_rate", "priority"]].head(8).to_string(index=False))


if __name__ == "__main__":
    main()
