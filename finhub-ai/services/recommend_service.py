from sqlalchemy.orm import Session
from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage
from core.config import settings
from core.database import FinancialProduct
from services.rag_service import embed_query, search_similar_products, build_context

llm = ChatOllama(
    model=settings.OLLAMA_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
    temperature=0.4,
)

RECOMMEND_PROMPT = """당신은 FinHub의 AI 금융 상품 추천 전문가입니다.
사용자의 요구사항과 관련 금융 상품 정보를 바탕으로 최적의 상품을 추천합니다.
항상 한국어로 답변하고, 각 상품의 장단점을 명확하게 설명하세요.
투자 상품 추천 시 리스크를 반드시 언급하세요."""


async def recommend_products(query: str, db: Session, top_k: int = 3) -> dict:
    """Semantic RAG: 벡터 유사도 검색 후 LLM으로 추천 설명 생성"""
    # Retrieve: 쿼리를 임베딩으로 변환 후 pgvector 유사도 검색
    query_vector = await embed_query(query)
    products = search_similar_products(query_vector, db, top_k)

    if not products:
        return {
            "query": query,
            "products": [],
            "recommendation": "관련 금융 상품을 찾을 수 없습니다.",
        }

    # Augment: 검색 결과를 컨텍스트로 조합
    context = build_context(products)

    # Generate: LLM에 컨텍스트 주입하여 추천 설명 생성
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
