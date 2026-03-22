"""
services/rag_service.py 단위 테스트

OllamaEmbeddings 와 SQLAlchemy Session을 모킹하여
실제 DB · Ollama 없이 RAG 핵심 로직을 검증한다.
"""
import pytest
from unittest.mock import AsyncMock, MagicMock, patch


# ── embed_query() ──────────────────────────────────────────────────────────

class TestEmbedQuery:

    @pytest.mark.asyncio
    async def test_returns_embedding_vector(self):
        """aembed_documents 결과의 첫 번째 벡터를 반환"""
        from services.rag_service import embed_query

        fake_vector = [0.1] * 768
        mock_model = AsyncMock()
        mock_model.aembed_documents.return_value = [fake_vector]

        with patch("services.rag_service._embed_model", mock_model):
            result = await embed_query("적금 추천")

        assert result == fake_vector
        mock_model.aembed_documents.assert_called_once_with(["적금 추천"])

    @pytest.mark.asyncio
    async def test_returns_correct_dimension(self):
        """768차원 벡터를 그대로 반환"""
        from services.rag_service import embed_query

        fake_vector = [float(i) / 1000 for i in range(768)]
        mock_model = AsyncMock()
        mock_model.aembed_documents.return_value = [fake_vector]

        with patch("services.rag_service._embed_model", mock_model):
            result = await embed_query("ETF 투자")

        assert len(result) == 768


# ── search_similar_products() ─────────────────────────────────────────────

class TestSearchSimilarProducts:

    def _make_product(self, name: str, category: str, description: str):
        """테스트용 FinancialProduct mock 생성"""
        product = MagicMock()
        product.name = name
        product.category = category
        product.description = description
        product.embedding = [0.1] * 768
        return product

    def test_returns_top_k_products(self):
        """DB 쿼리 결과를 그대로 반환"""
        from services.rag_service import search_similar_products

        mock_products = [
            self._make_product("KB 스타트적금", "적금", "월 50만원 납입, 연 4.5%"),
            self._make_product("신한 청년희망적금", "적금", "청년 대상, 연 6.0%"),
            self._make_product("하나 자유적금", "적금", "자유 납입식, 연 3.8%"),
        ]

        mock_query_chain = MagicMock()
        mock_query_chain.filter.return_value = mock_query_chain
        mock_query_chain.order_by.return_value = mock_query_chain
        mock_query_chain.limit.return_value = mock_query_chain
        mock_query_chain.all.return_value = mock_products

        mock_db = MagicMock()
        mock_db.query.return_value = mock_query_chain

        query_vector = [0.1] * 768
        result = search_similar_products(query_vector, mock_db, top_k=3)

        assert len(result) == 3
        assert result[0].name == "KB 스타트적금"
        mock_query_chain.limit.assert_called_once_with(3)

    def test_empty_results_when_no_match(self):
        """유사 상품이 없으면 빈 리스트 반환"""
        from services.rag_service import search_similar_products

        mock_query_chain = MagicMock()
        mock_query_chain.filter.return_value = mock_query_chain
        mock_query_chain.order_by.return_value = mock_query_chain
        mock_query_chain.limit.return_value = mock_query_chain
        mock_query_chain.all.return_value = []

        mock_db = MagicMock()
        mock_db.query.return_value = mock_query_chain

        result = search_similar_products([0.0] * 768, mock_db, top_k=3)

        assert result == []


# ── build_context() ───────────────────────────────────────────────────────

class TestBuildContext:

    def _make_product(self, name: str, category: str, description: str):
        product = MagicMock()
        product.name = name
        product.category = category
        product.description = description
        return product

    def test_formats_products_as_context_string(self):
        """상품 목록을 LLM 프롬프트용 문자열로 조합"""
        from services.rag_service import build_context

        products = [
            self._make_product("KB 스타트적금", "적금", "연 4.5% 금리"),
            self._make_product("KODEX 200 ETF", "ETF", "코스피200 추종"),
        ]
        context = build_context(products)

        assert "[적금] KB 스타트적금" in context
        assert "연 4.5% 금리" in context
        assert "[ETF] KODEX 200 ETF" in context
        assert "코스피200 추종" in context

    def test_empty_products_returns_empty_string(self):
        """빈 목록이면 빈 문자열 반환"""
        from services.rag_service import build_context

        assert build_context([]) == ""

    def test_multiple_products_separated_by_double_newline(self):
        """상품 간 구분자가 '\\n\\n'인지 검증"""
        from services.rag_service import build_context

        products = [
            self._make_product("상품A", "적금", "설명A"),
            self._make_product("상품B", "ETF", "설명B"),
        ]
        context = build_context(products)

        parts = context.split("\n\n")
        assert len(parts) == 2
