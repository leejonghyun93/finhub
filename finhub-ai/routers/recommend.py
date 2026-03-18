from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from core.security import get_current_user
from core.database import get_db
from services.recommend_service import recommend_products

router = APIRouter(prefix="/api/v1/ai/recommend", tags=["recommend"])


@router.get("")
async def recommend(
    q: str = Query(..., description="추천 요청 쿼리 (예: 안정적인 적금 상품)"),
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user),
):
    """금융 상품 AI 추천"""
    result = await recommend_products(q, db)
    return {"success": True, "data": result, "message": "OK"}
