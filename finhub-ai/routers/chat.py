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
    """AI 챗봇 메시지 전송 (세션: user_id 기반)"""
    session_id = current_user["user_id"]
    reply = await chat(req.message, session_id)
    return {
        "success": True,
        "data": {"reply": reply, "session_id": session_id},
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
