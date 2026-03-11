"""
main.py
────────
TaskHive ML Inference Server — FastAPI entry point.
Phase 7.1: Priority prediction route added.
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from api.routes import health
from api.routes import predict_priority
from api.routes import recommend_workload
from models.loader import load_all_models
from config.settings import settings

logging.basicConfig(
    level=getattr(logging, settings.log_level.upper(), logging.INFO),
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)


# ──────────────────────────────────────────────
# Lifespan: load models once at startup
# ──────────────────────────────────────────────

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("[Startup] Loading ML models …")
    load_all_models()
    logger.info("[Startup] ML server ready.")
    yield
    logger.info("[Shutdown] ML server shutting down.")


# ──────────────────────────────────────────────
# FastAPI app
# ──────────────────────────────────────────────

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="TaskHive ML Inference Server — provides priority suggestion and other ML features.",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# Allow Spring Boot (localhost:8080) to call this server during development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ──────────────────────────────────────────────
# Routers
# ──────────────────────────────────────────────

app.include_router(health.router)                  # GET  /health
app.include_router(predict_priority.router)        # POST /ml/predict/task-priority
app.include_router(recommend_workload.router)      # POST /ml/recommend/workload-balance

# Future routes — added phase by phase:
# from api.routes import predict_completion
# app.include_router(predict_completion.router)    # POST /ml/predict/completion-time

# from api.routes import productivity_score
# app.include_router(productivity_score.router)    # GET  /ml/productivity-score/{employeeId}


# ──────────────────────────────────────────────
# Run with: uvicorn main:app --reload --port 8000
# ──────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug,
        log_level=settings.log_level.lower(),
    )
