from typing import Annotated
from typing_extensions import TypedDict
from langgraph.graph import StateGraph, END
from langgraph.graph.message import add_messages
from langgraph.checkpoint.memory import MemorySaver
from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage
from core.config import settings

# ── LLM 초기화 ──────────────────────────────────────────
llm = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.7,
)

SYSTEM_PROMPT = """당신은 FinHub의 AI 금융 전문가 어시스턴트입니다.
사용자의 금융 관련 질문에 친절하고 전문적으로 답변합니다.
다음 분야에 전문 지식을 가지고 있습니다:
- 개인 금융 관리 (예산, 저축, 투자)
- 주식 및 펀드 투자 기초
- 보험 상품 선택 가이드
- 대출 및 신용 관리
- 금융 용어 설명

항상 한국어로 답변하고, 구체적이고 실용적인 조언을 제공하세요.
투자 권유나 특정 상품 추천은 참고용임을 명시하세요."""


# ── LangGraph 상태 정의 ──────────────────────────────────
class ChatState(TypedDict):
    messages: Annotated[list, add_messages]


# ── 챗봇 노드 ────────────────────────────────────────────
def chatbot_node(state: ChatState) -> ChatState:
    messages = [SystemMessage(content=SYSTEM_PROMPT)] + state["messages"]
    response = llm.invoke(messages)
    return {"messages": [response]}


# ── 그래프 빌드 ──────────────────────────────────────────
memory = MemorySaver()

workflow = StateGraph(ChatState)
workflow.add_node("chatbot", chatbot_node)
workflow.set_entry_point("chatbot")
workflow.add_edge("chatbot", END)

graph = workflow.compile(checkpointer=memory)


# ── 서비스 함수 ──────────────────────────────────────────
async def chat(message: str, session_id: str) -> str:
    """LangGraph 그래프를 통해 챗봇 응답 생성"""
    config = {"configurable": {"thread_id": session_id}}
    result = await graph.ainvoke(
        {"messages": [HumanMessage(content=message)]},
        config=config,
    )
    return result["messages"][-1].content


async def get_session_history(session_id: str) -> list[dict]:
    """세션의 대화 히스토리 반환"""
    config = {"configurable": {"thread_id": session_id}}
    state = await graph.aget_state(config)
    if not state or not state.values.get("messages"):
        return []

    history = []
    for msg in state.values["messages"]:
        role = "user" if isinstance(msg, HumanMessage) else "assistant"
        history.append({"role": role, "content": msg.content})
    return history
