from langchain_ollama import OllamaEmbeddings
from sqlalchemy.orm import Session
from core.config import settings
from core.database import FinancialProduct

_embed_model = OllamaEmbeddings(
    model=settings.OLLAMA_EMBED_MODEL,
    base_url=settings.OLLAMA_BASE_URL,
)


async def embed_query(text: str) -> list[float]:
    """쿼리 텍스트를 nomic-embed-text 벡터로 변환"""
    vectors = await _embed_model.aembed_documents([text])
    return vectors[0]


def search_similar_products(query_vector: list[float], db: Session, top_k: int = 3) -> list[FinancialProduct]:
    """pgvector 코사인 유사도로 가장 유사한 상품 반환"""
    results = (
        db.query(FinancialProduct)
        .filter(FinancialProduct.embedding.isnot(None))
        .order_by(FinancialProduct.embedding.cosine_distance(query_vector))
        .limit(top_k)
        .all()
    )
    return results


def build_context(products: list[FinancialProduct]) -> str:
    """검색된 상품 목록을 LLM 프롬프트용 컨텍스트 문자열로 조합"""
    return "\n\n".join(
        f"[{p.category}] {p.name}\n{p.description}" for p in products
    )
