import joblib
from pathlib import Path

_models = {}
MODEL_DIR = Path(__file__).parent

def load_all_models():
    model_files = {
        "priority": "priority_model.pkl",
        "completion": "completion_model.pkl",
        "workload": "workload_model.pkl",
        "productivity": "productivity_model.pkl",
    }
    for key, filename in model_files.items():
        path = MODEL_DIR / filename
        if path.exists():
            _models[key] = joblib.load(path)
            print(f"[ML] Loaded: {key}")
        else:
            print(f"[ML] Not found: {filename} — will use fallback")

def get_model(name: str):
    return _models.get(name)

def get_loaded_models():
    return list(_models.keys())
