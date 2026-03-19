import httpx
from datetime import datetime
from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage
from core.config import settings

BANKING_URL = "http://finhub-banking:8082"

# 지출로 분류할 거래 유형
SPENDING_TYPES = {"WITHDRAWAL", "TRANSFER_OUT"}

llm = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.3,
)

ANALYSIS_PROMPT = """당신은 FinHub의 AI 금융 분석 전문가입니다.
사용자의 지출 데이터를 분석하여 유용한 인사이트와 개선 방안을 제시합니다.
항상 한국어로 답변하고, 구체적인 수치와 함께 실용적인 조언을 제공하세요.
응답은 JSON 형식이 아닌 자연스러운 문장으로 작성하세요."""


async def fetch_real_spending(user_id: str, token: str) -> dict | None:
    """banking 서비스에서 실제 거래내역을 조회하여 지출 데이터 생성"""
    headers = {"Authorization": f"Bearer {token}"}
    now = datetime.now()
    month_str = f"{now.year}년 {now.month}월"

    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            # 계좌 목록 조회
            resp = await client.get(f"{BANKING_URL}/api/v1/banking/accounts", headers=headers)
            if resp.status_code != 200:
                return None

            accounts = resp.json().get("data", [])
            if not accounts:
                return None

            total = 0
            categories: dict[str, int] = {}

            for account in accounts:
                account_id = account["id"]
                tx_resp = await client.get(
                    f"{BANKING_URL}/api/v1/banking/accounts/{account_id}/transactions",
                    headers=headers,
                    params={"size": 100, "sort": "createdAt,desc"},
                )
                if tx_resp.status_code != 200:
                    continue

                transactions = tx_resp.json().get("data", {}).get("content", [])

                for tx in transactions:
                    if tx.get("transactionType") not in SPENDING_TYPES:
                        continue
                    amount = int(float(tx.get("amount", 0)))
                    desc = tx.get("description") or "기타"
                    total += amount
                    categories[desc] = categories.get(desc, 0) + amount

    except Exception:
        return None

    if total == 0:
        return None

    return {"month": month_str, "total": total, "categories": categories}


async def analyze_spending(user_id: str, spending_data: dict) -> dict:
    """사용자 지출 데이터를 LLM으로 분석"""
    total = spending_data.get("total", 0)
    categories = spending_data.get("categories", {})
    month = spending_data.get("month", "이번 달")

    category_text = "\n".join([f"- {k}: {v:,}원" for k, v in categories.items()])

    prompt = f"""{month} 지출 분석 요청:

총 지출: {total:,}원
항목별 지출:
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
