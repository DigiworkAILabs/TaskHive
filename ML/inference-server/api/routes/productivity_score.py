from fastapi import APIRouter, HTTPException
from api.schemas.request import ProductivityScoreRequest
from api.schemas.response import ProductivityScoreResponse
from services.productivity_service import productivity_service

router = APIRouter(prefix="/ml/score", tags=["Productivity"])

@router.post("/employee", response_model=ProductivityScoreResponse)
async def get_employee_score(request: ProductivityScoreRequest):
    """
    Predict productivity score for an employee based on historical task performance.
    Used in Employee Dashboard and Admin Analytics.
    """
    try:
        return productivity_service.get_score(request)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
