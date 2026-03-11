"""
models/loader.py
─────────────────
Loads all .pkl model bundles once at server startup.
Models are stored in ML/models/ directory.
"""

import joblib
import logging
import sys
from pathlib import Path

# Add parent directory to sys.path to ensure 'preprocessing' package is found correctly by joblib
MODEL_PATH = Path(__file__).parent.parent
if str(MODEL_PATH) not in sys.path:
    sys.path.insert(0, str(MODEL_PATH))

# Explicitly import custom transformers so joblib can unpickle them
try:
    import preprocessing.transformers
    logger_init = logging.getLogger(__name__)
    logger_init.info("[Loader] Preprocessing transformers registered for unpickling.")
except ImportError as e:
    logging.getLogger(__name__).warning(f"[Loader] Could not pre-import transformers: {e}")

logger = logging.getLogger(__name__)

_models: dict = {}

# Models directory is now ML/inference-server/artifacts/
MODEL_DIR = Path(__file__).parent.parent / "artifacts"


def load_all_models() -> None:
    """Load all available .pkl model bundles into memory."""
    model_files = {
        "priority":     "priority_model.pkl",
        "completion":   "completion_model.pkl",
        "workload":     "workload_model.pkl",
        "productivity": "productivity_model.pkl",
    }
    for key, filename in model_files.items():
        path = MODEL_DIR / filename
        if path.exists():
            try:
                # joblib.load may require custom classes to be in sys.modules
                _models[key] = joblib.load(path)
                logger.info("[Loader] Loaded model: %s (from %s)", key, filename)
            except Exception as exc:
                logger.error("[Loader] Failed to load %s: %s", filename, exc, exc_info=True)
        else:
            logger.warning("[Loader] Not found: %s — fallback will be used", filename)


def get_model(name: str):
    """Return a loaded model bundle by name, or None if not loaded."""
    return _models.get(name)


def load_priority_model(): return get_model("priority")
def load_completion_model(): return get_model("completion")
def load_workload_model(): return get_model("workload")
def load_productivity_model(): return get_model("productivity")


def get_loaded_models() -> list:
    """Return list of currently loaded model names."""
    return list(_models.keys())
