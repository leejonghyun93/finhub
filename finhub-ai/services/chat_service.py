from typing import Annotated, Optional
from typing_extensions import TypedDict
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from langgraph.checkpoint.memory import MemorySaver
from langgraph.prebuilt import ToolNode, tools_condition
from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage, AIMessage

from core.config import settings
from services.tools import tools

# ── LLM 설정 ─────────────────────────────────────────────
llm = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.7,
)
llm_classifier = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.0,
)
llm_with_tools = llm.bind_tools(tools)

# ── 의도 분류 카테고리 ────────────────────────────────────
INTENTS = {"PRODUCT_SEARCH", "ACCOUNT_INQUIRY", "SPENDING_ANALYSIS", "INSURANCE", "GENERAL"}

INTENT_ROUTING = {
    "PRODUCT_SEARCH":    "product_agent",
    "ACCOUNT_INQUIRY":   "account_agent",
    "SPENDING_ANALYSIS": "analysis_agent",
    "INSURANCE":         "product_agent",
    "GENERAL":           "general_agent",
}

# ── 시스템 프롬프트 ───────────────────────────────────────
CLASSIFIER_PROMPT = """사용자의 금융 관련 질문을 아래 카테고리 중 하나로 분류하세요.
반드시 카테고리 이름만 단어 하나로 답변하세요.

카테고리:
- PRODUCT_SEARCH: 금융상품 검색, 적금/ETF/펀드/대출/저축 추천, 상품 비교
- ACCOUNT_INQUIRY: 계좌 조회, 잔액 확인, 거래내역 조회
- SPENDING_ANALYSIS: 지출 분석, 소비 패턴, 이번 달 사용 금액, 절약 방법
- INSURANCE: 보험 상품, 실손보험, 종신보험, 보험료 문의
- GENERAL: 위에 해당하지 않는 일반 금융 상담, 경제 용어 설명, 기타"""

PRODUCT_PROMPT = """당신은 FinHub의 금융상품 전문 어시스턴트입니다.
적금, ETF, 펀드, 대출, 저축 등 금융상품 추천과 비교에 특화되어 있습니다.

사용 가능한 도구:
- search_financial_products: 금융 상품 검색 및 추천 (RAG 기반)
- get_insurance_products: 보험 상품 목록 조회

사용자 요청에 맞는 상품을 찾으려면 반드시 search_financial_products를 호출하세요.
상품 추천 시 금리, 기간, 특징을 구체적으로 설명하고,
투자 권유는 참고용임을 명시하세요. 항상 한국어로 답변하세요."""

ACCOUNT_PROMPT = """당신은 FinHub의 계좌/거래 전문 어시스턴트입니다.
계좌 잔액 조회, 거래내역 확인, 계좌 관리에 특화되어 있습니다.

사용 가능한 도구:
- get_my_accounts: 사용자 계좌 목록 및 잔액 조회

계좌 정보나 잔액이 필요하면 반드시 get_my_accounts를 호출하세요.
조회된 실제 데이터를 기반으로 정확하게 답변하세요. 항상 한국어로 답변하세요."""

ANALYSIS_PROMPT = """당신은 FinHub의 지출 분석 전문 어시스턴트입니다.
소비 패턴 파악, 지출 카테고리 분석, 절약 방법 제안에 특화되어 있습니다.

사용 가능한 도구:
- get_spending_analysis: 지출 내역 카테고리별 분석
- get_my_accounts: 계좌 잔액 조회

지출 분석 요청에는 반드시 get_spending_analysis를 호출하세요.
분석 결과를 바탕으로 구체적인 절약 방법과 재무 개선 조언을 제공하세요.
항상 한국어로 답변하세요."""

GENERAL_PROMPT = """당신은 FinHub의 AI 금융 상담사입니다.
금융 용어 설명, 경제 동향, 재테크 기초 등 일반적인 금융 상담을 담당합니다.

사용 가능한 도구:
- search_financial_products: 금융 상품 검색
- get_my_accounts: 계좌 조회
- get_spending_analysis: 지출 분석
- get_insurance_products: 보험 상품 조회

필요에 따라 적절한 도구를 활용하여 사용자에게 도움을 주세요.
친절하고 이해하기 쉽게 설명하며, 항상 한국어로 답변하세요."""


# ── LangGraph 상태 정의 ──────────────────────────────────
class ChatState(TypedDict):
    messages: Annotated[list, add_messages]
    user_token: str
    intent: Optional[str]


# ── 의도 분류 노드 ────────────────────────────────────────
def intent_classifier_node(state: ChatState) -> dict:
    last_msg = next(
        (m for m in reversed(state["messages"]) if isinstance(m, HumanMessage)),
        None,
    )
    if not last_msg:
        return {"intent": "GENERAL"}

    response = llm_classifier.invoke([
        SystemMessage(content=CLASSIFIER_PROMPT),
        HumanMessage(content=last_msg.content),
    ])
    intent = response.content.strip().upper()
    if intent not in INTENTS:
        intent = "GENERAL"
    return {"intent": intent}


def _route_by_intent(state: ChatState) -> str:
    return INTENT_ROUTING.get(state.get("intent", "GENERAL"), "general_agent")


# ── 전문화된 에이전트 노드 팩토리 ─────────────────────────
def _make_agent_node(system_prompt: str):
    def agent_node(state: ChatState) -> dict:
        messages = [SystemMessage(content=system_prompt)] + state["messages"]
        response = llm_with_tools.invoke(messages)
        return {"messages": [response]}
    return agent_node


product_agent_node  = _make_agent_node(PRODUCT_PROMPT)
account_agent_node  = _make_agent_node(ACCOUNT_PROMPT)
analysis_agent_node = _make_agent_node(ANALYSIS_PROMPT)
general_agent_node  = _make_agent_node(GENERAL_PROMPT)


def _route_after_tools(state: ChatState) -> str:
    return INTENT_ROUTING.get(state.get("intent", "GENERAL"), "general_agent")


# ── 그래프 빌드 ───────────────────────────────────────────
#
#   START → intent_classifier → (route) → product_agent  ─┐
#                                        → account_agent  ─┤
#                                        → analysis_agent ─┤→ tools_condition → tools → (route back)
#                                        → general_agent  ─┘                         ↘ END
#
memory = MemorySaver()

workflow = StateGraph(ChatState)
workflow.add_node("intent_classifier", intent_classifier_node)
workflow.add_node("product_agent",     product_agent_node)
workflow.add_node("account_agent",     account_agent_node)
workflow.add_node("analysis_agent",    analysis_agent_node)
workflow.add_node("general_agent",     general_agent_node)
workflow.add_node("tools",             ToolNode(tools))

workflow.add_edge(START, "intent_classifier")
workflow.add_conditional_edges(
    "intent_classifier",
    _route_by_intent,
    {v: v for v in INTENT_ROUTING.values()},
)
for agent in ("product_agent", "account_agent", "analysis_agent", "general_agent"):
    workflow.add_conditional_edges(agent, tools_condition)

workflow.add_conditional_edges(
    "tools",
    _route_after_tools,
    {v: v for v in INTENT_ROUTING.values()},
)

graph = workflow.compile(checkpointer=memory)


# ── 서비스 함수 ───────────────────────────────────────────
async def chat(message: str, session_id: str, user_token: str = "") -> dict:
    """의도 분류 라우터를 통해 전문 에이전트가 응답 생성"""
    config = {"configurable": {"thread_id": session_id}}
    result = await graph.ainvoke(
        {"messages": [HumanMessage(content=message)], "user_token": user_token},
        config=config,
    )

    intent = result.get("intent", "GENERAL")

    # 호출된 tool 이름 수집 (중복 제거, 순서 유지)
    tools_used: list[str] = []
    seen: set[str] = set()
    for msg in result["messages"]:
        if hasattr(msg, "tool_calls") and msg.tool_calls:
            for tc in msg.tool_calls:
                name = tc.get("name") if isinstance(tc, dict) else getattr(tc, "name", None)
                if name and name not in seen:
                    tools_used.append(name)
                    seen.add(name)

    # 마지막 AIMessage (content가 있는 것)
    response = ""
    for msg in reversed(result["messages"]):
        if isinstance(msg, AIMessage) and msg.content:
            response = msg.content
            break

    return {"response": response, "intent": intent, "tools_used": tools_used}


async def get_session_history(session_id: str) -> list[dict]:
    """세션의 대화 히스토리 반환 (Human/AI 메시지만)"""
    config = {"configurable": {"thread_id": session_id}}
    state = await graph.aget_state(config)
    if not state or not state.values.get("messages"):
        return []

    history = []
    for msg in state.values["messages"]:
        if isinstance(msg, HumanMessage):
            history.append({"role": "user", "content": msg.content})
        elif isinstance(msg, AIMessage) and msg.content:
            history.append({"role": "assistant", "content": msg.content})
    return history
