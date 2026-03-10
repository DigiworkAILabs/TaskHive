"""
models/loader.py
─────────────────
Loads all .pkl model bundles once at server startup.
Models are stored in ML/models/ directory.
"""

import joblib
import logging
from pathlib import Path

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
                _models[key] = joblib.load(path)
                logger.info("[Loader] Loaded model: %s", key)
            except Exception as exc:
                logger.error("[Loader] Failed to load %s: %s", filename, exc)
        else:
            logger.warning("[Loader] Not found: %s — fallback will be used", filename)


def get_model(name: str):
    """Return a loaded model bundle by name, or None if not loaded."""
    return _models.get(name)


def get_loaded_models() -> list:
    """Return list of currently loaded model names."""
    return list(_models.keys())
