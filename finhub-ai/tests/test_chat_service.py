"""
services/chat_service.py 단위 테스트

LangGraph graph 와 llm_classifier 를 모킹하여
실제 Ollama 서버 없이 비즈니스 로직만 검증한다.
"""
import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from langchain_core.messages import AIMessage, HumanMessage


# ── 의도 분류 노드 ──────────────────────────────────────────────────────────

class TestIntentClassifierNode:

    def test_no_human_message_returns_general(self):
        """메시지가 없으면 GENERAL 반환"""
        from services.chat_service import intent_classifier_node

        state = {"messages": [], "user_token": "", "intent": None}
        result = intent_classifier_node(state)

        assert result["intent"] == "GENERAL"

    def test_classifies_product_search_intent(self):
        """LLM이 PRODUCT_SEARCH를 반환하면 그대로 전달"""
        from services.chat_service import intent_classifier_node

        mock_response = MagicMock()
        mock_response.content = "PRODUCT_SEARCH"

        with patch("services.chat_service.llm_classifier") as mock_llm:
            mock_llm.invoke.return_value = mock_response
            state = {
                "messages": [HumanMessage(content="적금 추천해줘")],
                "user_token": "",
                "intent": None,
            }
            result = intent_classifier_node(state)

        assert result["intent"] == "PRODUCT_SEARCH"

    def test_unknown_intent_falls_back_to_general(self):
        """LLM이 알 수 없는 카테고리를 반환하면 GENERAL로 폴백"""
        from services.chat_service import intent_classifier_node

        mock_response = MagicMock()
        mock_response.content = "TOTALLY_UNKNOWN"

        with patch("services.chat_service.llm_classifier") as mock_llm:
            mock_llm.invoke.return_value = mock_response
            state = {
                "messages": [HumanMessage(content="이상한 질문")],
                "user_token": "",
                "intent": None,
            }
            result = intent_classifier_node(state)

        assert result["intent"] == "GENERAL"

    def test_intent_trimmed_and_uppercased(self):
        """앞뒤 공백이 있어도 정상 분류"""
        from services.chat_service import intent_classifier_node

        mock_response = MagicMock()
        mock_response.content = "  insurance  "

        with patch("services.chat_service.llm_classifier") as mock_llm:
            mock_llm.invoke.return_value = mock_response
            state = {
                "messages": [HumanMessage(content="보험 추천해줘")],
                "user_token": "",
                "intent": None,
            }
            result = intent_classifier_node(state)

        assert result["intent"] == "INSURANCE"


# ── chat() 서비스 함수 ─────────────────────────────────────────────────────

class TestChat:

    @pytest.mark.asyncio
    async def test_chat_returns_last_ai_message(self):
        """graph.ainvoke 결과에서 마지막 AIMessage를 응답으로 반환"""
        from services.chat_service import chat

        mock_result = {
            "intent": "GENERAL",
            "messages": [
                HumanMessage(content="안녕"),
                AIMessage(content="안녕하세요! 무엇을 도와드릴까요?"),
            ],
        }
        mock_graph = AsyncMock()
        mock_graph.ainvoke.return_value = mock_result

        with patch("services.chat_service.graph", mock_graph):
            result = await chat("안녕", "session-1", "token-abc")

        assert result["response"] == "안녕하세요! 무엇을 도와드릴까요?"
        assert result["intent"] == "GENERAL"
        assert result["tools_used"] == []

    @pytest.mark.asyncio
    async def test_chat_collects_unique_tool_names(self):
        """tool_calls 를 가진 AIMessage에서 tools_used를 중복 없이 수집"""
        from services.chat_service import chat

        tool_msg = AIMessage(content="")
        tool_msg.tool_calls = [{"name": "get_my_accounts"}, {"name": "get_my_accounts"}]

        mock_result = {
            "intent": "ACCOUNT_INQUIRY",
            "messages": [
                HumanMessage(content="내 계좌"),
                tool_msg,
                AIMessage(content="잔액은 100만원입니다."),
            ],
        }
        mock_graph = AsyncMock()
        mock_graph.ainvoke.return_value = mock_result

        with patch("services.chat_service.graph", mock_graph):
            result = await chat("내 계좌", "session-2")

        assert result["tools_used"] == ["get_my_accounts"]

    @pytest.mark.asyncio
    async def test_chat_empty_response_when_no_ai_content(self):
        """AIMessage가 없으면 response는 빈 문자열"""
        from services.chat_service import chat

        mock_result = {
            "intent": "GENERAL",
            "messages": [HumanMessage(content="...")],
        }
        mock_graph = AsyncMock()
        mock_graph.ainvoke.return_value = mock_result

        with patch("services.chat_service.graph", mock_graph):
            result = await chat("...", "session-3")

        assert result["response"] == ""


# ── get_session_history() ──────────────────────────────────────────────────

class TestGetSessionHistory:

    @pytest.mark.asyncio
    async def test_returns_empty_list_when_no_state(self):
        """세션 상태가 없으면 빈 리스트 반환"""
        from services.chat_service import get_session_history

        mock_graph = AsyncMock()
        mock_graph.aget_state.return_value = None

        with patch("services.chat_service.graph", mock_graph):
            result = await get_session_history("nonexistent-session")

        assert result == []

    @pytest.mark.asyncio
    async def test_returns_human_and_ai_messages(self):
        """Human/AI 메시지만 role 매핑하여 반환"""
        from services.chat_service import get_session_history

        mock_state = MagicMock()
        mock_state.values = {
            "messages": [
                HumanMessage(content="적금 추천해줘"),
                AIMessage(content="KB 스타트적금을 추천드립니다."),
            ]
        }
        mock_graph = AsyncMock()
        mock_graph.aget_state.return_value = mock_state

        with patch("services.chat_service.graph", mock_graph):
            result = await get_session_history("session-1")

        assert len(result) == 2
        assert result[0] == {"role": "user", "content": "적금 추천해줘"}
        assert result[1] == {"role": "assistant", "content": "KB 스타트적금을 추천드립니다."}

    @pytest.mark.asyncio
    async def test_skips_empty_ai_messages(self):
        """content가 없는 AIMessage(tool_call 중간 메시지)는 제외"""
        from services.chat_service import get_session_history

        mock_state = MagicMock()
        mock_state.values = {
            "messages": [
                HumanMessage(content="질문"),
                AIMessage(content=""),          # tool call 중간 메시지 — 제외
                AIMessage(content="최종 답변"),
            ]
        }
        mock_graph = AsyncMock()
        mock_graph.aget_state.return_value = mock_state

        with patch("services.chat_service.graph", mock_graph):
            result = await get_session_history("session-2")

        ai_messages = [m for m in result if m["role"] == "assistant"]
        assert len(ai_messages) == 1
        assert ai_messages[0]["content"] == "최종 답변"
