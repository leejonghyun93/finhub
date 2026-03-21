import httpx
from typing import Annotated
from langchain_core.tools import tool
from langgraph.prebuilt import InjectedState

from core.database import SessionLocal
from services.rag_service import _embed_model, search_similar_products, build_context

BANKING_URL = "http://finhub-banking:8082"
INSURANCE_URL = "http://finhub-insurance:8085"
SPENDING_TYPES = {"WITHDRAWAL", "TRANSFER_OUT"}


@tool
def search_financial_products(query: str) -> str:
    """금융 상품을 검색합니다. 적금, ETF, 보험, 대출, 저축 등 금융 상품 추천이 필요할 때 사용하세요."""
    try:
        vectors = _embed_model.embed_documents([query])
        db = SessionLocal()
        try:
            products = search_similar_products(vectors[0], db, top_k=3)
            if not products:
                return "관련 금융 상품을 찾을 수 없습니다."
            return build_context(products)
        finally:
            db.close()
    except Exception as e:
        return f"상품 검색 실패: {str(e)}"


@tool
def get_my_accounts(state: Annotated[dict, InjectedState]) -> str:
    """사용자의 계좌 목록과 잔액을 조회합니다. 내 계좌, 잔액, 계좌 정보가 필요할 때 사용하세요."""
    token = state.get("user_token", "")
    try:
        with httpx.Client(timeout=10.0) as client:
            resp = client.get(
                f"{BANKING_URL}/api/v1/banking/accounts",
                headers={"Authorization": f"Bearer {token}"},
            )
            if resp.status_code != 200:
                return "계좌 조회에 실패했습니다."
            accounts = resp.json().get("data", [])
            if not accounts:
                return "등록된 계좌가 없습니다."
            lines = []
            for a in accounts:
                balance = int(float(a.get("balance", 0)))
                lines.append(
                    f"- {a.get('bankName', '')} {a.get('accountNumber', '')} "
                    f"| 잔액: {balance:,}원 | 유형: {a.get('accountType', '')}"
                )
            return "\n".join(lines)
    except Exception as e:
        return f"계좌 조회 실패: {str(e)}"


@tool
def get_spending_analysis(state: Annotated[dict, InjectedState]) -> str:
    """사용자의 지출 내역을 카테고리별로 분석합니다. 소비 패턴, 지출 현황, 이번 달 사용 금액이 필요할 때 사용하세요."""
    token = state.get("user_token", "")
    try:
        with httpx.Client(timeout=15.0) as client:
            accounts_resp = client.get(
                f"{BANKING_URL}/api/v1/banking/accounts",
                headers={"Authorization": f"Bearer {token}"},
            )
            if accounts_resp.status_code != 200:
                return "계좌 조회에 실패했습니다."
            accounts = accounts_resp.json().get("data", [])

            total = 0
            categories: dict[str, int] = {}

            for account in accounts:
                tx_resp = client.get(
                    f"{BANKING_URL}/api/v1/banking/accounts/{account['id']}/transactions",
                    headers={"Authorization": f"Bearer {token}"},
                    params={"size": 100, "sort": "createdAt,desc"},
                )
                if tx_resp.status_code != 200:
                    continue
                for tx in tx_resp.json().get("data", {}).get("content", []):
                    if tx.get("transactionType") not in SPENDING_TYPES:
                        continue
                    amount = int(float(tx.get("amount", 0)))
                    desc = tx.get("description") or "기타"
                    total += amount
                    categories[desc] = categories.get(desc, 0) + amount

        if total == 0:
            return "이번 달 지출 내역이 없습니다."

        lines = [f"총 지출: {total:,}원", "항목별 지출:"]
        for k, v in sorted(categories.items(), key=lambda x: -x[1]):
            lines.append(f"  - {k}: {v:,}원")
        return "\n".join(lines)
    except Exception as e:
        return f"지출 분석 실패: {str(e)}"


@tool
def get_insurance_products(category: str = "") -> str:
    """보험 상품 목록을 조회합니다. 실손보험, 종신보험, 운전자보험 등 보험 상품 정보가 필요할 때 사용하세요."""
    try:
        params = {"category": category} if category else {}
        with httpx.Client(timeout=10.0) as client:
            resp = client.get(
                f"{INSURANCE_URL}/api/v1/insurance/products",
                params=params,
            )
            if resp.status_code != 200:
                return "보험 상품 조회에 실패했습니다."
            products = resp.json().get("data", [])
            if not products:
                return "조회된 보험 상품이 없습니다."
            lines = []
            for p in products:
                lines.append(
                    f"- [{p.get('category', '')}] {p.get('name', '')}: {p.get('description', '')}"
                )
            return "\n".join(lines)
    except Exception as e:
        return f"보험 상품 조회 실패: {str(e)}"


# 외부에서 임포트할 tool 목록
tools = [
    search_financial_products,
    get_my_accounts,
    get_spending_analysis,
    get_insurance_products,
]
