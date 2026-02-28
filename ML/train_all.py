# Run this to generate all data + train all models
import subprocess
import sys

scripts = [
    "generate_priority_data.py",
    "generate_completion_data.py",
    "generate_workload_data.py",
    "generate_productivity_data.py",
    "train_priority.py",
    "train_completion.py",
    "train_workload.py",
    "train_productivity.py",
]

for script in scripts:
    print(f"\n>>> Running {script}...")
    result = subprocess.run([sys.executable, script], capture_output=False)
    if result.returncode != 0:
        print(f"FAILED: {script}")
        sys.exit(1)

print("\n✅ All models trained successfully!")
