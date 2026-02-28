from fastapi import APIRouter
from models.loader import get_loaded_models

router = APIRouter()

@router.get("/health")
async def health_check():
    return {
        "status": "ok",
        "models_loaded": get_loaded_models(),
        "version": "1.0.0"
    }
