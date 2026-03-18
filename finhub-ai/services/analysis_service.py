from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage
from core.config import settings

llm = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.3,
)

ANALYSIS_PROMPT = """당신은 FinHub의 AI 금융 분석 전문가입니다.
사용자의 지출 데이터를 분석하여 유용한 인사이트와 개선 방안을 제시합니다.
항상 한국어로 답변하고, 구체적인 수치와 함께 실용적인 조언을 제공하세요.
응답은 JSON 형식이 아닌 자연스러운 문장으로 작성하세요."""


async def analyze_spending(user_id: str, spending_data: dict) -> dict:
    """사용자 지출 데이터를 LLM으로 분석"""
    total = spending_data.get("total", 0)
    categories = spending_data.get("categories", {})
    month = spending_data.get("month", "이번 달")

    category_text = "\n".join(
        [f"- {k}: {v:,}원" for k, v in categories.items()]
    )

    prompt = f"""{month} 지출 분석 요청:

총 지출: {total:,}원
카테고리별 지출:
{category_text}

위 지출 패턴을 분석하고 다음을 포함하여 답변해주세요:
1. 지출 패턴의 특징
2. 절약 가능한 영역
3. 재정 건전성 평가
4. 구체적인 개선 방안 2-3가지"""

    messages = [
        SystemMessage(content=ANALYSIS_PROMPT),
        HumanMessage(content=prompt),
    ]
    response = await llm.ainvoke(messages)

    return {
        "month": month,
        "total": total,
        "categories": categories,
        "analysis": response.content,
    }


def get_dummy_spending(user_id: str) -> dict:
    """더미 지출 데이터 반환 (실제로는 banking 서비스에서 조회)"""
    return {
        "month": "2026년 3월",
        "total": 1_850_000,
        "categories": {
            "식비": 450_000,
            "교통": 120_000,
            "쇼핑": 380_000,
            "통신": 80_000,
            "문화/여가": 220_000,
            "의료": 50_000,
            "저축/투자": 400_000,
            "기타": 150_000,
        },
    }
