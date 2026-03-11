import os
import subprocess
import sys
from pathlib import Path

# Base ML directory
ML_ROOT = Path(__file__).parent.parent

# Step 1: Data Generation
GENERATORS = [
    ML_ROOT / "training" / "training_generator" / "generate_priority_data.py",
    ML_ROOT / "training" / "training_generator" / "generate_completion_data.py",
    ML_ROOT / "training" / "training_generator" / "generate_workload_data.py",
    ML_ROOT / "training" / "training_generator" / "generate_productivity_data.py",
]

# Step 2: Training
TRAINERS = [
    ML_ROOT / "training" / "scripts" / "train_priority.py",
    ML_ROOT / "training" / "scripts" / "train_completion.py",
    ML_ROOT / "training" / "scripts" / "train_workload.py",
    ML_ROOT / "training" / "scripts" / "train_productivity.py",
]

def run_script(script_path):
    print(f"\n>>> Running {script_path.name}...")
    result = subprocess.run([sys.executable, str(script_path)], capture_output=False)
    if result.returncode != 0:
        print(f"FAILED: {script_path}")
        sys.exit(1)

for script in GENERATORS + TRAINERS:
    run_script(script)

print("\n✅ All models trained successfully!")
