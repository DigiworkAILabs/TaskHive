"""
api/routes/health.py
─────────────────────
GET /health — ML server health check endpoint.
Returns loaded model list and server version.
"""

from fastapi import APIRouter
from models.loader import get_loaded_models
from config.settings import settings

router = APIRouter(tags=["Health"])


@router.get(
    "/health",
    summary="Health Check",
    description="Returns server status and list of currently loaded ML models.",
)
async def health_check():
    return {
        "status": "ok",
        "models_loaded": get_loaded_models(),
        "version": settings.app_version,
        "app": settings.app_name,
    }
