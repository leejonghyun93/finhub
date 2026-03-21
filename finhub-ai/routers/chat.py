from fastapi import APIRouter, Depends
from pydantic import BaseModel
from core.security import get_current_user
from services.chat_service import chat, get_session_history

router = APIRouter(prefix="/api/v1/ai/chat", tags=["chat"])


class ChatRequest(BaseModel):
    message: str


class ChatResponse(BaseModel):
    success: bool
    data: dict
    message: str = ""


@router.post("", response_model=ChatResponse)
async def send_message(
    req: ChatRequest,
    current_user: dict = Depends(get_current_user),
):
    """AI 챗봇 메시지 전송 (의도 분류 라우팅)"""
    session_id = current_user["user_id"]
    result = await chat(req.message, session_id, user_token=current_user["token"])
    return {
        "success": True,
        "data": {
            "reply": result["response"],
            "intent": result.get("intent", "GENERAL"),
            "tools_used": result.get("tools_used", []),
            "session_id": str(current_user["user_id"]),
        },
        "message": "OK",
    }


@router.get("/history", response_model=ChatResponse)
async def get_history(
    current_user: dict = Depends(get_current_user),
):
    """대화 히스토리 조회"""
    session_id = current_user["user_id"]
    history = await get_session_history(session_id)
    return {
        "success": True,
        "data": {"history": history, "session_id": session_id},
        "message": "OK",
    }
