from fastapi import APIRouter, Depends
from core.security import get_current_user
from services.analysis_service import analyze_spending, fetch_real_spending

router = APIRouter(prefix="/api/v1/ai/analysis", tags=["analysis"])


@router.get("/spending")
async def spending_analysis(
    current_user: dict = Depends(get_current_user),
):
    """지출 패턴 AI 분석 (banking 서비스 실제 데이터 기반)"""
    user_id = current_user["user_id"]
    token = current_user["token"]

    spending_data = await fetch_real_spending(user_id, token)

    if not spending_data:
        return {
            "success": True,
            "data": {"analysis": "지출 내역이 없어 분석할 수 없습니다."},
            "message": "OK",
        }

    result = await analyze_spending(user_id, spending_data)
    return {"success": True, "data": result, "message": "OK"}
