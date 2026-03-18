from fastapi import APIRouter, Depends
from core.security import get_current_user
from services.analysis_service import analyze_spending, get_dummy_spending

router = APIRouter(prefix="/api/v1/ai/analysis", tags=["analysis"])


@router.get("/spending")
async def spending_analysis(
    current_user: dict = Depends(get_current_user),
):
    """지출 패턴 AI 분석"""
    user_id = current_user["user_id"]
    spending_data = get_dummy_spending(user_id)
    result = await analyze_spending(user_id, spending_data)
    return {"success": True, "data": result, "message": "OK"}
