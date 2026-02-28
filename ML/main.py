from fastapi import FastAPI
from contextlib import asynccontextmanager
from api.routes import health
from models.loader import load_all_models

@asynccontextmanager
async def lifespan(app: FastAPI):
    load_all_models()
    yield

app = FastAPI(
    title="TaskHive ML Inference Server",
    version="1.0.0",
    lifespan=lifespan
)

app.include_router(health.router)

# Feature routes added phase by phase:
# from api.routes import predict_priority
# app.include_router(predict_priority.router, prefix="/ml")
