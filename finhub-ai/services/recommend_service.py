from sqlalchemy.orm import Session
from sqlalchemy import or_
from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage
from core.config import settings
from core.database import FinancialProduct

llm = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.4,
)

RECOMMEND_PROMPT = """당신은 FinHub의 AI 금융 상품 추천 전문가입니다.
사용자의 요구사항과 관련 금융 상품 정보를 바탕으로 최적의 상품을 추천합니다.
항상 한국어로 답변하고, 각 상품의 장단점을 명확하게 설명하세요.
투자 상품 추천 시 리스크를 반드시 언급하세요."""


def _search_products(query: str, db: Session, top_k: int = 3) -> list:
    """키워드 기반 ILIKE 검색 — 쿼리를 공백으로 분리해 OR 검색"""
    keywords = [kw for kw in query.split() if len(kw) >= 1]
    if keywords:
        conditions = [
            or_(
                FinancialProduct.name.ilike(f"%{kw}%"),
                FinancialProduct.description.ilike(f"%{kw}%"),
                FinancialProduct.category.ilike(f"%{kw}%"),
            )
            for kw in keywords
        ]
        results = db.query(FinancialProduct).filter(or_(*conditions)).limit(top_k).all()
        if results:
            return results

    return []


async def recommend_products(query: str, db: Session, top_k: int = 3) -> dict:
    """키워드 검색 후 LLM으로 추천 설명 생성"""
    products = _search_products(query, db, top_k)

    if not products:
        return {
            "query": query,
            "products": [],
            "recommendation": "",
        }

    context = "\n\n".join(
        [f"[{p.category}] {p.name}\n{p.description}" for p in products]
    )

    prompt = f"""사용자 요구사항: {query}

관련 금융 상품 정보:
{context}

위 상품들을 사용자 요구사항에 맞게 추천해주세요. 각 상품별로:
1. 추천 이유
2. 주요 특징
3. 주의사항
을 포함해서 설명해주세요."""

    messages = [
        SystemMessage(content=RECOMMEND_PROMPT),
        HumanMessage(content=prompt),
    ]
    response = await llm.ainvoke(messages)

    return {
        "query": query,
        "products": [
            {"id": p.id, "name": p.name, "category": p.category, "description": p.description}
            for p in products
        ],
        "recommendation": response.content,
    }
